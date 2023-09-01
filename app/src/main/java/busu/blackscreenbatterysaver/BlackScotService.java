package busu.blackscreenbatterysaver;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by adibusu on 5/14/16.
 */
public class BlackScotService extends Service implements ViewPortController.OnTouchEvents {

    public final static String ACTION_SIZE_1P2 = "1p2";
    public final static String ACTION_SIZE_1P3 = "1p3";
    public final static String ACTION_SIZE_FULL = "1full";
    public final static String ACTION_STOP = "stop";
    public final static String ACTION_START = "start";
    public final static String ACTION_READPREFS = "read_prefs";
    public final static String ACTION_TUTORIAL = "tut";

    public static State state = State.STOPPED;

    public final static String EVENT_STATUS_CHANGED = "com.busu.blackscreenbatterysaver.STATUS_CHANGED";
    public final static String BROADCAST_CURRENT_STATE = "cst";

    private NotificationsHelper mNotifs;
    private Preferences mPrefs;

    private ViewPortController mVpCtrl;

    private int mTutorialStep = 0;
    private static final int[] TUTORIAL_STEPS = {R.string.tutorial1, R.string.tutorial2, R.string.tutorial3, R.string.tutorial4, R.string.tutorial5};


    @Override
    public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mPrefs = new Preferences(this);
        mNotifs = new NotificationsHelper(this);

        mVpCtrl = new ViewPortController(this, this);

