package com.example.audiocutter.functions.audiocutterscreen.widget

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import com.example.audiocutter.R


class ProgressCustomView :
    View {

    private var mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mPaint2: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var animator: ValueAnimator = ValueAnimator.ofFloat(0f, width.toFloat())
    var currStartLineX: Float = 0f

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        mPaint.color = resources.getColor(R.color.colorWhite)
        mPaint.style = Paint.Style.FILL
        mPaint2.color = resources.getColor(R.color.colorYelowAlpha)
        mPaint2.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBG(canvas)
    }

    private fun drawBG(canvas: Canvas) {

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), mPaint)
        canvas.drawRect(0f, 0f, currStartLineX, width / 2f, mPaint2)

    }


    fun startAnimator(duration: Long) {
        animator.duration = duration
        animator.addUpdateListener {
            val value = it.animatedValue as Float
            currStartLineX = value
            Log.d("TAG", "startAnimator: $value")
            invalidate()
        }
        animator.start()
    }

    fun stopAnimator() {
        animator.cancel()
    }

    @SuppressLint("NewApi")
    fun pauseAnimator() {
        animator.pause()
    }

    @SuppressLint("NewApi")
    fun resumeAnimator() {
        animator.resume()
    }


    fun convertDP(num: Int): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            num.toFloat(),
            resources.displayMetrics
        )
    }


}