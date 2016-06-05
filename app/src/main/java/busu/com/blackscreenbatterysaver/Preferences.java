package busu.com.blackscreenbatterysaver;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by adibusu on 6/5/16.
 */
public class Preferences {

    private final static String KEY_HEIGHT = "kh";
    private final static String KEY_POS = "kp";

    private SharedPreferences mPrefs;

    public Preferences(Context context) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static final float HOLE_HEIGHT_PERCENTAGE_1P3 = 0.33f;
    public static final float HOLE_HEIGHT_PERCENTAGE_1P2 = 0.5f;
    public static final float DEFAULT_HOLE_HEIGHT_PERCENTAGE = HOLE_HEIGHT_PERCENTAGE_1P2;

    public float getHoleHeightPercentage() {
        return mPrefs.getFloat(KEY_HEIGHT, DEFAULT_HOLE_HEIGHT_PERCENTAGE);
    }

    public void setHoleHeightPercentage(float holeHeightPercentage) {
        mPrefs.edit().putFloat(KEY_HEIGHT, holeHeightPercentage).apply();
    }

    public final static int DEFAULT_HOLE_POSITION = ViewPortView.BOTTOM;

    public int getHolePosition() {
        return mPrefs.getInt(KEY_POS, DEFAULT_HOLE_POSITION);
    }

    public void setHolePosition(int holePosition) {
        mPrefs.edit().putInt(KEY_POS, holePosition).apply();
    }


}
