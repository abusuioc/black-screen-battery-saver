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
    private final static String KEY_BTN_CLOSE = "kbnc";

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

    public final static int DEFAULT_HOLE_POSITION = Gravity.BOTTOM;

    public int getHolePosition() {
        return mPrefs.getInt(KEY_POS, DEFAULT_HOLE_POSITION);
    }

    public void setHolePosition(int holePosition) {
        mPrefs.edit().putInt(KEY_POS, holePosition).apply();
    }

    public final static boolean DEFAULT_BTN_CLOSE = false;

    public boolean hasToCloseAfterButtonPressed() {
        return mPrefs.getBoolean(KEY_BTN_CLOSE, DEFAULT_BTN_CLOSE);
    }

    public void setHasToCloseAfterButtonPressed(boolean hasToCloseAfterButtonPressed) {
        mPrefs.edit().putBoolean(KEY_BTN_CLOSE, hasToCloseAfterButtonPressed).apply();
    }
}
