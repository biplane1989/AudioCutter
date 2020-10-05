package com.example.audiocutter.functions.audiocutterscreen.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import java.util.*


class WaveAudio : View {
    private var mPaint1: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mPaint2: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mPaint3: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private lateinit var animator1: ValueAnimator
    private var startLineY1 = 0f
    private lateinit var random: Random
    private var currentLineY1 = 0f
    private var startLineY2 = 0f
    private var currentLineY2 = 0f
    private var startLineY3 = 0f
    private var currentLineY3 = 0f
    private var rectRow1 = RectF()
    private var rectRow2 = RectF()
    private var rectRow3 = RectF()
    private var spaceRow = 0
    private var midPoint = 18f //dp

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        random = Random()
        mPaint1.color = Color.RED
        mPaint2.color = Color.BLUE
        mPaint3.color = Color.GREEN
        mPaint1.style = Paint.Style.FILL
        mPaint2.style = Paint.Style.FILL
        mPaint3.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawRect(canvas)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
//        animator1 = ValueAnimator.ofFloat(0f, h * 1f)
        spaceRow = w / 3
        rectRow1 = RectF(
            dp2px((spaceRow / 2f) - dp2px(midPoint, context), context),
            0f,
            spaceRow / 2f + dp2px(midPoint, context),
            h.toFloat()
        )
        rectRow2 = RectF(
            dp2px(((w - spaceRow) - (w - spaceRow * 2)) / 2f - dp2px(midPoint, context), context),
            0f,
            ((w - spaceRow) - (w - spaceRow * 2)) / 2f + dp2px(midPoint, context),
            h.toFloat()
        )
//        rectRow3 = RectF(
//            dp2px((spaceRow / 2f) - dp2px(midPoint, context), context),
//            0f,
//            spaceRow / 2f + dp2px(midPoint, context),
//            h.toFloat()
//        )

    }

    private fun drawRect(canvas: Canvas) {
//        canvas?.let {
//            canvas.drawRect(width / 4f, width * 2f, width / 4f - 100f, currentLineY1, mPaint1)
//            canvas.drawRect(width / 3f, width * 2f, width / 3f - 100f, currentLineY2, mPaint2)
//            canvas.drawRect(width / 2f, width * 2f, width / 2f - 100f, currentLineY3, mPaint3)
//        }
        canvas.drawRect(rectRow1, Paint())
        canvas.drawRect(rectRow2, Paint())
    }

    fun startAnimator() {
        setAnimator(animator1)

        animator1.addUpdateListener {
            val value = it.animatedValue as Float
            currentLineY1 = startLineY1 + value + random.nextInt(100)
            currentLineY2 = startLineY1 + value
            currentLineY3 = startLineY1 + value


            invalidate()
        }



        animator1.addListener(listener)

        startAnim()
    }

    private fun startAnim() {
        animator1.start()
    }

    private fun setAnimator(animator: ValueAnimator) {
        animator.duration = 400
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.REVERSE
    }

    private val listener = object : AnimatorListenerAdapter() {
        override fun onAnimationRepeat(animation: Animator?) {
            super.onAnimationRepeat(animation)
            currentLineY1 = startLineY1
            invalidate()
        }
    }

    @SuppressLint("NewApi")
    fun pauseAnimator() {
        animator1.pause()

    }

    @SuppressLint("NewApi")
    fun resumeAnimator() {
        animator1.resume()

    }

    fun dp2px(dip: Float, context: Context): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dip,
            context.resources.displayMetrics
        )
    }

}