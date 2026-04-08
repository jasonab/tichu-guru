package com.tichuguru.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet

class SegmentedControlButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : androidx.appcompat.widget.AppCompatRadioButton(context, attrs, defStyle) {

    private var mX = 0f

    override fun onDraw(canvas: Canvas) {
        val text = getText().toString()
        val textPaint = Paint()
        textPaint.isAntiAlias = true
        val currentHeight = textPaint.measureText("x")
        textPaint.textSize = getTextSize()
        textPaint.textAlign = Paint.Align.CENTER
        if (isChecked) {
            GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(-2302756, -15658735))
                .apply { setBounds(0, 0, width, height) }
                .draw(canvas)
            textPaint.color = -1
        } else {
            GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(-5921371, -16777216))
                .apply { setBounds(0, 0, width, height) }
                .draw(canvas)
            textPaint.color = -3355444
        }
        canvas.drawText(text, mX, (height / 2) + currentHeight, textPaint)
        Paint().apply {
            color = -16777216
            style = Paint.Style.STROKE
        }.also { canvas.drawRect(Rect(0, 0, width, height), it) }
    }

    override fun onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) {
        super.onSizeChanged(w, h, ow, oh)
        mX = w * 0.5f
    }
}
