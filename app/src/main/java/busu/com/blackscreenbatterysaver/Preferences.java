package busu.com.blackscreenbatterysaver;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.squareup.seismic.ShakeDetector;

/**
 * Created by adibusu on 6/5/16.
 */
public class Preferences {

    private final static String KEY_HEIGHT = "kh";
    private final static String KEY_POS = "kp";
    private final static String KEY_BTN_CLOSE = "kbnc";
    private final static String KEY_SHAKE_SENSITIVITY = "kshs";
    private final static String KEY_SHAKE_START = "kstartonsh";
    private final static String KEY_SHAKE_STOP = "kstoponsh";

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

    public final static int DEFAULT_HOLE_POSITION = ViewPortView.BOTTOM;

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

    public final static int DEFAULT_SHAKE_SENSITIVITY = ShakeDetector.SENSITIVITY_MEDIUM;

    public int getShakeSensitivity() {
        return mPrefs.getInt(KEY_SHAKE_SENSITIVITY, DEFAULT_SHAKE_SENSITIVITY);
    }

    public void setShakeSensitivity(int shakeSensitivity) {
        mPrefs.edit().putInt(KEY_SHAKE_SENSITIVITY, shakeSensitivity).apply();
    }

    public final static boolean DEFAULT_SHAKE_START = false;

    public boolean hasToStartOnShake() {
        return mPrefs.getBoolean(KEY_SHAKE_START, DEFAULT_SHAKE_START);
    }

    public void setStartOnShake(boolean hasToStartOnShake) {
        mPrefs.edit().putBoolean(KEY_SHAKE_START, hasToStartOnShake).apply();
    }

    public final static boolean DEFAULT_SHAKE_STOP = true;

    public boolean hasToStopOnShake() {
        return mPrefs.getBoolean(KEY_SHAKE_STOP, DEFAULT_SHAKE_STOP);
    }

    public void setStopOnShake(boolean hasToStopOnShake) {
        mPrefs.edit().putBoolean(KEY_SHAKE_STOP, hasToStopOnShake).apply();
    }
}
