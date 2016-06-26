package busu.com.blackscreenbatterysaver;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by adibusu on 6/25/16.
 */
public class ViewPortController {

    private final Map<Integer, ViewLayout> mBlacks = new HashMap<>(4);
    private final OnTouchEvents mClickListener;
    private final WindowManager mWindowManager;

    //Gravity. TOP, BOTTOM or CENTER
    private int mHoleGravity = Preferences.DEFAULT_HOLE_POSITION;
    private float mHoleHeightRatio = Preferences.DEFAULT_HOLE_HEIGHT_PERCENTAGE / 100f;

    public ViewPortController(Context context, OnTouchEvents listener) {
        mBlacks.put(Gravity.TOP, new ViewLayout(context, Gravity.TOP));
        mBlacks.put(Gravity.BOTTOM, new ViewLayout(context, Gravity.BOTTOM));
        mClickListener = listener;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    /**
     * Call to add the blacks to the window; after this or even before,
     * a call to {@link #applyHoleHeigthPercentage} or {@link #applyHoleVerticalGravity} is ok to configure the blacks
     */
    public void addToWindow() {
        for (ViewLayout viewLayout : mBlacks.values()) {
            mWindowManager.addView(viewLayout.mView, viewLayout.mLayoutParams);
        }
    }

    public void removeFromWindow() {
        for (ViewLayout viewLayout : mBlacks.values()) {
            mWindowManager.removeView(viewLayout.mView);
        }
    }

    private int getWindowFlags() {
        return WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
    }

    protected void changeOfLayoutParamsRequested(ViewLayout black) {
        mWindowManager.removeView(black.mView);
        mWindowManager.addView(black.mView, black.mLayoutParams);
        LogUtil.logService("Ch lparams: " + black.mLayoutParams + " for " + black);
    }

    /**
     * Set and apply the hole (viewport in between blacks) 's height percentage
     *
     * @param percentage [0-100]
     */
    public void applyHoleHeigthPercentage(int percentage) {
        if (percentage >= 0 && percentage <= 100) {
            mHoleHeightRatio = percentage / 100f;
            applyHolePropertiesChanged();
        }
    }

    /**
     * Set and apply the hole (viewport in between blacks) 's gravity on the V-axis
     *
     * @param gravity [Gravity.TOP, CENTER or BOTTOM]
     */
    public void applyHoleVerticalGravity(int gravity) {
        if (gravity == Gravity.TOP ||
                gravity == Gravity.CENTER ||
                gravity == Gravity.BOTTOM) {
            mHoleGravity = gravity;
            applyHolePropertiesChanged();
        }
    }


    public void changeHoleGravity(boolean hasToMoveUpwards) {
        if (hasToMoveUpwards) {
            if (mHoleGravity == Gravity.CENTER) {
                mHoleGravity = Gravity.TOP;
            } else if (mHoleGravity == Gravity.BOTTOM) {
                mHoleGravity = Gravity.CENTER;
            }
        } else {
            if (mHoleGravity == Gravity.CENTER) {
                mHoleGravity = Gravity.BOTTOM;
            } else if (mHoleGravity == Gravity.TOP) {
                mHoleGravity = Gravity.CENTER;
            }
        }
        applyHolePropertiesChanged();
    }

    private void adjustBlacks() {
        float hrTop, hrBottom;
        switch (mHoleGravity) {
            case Gravity.TOP:
                hrTop = 0f;
                hrBottom = 1.0f - mHoleHeightRatio;
                break;
            case Gravity.CENTER:
                hrTop = hrBottom = (1.0f - mHoleHeightRatio) / 2;
                break;
            case Gravity.BOTTOM:
            default:
                hrBottom = 0f;
                hrTop = 1.0f - mHoleHeightRatio;
                break;

        }

        mBlacks.get(Gravity.TOP).applyHeightRatio(hrTop);
        mBlacks.get(Gravity.BOTTOM).applyHeightRatio(hrBottom);
    }

    /**
     * Call this when hole's gravity or height has changed
     */
    private void applyHolePropertiesChanged() {
        adjustBlacks();
    }


    public class ViewLayout {
        private View mView;
        private WindowManager.LayoutParams mLayoutParams;
        private boolean isAdded;
        public int gravity;
        private float mHeightRatio;
        private int mTotalHeight;

        public ViewLayout(Context context, int gravity) {
            this.gravity = gravity;
            initView(context);
            initLayoutParams();
        }

        private void initLayoutParams() {
            mLayoutParams = new WindowManager.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    getWindowFlags(),
                    PixelFormat.OPAQUE);
            mLayoutParams.gravity = Gravity.LEFT | gravity;
        }

        private void initView(Context context) {
            mView = new View(context);
            mView.setBackgroundColor(Color.BLACK);
            mView.setDrawingCacheEnabled(true);
            mView.setWillNotDraw(false);
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClickListener.onBlackClicked(ViewLayout.this);
                }
            });
            mView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    final int oldWidth = oldRight - oldLeft;
                    final int oldHeight = oldBottom - oldTop;
                    final int newWidth = right - left;
                    final int newHeight = bottom - top;
                    final boolean isTheSame = (newHeight == oldHeight && newWidth == oldWidth);
                    if (!isTheSame) {
                        v.removeOnLayoutChangeListener(this);
                        mTotalHeight = newHeight;
                        applyHeightRatio(mHeightRatio);
                    }
                }
            });
        }

        public void applyHeightRatio(float ratio) {
            mHeightRatio = ratio;
            if (mTotalHeight > 0) {
                //after we know
                mLayoutParams.height = (int) (mTotalHeight * mHeightRatio);
                changeOfLayoutParamsRequested(this);
            } else {
                //just set the height ratio
            }
        }
    }

    public interface OnTouchEvents {
        void onBlackClicked(ViewLayout black);

        void onCloseClicked();
    }
}
