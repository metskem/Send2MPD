package nl.computerhok.send2mpd;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

public class BorderedTextView extends TextView {
    Rect rect = new Rect();
    Paint paint = new Paint();

    public BorderedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public BorderedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BorderedTextView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(12);
        getLocalVisibleRect(rect);
        canvas.drawRect(rect, paint);
    }
}