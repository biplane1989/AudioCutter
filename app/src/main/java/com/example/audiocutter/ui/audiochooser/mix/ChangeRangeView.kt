package com.example.audiocutter.ui.audiochooser.mix

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.media.MediaMetadataRetriever
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.audiocutter.R
import com.example.audiocutter.core.ManagerFactory
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.util.Utils

class ChangeRangeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var duration: Int = 0
    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mHeight = 0
    private var mWidth = 0
    private var startCurrentX = 0
    private var endCurrentX = 0
    private lateinit var audioFile: AudioFile
    lateinit var mCallback: OnPlayLineChange
    private var isTouch = false
    private var numPos = ""
    private var RADIUS = Utils.convertDp2Px(9, context)


    init {
        mPaint.color = context.resources.getColor(R.color.colorYelowDark)
        mPaint.style = Paint.Style.FILL
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mHeight = h
        mWidth = w - 16
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        initLine(canvas)
    }


    private fun initLine(canvas: Canvas?) {
        canvas?.let {
            canvas.drawLine(
                startCurrentX.toFloat() + Utils.convertDp2Px(9, context),
                0f + Utils.convertDp2Px(15, context),
                endCurrentX.toFloat() + Utils.convertDp2Px(9, context),
                mHeight * 1f,
                mPaint
            )
            canvas.drawCircle(
                startCurrentX.toFloat() + Utils.convertDp2Px(9, context),
                (mHeight * 1f) - RADIUS,
                RADIUS,
                mPaint
            )
            drawText(canvas, numPos)
            drawTextStart(canvas, "00:00")
            drawTextDuration(canvas, Utils.convertTime(duration))

        }
    }

    private fun drawText(canvas: Canvas, text: String) {
        mPaint.textSize = Utils.convertDp2Px(15, context)
        mPaint.style = Paint.Style.FILL
        val rect = Rect()
        mPaint.getTextBounds(text, 0, text.length, rect)
        canvas.drawText(
            text,
            startCurrentX.toFloat() - rect.width() / 2,
            rect.height().toFloat(),
            mPaint
        )

    }

    private fun drawTextStart(canvas: Canvas, text: String) {
        mPaint.textSize = Utils.convertDp2Px(15, context)
        mPaint.style = Paint.Style.FILL
        val rect = Rect()
        mPaint.getTextBounds(text, 0, text.length, rect)
        canvas.drawText(
            text,
            0f + Utils.convertDp2Px(9, context),
            rect.height().toFloat(),
            mPaint
        )

    }

    private fun drawTextDuration(canvas: Canvas, text: String) {
        mPaint.textSize = Utils.convertDp2Px(15, context)
        mPaint.style = Paint.Style.FILL
        val rect = Rect()
        mPaint.getTextBounds(text, 0, text.length, rect)
        canvas.drawText(
            text,
            (mWidth - rect.width()).toFloat() - Utils.convertDp2Px(9, context),
            rect.height().toFloat(), mPaint
        )

    }

    private fun getW(): Int {
        return mWidth
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                if (isTouch) {
                    if (x >= 0 && x <= mWidth - RADIUS) {
                        mCallback.pauseInvalid()
                        drawLineTouch(x, x)
                    }
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                isTouch = false
                val mWidth = getW()
                val pos = Utils.convertValue(
                    0.0,
                    mWidth.toDouble(),
                    0.0,
                    duration.toDouble(),
                    x.toDouble()
                )
                mCallback.onLineChange(audioFile, pos.toInt())
            }
            MotionEvent.ACTION_DOWN -> {
                return if (event.y >= mHeight - Utils.convertDp2Px(9 * 2, context) * 2) {
                    isTouch = true
                    isTouch
                } else {
                    isTouch = false
                    isTouch
                }

            }
        }
        return false
    }

    private fun drawLineTouch(startX: Float, endX: Float) {
        try {
            numPos = Utils.longDurationMsToStringMs(
                Utils.convertValue(
                    0.0,
                    mWidth.toDouble(),
                    0.0,
                    duration.toDouble(),
                    startX.toDouble()
                ).toLong()
            )

            endCurrentX = endX.toInt()
            startCurrentX = startX.toInt()
//            if (startX - (LIMIT_RANGE) < 0) {
//                startCurrentX = Utils.convertDp2Px(9, context).toInt()
//                endCurrentX = Utils.convertDp2Px(9, context).toInt()
//            }
//            if (endX + LIMIT_RANGE > mWidth) {
//                endCurrentX = mWidth - Utils.convertDp2Px(9, context).toInt()
//                startCurrentX = mWidth - Utils.convertDp2Px(9, context).toInt()
//            }
            invalidate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setFileAudio(audioFile: AudioFile) {
        this.audioFile = audioFile
        duration = (ManagerFactory.getAudioFileManager()
            .getInfoAudioFile(
                audioFile.file, MediaMetadataRetriever
                    .METADATA_KEY_DURATION
            ))!!.toInt()

    }

    fun setPosition(posision: Int) {
        val pos = Utils.convertValue(
            0.0,
            duration.toDouble(),
            0.0,
            mWidth.toDouble(),
            posision.toDouble()
        )
        numPos = Utils.longDurationMsToStringMs(posision.toLong())
        startCurrentX = pos.toInt()
        endCurrentX = pos.toInt()
        invalidate()
    }

    interface OnPlayLineChange {
        fun onLineChange(audioFile: AudioFile, pos: Int)
        fun pauseInvalid()
    }

}