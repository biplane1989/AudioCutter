package com.example.audiocutter.ui.common

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.res.ResourcesCompat
import com.example.audiocutter.R
import com.example.audiocutter.util.Utils
import kotlinx.coroutines.*
import kotlin.math.min

class RotationBorderAnimation(val adView: AdView) {
    private var startDegree: Float = -90f
    private var endDegree: Float = -90f

    private var valueAnimator: ValueAnimator? = null
    fun reset() {
        startDegree = -90f
        endDegree = -90f
    }

    fun start(animationDuration: Long = 2000) {
        reset()
        val v0 = 360f / animationDuration
        val v11 = v0
        val v12 = v0
        val v21 = v0 / 3
        val v22 = 2 * v0 - v21
        valueAnimator?.cancel()
        valueAnimator = ValueAnimator.ofFloat(0f, animationDuration.toFloat())
        valueAnimator?.interpolator = LinearInterpolator()
        valueAnimator?.addUpdateListener {
            val halfDuration = animationDuration / 2f
            val time = (it.animatedValue as Float)
            if (time > halfDuration) {
                startDegree =
                    (v11 * halfDuration) + (v12 * (time - halfDuration))
                endDegree = (v21 * halfDuration) + (v22 * (time - halfDuration))
            } else {
                startDegree = (v11 * time)
                endDegree = (v21 * time)
            }
            startDegree -= 90f
            endDegree -= 90f
            if (startDegree < endDegree) {
                startDegree = endDegree
            }
            adView.invalidate()

        }
        valueAnimator?.duration = animationDuration
        valueAnimator?.start()
    }

    fun isRunning(): Boolean {
        return valueAnimator?.isRunning ?: false
    }

    fun getStartDegree(): Float {
        return startDegree
    }

    fun getEndDegree(): Float {
        return endDegree
    }
}

