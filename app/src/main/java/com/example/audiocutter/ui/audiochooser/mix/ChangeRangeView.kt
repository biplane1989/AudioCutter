package com.example.audiocutter.ui.audiochooser.mix

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.example.audiocutter.R

class ChangeRangeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mHeight = 0
    private var mWidth = 0
    private var startCurrentX = 0
    private var endCurrentX = 0

    init {
        mPaint.color = context.resources.getColor(R.color.colorYelowDark)
        mPaint.style = Paint.Style.FILL
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mHeight = h
        mWidth = w
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        initLine(canvas)
    }


    private fun initLine(canvas: Canvas?) {
        canvas?.let {
            canvas.drawLine(
                startCurrentX.toFloat(),
                0f,
                endCurrentX.toFloat(),
                mHeight * 1f,
                mPaint
            )
            canvas.drawCircle(startCurrentX.toFloat(), (mHeight * 1f) - 20f, 20f, mPaint)
        }
    }


    fun drawLineTouch(startX: Float, endX: Float) {
        try {
            Log.d("TAG", "darwLineTouch: $startX   $endX")
            endCurrentX = endX.toInt()
            startCurrentX = startX.toInt()
            invalidate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}