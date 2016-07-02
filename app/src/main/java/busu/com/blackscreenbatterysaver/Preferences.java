package busu.com.blackscreenbatterysaver;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.Gravity;

/**
 * Created by adibusu on 6/5/16.
 */
public class Preferences {

    private final static String KEY_HEIGHT = "kh";
    private final static String KEY_POS = "kp";
    private final static String KEY_TUTORIAL = "ktut";

    private SharedPreferences mPrefs;

    public Preferences(Context context) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static final int HOLE_HEIGHT_PERCENTAGE_1P3 = 33;
    public static final int HOLE_HEIGHT_PERCENTAGE_1P2 = 50;
    public static final int DEFAULT_HOLE_HEIGHT_PERCENTAGE = HOLE_HEIGHT_PERCENTAGE_1P2;

    public int getHoleHeightPercentage() {
        return mPrefs.getInt(KEY_HEIGHT, DEFAULT_HOLE_HEIGHT_PERCENTAGE);
    }

    public void setHoleHeightPercentage(int holeHeightPercentage) {
        mPrefs.edit().putInt(KEY_HEIGHT, holeHeightPercentage).apply();
    }

    public final static int DEFAULT_HOLE_GRAVITY = Gravity.BOTTOM;

    public int getHoleGravity() {
        return mPrefs.getInt(KEY_POS, DEFAULT_HOLE_GRAVITY);
    }

    public void setHoleGravity(int holePosition) {
        mPrefs.edit().putInt(KEY_POS, holePosition).apply();
    }

    public final static boolean DEFAULT_SHOW_TUTORIAL = true;

    public boolean hasToShowTutorial() {
        return mPrefs.getBoolean(KEY_TUTORIAL, DEFAULT_SHOW_TUTORIAL);
    }

    public void setHasToShowTutorial(boolean hasToShowTutorial) {
        mPrefs.edit().putBoolean(KEY_TUTORIAL, hasToShowTutorial).apply();
    }
}
