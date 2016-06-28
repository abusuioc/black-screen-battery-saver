package busu.com.blackscreenbatterysaver;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by adibusu on 6/25/16.
 */
public class ViewPortController {

    private final Map<Integer, ViewLayout> mBlacks = new HashMap<>(4);
    private final OnTouchEvents mClickListener;
    private final WindowManager mWindowManager;
    private final View mCloseButton;

    //Gravity. TOP, BOTTOM or CENTER
    private int mHoleGravity = Preferences.DEFAULT_HOLE_POSITION;
    private float mHoleHeightRatio = Preferences.DEFAULT_HOLE_HEIGHT_PERCENTAGE / 100f;

    public ViewPortController(Context context, OnTouchEvents listener) {
        mBlacks.put(Gravity.TOP, new ViewLayout(context, Gravity.TOP));
        mBlacks.put(Gravity.BOTTOM, new ViewLayout(context, Gravity.BOTTOM));
        mClickListener = listener;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mCloseButton = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.close_button, null);
        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickListener.onCloseClicked();
            }
        });
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
        mWindowManager.updateViewLayout(black.mView, black.mLayoutParams);
//        mWindowManager.removeView(black.mView);
//        mWindowManager.addView(black.mView, black.mLayoutParams);
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

    public void changeHoleGravity(boolean centerRequested, int requesterGravity) {
        if (mHoleGravity == Gravity.CENTER || !centerRequested) {
            mHoleGravity = requesterGravity;
        } else {
            mHoleGravity = Gravity.CENTER;
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
        positionCloseButton();
    }

    private void positionCloseButton() {
        if (mHoleGravity == Gravity.BOTTOM) {
            mBlacks.get(Gravity.TOP).showCloseButton();
        } else {
            mBlacks.get(Gravity.BOTTOM).showCloseButton();
        }
    }


    public int getHoleGravity() {
        return mHoleGravity;
    }

    public static String getGravityString(int gravity) {
        switch (gravity) {
            case Gravity.TOP:
                return "TOP";
            case Gravity.CENTER:
                return "CENTER";
            case Gravity.BOTTOM:
                return "BOTTOM";
        }
        return "UNKNOWN GRAVITY: " + gravity;
    }

    public class ViewLayout {
        private ViewGroup mView;
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
            disableAnimations();
        }

        /**
         * Use refletion to disable;
         * In the current setup where only the height changes and the black are laid using gravity, animations do a weird effect
         */
        private void disableAnimations() {
            try {
                int currentFlags = (Integer) mLayoutParams.getClass().getField("privateFlags").get(mLayoutParams);
                mLayoutParams.getClass().getField("privateFlags").set(mLayoutParams, currentFlags | 0x00000040);
            } catch (Exception e) {
            }
        }

        private View.OnLayoutChangeListener mLayoutChangeListener = new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
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
        };

        private void initView(Context context) {
            mView = new FrameLayout(context);
            mView.setBackgroundColor(Color.BLACK);
            mView.setDrawingCacheEnabled(true);
            mView.setWillNotDraw(false);
            mView.setOnTouchListener(new View.OnTouchListener() {
                private static final int MAX_CLICK_DURATION_MS = 1000;
                private static final int MAX_CLICK_DISTANCE_DP = 15;

                private long pressStartTime;
                private float pressedX;
                private float pressedY;
                private boolean stayedWithinClickDistance;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            pressStartTime = System.currentTimeMillis();
                            pressedX = event.getX();
                            pressedY = event.getY();
                            stayedWithinClickDistance = true;
                            break;
                        }
                        case MotionEvent.ACTION_MOVE: {
                            if (stayedWithinClickDistance && distance(pressedX, pressedY, event.getX(), event.getY()) > MAX_CLICK_DISTANCE_DP) {
                                stayedWithinClickDistance = false;
                            }
                            break;
                        }
                        case MotionEvent.ACTION_UP: {
                            long pressDuration = System.currentTimeMillis() - pressStartTime;
                            if (pressDuration < MAX_CLICK_DURATION_MS && stayedWithinClickDistance) {
                                // Click event has occurred
                                mClickListener.onBlackClicked(ViewLayout.this, pressedY / mView.getHeight());
                            }
                        }
                    }
                    return true;
                }

                private float distance(float x1, float y1, float x2, float y2) {
                    float dx = x1 - x2;
                    float dy = y1 - y2;
                    float distanceInPx = (float) Math.sqrt(dx * dx + dy * dy);
                    return pxToDp(distanceInPx);
                }

                private float pxToDp(float px) {
                    return px / mView.getResources().getDisplayMetrics().density;
                }
            });
            mView.addOnLayoutChangeListener(mLayoutChangeListener);
        }

        public void applyHeightRatio(float ratio) {
            mHeightRatio = ratio;
            if (mTotalHeight > 0) {
                //after we know
                mLayoutParams.height = (int) (mTotalHeight * mHeightRatio);
                if (mLayoutParams.height == 0) {
                    //not repainting for width = 0; TODO switch to position based resizing effect (constant height, adjust y)
                    mLayoutParams.height = 1;
                }
                changeOfLayoutParamsRequested(this);
            } else {
                //just set the height ratio
            }
        }

        @Override
        public String toString() {
            final String gravityString = getGravityString(gravity);
            return "Black_" + gravityString;
        }

        protected void showCloseButton() {
            ViewGroup parent = (ViewGroup) mCloseButton.getParent();
            if (parent != null) {
                parent.removeView(mCloseButton);
            }
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.gravity = gravity | Gravity.RIGHT;
            mView.addView(mCloseButton, lp);
        }
    }

    public interface OnTouchEvents {
        void onBlackClicked(ViewLayout black, float clickVerticalRatio);

        void onCloseClicked();
    }
}
