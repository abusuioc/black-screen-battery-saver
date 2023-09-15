package busu.blackscreenbatterysaver;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import android.view.Gravity;

import androidx.annotation.NonNull;

public class Preferences {

    private final static String KEY_HEIGHT = "kh";
    private final static String KEY_POS = "kp";
    private final static String KEY_TUTORIAL = "ktut";
    private final static String KEY_FULL_OPAQUE = "kfo";

    private final SharedPreferences mPrefs;

    public Preferences(Context context) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public enum ViewportHeight {
        ZERO(0), HALF(50), THIRD(33);

        private final int mPercentage;
        ViewportHeight(int percentage) {
            this.mPercentage = percentage;
        }

        public int getPercentage() {
            return mPercentage;
        }

        @NonNull
        public static ViewportHeight fromAcceptedPercentages(int value) {
            for (ViewportHeight type : values()) {
                if (type.getPercentage() == value) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Cannot create from " + value);
        }
    }

    public static final ViewportHeight DEFAULT_HOLE_HEIGHT_PERCENTAGE = ViewportHeight.HALF;

    public ViewportHeight getViewportHeight() {
        return ViewportHeight.fromAcceptedPercentages(mPrefs.getInt(KEY_HEIGHT, DEFAULT_HOLE_HEIGHT_PERCENTAGE.getPercentage()));
    }

    public void setViewportHeight(ViewportHeight viewportHeight) {
        mPrefs.edit().putInt(KEY_HEIGHT, viewportHeight.getPercentage()).apply();
    }

    public final static int DEFAULT_HOLE_GRAVITY = Gravity.BOTTOM;

    public int getViewportGravity() {
        return mPrefs.getInt(KEY_POS, DEFAULT_HOLE_GRAVITY);
    }

    public void setViewportGravity(int viewportGravity) {
        mPrefs.edit().putInt(KEY_POS, viewportGravity).apply();
    }

    public final static boolean DEFAULT_SHOW_TUTORIAL = true;

    public boolean hasToShowTutorial() {
        return mPrefs.getBoolean(KEY_TUTORIAL, DEFAULT_SHOW_TUTORIAL);
    }

    public void setHasToShowTutorial(boolean hasToShowTutorial) {
        mPrefs.edit().putBoolean(KEY_TUTORIAL, hasToShowTutorial).apply();
    }

    public final static boolean DEFAULT_FULL_OPAQUE = true;

    public boolean isFullOpaque() {
        return mPrefs.getBoolean(KEY_FULL_OPAQUE, DEFAULT_FULL_OPAQUE);
    }

    public void setIsFullOpaque(boolean isFullOpaque) {
        mPrefs.edit().putBoolean(KEY_FULL_OPAQUE, isFullOpaque).apply();
    }
}
