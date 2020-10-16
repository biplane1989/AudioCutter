package com.example.audiocutter.ui.audiochooser.mix

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.media.MediaMetadataRetriever
import android.util.AttributeSet
import android.util.Log
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

    private lateinit var rect: Rect
    private var durationAudio2: Int = 0
    private var durationAudio1: Int = 0
    private var duration: Int = 0
    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mPaint2 = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mHeight = 0
    private var mWidth = 0
    private var rowRect1: RectF? = null
    private var rowRect2: RectF? = null
    private var startCurrentX = 0
    private var endCurrentX = 0
    private lateinit var audioFile1: AudioFile
    private lateinit var audioFile2: AudioFile
    lateinit var mCallback: OnPlayLineChange
    private var isTouch = false
    private var rs = false
    private var numPos = ""
    private var RADIUS = Utils.convertDp2Px(9, context)
    private var mHeightText = 0
    private var currentLength1 = RADIUS.toDouble()
    private var currentLength2 = RADIUS.toDouble()


    init {
        mPaint.color = context.resources.getColor(R.color.colorYelowDark)
        mPaint.style = Paint.Style.FILL
        mPaint2.color = context.resources.getColor(R.color.colorYelowAlpha)
        mPaint2.style = Paint.Style.FILL
        mPaint.textSize = Utils.convertDp2Px(15, context)
        rect = Rect()
        mHeightText = Utils.convertDp2Px(20, context).toInt()

    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mHeight = h
        mWidth = w - 16
    }


    private fun Canvas.drawTopRoundRect(rect: RectF, radius: Float, paint: Paint) {
        drawRoundRect(rect, radius, radius, paint)
        drawRect(
            rect.left,
            rect.top,
            rect.right,
            rect.bottom,
            paint
        )
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        initRect()
        initLine(canvas)
        canvas.drawTopRoundRect(rowRect1!!, 0f, mPaint2)
        canvas.drawTopRoundRect(rowRect2!!, 0f, mPaint2)

    }

    private fun initRect() {
        val rs = Utils.convertDp2Px(70, context)
        rowRect1 = RectF(RADIUS, mHeightText.toFloat(), currentLength1.toFloat(), rs + mHeightText)

        rowRect2 = RectF(
            RADIUS,
            mHeightText + rowRect1!!.height() + Utils.convertDp2Px(10, context),
            currentLength2.toFloat(),
            mHeightText + rs * 2 + Utils.convertDp2Px(10, context)
        )

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
            rs = durationAudio1 > durationAudio2
            if (!rs) {
                drawTextDuration(canvas, Utils.convertTime(durationAudio2))
                drawTextDurationMin(
                    canvas,
                    Utils.convertTime(
                        ManagerFactory.getAudioFileManager().getInfoAudioFile(
                            audioFile1.file,
                            MediaMetadataRetriever.METADATA_KEY_DURATION
                        )!!.toInt()
                    )
                )
            } else {
                drawTextDuration(canvas, Utils.convertTime(durationAudio1))
                drawTextDurationMin(
                    canvas,
                    Utils.convertTime(
                        ManagerFactory.getAudioFileManager().getInfoAudioFile(
                            audioFile2.file,
                            MediaMetadataRetriever.METADATA_KEY_DURATION
                        )!!.toInt()
                    )
                )
            }
        }
    }

    private fun drawText(canvas: Canvas, text: String) {
        mPaint.getTextBounds(text, 0, text.length, rect)
        canvas.drawText(
            text,
            startCurrentX.toFloat() - rect.width() / 2,
            rect.height().toFloat(),
            mPaint
        )

    }

    private fun drawTextStart(canvas: Canvas, text: String) {
        mPaint.getTextBounds(text, 0, text.length, rect)
        canvas.drawText(
            text,
            0f + Utils.convertDp2Px(9, context),
            rect.height().toFloat(),
            mPaint
        )

    }

    private fun drawTextDuration(canvas: Canvas, text: String) {
        mPaint.getTextBounds(text, 0, text.length, rect)
        canvas.drawText(
            text,
            (mWidth - rect.width()).toFloat() - Utils.convertDp2Px(9, context),
            rect.height().toFloat(), mPaint
        )

    }

    private fun drawTextDurationMin(canvas: Canvas, text: String) {
        if (rowRect1 != null && rowRect2 != null) {

            val rs = rowRect1!!.width() > rowRect2!!.width()
            if (rs) {
                canvas.drawText(
                    text,
                    rowRect2!!.width(),
                    rect.height().toFloat(), mPaint
                )
            } else {
                canvas.drawText(
                    text,
                    rowRect1!!.width(),
                    rect.height().toFloat(), mPaint
                )
            }
        }


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
                mCallback.onLineChange(audioFile2, pos.toInt())
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

            invalidate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setFileAudio(audioFile1: AudioFile, audioFile2: AudioFile) {
        this.audioFile1 = audioFile1
        this.audioFile2 = audioFile2

        durationAudio1 = ManagerFactory.getAudioFileManager()
            .getInfoAudioFile(audioFile1.file, MediaMetadataRetriever.METADATA_KEY_DURATION)!!
            .toInt()
        durationAudio2 = ManagerFactory.getAudioFileManager()
            .getInfoAudioFile(audioFile2.file, MediaMetadataRetriever.METADATA_KEY_DURATION)!!
            .toInt()

        duration = if (!rs) {
            (ManagerFactory.getAudioFileManager()
                .getInfoAudioFile(
                    audioFile2.file, MediaMetadataRetriever
                        .METADATA_KEY_DURATION
                ))!!.toInt()
        } else {
            (ManagerFactory.getAudioFileManager()
                .getInfoAudioFile(
                    audioFile1.file, MediaMetadataRetriever
                        .METADATA_KEY_DURATION
                ))!!.toInt()
        }

        Log.d("TAG", "setFileAudio: duration $duration  dur1 $durationAudio1  dur2 $durationAudio2")


    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val count = Utils.convertDp2Px(18, context)
        setPadding(count.toInt(), 0, 20, 0)
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

    fun setLengthAudio(durAudio1: String?, durAudio2: String?) {

        val isCompare = durAudio1!!.toInt() > durAudio2!!.toInt()
        if (isCompare) {
            currentLength1 = Utils.convertValue(
                0.0,
                durAudio1!!.toDouble(),
                0.0,
                mWidth.toDouble(),
                durAudio1.toDouble()
            )
            currentLength2 = (durAudio2.toDouble() / durAudio1.toDouble()) * currentLength1
        } else {
            currentLength2 = Utils.convertValue(
                0.0,
                durAudio2!!.toDouble(),
                0.0,
                mWidth.toDouble(),
                durAudio2.toDouble()
            )
            currentLength1 = (durAudio1.toDouble() / durAudio2.toDouble()) * currentLength2

        }
        if (durAudio1.toInt() == durAudio2.toInt()) {
            currentLength1 = Utils.convertValue(
                0.0,
                durAudio1.toDouble(),
                0.0,
                mWidth.toDouble(),
                durAudio1.toDouble()
            )
            currentLength2 = currentLength1
        }
        invalidate()
    }

    fun setDuration(duration: String) {
        /**
         setjust the parameters
         */
//        this.duration = duration.toInt()
        startCurrentX = 0
        endCurrentX = 0
        mCallback.changeDuration()
        invalidate()

    }

    interface OnPlayLineChange {
        fun onLineChange(audioFile: AudioFile, pos: Int)
        fun pauseInvalid()
        fun changeDuration()
    }

}