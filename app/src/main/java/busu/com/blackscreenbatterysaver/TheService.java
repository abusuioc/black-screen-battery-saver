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

    private WindowManager windowManager;
    private ViewPortView viewPort;
    private NotificationsHelper mNotifs;

    @Override
    public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(Starter.TAG, "Service started");

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        viewPort = new ViewPortView(this);

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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (viewPort != null) windowManager.removeView(viewPort);
        Log.i(Starter.TAG, "Service destroyed");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            if (action != null) {
                if (action.equals(NotificationsHelper.ACTION_SIZE_1P2)) {
                    viewPort.setHeigthPercentage(0.5f);
                } else if (action.equals(NotificationsHelper.ACTION_SIZE_1P3)) {
                    viewPort.setHeigthPercentage(0.33f);
                } else if (action.equals(NotificationsHelper.ACTION_POS_TOP)) {
                    viewPort.setPosition(ViewPortView.TOP);
                } else if (action.equals(NotificationsHelper.ACTION_POS_CENTER)) {
                    viewPort.setPosition(ViewPortView.CENTER);
                } else if (action.equals(NotificationsHelper.ACTION_POS_BOTTOM)) {
                    viewPort.setPosition(ViewPortView.BOTTOM);
                } else if (action.equals(NotificationsHelper.ACTION_STOP)) {
                    stop();
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