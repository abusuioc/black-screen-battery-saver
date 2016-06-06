package busu.com.blackscreenbatterysaver;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by adibusu on 5/28/16.
 */
public class ViewPortView extends View {

    private RectF hole;

    private RectF[] blacks;

    private Paint paintBlack;

    public final static int TOP = 0;
    public final static int BOTTOM = 1;
    public final static int LEFT = 2;
    public final static int RIGHT = 3;
    public final static int CENTER = 4;

    //TOP, BOTTOM or CENTER
    private int currentPosition = Preferences.DEFAULT_HOLE_POSITION;
    private float currentHeightPercentage = Preferences.DEFAULT_HOLE_HEIGHT_PERCENTAGE;

    public ViewPortView(Context context, int initialHoleHeightPercentage, int initialHolePosition) {
        super(context);
        currentHeightPercentage = initialHoleHeightPercentage / 100f;
        currentPosition = initialHolePosition;
        init();
    }

    void init() {
        hole = new RectF();
        blacks = new RectF[4];
        for (int i = 0; i < 4; i++) {
            blacks[i] = new RectF();
        }
        paintBlack = new Paint();
        paintBlack.setColor(Color.BLACK);
        paintBlack.setStyle(Paint.Style.FILL);

//        setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (lastClickedX >= 0f && lastClickedY >= 0f) {
//                    onClicked();
//                }
//            }
//        });
    }

    public void applyHoleHeigthPercentage(int percentage) {
        this.currentHeightPercentage = percentage / 100f;
        setHoleHeight(currentHeightPercentage);
        applyHoleChanged();
    }

    public void applyHolePosition(int position) {
        currentPosition = position;
        applyHoleChanged();
    }

//    private void onClicked() {
//        changeHolePosition(lastClickedY < hole.top);
//        applyHoleChanged();
//    }

    private void changeHolePosition(boolean hasToMoveUpwards) {
        if (hasToMoveUpwards) {
            if (currentPosition == CENTER) {
                currentPosition = TOP;
            } else if (currentPosition == BOTTOM) {
                currentPosition = CENTER;
            }
        } else {
            if (currentPosition == CENTER) {
                currentPosition = BOTTOM;
            } else if (currentPosition == TOP) {
                currentPosition = CENTER;
            }
        }
    }

    private void commitPositionToHole() {
        float holeTop = hole.top;
        switch (currentPosition) {
            case TOP:
                holeTop = 0f;
                break;
            case CENTER:
                holeTop = (getHeight() - hole.height()) / 2.0f;
                break;
            case BOTTOM:
                holeTop = getHeight() - hole.height();
                break;
        }
        final float holeHeight = hole.height();
        hole.top = holeTop;
        hole.bottom = holeTop + holeHeight;
    }

    private void adjustBlacks() {
        final int parentWidth = getWidth();
        final int parentHeight = getHeight();
        //
        blacks[TOP].set(0, 0, parentWidth, hole.top);
        blacks[BOTTOM].set(0, hole.bottom, parentWidth, parentHeight);
        blacks[LEFT].set(0, hole.top, hole.left, hole.height());
        blacks[RIGHT].set(hole.right, hole.top, parentWidth, hole.height());
    }

    /**
     * Applies a new height to the hole, but does no check if the percentage is valid
     * Always follow it by a #commitPositionToHole or, even better, #applyHoleChanged
     *
     * @param percentageOfViewHeight
     */
    private void setHoleHeight(float percentageOfViewHeight) {
        final float holeHeight = getHeight() * percentageOfViewHeight;
        //always position TOP, next should come always code that positions the hole vertically
        hole.set(0, 0, getWidth(), holeHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w > 0 && h > 0 && w != oldw && h != oldh) {
            setHoleHeight(currentHeightPercentage);
            applyHoleChanged();
        }
    }

    /**
     * Call this when hole's position or size has changed
     */
    private void applyHoleChanged() {
        commitPositionToHole();
        adjustBlacks();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (RectF black : blacks) {
            canvas.drawRect(black, paintBlack);
        }
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        if (hole.contains(event.getX(), event.getY())) {
//            return false;
//        }
//        super.onTouchEvent(event);
//        recordPositionOfClick(event);
//        return true;
//    }
//
//    private float lastClickedX, lastClickedY;
//
//    private void recordPositionOfClick(MotionEvent motionEvent) {
//        lastClickedX = lastClickedY = -1f;
//        switch (motionEvent.getAction()) {
//            case MotionEvent.ACTION_UP:
//            case MotionEvent.ACTION_MOVE:
//                lastClickedX = motionEvent.getX();
//                lastClickedY = motionEvent.getY();
//                break;
//        }
//    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    changeHolePosition(true);
                    applyHoleChanged();
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    changeHolePosition(false);
                    applyHoleChanged();
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }


}
