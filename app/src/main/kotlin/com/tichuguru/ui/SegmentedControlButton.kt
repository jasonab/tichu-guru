package com.tichuguru.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet

class SegmentedControlButton
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
    ) : androidx.appcompat.widget.AppCompatRadioButton(context, attrs, defStyle) {
        private var mX = 0f

        init {
            isClickable = true
            isFocusable = true
        }

        override fun setChecked(checked: Boolean) {
            super.setChecked(checked)
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            val text = getText().toString()
            val textPaint =
                Paint().apply {
                    isAntiAlias = true
                    textSize = height * 0.45f
                    textAlign = Paint.Align.CENTER
                    isFakeBoldText = true
                }
            if (isChecked) {
                GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(CHECKED_TOP, CHECKED_BOTTOM))
                    .apply { setBounds(0, 0, width, height) }
                    .draw(canvas)
                textPaint.color = Color.WHITE
            } else {
                GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(UNCHECKED_TOP, UNCHECKED_BOTTOM))
                    .apply { setBounds(0, 0, width, height) }
                    .draw(canvas)
                textPaint.color = UNCHECKED_TEXT
            }
            val fm = textPaint.fontMetrics
            val textY = (height - fm.descent - fm.ascent) / 2f
            canvas.drawText(text, mX, textY, textPaint)
            Paint()
                .apply {
                    color = Color.rgb(110, 110, 110)
                    style = Paint.Style.STROKE
                }.also { canvas.drawRect(Rect(0, 0, width, height), it) }
        }

        override fun onSizeChanged(
            w: Int,
            h: Int,
            ow: Int,
            oh: Int,
        ) {
            super.onSizeChanged(w, h, ow, oh)
            mX = w * 0.5f
        }

        companion object {
            private val CHECKED_TOP = Color.rgb(0, 150, 136) // Teal 500
            private val CHECKED_BOTTOM = Color.rgb(0, 77, 64) // Teal 800
            private val UNCHECKED_TOP = Color.rgb(90, 90, 90)
            private val UNCHECKED_BOTTOM = Color.rgb(55, 55, 55)
            private val UNCHECKED_TEXT = Color.rgb(170, 170, 170)
        }
    }
