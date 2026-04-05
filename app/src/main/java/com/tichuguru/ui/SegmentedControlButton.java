package com.tichuguru.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.RadioButton;

public class SegmentedControlButton extends androidx.appcompat.widget.AppCompatRadioButton {
    private float mX;

    public SegmentedControlButton(Context context) {
        super(context);
    }

    public SegmentedControlButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SegmentedControlButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onDraw(Canvas canvas) {
        String text = getText().toString();
        Paint textPaint = new Paint();
        textPaint.setAntiAlias(true);
        float currentHeight = textPaint.measureText("x");
        textPaint.setTextSize(getTextSize());
        textPaint.setTextAlign(Paint.Align.CENTER);
        if (isChecked()) {
            GradientDrawable grad = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{-2302756, -15658735});
            grad.setBounds(0, 0, getWidth(), getHeight());
            grad.draw(canvas);
            textPaint.setColor(-1);
        } else {
            GradientDrawable grad2 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{-5921371, -16777216});
            grad2.setBounds(0, 0, getWidth(), getHeight());
            grad2.draw(canvas);
            textPaint.setColor(-3355444);
        }
        float h = (getHeight() / 2) + currentHeight;
        canvas.drawText(text, this.mX, h, textPaint);
        Paint paint = new Paint();
        paint.setColor(-16777216);
        paint.setStyle(Paint.Style.STROKE);
        Rect rect = new Rect(0, 0, getWidth(), getHeight());
        canvas.drawRect(rect, paint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        this.mX = w * 0.5f;
    }
}
