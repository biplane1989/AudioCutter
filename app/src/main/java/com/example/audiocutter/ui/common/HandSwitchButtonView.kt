package com.example.audiocutter.ui.common

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import com.example.audiocutter.R


class HandSwitchButtonView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    companion object {
        private val BACKGROUND_COLOR = Color.parseColor("#589FFF")
        private val BACKGROUND_INACTIVE_COLOR = Color.parseColor("#B8B8B8")

    }

    private var progressPercent = 0f
    private val circleRect = RectF()
    private val roundRect = RectF()
    private val progressRect = RectF()
    private val drawRect = RectF()
    private val handRect = RectF()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bmHand: Bitmap
    private var valueAnimator = ValueAnimator.ofFloat(0f, 1f)
    private val modeSrcIn = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)

    init {
        paint.setColor(BACKGROUND_COLOR)
        bmHand = BitmapFactory.decodeResource(context!!.resources, R.drawable.switch_button_ic_hand)
        valueAnimator.duration = 1500
        valueAnimator.addUpdateListener {
            progressPercent = it.animatedValue as Float
            updateHandAndCircleRect()
            invalidate()
        }
        valueAnimator.repeatCount = Animation.INFINITE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

         paint.setColor(BACKGROUND_INACTIVE_COLOR)
         canvas.drawRoundRect(
             roundRect,
             roundRect.height() / 2f,
             roundRect.height() / 2f,
             paint
         )
         drawProcess(canvas)
         paint.setColor(BACKGROUND_COLOR)
         canvas.drawCircle(
             circleRect.centerX(),
             circleRect.centerY(),
             circleRect.width() / 2f,
             paint
         )
         canvas.drawBitmap(bmHand, null, handRect, null)

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        initRect()
    }

    private fun initRect() {
        drawRect.left = paddingLeft * 1f
        drawRect.top = paddingTop * 1f
        drawRect.right = width - paddingRight * 1f
        drawRect.bottom = height - paddingBottom * 1f
        val radius = drawRect.height() / 3f
        circleRect.left = drawRect.left
        circleRect.right = drawRect.left + 2 * radius
        circleRect.top = drawRect.top
        circleRect.bottom = circleRect.top + 2 * radius

        val roundRectHeight = drawRect.height() / 2f
        roundRect.left = drawRect.left
        roundRect.top = drawRect.left + (circleRect.height() - roundRectHeight) / 2f
        roundRect.right = drawRect.right
        roundRect.bottom = roundRect.top + roundRectHeight

        handRect.top = circleRect.centerY()
        handRect.left = circleRect.left
        handRect.bottom = handRect.top + circleRect.height()
        handRect.right = handRect.left + circleRect.height()
        valueAnimator.start()
    }

    private fun drawProcess(canvas: Canvas) {
        progressRect.left = roundRect.left + roundRect.width() * progressPercent
        progressRect.right = roundRect.right
        progressRect.top = roundRect.top
        progressRect.bottom = roundRect.bottom
        paint.setXfermode(null)
        var sc = -1
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            sc =
                canvas.saveLayer(
                    roundRect.left,
                    roundRect.top,
                    roundRect.right,
                    roundRect.bottom,
                    null
                )
        } else {
            sc = canvas.saveLayer(
                roundRect.left,
                roundRect.top,
                roundRect.right,
                roundRect.bottom,
                null,
                Canvas.ALL_SAVE_FLAG
            )
        }
        paint.setColor(BACKGROUND_COLOR)
        paint.alpha = 200

        canvas.drawRoundRect(
            roundRect,
            roundRect.height() / 2f,
            roundRect.height() / 2f,
            paint
        )

        paint.setXfermode(modeSrcIn)
        paint.setColor(BACKGROUND_COLOR)
        canvas.drawRect(progressRect, paint)

        paint.setXfermode(null)
        canvas.restoreToCount(sc)
    }

    private fun updateHandAndCircleRect() {
        val minX = drawRect.left
        val maxX = drawRect.right - circleRect.width()
        val distance = maxX - minX
        val transX = progressPercent * distance
        val circleWidth = circleRect.width()
        circleRect.left = drawRect.left
        circleRect.right = drawRect.left + circleWidth
        circleRect.offset(transX, 0f)

        handRect.left = drawRect.left
        handRect.right = drawRect.left + circleWidth
        handRect.offset(transX, 0f)
    }

}