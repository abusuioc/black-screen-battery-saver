package busu.com.blackscreenbatterysaver;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;

/**
 * Created by adibusu on 5/14/16.
 */
public class TheService extends Service {

    public static boolean isStarted;

    public final static String BROADCAST = "com.busu.blackscreenbatterysaver.STATUS_CHANGED";

    private WindowManager windowManager;
    private ViewPortView viewPort;
    private NotificationsHelper mNotifs;
    private Preferences mPrefs;

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
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE /*| WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM*/ | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
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
                    stop();
                } else if (action.equals(NotificationsHelper.ACTION_READPREFS)) {
                    viewPort.applyHoleHeigthPercentage(mPrefs.getHoleHeightPercentage());
                    viewPort.applyHolePosition(mPrefs.getHolePosition());
                }
            }
        }

        return START_STICKY;
    }

    private void stop() {
        stopSelf();
        mNotifs.cancelNotification();
    }
}