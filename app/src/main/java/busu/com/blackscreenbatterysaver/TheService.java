package busu.com.blackscreenbatterysaver;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.squareup.seismic.ShakeDetector;

/**
 * Created by adibusu on 5/14/16.
 */
public class TheService extends Service implements ShakeDetector.Listener {

    public static State state = State.STOPPED;

    public final static String BROADCAST = "com.busu.blackscreenbatterysaver.STATUS_CHANGED";
    public final static String BROADCAST_CURRENT_STATE = "cst";
    public final static String BROADCAST_OLD_STATE = "ost";

    private WindowManager windowManager;
    private ViewPortView viewPort;
    private NotificationsHelper mNotifs;
    private Preferences mPrefs;
    private ShakeDetector mShakeDetector;

    public final static String ACTION_READPREFS = "read_prefs";
    public final static String ACTION_CHANGE_SHAKE_SENSITIVITY = "ch_shk";
    private final static String CHANGE_SHAKE_EXTRA = "ch_shk_extra";

    @Override
    public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mPrefs = new Preferences(this);
        viewPort = new ViewPortView(this, mPrefs.getHoleHeightPercentage(), mPrefs.getHolePosition());
        mNotifs = new NotificationsHelper(this);

        configureShaker();
        //
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
                    viewPort.applyHoleHeigthPercentage(Preferences.HOLE_HEIGHT_PERCENTAGE_1P2);
                } else if (action.equals(NotificationsHelper.ACTION_SIZE_1P3)) {
                    mPrefs.setHoleHeightPercentage(Preferences.HOLE_HEIGHT_PERCENTAGE_1P3);
                    viewPort.applyHoleHeigthPercentage(Preferences.HOLE_HEIGHT_PERCENTAGE_1P3);
                } else if (action.equals(NotificationsHelper.ACTION_POS_TOP)) {
                    mPrefs.setHolePosition(ViewPortView.TOP);
                    viewPort.applyHolePosition(ViewPortView.TOP);
                } else if (action.equals(NotificationsHelper.ACTION_POS_CENTER)) {
                    mPrefs.setHolePosition(ViewPortView.CENTER);
                    viewPort.applyHolePosition(ViewPortView.CENTER);
                } else if (action.equals(NotificationsHelper.ACTION_POS_BOTTOM)) {
                    mPrefs.setHolePosition(ViewPortView.BOTTOM);
                    viewPort.applyHolePosition(ViewPortView.BOTTOM);
                } else if (action.equals(NotificationsHelper.ACTION_STOP)) {
//                    changeServiceState(State.STANDBY);
                    // ^ ok in the scenario in which we allow service to live forever
                    changeServiceState(State.STOPPED);
                } else if (action.equals(NotificationsHelper.ACTION_START)) {
                    changeServiceState(State.ACTIVE);
                } else if (action.equals(ACTION_READPREFS)) {
                    viewPort.applyHoleHeigthPercentage(mPrefs.getHoleHeightPercentage());
                    viewPort.applyHolePosition(mPrefs.getHolePosition());
                    configureShaker();
                } else if (action.equals(ACTION_CHANGE_SHAKE_SENSITIVITY)) {
                    final int shakeSensitivity = intent.getIntExtra(CHANGE_SHAKE_EXTRA, ShakeDetector.SENSITIVITY_MEDIUM);
                    mShakeDetector.setSensitivity(shakeSensitivity);
                }
            }
        }

        return START_STICKY;
    }

    @Override
    public void hearShake() {
        //DISABLE SHAKING FOR NOW

//        LogUtil.logService("Shake received in state: " + state);
//
//        final boolean hasToStopOnShake = mPrefs.hasToStopOnShake();
//        final boolean hasToStartOnShake = mPrefs.hasToStartOnShake();
//
//        if (state == State.ACTIVE && hasToStopOnShake) {
//            changeServiceState(State.STANDBY);
//            if (!hasToStartOnShake) {
//                stopListeningToShakeEvents();
//            }
//        } else if (state == State.STANDBY && hasToStartOnShake) {
//            changeServiceState(State.ACTIVE);
//            if (!hasToStopOnShake) {
//                stopListeningToShakeEvents();
//            }
//        }
    }

    private void configureShaker() {
        //DISABLE SHAKING FOR NOW

//        final boolean hasToStopOnShake = mPrefs.hasToStopOnShake();
//        final boolean hasToStartOnShake = mPrefs.hasToStartOnShake();
//        final boolean hasNeedForShaker = hasToStartOnShake || hasToStopOnShake;
//        if (hasNeedForShaker) {
//            if (mShakeDetector == null) {
//                SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
//                mShakeDetector = new ShakeDetector(this);
//                mShakeDetector.start(sensorManager);
//            }
//            mShakeDetector.setSensitivity(mPrefs.getShakeSensitivity());
//        } else {
//            stopListeningToShakeEvents();
//        }
    }

    private void stopListeningToShakeEvents() {
        //DISABLE SHAKING FOR NOW

//        if (mShakeDetector != null) {
//            mShakeDetector.stop();
//            mShakeDetector = null;
//        }
    }

    private void addViewPort() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM /*| WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM*/ | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);

        windowManager.addView(viewPort, params);
    }

    private void removeViewPort() {
        windowManager.removeView(viewPort);
    }


    enum State {
        STOPPED,
        STANDBY,
        ACTIVE
    }
}