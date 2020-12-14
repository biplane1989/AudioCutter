package com.example.audiocutter.ui.audiochooser.cut

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.example.audiocutter.R
import com.example.audiocutter.util.Utils

class ProgressView : View {

    private val TAG = ProgressView::class.java.name
    private var mPaint1 = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mPaint2 = Paint(Paint.ANTI_ALIAS_FLAG)
    private var currentLineX = 0f
    private var currPos: Float = -1f
    private var destPos: Float = -1f
    private var animator: ValueAnimator? = ValueAnimator()
    private var duration: Float = 0f

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

    override fun onDraw(canvas: Canvas?) {
        Log.e(TAG, "updatePlayInfor $this  onDraw  currentLineX  $currentLineX duration $duration")
        super.onDraw(canvas)
        drawView(canvas)
    }

    private fun drawView(canvas: Canvas?) {
        canvas?.let {
            it.drawRect(0f, 0f, width.toFloat(), height.toFloat(), mPaint1)
            it.drawRect(0f, 0f, currentLineX, height.toFloat(), mPaint2)
        }
    }

    fun resetView() {
        Log.i(
            TAG,
            "updatePlayInfor   resetView: $currentLineX  prevPos $currPos nextPos $destPos"
        )
        currentLineX = 0f
        currPos = -1f
        destPos = -1f
        invalidate()
    }


    fun updatePG(currentPos: Long, duration: Long, useAnimation: Boolean = true) {
        Log.e(TAG, "updatePlayInfor  $this updatePG  currentLineX  $currentPos duration $duration")
        this.duration = duration.toFloat()
        moveProcess(currentPos.toFloat(), useAnimation)

    }

    private fun moveProcess(newPos: Float, useAnimation: Boolean = true) {
        if (useAnimation && currPos != -1f && newPos != duration && newPos > currPos) {
            if (animator != null && animator!!.isRunning) {
                destPos = newPos
                Log.d("taihhhhh", " destPos ${destPos} ")
            } else {
                destPos = newPos
                Log.d(
                    "taihhhhh",
                    " currPos ${currPos}  destPos ${destPos} duration ${((destPos - currPos) * 2).toLong()}"
                )

                animator = ValueAnimator.ofFloat(currPos, destPos)
                animator?.duration = ((destPos - currPos)).toLong()

                animator?.addUpdateListener {
                    currPos = (it.animatedValue as Float)
                    currentLineX = Utils.convertValue(
                        0f,
                        duration,
                        0f,
                        width.toFloat(),
                        currPos
                    )
                    invalidate()
                }

                animator?.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationEnd(p0: Animator?) {
                        if (currPos < destPos) {
                            post {
                                moveProcess(destPos)
                            }
                        }
                    }

                    override fun onAnimationStart(p0: Animator?) {
                    }

                    override fun onAnimationCancel(p0: Animator?) {
                    }

                    override fun onAnimationRepeat(p0: Animator?) {
                    }
                })
                animator?.start()
            }

        } else {
            animator?.cancel()
            currPos = newPos
            currentLineX = Utils.convertValue(
                0f,
                duration,
                0f,
                width.toFloat(),
                currPos
            )
            if (!useAnimation) {
                Log.e(
                    TAG,
                    "updatePlayInfor:  $this  moveProcess currentPos $currPos    currentLineX  $currentLineX     duration$duration     width  ${width.toFloat()}"
                )
            }
            invalidate()
        }


    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.e(TAG, "updatePlayInfor  onDetachedFromWindow")
    }
}

