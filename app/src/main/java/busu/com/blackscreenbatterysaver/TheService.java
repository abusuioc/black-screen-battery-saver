package busu.com.blackscreenbatterysaver;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.view.Gravity;

/**
 * Created by adibusu on 5/14/16.
 */
public class TheService extends Service implements ViewPortController.OnTouchEvents {

    public final static String ACTION_SIZE_1P2 = "1p2";
    public final static String ACTION_SIZE_1P3 = "1p3";
    public final static String ACTION_STOP = "stop";
    public final static String ACTION_START = "start";
    public final static String ACTION_READPREFS = "read_prefs";
    public final static String ACTION_TUTORIAL = "tut";

    public static State state = State.STOPPED;

    public final static String EVENT_STATUS_CHANGED = "com.busu.blackscreenbatterysaver.STATUS_CHANGED";
    public final static String BROADCAST_CURRENT_STATE = "cst";
    public final static String BROADCAST_OLD_STATE = "ost";
    public final static String EVENT_PROPERTIES_CHANGED = "com.busu.blackscreenbatterysaver.PROPS_CHANGED";

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
                    mNotifs.cancelNotification();
                }
                break;
            case ACTIVE:
                addViewPort();
                mNotifs.fireNotification(mNotifs.buildServiceStarted());
                break;
            case STOPPED:
                if (oldState == State.ACTIVE) {
                    removeViewPort();
                    mNotifs.cancelNotification();
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
                if (action.equals(ACTION_SIZE_1P2)) {
                    mPrefs.setHoleHeightPercentage(Preferences.HOLE_HEIGHT_PERCENTAGE_1P2);
                    mVpCtrl.applyHoleHeigthPercentage(Preferences.HOLE_HEIGHT_PERCENTAGE_1P2);
                    sendBroadcast(new Intent(EVENT_PROPERTIES_CHANGED));
                } else if (action.equals(ACTION_SIZE_1P3)) {
                    mPrefs.setHoleHeightPercentage(Preferences.HOLE_HEIGHT_PERCENTAGE_1P3);
                    mVpCtrl.applyHoleHeigthPercentage(Preferences.HOLE_HEIGHT_PERCENTAGE_1P3);
                    sendBroadcast(new Intent(EVENT_PROPERTIES_CHANGED));
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
                sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
            }
        }

        return START_STICKY;
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
        boolean isCenterRequested = (clickVerticalRatio <= 0.5f && black.gravity == Gravity.BOTTOM)
                || (clickVerticalRatio > 0.5f && black.gravity == Gravity.TOP);
        mVpCtrl.changeHoleGravity(isCenterRequested, black.gravity);
        mPrefs.setHoleGravity(mVpCtrl.getHoleGravity());
        sendBroadcast(new Intent(EVENT_PROPERTIES_CHANGED));
        incrementTutorial(true);
        LogUtil.logService("Click on: " + black.toString() + ", center req: " + isCenterRequested + ", hole gravity: "
                + ViewPortController.getGravityString(mVpCtrl.getHoleGravity()));
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
}