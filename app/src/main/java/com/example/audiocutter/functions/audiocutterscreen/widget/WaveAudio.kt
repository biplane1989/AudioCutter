package com.example.audiocutter.functions.audiocutterscreen.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class WaveAudio : View {
    private var mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private lateinit var animator: ValueAnimator

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        mPaint.color = Color.RED
        mPaint.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawRect(canvas)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        animator = ValueAnimator.ofFloat(0f, w.toFloat())
    }

    private fun drawRect(canvas: Canvas?) {
        canvas?.let {
            canvas.drawRect(width / 2 * 1f, height / 2 * 1f, 100f, 200f, mPaint)
            canvas.drawRect(120f, 0f, 100f, 200f, mPaint)
            canvas.drawRect(300f, 20f, 100f, 200f, mPaint)
        }
    }

    fun runningWave(rs: Boolean) {
        if (rs) {

        }
    }

}