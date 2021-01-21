package com.example.audiocutter.ui.common

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.example.audiocutter.R
import kotlinx.coroutines.*
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class BlinkValueAnimation(val vipView: VipView, val pivotX: Int, val pivotY: Int) {
    private var blinkAlphaValue = 0
    private var blinkScaleValue = 0.1f
    private var valueAnimator: ValueAnimator? = null

    fun start() {
        valueAnimator?.cancel()

        valueAnimator = ValueAnimator.ofFloat(0f, 1.5f)
        valueAnimator?.duration = Random.nextLong(1000, 1500)
        valueAnimator?.addUpdateListener {
            val animValue = it.animatedValue as Float
            blinkScaleValue = max(0.1f, animValue)
            blinkAlphaValue = min(255, (animValue * 255).toInt())
            vipView.invalidate()
        }
        valueAnimator?.start()
    }

    fun isRunning(): Boolean {
        return valueAnimator?.isRunning ?: false
    }

    fun getBlinkAlphaValue(): Int {
        return blinkAlphaValue
    }

    fun getBlinkScaleValue(): Float {
        return blinkScaleValue
    }
}

class VipView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var mVipDrawable =
        ResourcesCompat.getDrawable(context.resources, R.drawable.ic_home_vip, null)
    private var mVipTwinkleDrawable =
        ResourcesCompat.getDrawable(context.resources, R.drawable.ic_vip_twinkle, null)

    private val vipDrawableMatrix = Matrix()
    private val vipTwinkleDrawableMatrix = Matrix()
    private val drawingRect = Rect(0, 0, 0, 0)
    private var vipTwinkleWidth = 0
    private var vipTwinkleHeight = 0
    private var blinkAnimationJob: Job? = null
    private var listValueAnimators = ArrayList<BlinkValueAnimation>()
    private val mNumBlinkValueAnimation = 3

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        configureBounds()
    }

    private fun configureBounds() {
        drawingRect.left = paddingLeft
        drawingRect.top = paddingTop
        drawingRect.right = width - paddingRight
        drawingRect.bottom = height - paddingBottom

        computeVipDrawableMatrix()
        computeVipTwinkDrawableMatrix()
    }

    private fun computeVipTwinkDrawableMatrix() {
        val vipTwinkleIcon = mVipTwinkleDrawable
        if (vipTwinkleIcon == null) {
            return
        }

        val dwidth: Int = vipTwinkleIcon.intrinsicWidth
        val dheight: Int = vipTwinkleIcon.intrinsicHeight

        val aspect = dwidth.toFloat() / dheight

        vipTwinkleWidth = (drawingRect.width() / 1.5f).toInt()
        vipTwinkleHeight = (vipTwinkleWidth / aspect).toInt()

        // We need to do the scaling ourself, so have the drawable
        // use its native size.
        vipTwinkleIcon.setBounds(0, 0, dwidth, dheight)
        vipTwinkleDrawableMatrix.reset()
        val scale: Float
        val dx: Float
        val dy: Float

        scale = if (dwidth <= vipTwinkleWidth && dheight <= vipTwinkleHeight) {
            1.0f
        } else {
            Math.min(
                vipTwinkleWidth.toFloat() / dwidth.toFloat(),
                vipTwinkleHeight.toFloat() / dheight.toFloat()
            )
        }

        dx = Math.round((vipTwinkleWidth - dwidth * scale) * 0.5f).toFloat()
        dy = Math.round((vipTwinkleHeight - dheight * scale) * 0.5f).toFloat()

        vipTwinkleDrawableMatrix.setScale(scale, scale)
        vipTwinkleDrawableMatrix.postTranslate(dx, dy)
    }

    private fun computeVipDrawableMatrix() {
        val vipIcon = mVipDrawable
        if (vipIcon == null) {
            return
        }

        val dwidth: Int = vipIcon.intrinsicWidth
        val dheight: Int = vipIcon.intrinsicHeight

        val vwidth = drawingRect.width()
        val vheight = drawingRect.height()

        // We need to do the scaling ourself, so have the drawable
        // use its native size.
        vipIcon.setBounds(0, 0, dwidth, dheight)
        vipDrawableMatrix.reset()
        val scale: Float
        val dx: Float
        val dy: Float

        scale = if (dwidth <= vwidth && dheight <= vheight) {
            1.0f
        } else {
            Math.min(
                vwidth.toFloat() / dwidth.toFloat(),
                vheight.toFloat() / dheight.toFloat()
            )
        }

        dx = Math.round((vwidth - dwidth * scale) * 0.5f).toFloat()
        dy = Math.round((vheight - dheight * scale) * 0.5f).toFloat()

        vipDrawableMatrix.setScale(scale, scale)
        vipDrawableMatrix.postTranslate(dx, dy)

    }

    override fun onDraw(canvas: Canvas) {
        if (width == 0 || height == 0) {
            return
        }
        super.onDraw(canvas)
        drawVipIcon(canvas)
        drawVipTwinkleIcon(canvas)
    }

    private fun drawVipIcon(canvas: Canvas) {
        canvas.save()
        canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())
        canvas.concat(vipDrawableMatrix)
        mVipDrawable?.draw(canvas)
        canvas.restore()
    }

    private fun drawVipTwinkleIcon(canvas: Canvas) {
        listValueAnimators.forEach {
            if (it.isRunning()) {
                canvas.save()
                canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())
                canvas.translate(
                    it.pivotX - vipTwinkleWidth / 2f,
                    it.pivotY - vipTwinkleHeight / 2f
                )
                canvas.scale(
                    it.getBlinkScaleValue(),
                    it.getBlinkScaleValue(),
                    vipTwinkleWidth / 2f,
                    vipTwinkleHeight / 2f
                )
                canvas.concat(vipTwinkleDrawableMatrix)
                mVipTwinkleDrawable?.alpha = it.getBlinkAlphaValue()
                mVipTwinkleDrawable?.draw(canvas)
                canvas.restore()
            }
        }

    }

    private fun genBlink() {
        if (drawingRect.width() > 0 && drawingRect.height() > 0) {
            val listCopy = listValueAnimators.filter { it.isRunning() }
            listValueAnimators.clear()
            listValueAnimators.addAll(listCopy)
            if (listValueAnimators.size < mNumBlinkValueAnimation) {
                val pivotX = Random.nextInt(
                    drawingRect.left + vipTwinkleWidth / 2,
                    drawingRect.right - vipTwinkleWidth / 2
                )
                val pivotY = Random.nextInt(
                    drawingRect.top + vipTwinkleHeight / 2,
                    drawingRect.bottom - vipTwinkleHeight / 2
                )
                val anim = BlinkValueAnimation(this, pivotX, pivotY)
                listValueAnimators.add(anim)
                anim.start()
            }
        }
    }


    suspend fun startBlink() = coroutineScope {
        blinkAnimationJob?.cancelAndJoin()
        blinkAnimationJob = launch {
            while (isActive) {
                genBlink()
                delay(800)
            }
        }
    }
}