package com.example.audiocutter.functions.audiocutterscreen.view.animation

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import androidx.annotation.Nullable
import java.util.*

class BarVisualizer : BaseVisualizer {
    private var mMaxBatchCount = 0
    private var nPoints = 0
    lateinit var mSrcY: FloatArray
    lateinit var mDestY: FloatArray
    private var mBarWidth = 0f
    private var mClipBounds: Rect? = null
    private var nBatchCount = 0
    private var mRandom: Random? = null

    constructor(context: Context?) : super(context!!) {}
    constructor(
        context: Context?,
        @Nullable attrs: AttributeSet?
    ) : super(context!!, attrs) {
    }

    constructor(
        context: Context?,
        @Nullable attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context!!, attrs, defStyleAttr) {
    }

    override fun init() {
        nPoints = (BAR_MAX_POINTS * mDensity).toInt()
        if (nPoints < BAR_MIN_POINTS) nPoints = BAR_MIN_POINTS
        mBarWidth = -1f
        nBatchCount = 0
        setAnimationSpeed(mAnimSpeed)
        mRandom = Random()
        mClipBounds = Rect()
        mSrcY = FloatArray(nPoints)
        mDestY = FloatArray(nPoints)
    }

    override fun setAnimationSpeed(animSpeed: AnimSpeed) {
        super.setAnimationSpeed(animSpeed!!)
        mMaxBatchCount = AVConstants.MAX_ANIM_BATCH_COUNT - mAnimSpeed.ordinal
    }

    override fun onDraw(canvas: Canvas) {
        if (mBarWidth == -1f) {
            canvas.getClipBounds(mClipBounds)
            mBarWidth = canvas.width / nPoints.toFloat()

            //initialize points
            for (i in mSrcY.indices) {
                var posY: Float
                posY =
                    if (mPositionGravity === PositionGravity.TOP) mClipBounds!!.top.toFloat() else mClipBounds!!.bottom.toFloat()
                mSrcY[i] = posY
                mDestY[i] = posY
            }
        }

        //create the path and draw
        if (isVisualizationEnabled) {
            if (mRawAudioBytes.size == 0) {
                return
            }

            //find the destination bezier point for a batch
            if (nBatchCount == 0) {
                val randPosY = mDestY[mRandom!!.nextInt(nPoints)]
                for (i in mSrcY.indices) {
                    val x =
                        Math.ceil(((i + 1) * (mRawAudioBytes.size) / nPoints).toDouble()).toInt()

                    var t = 0
                    if (x < 1024) t =
                        height + (Math.abs(mRawAudioBytes[x].toDouble()) + 128) as Byte * height / 128
                    var posY: Float
                    posY =
                        if (mPositionGravity === PositionGravity.TOP) mClipBounds!!.bottom - t.toFloat() else mClipBounds!!.top + t.toFloat()

                    //change the source and destination y
                    mSrcY[i] = mDestY[i]
                    mDestY[i] = posY
                }
                mDestY[mSrcY.size - 1] = randPosY
            }

            //increment batch count
            nBatchCount++

            //calculate bar position and draw
            for (i in mSrcY.indices) {
                val barY =
                    mSrcY[i] + nBatchCount.toFloat() / mMaxBatchCount * (mDestY[i] - mSrcY[i])
                val barX = i * mBarWidth + mBarWidth / 2
                canvas.drawLine(barX, canvas.height.toFloat(), barX, barY, mPaint!!)
            }

            //reset the batch count
            if (nBatchCount == mMaxBatchCount) nBatchCount = 0
        }
        super.onDraw(canvas)
    }

    companion object {
        private const val BAR_MAX_POINTS = 120
        private const val BAR_MIN_POINTS = 3
    }
}