package com.example.audiocutter.functions.audiocutterscreen.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.example.audiocutter.R


class SeekBarCustom :
    View {
    private var mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mPaint2: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    var currStartLineX: Float = 0f


    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        mPaint.color = resources.getColor(R.color.colorGray)
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

    fun resetView() {
        currStartLineX = 0f
        invalidate()
    }


    fun updateSB(currentPos: Long, duration: Long) {
        Log.d("TAG", "updateSB: currentTime ${System.currentTimeMillis()}")

        if (currentPos > 0L && duration > 0L) {
            val widthSpace = measuredWidth / (duration.toDouble())
            currStartLineX = ((currentPos.toDouble()) * widthSpace).toFloat()
            if (currStartLineX != 0f) {
                val distance = currStartLineX
                if (currentPos >= duration) {
//                    mCanvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), mPaint)
                }
                Log.d("TAG", "updateSB: distance $distance")
            }
            invalidate()
        }
    }


}