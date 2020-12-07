package com.example.audiocutter.ui.audiochooser.mix

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.example.audiocutter.R
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.util.Utils
import kotlin.math.max

class ChangeRangeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val RADIUS_RECT = 15f
    private var mPaint7 = Paint(Paint.ANTI_ALIAS_FLAG)
    private val DEFAULT_TIME_POSITION: String = "00:00:0"
    private var ratio: Int = 0
    private lateinit var rectImageDst2: Rect
    private lateinit var rectImageDst1: Rect
    private lateinit var rectImage: Rect
    private var typeFace: Typeface
    private lateinit var rectText2: Rect
    private lateinit var rectText1: Rect
    private val TAG = ChangeRangeView::class.java.name
    private lateinit var rect: Rect
    private var durationAudio2: Int = 0
    private var durationAudio1: Int = 0
    private var duration: Int = 0
    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mPaint2 = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mPaint4 = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mPaint5 = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mPaint6 = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mHeight = 0
    var mWidth = 0
    private lateinit var rectDuration1: RectF
    private lateinit var rectDuration2: RectF

    private lateinit var rectSeekbarSound1: RectF
    private lateinit var rectSeekbarSound2: RectF
    private lateinit var rectCurrentSeekbar1: RectF
    private lateinit var rectCurrentSeekbar2: RectF
    private var startCurrentX = 0
    private var endCurrentX = 0
    private lateinit var audioFile1: AudioFile
    private lateinit var audioFile2: AudioFile
    lateinit var mCallback: OnPlayLineChange
    private var isTouch = TOUCHITEM.NONTOUCH
    private var rs = false
    private var isChangeNumpos = false
    private var numPos = ""
    private var RADIUS = Utils.convertDp2Px(9, context)
    private var RANGE = Utils.convertDp2Px(20, context)
    private var mHeightText = 0
    private var currentLength1 = RADIUS.toDouble()
    private var maxLength = RADIUS.toDouble()
    private var currentLength2 = RADIUS.toDouble()
    private var textGetX = 0f
    private var rangeCircleProgress1 = 0f
    private var rangeCircleProgress2 = 0f
    private var circleProgressGetY1 = 0f
    private var circleProgressGetY2 = 0f
    private val DISTANCE = Utils.convertDp2Px(20, context)
    private var currentXCircle1 = 0f
    private var currentXCircle2 = 0f
    private var isChangeLengtDuration = 0
    private var textName1 = ""
    private var textName2 = ""

    private var ratioSound1 = "100%"
    private var ratioSound2 = "100%"
    private var maxDistance = 0.0
    private var position = 0



    init {

        typeFace = ResourcesCompat.getFont(context, R.font.opensans_regular)!!
        setPaint(mPaint5, R.color.colorGraySeekbar)
        setPaint(mPaint6, R.color.colorgray)
        setPaint(mPaint4, R.color.colorBlack)
        setPaint(mPaint2, R.color.colorYelowAlpha)
        setPaint(mPaint, R.color.colorYelowDark)
        setPaint(mPaint7, R.color.colorGrayProgressChangerangeView)
        mPaint6.textSize = Utils.convertDp2Px(15, context)
        mPaint.textSize = Utils.convertDp2Px(15, context)
        mPaint2.textSize = Utils.convertDp2Px(15, context)
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
        mWidth = w - 32
        maxDistance = mWidth.toDouble()
        Log.d(TAG, "nmcode: $mWidth")
        currentXCircle1 =mWidth / 1.3f + RANGE
        currentXCircle2 =mWidth / 1.3f + RANGE
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        initRect(canvas)
        initLine(canvas)
    }

    private fun initRect(canvas: Canvas) {

        /** draw rect cover all view and rect
         *  seekbar of 2 view audio file
         *  current position seekbar Rect
         *  draw circle move seekbar sound audio
         *  drawtext ratio sound
         *  draw bitmap sound audio
         ***/
        textName1 = audioFile1.fileName
        textName2 = audioFile2.fileName


        rectDuration1 = RectF(
            RADIUS,
            mHeightText.toFloat(),
            currentLength1.toFloat() + RADIUS,
            mHeight / 2 - DISTANCE
        )

        rectDuration2 = RectF(
            RADIUS,
            mHeightText + rectDuration1.height() + Utils.convertDp2Px(10, context),
            currentLength2.toFloat() + RADIUS,
            (mHeight / 2 - DISTANCE) * 2 - Utils.convertDp2Px(10, context)
        )

        canvas.drawRoundRect(rectDuration1, RADIUS_RECT, RADIUS_RECT, mPaint2)
        canvas.drawRoundRect(rectDuration2, RADIUS_RECT, RADIUS_RECT, mPaint2)

        drawTextName(canvas, textName1)
        drawTextName2(canvas, textName2)

        drawBitmap(canvas, rectDuration1.top + rectText1.height() + RANGE * 3 / 2)
        drawBitmap2(canvas, rectDuration2.top + rectText2.height() + RANGE * 3 / 2)

        rectSeekbarSound1 = RectF(
            rectImageDst1.width() + RANGE * 2,
            rectImageDst1.top + RADIUS,
            mWidth / 1.3f + RANGE,
            rectImageDst1.top + RADIUS + 10
        )

        rectSeekbarSound2 = RectF(
            rectImageDst2.width() + RANGE * 2,
            rectImageDst2.top + RADIUS,
            mWidth / 1.3f + RANGE,
            rectImageDst2.top + RADIUS + 10
        )

        if (currentXCircle1 == 0f || currentXCircle2 == 0f) {
            currentXCircle1 = (rectImageDst1.width() + RANGE * 2)
            currentXCircle2 = (rectImageDst1.width() + RANGE * 2)
//            currentXCircle1 =mWidth / 1.3f + RANGE
//            currentXCircle2 = mWidth / 1.3f + RANGE
        }
        rectCurrentSeekbar1 = RectF(
            rectImageDst1.width() + RANGE * 2,
            rectImageDst1.top + RADIUS,
            currentXCircle1,
            rectImageDst1.top + RADIUS + 10
        )
        rectCurrentSeekbar2 = RectF(
            rectImageDst2.width() + RANGE * 2,
            rectImageDst2.top + RADIUS,
            currentXCircle2,
            rectImageDst2.top + RADIUS + 10
        )
        canvas.drawRoundRect(rectSeekbarSound1, 5f, 5f, mPaint5)
        canvas.drawRoundRect(rectSeekbarSound2, 5f, 5f, mPaint5)
        canvas.drawRoundRect(rectCurrentSeekbar1, 5f, 5f, mPaint)
        canvas.drawRoundRect(rectCurrentSeekbar2, 5f, 5f, mPaint)

        /**draw circle of seekbar audio file**/
        canvas.drawCircle(
            currentXCircle1,
            rectImageDst1.top + RADIUS + 3,
            RADIUS,
            mPaint
        )
        canvas.drawCircle(
            currentXCircle2,
            rectImageDst2.top + RADIUS + 3,
            RADIUS,
            mPaint
        )

        rangeCircleProgress1 = rectImageDst1.width() + RANGE * 2
        rangeCircleProgress2 = rectImageDst2.width() + RANGE * 2
        circleProgressGetY1 = rectImageDst1.top + RADIUS + 3
        circleProgressGetY2 = rectImageDst2.top + RADIUS + 3

        drawTextSound(ratioSound1, canvas, rectSeekbarSound1.right + RANGE, rectSeekbarSound1)
        drawTextSound(ratioSound2, canvas, rectSeekbarSound2.right + RANGE, rectSeekbarSound2)
    }

    private fun drawTextSound(ratioSound1: String, canvas: Canvas, x: Float, y: RectF) {
        canvas.drawText(
            ratioSound1,
            x,
            y.top + RADIUS / 2.1f,
            mPaint6
        )
    }

    private fun drawTextName2(canvas: Canvas, nameAudio: String) {
        val name: String = if (nameAudio.length > 20) {
            "${nameAudio.substring(0, 20)}...."
        } else {
            nameAudio
        }
        rectText2 = Rect()
        mPaint4.textSize = Utils.convertDp2Px(20, context)
        mPaint4.getTextBounds(nameAudio, 0, nameAudio.length, rectText2)
        mPaint4.color = context.resources.getColor(R.color.colorBlack)
        mPaint4.typeface = typeFace
        canvas.drawText(
            name,
            RADIUS * 2,
            rectDuration2.top + rectText2.height() + RADIUS,
            mPaint4
        )
    }

    private fun drawTextName(canvas: Canvas, nameAudio: String) {
        val name: String = if (nameAudio.length > 20) {
            "${nameAudio.substring(0, 20)}...."
        } else {
            nameAudio
        }
        rectText1 = Rect()
        mPaint4.textSize = Utils.convertDp2Px(20, context)
        mPaint4.getTextBounds(nameAudio, 0, nameAudio.length, rectText1)
        mPaint4.color = context.resources.getColor(R.color.colorBlack)
        mPaint4.typeface = typeFace

        canvas.drawText(name, RADIUS * 2, rectDuration1.top + rectText1.height() + RADIUS, mPaint4)
    }

    private fun initLine(canvas: Canvas) {
        /** draw line seekbar file audio custom
         *          draw circle change folow event touchmove
         *          draw text duration audio
         *          draw text change position
         **/
        canvas.drawLine(
            startCurrentX.toFloat() + RADIUS,
            0f,
            endCurrentX.toFloat() + RADIUS,
            mHeight * 1f - (RANGE + RADIUS),
            mPaint
        )
        canvas.drawCircle(
            startCurrentX.toFloat() + RADIUS,
            (mHeight * 1f) - (RANGE + RADIUS),
            RADIUS,
            mPaint
        )

        drawText(canvas, "00:00", 0f + RADIUS, mPaint6)
        try {
            rs = durationAudio1 > durationAudio2
            if (!rs) {
                drawText(
                    canvas,
                    Utils.convertTime(durationAudio2),
//                    (mWidth - RANGE*2 - RADIUS * 2),
                    mWidth - rect.width() * 1f - RANGE,
                    mPaint6
                )
                drawTextDurationMin(
                    canvas,
                    Utils.convertTime(audioFile1.duration.toInt())
//                    audioFile1.duration.convertToAudioDuration()
                )
            } else {
                drawText(
                    canvas,
                    Utils.convertTime(durationAudio1),
//                    (mWidth - RANGE - RADIUS * 2),
                    mWidth - rect.width() * 1f - RANGE,
                    mPaint6
                )
                drawTextDurationMin(
                    canvas,
                    Utils.convertTime(audioFile2.duration.toInt())
//                    audioFile2.duration.convertToAudioDuration()
                )
            }
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }

        /**text number position*/
        drawTextPosition(canvas, numPos, startCurrentX.toFloat() - rect.width() / 2.5f, mPaint)


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
        Log.d(TAG, "checkRange: $RANGE")
        Log.d(TAG, "drawBitmap1: $rectImageDst1")
        canvas.drawBitmap(
            imageSound, rectImage,
            rectImageDst1, null
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
            rectImageDst2, null
        )
    }

    private fun drawText(canvas: Canvas, text: String, getX: Float, mPaint: Paint) {
        mPaint.getTextBounds(text, 0, text.length, rect)
        canvas.drawText(
            text,
            getX,
            rect.height().toFloat(),
            mPaint
        )

    }

    private fun drawTextPosition(canvas: Canvas, text: String, fl: Float, mPaint: Paint) {
        mPaint.getTextBounds(text, 0, text.length, rect)
        canvas.drawText(
            text,
            fl,
            mHeight * 1f - 3,
            mPaint
        )

    }

    private fun drawTextDurationMin(canvas: Canvas, text: String) {
        Log.d(TAG, "drawTextDurationMin: $text")
        mPaint.getTextBounds(text, 0, text.length, rect)
        val rs = rectDuration1.width() > rectDuration2.width()
        textGetX = if (rs) {
            rectDuration2.width()
        } else {
            rectDuration1.width()
        }
        rect = Rect()
        mPaint6.getTextBounds(text, 0, text.length, rect)

        if (textGetX < rect.width() + RADIUS * 2) {
            textGetX = rect.width() + RADIUS * 2
        } else
            if (textGetX >= (mWidth - rect.width() - (RADIUS * 4))) {
                textGetX = mWidth - rect.width() * 2 - RANGE
            }
        canvas.drawText(
            text, textGetX,
            rect.height().toFloat(), mPaint6
        )
    }

    private fun getW(): Int {
        return mWidth
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        /**check touch event**/
        val x = event.x

        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                when (isTouch) {
                    TOUCHITEM.SEEKBAR -> {
                        if (x >= 0 && x <= mWidth - RADIUS) {
                            mCallback.pauseInvalid()
                            drawLineTouch(x)
                        }
                    }
                    TOUCHITEM.SEEKBARSOUND1 -> {
                        currentXCircle1 = x
                        if (x < (rectImageDst1.width() + RANGE * 2)) {
                            currentXCircle1 = (rectImageDst1.width() + RANGE * 2)
                        } else
                            if (x > (mWidth / 1.3f + RANGE)) {
                                currentXCircle1 = (mWidth / 1.3f + RANGE)
                            }
                        mCallback.setVolumeAudio1(
                            x,
                            (rectImageDst1.width() + RANGE * 2),
                            (mWidth / 1.3f + RANGE)
                        )
                        val newValueSound =
                            Utils.convertValue(
                                (rectImageDst1.width() + RANGE * 2).toDouble(),
                                (mWidth / 1.3f + RANGE).toDouble(),
                                0.0,
                                1.0,
                                x.toDouble()
                            )
                        ratio = ((newValueSound / 1.0) * 100).toInt()
                        checkRangeRatio(ratio)
                        ratioSound1 = "${ratio}%"
                        invalidate()
                    }
                    TOUCHITEM.SEEKBARSOUND2 -> {
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
                        val newValueSound =
                            Utils.convertValue(
                                (rectImageDst2.width() + RANGE * 2).toDouble(),
                                (mWidth / 1.3f + RANGE).toDouble(),
                                0.0,
                                1.0,
                                x.toDouble()
                            )
                        ratio = ((newValueSound / 1.0) * 100).toInt()
                        checkRangeRatio(ratio)
                        ratioSound2 = "${ratio}%"

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
                        mCallback.onLineChange(audioFile1, audioFile2, pos.toInt())

                    }
                    TOUCHITEM.SEEKBARSOUND1 -> {
                        isTouch = TOUCHITEM.NONTOUCH
                    }
                    TOUCHITEM.SEEKBARSOUND2 -> {
                        isTouch = TOUCHITEM.NONTOUCH

                    }

                }

            }
            MotionEvent.ACTION_DOWN -> {
                return if (event.y >= mHeight - Utils.convertDp2Px(20, context) * 2) {
                    isTouch = TOUCHITEM.SEEKBAR
                    true
                } else if (event.y < circleProgressGetY1 + RANGE && event.y > circleProgressGetY1 - RANGE) {
                    isTouch = TOUCHITEM.SEEKBARSOUND1
                    true
                } else if (event.y < circleProgressGetY2 + RANGE && event.y > circleProgressGetY2 - RANGE) {
                    isTouch = TOUCHITEM.SEEKBARSOUND2
                    true
                } else {
                    isTouch = TOUCHITEM.NONTOUCH
                    false
                }
            }
        }
        return false
    }

    private fun checkRangeRatio(ratio: Int) {
        if (ratio > 100) {
            this.ratio = 100
        } else if (ratio < 0) {
            this.ratio = 0
        }
    }

    private fun drawLineTouch(x: Float) {
        try {
            endCurrentX = x.toInt()
            startCurrentX = x.toInt()
            if (startCurrentX > maxDistance && endCurrentX > maxDistance) {
                startCurrentX = maxDistance.toInt()
                endCurrentX = maxDistance.toInt()
            }
            numPos = Utils.longDurationMsToStringMs(
                Utils.convertValue(
                    0.0,
                    mWidth.toDouble(),
                    0.0,
                    duration.toDouble(),
                    startCurrentX.toDouble()
                ).toLong()
            )
            invalidate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setFileAudio(audioFile1: AudioFile, audioFile2: AudioFile) {
        this.audioFile1 = audioFile1
        this.audioFile2 = audioFile2

        durationAudio1 = audioFile1.duration.toInt()
        durationAudio2 = audioFile2.duration.toInt()
        Log.d("manhnq", "duration:  $durationAudio1 - duration2 : $durationAudio2")
        val rs = durationAudio1 > durationAudio2
        duration = if (!rs) {
            audioFile2.duration.toInt()
        } else {
            audioFile1.duration.toInt()
        }

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val count = Utils.convertDp2Px(18, context)
        setPadding(count.toInt(), 0, 20, 0)
    }

    fun setPosition(position: Int) {
        val pos = Utils.convertValue(
            0.0,
            duration.toDouble(),
            0.0,
            mWidth.toDouble(),
            position.toDouble()
        )
        this.position = position


        Log.d(TAG, "setPosition: numpos $numPos")
        startCurrentX = pos.toInt()
        endCurrentX = pos.toInt()

        if (startCurrentX > maxDistance && endCurrentX > maxDistance) {
            Log.d(TAG, "onClick:  maxdistance ${maxDistance} ")
            startCurrentX = maxDistance.toInt()
            endCurrentX = maxDistance.toInt()
            mCallback.endAudioAtMaxdistance()
            isChangeNumpos = true
        }

        if (endCurrentX >= mWidth - RADIUS) {
            endCurrentX = mWidth - RADIUS.toInt()
            startCurrentX = mWidth - RADIUS.toInt()
            isChangeNumpos = true
        }
        numPos = Utils.longDurationMsToStringMs(this.position.toLong())
        if (!isChangeNumpos) {
            invalidate()
        }
        isChangeNumpos = false
    }

    fun setLengthAudio(durAudio1: String?, durAudio2: String?) {

        val isCompare = durAudio1!!.toInt() > durAudio2!!.toInt()
        if (isCompare) {
            currentLength1 = Utils.convertValue(
                0.0,
                durAudio1.toDouble(),
                0.0,
                mWidth.toDouble(),
                durAudio1.toDouble()
            )


            Log.d(TAG, "nmcode:  duration ${durAudio1.toDouble()}  -- dur2 ${durAudio2.toDouble()}  width  ${mWidth.toDouble()}")


            currentLength2 = (durAudio2.toDouble() / durAudio1.toDouble()) * currentLength1

//            Log.d(TAG, "setLengthAudio: currentLenght $currentLength1  -- dur2 $currentLength2")

        } else {
            currentLength2 = Utils.convertValue(
                0.0,
                durAudio2.toDouble(),
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
        if (currentLength1 + RADIUS > mWidth) {
            currentLength1 = mWidth - RADIUS.toDouble()
        }
        if (currentLength2 + RADIUS > mWidth) {
            currentLength2 = mWidth - RADIUS.toDouble()
        }

        invalidate()
    }

    fun setDuration(duration: String) {
        Log.d(TAG, "222: $duration")
        /**
        setjust the parameters
         */
//        this.duration = duration.toInt()
        numPos = DEFAULT_TIME_POSITION
        maxDistance =
            Utils.convertValue(
                0.0,
                this.duration.toDouble(),
                0.0,
                mWidth.toDouble(),
                duration.toDouble()
            )
        startCurrentX = 0
        endCurrentX = 0
        mCallback.changeDuration()
        invalidate()
    }


    fun seekNext5S(moreDuration: Int) {


        if (startCurrentX > maxDistance && endCurrentX > maxDistance) {
            startCurrentX = maxDistance.toInt()
            endCurrentX = maxDistance.toInt()
            position = duration
        } else {
            position += moreDuration
        }
        if (position > duration) {
            position = duration
        }

        mCallback.onLineChange(audioFile1, audioFile2, position)
        invalidate()
    }

    fun seekPrev5S(preDuration: Int) {
        if (startCurrentX <= RADIUS || endCurrentX <= RADIUS) {
            startCurrentX = RADIUS.toInt()
            endCurrentX = RADIUS.toInt()
        } else {
            position -= preDuration
        }
        if (position < 0) {
            position = 0
        }
        mCallback.onLineChange(audioFile1, audioFile2, position)
        invalidate()
    }

    fun setCurrent() {
        if (currentLength1 > currentLength2) {
            isChangeLengtDuration = 1
            maxLength = currentLength1
            currentLength1 = currentLength2
        } else if (currentLength1 < currentLength2) {
            isChangeLengtDuration = 2
            maxLength = currentLength2
            currentLength2 = currentLength1
        }
    }

    fun prevCurrentDuration() {
        if (isChangeLengtDuration != 0) {
            when (isChangeLengtDuration) {
                1 -> {
                    currentLength1 = maxLength
                }
                2 -> {
                    currentLength2 = maxLength
                }
            }
        }
    }


    interface OnPlayLineChange {
        fun onLineChange(audioFile1: AudioFile, audioFile2: AudioFile, pos: Int)
        fun pauseInvalid()
        fun changeDuration()
        fun setVolumeAudio1(x: Float, min1: Float, max1: Float)
        fun setVolumeAudio2(x: Float, min2: Float, max2: Float)
        fun endAudioAtMaxdistance()
    }

    enum class TOUCHITEM(num: Int) {
        NONTOUCH(0),
        SEEKBAR(1),
        SEEKBARSOUND1(2),
        SEEKBARSOUND2(3)
    }

}