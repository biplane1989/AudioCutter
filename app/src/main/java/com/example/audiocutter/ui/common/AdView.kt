package com.example.audiocutter.ui.common

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.example.audiocutter.R

class AdView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var mAdvertisementDrawable =
        ResourcesCompat.getDrawable(context.resources, R.drawable.home_ic_advertisement, null)
    private var mAdvertisementBorderDrawable =
        ResourcesCompat.getDrawable(context.resources, R.drawable.home_ic_advertisement_border, null)
    private val drawingRect = Rect(0, 0, 0, 0)
    private val advertisementDrawableMatrix = Matrix()
    private val advertisementBorderDrawableMatrix = Matrix()
    private var diamondPadding = 0

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        configureBounds()
    }
    val paint = Paint()
    init {
        paint.isAntiAlias = true
        paint.color = Color.RED
        paint.style = Paint.Style.FILL
    }
    override fun onDraw(canvas: Canvas) {
        if (width == 0 || height == 0) {
            return
        }
        super.onDraw(canvas)
        canvas.drawRect(drawingRect, paint)
        drawAdvertisementIcon(canvas)

    }

    private fun drawAdvertisementIcon(canvas: Canvas) {
        canvas.save()
        canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())
        canvas.concat(advertisementDrawableMatrix)
        mAdvertisementDrawable?.draw(canvas)
        canvas.restore()
    }

    private fun configureBounds() {
        drawingRect.left = paddingLeft
        drawingRect.top = paddingTop
        drawingRect.right = width - paddingRight
        drawingRect.bottom = height - paddingBottom
        diamondPadding = drawingRect.width() / 4

        computeAdvertisementDrawableMatrix()
        computeAdvertisementBorderDrawableMatrix()
    }
    private fun computeAdvertisementBorderDrawableMatrix(){
        val advertisementBorderIcon = mAdvertisementBorderDrawable
        if (advertisementBorderIcon == null) {
            return
        }

        val dwidth: Int = advertisementBorderIcon.intrinsicWidth
        val dheight: Int = advertisementBorderIcon.intrinsicHeight

        val vwidth = drawingRect.width() - diamondPadding * 2
        val vheight = drawingRect.height() - diamondPadding * 2

        // We need to do the scaling ourself, so have the drawable
        // use its native size.
        advertisementBorderIcon.setBounds(0, 0, dwidth, dheight)
        advertisementDrawableMatrix.reset()
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

        dx = Math.round((vwidth - dwidth * scale) * 0.5f).toFloat() + diamondPadding
        dy = Math.round((vheight - dheight * scale) * 0.5f).toFloat() + diamondPadding

        advertisementDrawableMatrix.setScale(scale, scale)
        advertisementDrawableMatrix.postTranslate(dx, dy)
    }
    private fun computeAdvertisementDrawableMatrix() {
        val advertisementIcon = mAdvertisementDrawable
        if (advertisementIcon == null) {
            return
        }

        val dwidth: Int = advertisementIcon.intrinsicWidth
        val dheight: Int = advertisementIcon.intrinsicHeight

        val vwidth = drawingRect.width() - diamondPadding * 2
        val vheight = drawingRect.height() - diamondPadding * 2

        // We need to do the scaling ourself, so have the drawable
        // use its native size.
        advertisementIcon.setBounds(0, 0, dwidth, dheight)
        advertisementDrawableMatrix.reset()
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

        dx = Math.round((vwidth - dwidth * scale) * 0.5f).toFloat() + diamondPadding
        dy = Math.round((vheight - dheight * scale) * 0.5f).toFloat() + diamondPadding

        advertisementDrawableMatrix.setScale(scale, scale)
        advertisementDrawableMatrix.postTranslate(dx, dy)

    }
}