class AdView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val drawingRect = Rect(0, 0, 0, 0)

    private var diamondPadding = Utils.dpToPx(context, 8f)
    private val clipPath = Path()
    private val diamondPath = Path()
    private val diamondDrawingRectF = RectF(0f, 0f, 0f, 0f)
    private val diamondBorderDrawingRectF = RectF(0f, 0f, 0f, 0f)
    private val diamondBorderPaint = Paint()
    private val diamondPaint = Paint()
    private val diamondBorderSize = 10f
    private val diamondAspect = 1f
    private var borderAnimationJob: Job? = null
    private val rotationBorderAnimation1 = RotationBorderAnimation(this)
    private val rotationBorderAnimation2 = RotationBorderAnimation(this)
    private val mAdTextPaint = Paint()
    private val mBorderCirclePaint = Paint()
    private val mAdText = resources.getString(R.string.home_ad)
    private val textBounds = Rect()

    init {
        diamondBorderPaint.color = Color.parseColor("#FFEA41")
        diamondBorderPaint.isAntiAlias = true
        diamondBorderPaint.style = Paint.Style.FILL

        diamondPaint.color = Color.parseColor("#FDAA74")
        diamondPaint.isAntiAlias = true
        diamondPaint.style = Paint.Style.FILL

        mAdTextPaint.isAntiAlias = true
        mAdTextPaint.setTypeface(ResourcesCompat.getFont(context, R.font.opensans_bold))
        mAdTextPaint.textSize = Utils.dpToPx(context, 10f).toFloat()
        mAdTextPaint.color = Color.WHITE

        mBorderCirclePaint.isAntiAlias = true
        mBorderCirclePaint.color = Color.parseColor("#F6F6F6")
        mBorderCirclePaint.style = Paint.Style.STROKE
        mBorderCirclePaint.strokeWidth = Utils.dpToPx(context, 2f).toFloat()


    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        configureBounds()
    }

    override fun onDraw(canvas: Canvas) {
        if (drawingRect.width() == 0 || drawingRect.height() == 0) {
            return
        }
        super.onDraw(canvas)
        drawAdvertisementBorderIcon(canvas)
        drawAdvertisementIcon(canvas)

        drawCircleBorder(canvas)
        drawAdText(canvas)
    }

    private fun drawAdText(canvas: Canvas) {
        canvas.save()
        canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())
        mAdTextPaint.getTextBounds(mAdText, 0, mAdText.length, textBounds)
        val x = diamondDrawingRectF.centerX() - textBounds.width() / 2f
        val height = diamondDrawingRectF.height() - diamondDrawingRectF.height() * 0.2f

        val y = diamondDrawingRectF.top + diamondDrawingRectF.height() * 0.2f + (height - textBounds.height()) / 2f + textBounds.height()/2f
        canvas.drawText(mAdText, x, y, mAdTextPaint)
        canvas.restore()
    }

    private fun drawCircleBorder(canvas: Canvas) {
        canvas.save()
        canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())
        canvas.drawCircle(
            drawingRect.centerX().toFloat(),
            drawingRect.centerY().toFloat(),
            min(drawingRect.width(), drawingRect.height()) / 2f - mBorderCirclePaint.strokeWidth,
            mBorderCirclePaint
        )
        canvas.restore()
    }

    private fun drawAdvertisementIcon(canvas: Canvas) {
        canvas.save()
        canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())
        drawDiamond(canvas, diamondDrawingRectF, diamondPaint)
        canvas.restore()
    }

    private fun convertAngleToDegree(angle: Float): Float {
        return (angle * 180f / Math.PI).toFloat()
    }

    private fun convertDegreeToAngle(degree: Float): Float {
        return (degree * Math.PI / 180f).toFloat()
    }

    private fun computeClipPath(startDegree: Float, endDegree: Float) {
        val startAngle = convertDegreeToAngle(startDegree)
        val endAngle = convertDegreeToAngle(endDegree)
        clipPath.reset()
        val radius = diamondBorderDrawingRectF.width() / 2f
        clipPath.moveTo(diamondBorderDrawingRectF.centerX(), diamondBorderDrawingRectF.centerY())
        val x1 =
            (diamondBorderDrawingRectF.centerX() + radius * Math.cos(startAngle.toDouble())).toFloat()
        val y1 =
            (diamondBorderDrawingRectF.centerX() + radius * Math.sin(startAngle.toDouble())).toFloat()

        val x2 =
            (diamondBorderDrawingRectF.centerX() + radius * Math.cos(endAngle.toDouble())).toFloat()
        val y2 =
            (diamondBorderDrawingRectF.centerX() + radius * Math.sin(endAngle.toDouble())).toFloat()

        clipPath.lineTo(x1, y1)
        clipPath.lineTo(x2, y2)
        clipPath.close()
        clipPath.addArc(
            diamondBorderDrawingRectF,
            startDegree,
            endDegree - startDegree
        )
    }

    private fun drawDiamond(canvas: Canvas, drawingRegion: RectF, paint: Paint) {
        canvas.save()
        diamondPath.reset()
        diamondPath.moveTo(drawingRegion.left, drawingRegion.top + drawingRegion.height() * 0.2f)
        diamondPath.lineTo(drawingRegion.right, drawingRegion.top + drawingRegion.height() * 0.2f)
        diamondPath.lineTo(drawingRegion.right, drawingRegion.bottom)
        diamondPath.lineTo(drawingRegion.left, drawingRegion.bottom)
        diamondPath.close()
        canvas.clipPath(diamondPath)

        diamondPath.reset()
        diamondPath.moveTo(drawingRegion.centerX(), drawingRegion.top)
        diamondPath.lineTo(drawingRegion.right, drawingRegion.centerY())
        diamondPath.lineTo(drawingRegion.centerX(), drawingRegion.bottom)
        diamondPath.lineTo(drawingRegion.left, drawingRegion.centerY())
        diamondPath.close()
        canvas.drawPath(diamondPath, paint)
        canvas.restore()
    }

    private fun drawAdvertisementBorderIcon(canvas: Canvas) {

        canvas.save()
        computeClipPath(
            rotationBorderAnimation1.getStartDegree(),
            rotationBorderAnimation1.getEndDegree()
        )
        canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())
        canvas.clipPath(clipPath)
        drawDiamond(canvas, diamondBorderDrawingRectF, diamondBorderPaint)
        canvas.restore()

        canvas.save()
        computeClipPath(
            rotationBorderAnimation2.getStartDegree(),
            rotationBorderAnimation2.getEndDegree()
        )
        canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())
        canvas.clipPath(clipPath)
        drawDiamond(canvas, diamondBorderDrawingRectF, diamondBorderPaint)
        canvas.restore()
    }

    private fun configureBounds() {
        drawingRect.left = paddingLeft
        drawingRect.top = paddingTop
        drawingRect.right = width - paddingRight
        drawingRect.bottom = height - paddingBottom
        diamondPadding = drawingRect.width() / 4


        computeAdvertisementDrawableMatrix()
    }

    private fun computeAdvertisementDrawableMatrix() {

        val viewAspect = drawingRect.width() / drawingRect.height()
        var diamondWidth = drawingRect.width().toFloat()
        var diamondHeight = diamondWidth / diamondAspect

        if (viewAspect > diamondAspect) {
            // scale by height
            diamondHeight = drawingRect.height().toFloat()
            diamondWidth = diamondAspect * diamondHeight

        }

        diamondBorderDrawingRectF.left = drawingRect.centerX() - diamondWidth / 2f
        diamondBorderDrawingRectF.right = drawingRect.centerX() + diamondWidth / 2f
        diamondBorderDrawingRectF.top = drawingRect.centerY() - diamondHeight / 2f
        diamondBorderDrawingRectF.bottom = drawingRect.centerY() + diamondHeight / 2f

        val newHeight = diamondBorderDrawingRectF.height() - diamondBorderSize * 2
        val newWidth =
            diamondBorderDrawingRectF.width() / diamondBorderDrawingRectF.height() * newHeight

        diamondDrawingRectF.left = diamondBorderDrawingRectF.centerX() - newWidth / 2f
        diamondDrawingRectF.right = diamondBorderDrawingRectF.centerX() + newWidth / 2f
        diamondDrawingRectF.top = diamondBorderDrawingRectF.centerY() - newHeight / 2f
        diamondDrawingRectF.bottom = diamondBorderDrawingRectF.centerY() + newHeight / 2f
    }

    suspend fun startAnim() = coroutineScope {
        borderAnimationJob?.cancelAndJoin()
        borderAnimationJob = launch {

            while (isActive) {
                delay(1000)
                if (!rotationBorderAnimation1.isRunning() && !rotationBorderAnimation2.isRunning()) {
                    rotationBorderAnimation1.reset()
                    rotationBorderAnimation2.reset()

                    rotationBorderAnimation1.start(2000)
                    delay(1500)
                    rotationBorderAnimation2.start(500)
                }

            }
        }
    }
}