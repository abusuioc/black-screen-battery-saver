package busu.com.blackscreenbatterysaver;

import android.util.Log;

/**
 * Created by adibusu on 6/20/16.
 */
public class LogUtil {

    private static boolean canLog() {
        return BuildConfig.DEBUG;
    }

    public static void logService(String message) {
        if (canLog()) {
            Log.d("BBS_Service", message);
        }
    }

    public static void logSettings(String message) {
        if (canLog()) {
            Log.d("BBS_Settings", message);
        }
    }
}
