package com.example.audiocutter.ui.audiochooser.cut

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import com.example.audiocutter.R
import com.example.audiocutter.util.Utils
import kotlin.math.abs

class ProgressView : View {

    private val TAG = ProgressView::class.java.name
    private var mPaint1 = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mPaint2 = Paint(Paint.ANTI_ALIAS_FLAG)
    private var currentLineX = 0f
    private var prevPos = 0L
    private var nextPos = 0L
    private var pendingPos = 0L
    private var animator: ValueAnimator? = ValueAnimator()
    private var duration: Long = 0

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )


    init {
        mPaint1.color = resources.getColor(R.color.colorwhite)
        mPaint1.style = Paint.Style.FILL
        mPaint2.color = resources.getColor(R.color.colorYelowAlpha)
        mPaint2.style = Paint.Style.FILL
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawView(canvas)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun drawView(canvas: Canvas?) {
        canvas?.let {
            it.drawRect(0f, 0f, width.toFloat(), height.toFloat(), mPaint1)
            it.drawRect(0f, 0f, currentLineX, height.toFloat(), mPaint2)
        }
    }

    fun resetView() {
        currentLineX = 0f
        prevPos = 0L
        nextPos = 0L
        pendingPos = 0L
        requestLayout()
    }


    fun updatePG(currentPos: Long, duration: Long) {

        this.duration = duration
        if (currentPos > 0 && this.duration > 0) {
            if (prevPos != nextPos) {
                pendingPos = currentPos
            } else {
                nextPos = currentPos
                moveProcess()
            }
        }
    }

    private fun moveProcess() {
        val speed = (width * 1f / duration).toDouble()
        val endPos = Utils.convertValue(
            0.0,
            duration.toDouble(),
            0.0,
            width.toDouble(),
            nextPos.toDouble()
        )
        val startPos = Utils.convertValue(
            0.0,
            duration.toDouble(),
            0.0,
            width.toDouble(),
            prevPos.toDouble()
        )
        Log.d("nmCheckInfomation", "updatePG:  pendingPost $pendingPos\n nextPos: $nextPos\n prevPos : $prevPos")
        Log.e("nmCheckInfomation", "updatePG: start: $startPos \n endpos : $endPos")
        val time = abs(endPos - startPos) / speed


        animator = ValueAnimator.ofFloat(startPos.toFloat(), endPos.toFloat())

        if (time > 0) {
            animator!!.duration = time.toLong()

            Log.d(TAG, "infomation: Time $time distance $duration  speed $speed")

            animator!!.addUpdateListener {
                val start = (it.animatedValue as Float)
                currentLineX = start
                requestLayout()
            }
            animator!!.addListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(p0: Animator?) {
                    prevPos = nextPos
                    if (nextPos < pendingPos) {
                        updatePG(pendingPos, duration)
                    }
                }

                override fun onAnimationStart(p0: Animator?) {
                }

                override fun onAnimationCancel(p0: Animator?) {
                }

                override fun onAnimationRepeat(p0: Animator?) {
                }
            })

        }
        animator!!.start()
    }
}