        changeServiceState(State.STANDBY);
    }


    private void changeServiceState(State newState) {
        final State oldState = state;
        state = newState;
        applyCurrentServiceState(newState, oldState);
        LogUtil.logService("State changed from " + oldState + " to " + newState);
        sendBroadcast(new Intent(EVENT_STATUS_CHANGED)
                .putExtra(BROADCAST_CURRENT_STATE, newState));
    }

    private void applyCurrentServiceState(State currentState, State oldState) {
        switch (currentState) {
            case STANDBY:
                if (oldState == State.ACTIVE) {
                    removeViewPort();
                    mNotifs.cancelMainNotification();
                }
                break;
            case ACTIVE:
                addViewPort();
                mNotifs.startOrUpdateMainNotification(new NotificationsHelper.ChangeHeightSelection(mPrefs.getHoleHeightPercentage()));
                updateLastTime();
                break;
            case STOPPED:
                if (oldState == State.ACTIVE) {
                    removeViewPort();
                    mNotifs.cancelMainNotification();
                    addSaving();
                    showSavings();
                }
                stopSelf();
                break;
        }
    }

    @Override
    public void onDestroy() {
        changeServiceState(State.STOPPED);
        mVpCtrl = null;
        //force ending of tutorial
        endTutorial(false);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            LogUtil.logService("Action received: " + action + " in state: " + state);
            if (action != null) {
                if (commandChangeSize(action)) {
                } else if (action.equals(ACTION_STOP)) {
                    changeServiceState(State.STOPPED);
                } else if (action.equals(ACTION_START)) {
                    changeServiceState(State.ACTIVE);
                } else if (action.equals(ACTION_READPREFS)) {
                    mVpCtrl.applyHoleHeigthPercentage(mPrefs.getHoleHeightPercentage());
                    mVpCtrl.applyHoleVerticalGravity(mPrefs.getHoleGravity());
                } else if (action.equals(ACTION_TUTORIAL)) {
                    mTutorialStep = 0;
                    mPrefs.setHasToShowTutorial(true);
                    loadTutorial();
                }
            }
        }

        return START_STICKY;
    }

    private boolean commandChangeSize(String action) {
        int vpHeightPer = Integer.MAX_VALUE;
        if (action.equals(ACTION_SIZE_1P2)) {
            vpHeightPer = Preferences.HOLE_HEIGHT_PERCENTAGE_1P2;
        } else if (action.equals(ACTION_SIZE_1P3)) {
            vpHeightPer = Preferences.HOLE_HEIGHT_PERCENTAGE_1P3;
        } else if (action.equals(ACTION_SIZE_FULL)) {
            vpHeightPer = Preferences.HOLE_HEIGHT_PERCENTAGE_FULL;
        }
        //
        if (vpHeightPer == Integer.MAX_VALUE) {
            return false;
        } else {
            addSaving();
            //
            mPrefs.setHoleHeightPercentage(vpHeightPer);
            mVpCtrl.applyHoleHeigthPercentage(vpHeightPer);
            mNotifs.startOrUpdateMainNotification(new NotificationsHelper.ChangeHeightSelection(vpHeightPer));
            return true;
        }
    }

    private void addViewPort() {
        mVpCtrl.applyHoleHeigthPercentage(mPrefs.getHoleHeightPercentage());
        mVpCtrl.applyHoleVerticalGravity(mPrefs.getHoleGravity());
        loadTutorial();
        mVpCtrl.addToWindow();
    }

    private void removeViewPort() {
        mVpCtrl.removeFromWindow();
    }

    @Override
    public void onBlackClicked(ViewPortController.ViewLayout black, float clickVerticalRatio) {
        //if black is full screen, the click will quickly restore a 1/2 viewport
        if (mPrefs.getHoleHeightPercentage() == Preferences.HOLE_HEIGHT_PERCENTAGE_FULL) {
            commandChangeSize(ACTION_SIZE_1P2);
            incrementTutorial(true);
            LogUtil.logService("Click on: " + black.toString() + " while full screen black");
        } else {
            boolean isCenterRequested = (clickVerticalRatio <= 0.5f && black.gravity == Gravity.BOTTOM)
                    || (clickVerticalRatio > 0.5f && black.gravity == Gravity.TOP);
            mVpCtrl.changeHoleGravity(isCenterRequested, black.gravity);
            mPrefs.setHoleGravity(mVpCtrl.getHoleGravity());
            incrementTutorial(true);
            LogUtil.logService("Click on: " + black.toString() + ", center req: " + isCenterRequested + ", hole gravity: "
                    + ViewPortController.getGravityString(mVpCtrl.getHoleGravity()));
        }
    }

    @Override
    public void onCloseClicked() {
        changeServiceState(State.STOPPED);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        LogUtil.logService("Configuration changed ");
        if (state == State.ACTIVE) {
            removeViewPort();
            mVpCtrl = new ViewPortController(this, this);
            addViewPort();
        }
    }

    enum State {
        STOPPED,
        STANDBY,
        ACTIVE
    }

    private void loadTutorial() {
        if (mPrefs.hasToShowTutorial()) {
            mVpCtrl.showTutorial(TUTORIAL_STEPS[mTutorialStep]);
        }
    }

    private void incrementTutorial(boolean hasToUpdateViewController) {
        if (mPrefs.hasToShowTutorial()) {
            mTutorialStep++;
            if (mTutorialStep >= TUTORIAL_STEPS.length) {
                endTutorial(hasToUpdateViewController);
            } else {
                mVpCtrl.showTutorial(TUTORIAL_STEPS[mTutorialStep]);
            }
        }
    }

    private void endTutorial(boolean hasToUpdateViewController) {
        mTutorialStep = TUTORIAL_STEPS.length;
        if (hasToUpdateViewController) {
            mVpCtrl.hideTutorial();
        }
        mPrefs.setHasToShowTutorial(false);
    }


    private long totalSavingMs;
    private long lastTime;

    private void updateLastTime() {
        lastTime = SystemClock.uptimeMillis();
    }

    private long getTimeDifference() {
        final long dif = SystemClock.uptimeMillis() - lastTime;
        return dif < 0 ? 0 : dif;
    }

    private void addSaving() {
        final int blackScreenPercentage = 100 - mPrefs.getHoleHeightPercentage();
        final long timeDiffMs = getTimeDifference();
        final long timeSaved = timeDiffMs * blackScreenPercentage / 100;
        totalSavingMs += timeSaved;
        updateLastTime();
    }

    private final static int MEANINGFUL_SAVING_TIME_MINUTES = 2; //at least 2mins

    private void showSavings() {
        int timeInMins = (int) (totalSavingMs / 60000);
        if (timeInMins > MEANINGFUL_SAVING_TIME_MINUTES) {
            View toastView = View.inflate(this, R.layout.saving_toast, null);
            ((TextView) toastView.findViewById(R.id.saving_text)).setText(getString(R.string.saving, timeInMins));
            //use this creator because otherwise the LENGTH_LONG is ignored (stupid bug)
            Toast toast = Toast.makeText(this, R.string.saving, Toast.LENGTH_LONG);
            toast.setView(toastView);
            toast.show();
        }
        totalSavingMs = 0;
    }
}