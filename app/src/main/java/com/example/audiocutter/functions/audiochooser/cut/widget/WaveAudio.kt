package com.example.audiocutter.functions.audiochooser.cut.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.example.audiocutter.util.Utils
import kotlin.random.Random

class WaveAudio @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var rowPain: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private lateinit var rowRect1: RectF

    private lateinit var rowRect2: RectF
    private lateinit var rowRect3: RectF
    private var mWithView = 0
    private var mHeightView = 0
    private var spaceDraw = 0
    private var animation1 = ValueAnimator()
    private var random = Random
    private var animation2 = ValueAnimator()
    private var animation3 = ValueAnimator()
    private var mCurrentPlayTime1 = 0L
    private var mCurrentPlayTime2 = 0L
    private var mCurrentPlayTime3 = 0L
    private var valueRow1 = 0f
    private var valueRow2 = 0f
    private var valueRow3 = 0f


    companion object {
        private const val BORDER_ROW = 1
        private const val MID_POINT = 2
        private val START_COLOR = Color.parseColor("#FCB674")
        private val END_COLOR = Color.parseColor("#FF9292")
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        initRectRow()

        canvas.drawTopRoundRect(
            rowRect1,
            Utils.convertDp2Px(BORDER_ROW, context), rowPain

        )
        canvas.drawTopRoundRect(
            rowRect2,
            Utils.convertDp2Px(BORDER_ROW, context), rowPain
        )
        canvas.drawTopRoundRect(
            rowRect3,
            Utils.convertDp2Px(BORDER_ROW, context), rowPain
        )
    }

    private fun Canvas.drawTopRoundRect(rect: RectF, radius: Float, paint: Paint) {
        drawRoundRect(rect, radius, radius, paint)
        drawRect(
            rect.left,
            rect.top + radius,
            rect.right,
            rect.bottom,
            paint
        )
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rowPain.shader = LinearGradient(
            0f,
            0f,
            0f,
            h.toFloat(),
            START_COLOR,
            END_COLOR,
            Shader.TileMode.REPEAT
        )

        mWithView = w
        mHeightView = h
        spaceDraw = (w / 3) / 2

        createAnim1(0f, mHeightView.toFloat())
        handler.postDelayed({
            createAnim2(0f, mHeightView.toFloat())
            postDelayed({ createAnim3(0f, mHeightView.toFloat()) }, 100)
        }, 100)

    }

    private fun initRectRow() {
        rowRect1 = RectF(
            spaceDraw - convertDp2Px(MID_POINT, context),
            valueRow1,
            spaceDraw + convertDp2Px(MID_POINT, context), mHeightView.toFloat()
        )
        rowRect2 = RectF(
            3 * spaceDraw - convertDp2Px(MID_POINT, context),
            valueRow2,
            3 * spaceDraw + convertDp2Px(MID_POINT, context), mHeightView.toFloat()
        )
        rowRect3 = RectF(
            5 * spaceDraw - convertDp2Px(MID_POINT, context),
            valueRow3,
            5 * spaceDraw + convertDp2Px(MID_POINT, context), mHeightView.toFloat()
        )
    }


    private fun convertDp2Px(dip: Int, context: Context): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dip.toFloat(),
            context.resources.displayMetrics
        )
    }


    private fun createAnim1(startValue: Float, endValue: Float) {
        animation1.cancel()
        animation1 = ValueAnimator.ofFloat(startValue, endValue)
        animation1.duration = 400
        animation1.repeatCount = ValueAnimator.INFINITE
        animation1.repeatMode = ValueAnimator.REVERSE
        animation1.addUpdateListener {
            valueRow1 = (it.animatedValue) as Float
            invalidate()
        }
        animation1.start()
    }

    private fun createAnim2(startValue: Float, endValue: Float) {
        animation2.cancel()
        animation2 = ValueAnimator.ofFloat(startValue, endValue)
        animation2.duration = 400
        animation2.repeatCount = ValueAnimator.INFINITE
        animation2.repeatMode = ValueAnimator.REVERSE
        animation2.addUpdateListener {
            valueRow2 = it.animatedValue as Float
            invalidate()
        }
        animation2.start()
    }

    private fun createAnim3(startValue: Float, endValue: Float) {
        animation3.cancel()
        animation3 = ValueAnimator.ofFloat(startValue, endValue)
        animation3.duration = 400
        animation3.repeatCount = ValueAnimator.INFINITE
        animation3.repeatMode = ValueAnimator.REVERSE
        animation3.addUpdateListener {
            valueRow3 = it.animatedValue as Float
            invalidate()
        }
        animation3.start()
    }

    fun pauseAnimation() {
        mCurrentPlayTime1 = animation1.currentPlayTime
        animation1.cancel()
        mCurrentPlayTime2 = animation2.currentPlayTime
        animation2.cancel()
        mCurrentPlayTime3 = animation3.currentPlayTime
        animation3.cancel()
    }

    fun resumeAnimation() {
        animation1.start()
        animation1.currentPlayTime = mCurrentPlayTime1
        animation2.start()
        animation2.currentPlayTime = mCurrentPlayTime2
        animation3.start()
        animation3.currentPlayTime = mCurrentPlayTime3
    }
}