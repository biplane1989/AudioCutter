package com.example.audiocutter.functions.audiochooser.cut.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import java.util.*
import kotlin.math.abs
import kotlin.math.ceil

class BarVisualizer : BaseVisuaLizer {
    val TAG = "nqmanh"

    private val BAR_MAX_POINTS = 3
    private val BAR_MIN_POINTS = 30

    private var mMaxBatchCount = 0

    private var nPoints: Int? = 0

    private var mSrcY: FloatArray
    private var mDestY: FloatArray

    private var mBarWidth = 0f
    private var mClipBounds: Rect

    private var nBatchCount = 0

    private var mRandom: Random

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        nPoints = (BAR_MAX_POINTS * mDensity).toInt()
        Log.d(TAG, "npoints: $nPoints ")
        if (nPoints!! < BAR_MIN_POINTS) nPoints = BAR_MIN_POINTS

        mBarWidth = -1f
        nBatchCount = 0

        setAnimationSpeed(mAnimSpeed)

        mRandom = Random()

        mClipBounds = Rect()

        mSrcY = FloatArray(nPoints!!)
        mDestY = FloatArray(nPoints!!)
    }

    override fun setAnimationSpeed(animSpeed: AnimSpeed?) {
        super.setAnimationSpeed(animSpeed)
        mMaxBatchCount = AVConstants.MAX_ANIM_BATCH_COUNT - mAnimSpeed.ordinal
    }

    override fun onDraw(canvas: Canvas?) {

        if (mBarWidth == -1f) {
            canvas!!.getClipBounds(mClipBounds)
            mBarWidth = width / nPoints!!.toFloat()

            //initialize points
            for (i in mSrcY.indices) {
                var posY: Float
                posY =
                    if (mPositionGravity === PositionGravity.TOP) mClipBounds.top.toFloat() else mClipBounds!!.bottom.toFloat()
                mSrcY[i] = posY
                mDestY[i] = posY
            }
        }

        //create the path and draw

        //create the path and draw
        if (isVisualizationEnabled && mRawAudioBytes != null) {
            if (mRawAudioBytes!!.isEmpty()) {
                return
            }
            //find the destination bezier point for a batch
            if (nBatchCount == 0) {
                val randPosY = mDestY[mRandom.nextInt(nPoints!!)]
//                val randPosY = 0
                for (i in mSrcY.indices) {
                    val x =
                        ceil((i + 1) * (mRawAudioBytes!!.size / nPoints!!).toDouble()).toInt()
                    var t = 0
                    if (x < 1024)
                        t = height + abs(mRawAudioBytes!![x] + 128) * height / 128.toByte()
                    var posY: Float
                    posY =
                        if (mPositionGravity == PositionGravity.TOP) mClipBounds!!.bottom - t.toFloat() else mClipBounds!!.top + t.toFloat()

                    //change the source and destination y
                    mSrcY[i] = mDestY[i]
                    mDestY[i] = posY
                }
                if (mDestY.isNotEmpty()) {
                    mDestY[mSrcY.size - 1] = randPosY
                    Log.d(TAG, "onDraw: $randPosY")
                }
            }

            //increment batch count
            nBatchCount++
            Log.d(TAG, "batcount: $nBatchCount")
            //calculate bar position and draw
            for (i in mSrcY.indices) {
                val barY =
                    mSrcY[i] + nBatchCount.toFloat() / mMaxBatchCount * (mDestY[i] - mSrcY[i])
                val barX = i * mBarWidth + mBarWidth / 2
                canvas!!.drawLine(barX, height.toFloat(), barX, barY, mPaint)
                requestLayout()
            }

            //reset the batch count
            if (nBatchCount == mMaxBatchCount) nBatchCount = 0
        }

        super.onDraw(canvas)
    }


}
