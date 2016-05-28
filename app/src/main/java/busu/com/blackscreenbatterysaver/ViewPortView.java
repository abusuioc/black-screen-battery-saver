package busu.com.blackscreenbatterysaver;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

/**
 * Created by adibusu on 5/28/16.
 */
public class ViewPortView extends View {

    private Rect hole;

    private Rect[] blacks;

    private Paint paintBlack;

    final float DEFAULT_HEIGHT_PERCENTAGE = 0.3f;

    final int TOP = 0;
    final int BOTTOM = 1;
    final int LEFT = 2;
    final int RIGHT = 3;

    public ViewPortView(Context context) {
        super(context);
        init();
    }

    void init() {
        hole = new Rect();
        blacks = new Rect[4];
        for (int i = 0; i < 4; i++) {
            blacks[i] = new Rect();
        }
        paintBlack = new Paint();
        paintBlack.setColor(Color.BLACK);
        paintBlack.setStyle(Paint.Style.FILL);
    }

    private void adjustBlacks() {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        //
        blacks[TOP].set(0, 0, width, hole.top);
        blacks[BOTTOM].set(0, hole.bottom, width, height);
        blacks[LEFT].set(0, hole.top, hole.left, hole.height());
        blacks[RIGHT].set(hole.right, hole.top, width, hole.height());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();

        final int holeHeight = (int) (height * DEFAULT_HEIGHT_PERCENTAGE);
        hole.set(0, (height - holeHeight) / 2, width, (height + holeHeight) / 2);
        adjustBlacks();
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
        for (Rect black : blacks) {
            canvas.drawRect(black, paintBlack);
        }

    }
}
