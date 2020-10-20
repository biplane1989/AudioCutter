package com.example.audiocutter.ui.audiochooser.mix

import android.content.Context
import android.graphics.*
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

    private lateinit var rectImageDst2: Rect
    private lateinit var rectImageDst1: Rect
    private lateinit var rectImage: Rect
    private val FONT_MEDIUM = "fonts/sanfranciscodisplay_medium.otf"
    private val FONT_BOLD = "fonts/sanfranciscodisplay_bold.otf"
    private val FONT_LIGHT = "fonts/sanfranciscodisplay_light.otf"
    private val FONT_REGULAR = "fonts/sanfranciscodisplay_regular.otf"
    private lateinit var typeFace: Typeface
    private lateinit var rectText2: Rect
    private lateinit var rectText1: Rect
    private val TAG = ChangeRangeView::class.java.name
    private lateinit var rect: Rect
    private var durationAudio2: Int = 0
    private var durationAudio1: Int = 0
    private var duration: Int = 0
    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mPaint2 = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mPaint3 = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mPaint4 = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mPaint5 = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mHeight = 0
    private var mWidth = 0
    private lateinit var rowRect1: RectF
    private lateinit var rowRect2: RectF
    private lateinit var rowRectProGress1: RectF
    private lateinit var rowRectProGress2: RectF
    private lateinit var rectCurrentProgress1: RectF
    private lateinit var rectCurrentProgress2: RectF
    private var startCurrentX = 0
    private var endCurrentX = 0
    private lateinit var audioFile1: AudioFile
    private lateinit var audioFile2: AudioFile
    lateinit var mCallback: OnPlayLineChange
    private var isTouch = TOUCHITEM.NONTOUCH
    private var rs = false
    private var numPos = ""
    private var RADIUS = Utils.convertDp2Px(9, context)
    private var RANGE = Utils.convertDp2Px(20, context)
    private var SIZE_IMAGE = Utils.convertDp2Px(30, context)
    private var mHeightText = 0
    private var currentLength1 = RADIUS.toDouble()
    private var currentLength2 = RADIUS.toDouble()
    private var textGetX = 0f
    private var rangeCircleProgress1 = 0f
    private var circleProgressGetY1 = 0f
    private var rangeCircleProgress2 = 0f
    private var circleProgressGetY2 = 0f

    private var currentXCircle1 = 0f
    private var currentXCircle2 = 0f
    private var textName1 = ""
    private var textName2 = ""

    private var ratioSound1 = "0%"
    private var ratioSound2 = "0%"

    init {
        typeFace = Typeface.createFromAsset(context.assets, FONT_MEDIUM)
        setPaint(mPaint5, R.color.colorAlphaGray)
        setPaint(mPaint4, R.color.colorBlack)
        setPaint(mPaint3, R.color.colorBlack)
        setPaint(mPaint2, R.color.colorYelowAlpha)
        setPaint(mPaint, R.color.colorYelowDark)
        mPaint.textSize = Utils.convertDp2Px(15, context)
        mPaint2.textSize = Utils.convertDp2Px(15, context)
        mPaint3.textSize = Utils.convertDp2Px(15, context)
        rect = Rect()
        mHeightText = Utils.convertDp2Px(20, context).toInt()

    }

    private fun setPaint(paint: Paint, color: Int) {
        paint.color = context.resources.getColor(color)
        paint.style = Paint.Style.FILL
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mHeight = h
        mWidth = w - 20
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        initRect(canvas)
        initLine(canvas)
        canvas.drawRoundRect(rowRect1, 10f, 10f, mPaint2)
        canvas.drawRoundRect(rowRect2, 10f, 10f, mPaint2)

    }

    private fun initRect(canvas: Canvas) {
        val rs = Utils.convertDp2Px(20, context)
        rowRect1 = RectF(RADIUS, mHeightText.toFloat(), currentLength1.toFloat(), mHeight / 2 - rs)

        rowRect2 = RectF(
            RADIUS,
            mHeightText + rowRect1.height() + Utils.convertDp2Px(10, context),
            currentLength2.toFloat(),
            (mHeight / 2 - rs) * 2 - Utils.convertDp2Px(10, context)
        )


        drawTextName(canvas, "day la bai hat 1")
        drawTextName2(canvas, "day la bai hat 2")
        drawBitmap(canvas, rowRect1.top + rectText1.height() + RANGE * 3 / 2)
        drawBitmap2(canvas, rowRect2.top + rectText2.height() + RANGE * 3 / 2)

        rowRectProGress1 = RectF(
            rectImageDst1.width() + RANGE * 2,
            rectImageDst1.top + RADIUS,
            mWidth / 1.3f + RANGE,
            rectImageDst1.top + RADIUS + 10
        )
        rowRectProGress2 = RectF(
            rectImageDst2.width() + RANGE * 2,
            rectImageDst2.top + RADIUS,
            mWidth / 1.3f + RANGE,
            rectImageDst2.top + RADIUS + 10
        )

        if (currentXCircle1 == 0f || currentXCircle2 == 0f) {
            currentXCircle1 = mWidth / 1.3f + RANGE
            currentXCircle2 = mWidth / 1.3f + RANGE
        }

        rectCurrentProgress1 = RectF(
            rectImageDst1.width() + RANGE * 2,
            rectImageDst1.top + RADIUS,
            currentXCircle1,
            rectImageDst1.top + RADIUS + 10
        )
        rectCurrentProgress2 = RectF(
            rectImageDst2.width() + RANGE * 2,
            rectImageDst2.top + RADIUS,
            currentXCircle2,
            rectImageDst2.top + RADIUS + 10
        )
        canvas.drawRoundRect(rowRectProGress1, 5f, 5f, mPaint5)
        canvas.drawRoundRect(rowRectProGress2, 5f, 5f, mPaint5)
        canvas.drawRoundRect(rectCurrentProgress1, 5f, 5f, mPaint)
        canvas.drawRoundRect(rectCurrentProgress2, 5f, 5f, mPaint)


        canvas.drawCircle(
//            rectImageDst1.width() + RANGE * 2 +
            currentXCircle1,
            rectImageDst1.top + RADIUS + 3,
            RADIUS,
            mPaint
        )
        canvas.drawCircle(
//            rectImageDst2.width() + RANGE * 2 +
            currentXCircle2,
            rectImageDst2.top + RADIUS + 3,
            RADIUS,
            mPaint
        )


        rangeCircleProgress1 = rectImageDst1.width() + RANGE * 2
        circleProgressGetY1 = rectImageDst1.top + RADIUS + 3
        rangeCircleProgress2 = rectImageDst2.width() + RANGE * 2
        circleProgressGetY2 = rectImageDst2.top + RADIUS + 3
    }


    private fun initLine(canvas: Canvas) {

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
        drawText(canvas, numPos, startCurrentX.toFloat() - rect.width() / 2, mPaint)
        drawText(canvas, "00:00", 0f + Utils.convertDp2Px(9, context), mPaint3)
        rs = durationAudio1 > durationAudio2
        if (!rs) {
            drawText(
                canvas,
                Utils.convertTime(durationAudio2),
                (mWidth - rect.width()).toFloat(),
                mPaint3
            )
            drawTextDurationMin(
                canvas,
                Utils.convertTime(
                    ManagerFactory.getAudioFileManager().getInfoAudioFile(
                        audioFile1.file, MediaMetadataRetriever.METADATA_KEY_DURATION
                    )!!.toInt()
                )
            )
        } else {
            drawText(
                canvas,
                Utils.convertTime(durationAudio1),
                (mWidth - rect.width()).toFloat(),
                mPaint3
            )
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

//        canvas.drawTopRoundRectRadius(rowRectProGress2, 5f, mPaint)


    }

    private fun drawTextName2(canvas: Canvas, nameAudio: String) {

        rectText2 = Rect()
        mPaint4.textSize = Utils.convertDp2Px(20, context)
        mPaint4.getTextBounds(nameAudio, 0, nameAudio.length, rectText2)
        mPaint4.color = context.resources.getColor(R.color.colorBlack)
        mPaint4.typeface = typeFace
        canvas.drawText(
            nameAudio,
            RADIUS * 2,
            rowRect2.top + rectText2.height() + RADIUS,
            mPaint4
        )
    }

    private fun drawBitmap(canvas: Canvas, fl: Float) {
        val imageSound = BitmapFactory.decodeResource(resources, R.drawable.ic_sound_mixing)
        rectImage = Rect(
            0,
            0,
            imageSound.width,
            imageSound.height
        )
        rectImageDst1 = Rect(
            (RADIUS * 2).toInt(),
            fl.toInt(),
            (RADIUS * 2 + RANGE).toInt(),
            (fl + RANGE).toInt()
        )
        Log.d(TAG, "drawBitmap: $rectImage")
        Log.d(TAG, "drawBitmap1: $rectImageDst1")
        canvas.drawBitmap(
            imageSound, rectImage,
            rectImageDst1, mPaint
        )
    }

    private fun drawBitmap2(canvas: Canvas, fl: Float) {
        val imageSound = BitmapFactory.decodeResource(resources, R.drawable.ic_sound_mixing)
        rectImage = Rect(
            0,
            0,
            imageSound.width,
            imageSound.height
        )
        rectImageDst2 = Rect(
            (RADIUS * 2).toInt(),
            fl.toInt(),
            (RADIUS * 2 + RANGE).toInt(),
            (fl + RANGE).toInt()
        )
        Log.d(TAG, "drawBitmap: $rectImage")
        Log.d(TAG, "drawBitmap1: $rectImageDst1")
        canvas.drawBitmap(
            imageSound, rectImage,
            rectImageDst2, mPaint
        )
    }

    private fun drawTextName(canvas: Canvas, nameAudio: String) {

        rectText1 = Rect()
        mPaint4.textSize = Utils.convertDp2Px(20, context)
        mPaint4.getTextBounds(nameAudio, 0, nameAudio.length, rectText1)
        mPaint4.color = context.resources.getColor(R.color.colorBlack)
        mPaint4.typeface = typeFace
        canvas.drawText(nameAudio, RADIUS * 2, rowRect1.top + rectText1.height() + RADIUS, mPaint4)
    }

    private fun drawText(canvas: Canvas, text: String, fl: Float, mPaint: Paint) {
        mPaint.getTextBounds(text, 0, text.length, rect)
        canvas.drawText(
            text,
            fl,
            rect.height().toFloat(),
            mPaint
        )
    }

    private fun drawTextDurationMin(canvas: Canvas, text: String) {
        mPaint.getTextBounds(text, 0, text.length, rect)
        if (rowRect1 != null && rowRect2 != null) {
            val rs = rowRect1!!.width() > rowRect2!!.width()
            textGetX = if (rs) {
                rowRect2!!.width()
            } else {
                rowRect1!!.width()
            }

            if (textGetX >= (mWidth - rect.width() - (RADIUS * 2))) {
                textGetX = mWidth - rect.width() * 2.toFloat() - RADIUS
                canvas.drawText(
                    text, textGetX,
                    rect.height().toFloat(), mPaint3
                )
            } else if (textGetX < rect.width()) {
                textGetX = rect.width() + RADIUS * 2
                canvas.drawText(
                    text, textGetX,
                    rect.height().toFloat(), mPaint3
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
                when (isTouch) {
                    TOUCHITEM.SEEKBAR -> {
                        if (x >= 0 && x <= mWidth - RADIUS) {
                            mCallback.pauseInvalid()
                            drawLineTouch(x, x)
                        }
                    }
                    TOUCHITEM.PROGRESS1 -> {
                        currentXCircle1 = x
                        if (x < (rectImageDst1.width() + RANGE * 2)) {
                            currentXCircle1 = (rectImageDst1.width() + RANGE * 2)
                        } else
                            if (x > (mWidth / 1.3f + RANGE)) {
                                currentXCircle1 = (mWidth / 1.3f + RANGE)
                            }
                        mCallback.setVolumeAudio1(x,(rectImageDst1.width() + RANGE * 2), (mWidth / 1.3f + RANGE) )

                        invalidate()
                    }
                    TOUCHITEM.PROGRESS2 -> {
                        currentXCircle2 = x
                        if (x < (rectImageDst2.width() + RANGE * 2)) {
                            currentXCircle2 = (rectImageDst2.width() + RANGE * 2)
                        } else
                            if (x > (mWidth / 1.3f + RANGE)) {
                                currentXCircle2 = (mWidth / 1.3f + RANGE)
                            }
                        mCallback.setVolumeAudio2(
                            x,
                            (rectImageDst2.width() + RANGE * 2),
                            (mWidth / 1.3f + RANGE)
                        )
                        invalidate()
                    }
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                when (isTouch) {
                    TOUCHITEM.SEEKBAR -> {
                        isTouch = TOUCHITEM.NONTOUCH
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
                    TOUCHITEM.PROGRESS1 -> {
                        isTouch = TOUCHITEM.NONTOUCH
                    }
                    TOUCHITEM.PROGRESS2 -> {
                        isTouch = TOUCHITEM.NONTOUCH
                    }

                }

            }
            MotionEvent.ACTION_DOWN -> {
                if (event.y >= mHeight - Utils.convertDp2Px(9 * 2, context) * 2) {
                    isTouch = TOUCHITEM.SEEKBAR
                    return true
                } else if (event.y < circleProgressGetY1 + RANGE && event.y > circleProgressGetY1 - RANGE) {
                    isTouch = TOUCHITEM.PROGRESS1
                    return true
                } else if (event.y < circleProgressGetY2 + RANGE && event.y > circleProgressGetY2 - RANGE) {
                    isTouch = TOUCHITEM.PROGRESS2
                    return true
                } else {
                    isTouch = TOUCHITEM.NONTOUCH
                    return false
                }
            }
        }
        return false
    }

    private fun drawLineTouch(startX: Float, endX: Float) {
        try {
            Log.d(TAG, "drawLineTouch: drawline")
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

        if (endCurrentX >= mWidth - RADIUS) {
            endCurrentX = mWidth - RADIUS.toInt() - 10
            startCurrentX = mWidth - RADIUS.toInt() - 10
        }

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

    fun setDuration(rs: Boolean) {
        /**
        setjust the parameters
         */
//        this.duration = duration.toInt()
        duration = if (rs) {
            durationAudio1
        } else {
            durationAudio2
        }
        startCurrentX = 0
        endCurrentX = 0
        mCallback.changeDuration()
        invalidate()

    }

    interface OnPlayLineChange {
        fun onLineChange(audioFile: AudioFile, pos: Int)
        fun pauseInvalid()
        fun changeDuration()
        fun setVolumeAudio1(x: Float, min1: Float, max1: Float)
        fun setVolumeAudio2(x: Float, min2: Float, max2: Float)
    }

    enum class TOUCHITEM(num: Int) {
        NONTOUCH(0),
        SEEKBAR(1),
        PROGRESS1(2),
        PROGRESS2(3)
    }

}