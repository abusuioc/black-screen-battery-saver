package busu.com.blackscreenbatterysaver;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

/**
 * Created by adibusu on 5/14/16.
 */
public class TheService extends Service {

    private WindowManager windowManager;
    private View viewPort;
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
}