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

    public static boolean isStarted;

    public final static String BROADCAST = "com.busu.blackscreenbatterysaver.STATUS_CHANGED";

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

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM /*| WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM*/ | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;

        windowManager.addView(viewPort, params);

        //
        mNotifs = new NotificationsHelper(this);
        mNotifs.fireNotification(mNotifs.buildServiceStarted());
        //
        changeStartedStatus(true);

        //
        configureShaker();
    }


    private void changeStartedStatus(boolean hasToStart) {
        isStarted = hasToStart;
        sendBroadcast(new Intent(BROADCAST));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (viewPort != null) windowManager.removeView(viewPort);
        changeStartedStatus(false);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
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
                    stopService();
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

    private void stopService() {
        stopSelf();
        mNotifs.cancelNotification();
    }

    @Override
    public void hearShake() {
        stopListeningToShakeEvents();
        stopService();
    }

    private void configureShaker() {
        final boolean hasToStopOnShake = mPrefs.hasToStopOnShake();
        if (hasToStopOnShake) {
            if (mShakeDetector == null) {
                SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
                mShakeDetector = new ShakeDetector(this);
                mShakeDetector.start(sensorManager);
            }
            mShakeDetector.setSensitivity(mPrefs.getShakeSensitivity());
        } else {
            stopListeningToShakeEvents();
        }
    }

    private void stopListeningToShakeEvents() {
        if (mShakeDetector != null) {
            mShakeDetector.stop();
            mShakeDetector = null;
        }
    }
}