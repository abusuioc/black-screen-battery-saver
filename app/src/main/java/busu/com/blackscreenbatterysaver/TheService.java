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

    public static State state = State.STOPPED;

    public final static String BROADCAST = "com.busu.blackscreenbatterysaver.STATUS_CHANGED";
    public final static String BROADCAST_CURRENT_STATE = "cst";
    public final static String BROADCAST_OLD_STATE = "ost";

    private NotificationsHelper mNotifs;
    private Preferences mPrefs;

    private ViewPortController mVpCtrl;

    public final static String ACTION_READPREFS = "read_prefs";

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
            sendBroadcast(new Intent(BROADCAST)
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
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            LogUtil.logService("Action received: " + action + " in state: " + state);
            if (action != null) {
                if (action.equals(NotificationsHelper.ACTION_SIZE_1P2)) {
                    mPrefs.setHoleHeightPercentage(Preferences.HOLE_HEIGHT_PERCENTAGE_1P2);
                    mVpCtrl.applyHoleHeigthPercentage(Preferences.HOLE_HEIGHT_PERCENTAGE_1P2);
                } else if (action.equals(NotificationsHelper.ACTION_SIZE_1P3)) {
                    mPrefs.setHoleHeightPercentage(Preferences.HOLE_HEIGHT_PERCENTAGE_1P3);
                    mVpCtrl.applyHoleHeigthPercentage(Preferences.HOLE_HEIGHT_PERCENTAGE_1P3);
                } else if (action.equals(NotificationsHelper.ACTION_STOP)) {
//                    changeServiceState(State.STANDBY);
                    // ^ ok in the scenario in which we allow service to live forever
                    changeServiceState(State.STOPPED);
                } else if (action.equals(NotificationsHelper.ACTION_START)) {
                    changeServiceState(State.ACTIVE);
                } else if (action.equals(ACTION_READPREFS)) {
                    mVpCtrl.applyHoleHeigthPercentage(mPrefs.getHoleHeightPercentage());
                    mVpCtrl.applyHoleVerticalGravity(mPrefs.getHolePosition());
                }
            }
        }

        return START_STICKY;
    }


    private void addViewPort() {
        mVpCtrl.applyHoleHeigthPercentage(mPrefs.getHoleHeightPercentage());
        mVpCtrl.applyHoleVerticalGravity(mPrefs.getHolePosition());
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
        LogUtil.logService("Click on: " + black.toString() + ", center req: " + isCenterRequested + ", hole gravity: "
                + ViewPortController.getGravityString(mVpCtrl.getHoleGravity()));
    }

    @Override
    public void onCloseClicked() {

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
}