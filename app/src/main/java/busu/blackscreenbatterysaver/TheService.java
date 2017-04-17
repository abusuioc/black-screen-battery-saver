package busu.blackscreenbatterysaver;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by adibusu on 5/14/16.
 */
public class TheService extends Service implements ViewPortController.OnTouchEvents {

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
    public final static String BROADCAST_OLD_STATE = "ost";
//    public final static String EVENT_PROPERTIES_CHANGED = "com.busu.blackscreenbatterysaver.PROPS_CHANGED";

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
        LogUtil.logService("State changed from " + state + " to " + newState);
        if (state != newState) {
            final State oldState = state;
            state = newState;
            applyCurrentServiceState(newState, oldState);
            sendBroadcast(new Intent(EVENT_STATUS_CHANGED)
                    .putExtra(BROADCAST_CURRENT_STATE, newState)
                    .putExtra(BROADCAST_OLD_STATE, oldState));
        }
    }

    private void applyCurrentServiceState(State currentState, State oldState) {
        switch (currentState) {
            case STANDBY:
                if (oldState == State.ACTIVE) {
                    removeViewPort();
                    mNotifs.cancelMainNotification();
                    mNotifs.startStandbyNotification();
                }
                break;
            case ACTIVE:
                addViewPort();
                mNotifs.cancelStandbyNotification();
                mNotifs.startOrUpdateMainNotification(new NotificationsHelper.ChangeHeightSelection(mPrefs.getHoleHeightPercentage()));
                updateLastTime();
                break;
            case STOPPED:
                if (oldState == State.ACTIVE) {
                    removeViewPort();
                    mNotifs.cancelMainNotification();
                    mNotifs.startStandbyNotification();
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
                boolean hasToCloseSystemBar = true;
                if (commandChangeSize(action)) {
                    // ^ takes care of it
                    hasToCloseSystemBar = false;
                } else if (action.equals(ACTION_STOP)) {
//                    changeServiceState(State.STANDBY);
                    // ^ ok in the scenario in which we allow service to live forever
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
                if (hasToCloseSystemBar) {
                    sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
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
//            sendBroadcast(new Intent(EVENT_PROPERTIES_CHANGED));
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
//            sendBroadcast(new Intent(EVENT_PROPERTIES_CHANGED));
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
        removeViewPort();
        mVpCtrl = new ViewPortController(this, this);
        addViewPort();
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
        //just to be safe
        return dif < 0 ? 0 : dif;
    }

    private void addSaving() {
        final int blackScreenPercentage = 100 - mPrefs.getHoleHeightPercentage();
        final long timeDiffMs = getTimeDifference();
        final long timeSaved = timeDiffMs * blackScreenPercentage / 100;
        totalSavingMs += timeSaved;
//        Toast.makeText(this, "S: " + timeDiffMs / 1000 + "s, hp%: " + blackScreenPercentage + " :: total = " + totalSavingMs / 1000, Toast.LENGTH_LONG).show();
        updateLastTime();
    }

    private final static int MEANINGFUL_SAVING_TIME_MINUTES = 1; //at least 1min

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