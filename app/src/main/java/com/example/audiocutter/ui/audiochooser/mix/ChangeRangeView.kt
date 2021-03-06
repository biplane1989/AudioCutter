package com.example.audiocutter.ui.audiochooser.mix

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.example.audiocutter.R
import com.example.audiocutter.objects.AudioFile
import com.example.audiocutter.util.Utils


class ChangeRangeView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    private val RADIUS_RECT = 15f

    private val DEFAULT_TIME_POSITION: String = "00:00.0"
    private var ratio: Int = 0
    private lateinit var rectImageDst2: Rect
    private lateinit var rectImageDst1: Rect
    private lateinit var rectImage: Rect
    private var typeFace = ResourcesCompat.getFont(context, R.font.opensans_regular)!!
    private lateinit var rectText2: Rect
    private lateinit var rectText1: Rect
    private val TAG = ChangeRangeView::class.java.name
    private var rect: Rect
    private var durationAudio2: Int = 0
    private var durationAudio1: Int = 0
    private var duration: Int = 0
    private var durationTmp: Int = 0
    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mPaint2 = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mPaint4 = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mPaint5 = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mPaint6 = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mPaint7 = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mHeight = 0
    var mWidth = 0
    private lateinit var rectDuration1: RectF
    private lateinit var rectDuration2: RectF

    private lateinit var rectSeekbarSound1: RectF
    private lateinit var rectSeekbarSound2: RectF
    private lateinit var rectProgressSeekbar1: RectF
    private lateinit var rectProgressSeekbar2: RectF
    private var currentLineX = 0
    private var currentLineTextPos = 0
    private lateinit var audioFile1: AudioFile
    private lateinit var audioFile2: AudioFile
    lateinit var mCallback: OnPlayLineChange
    private var isTouch = TOUCHITEM.NONTOUCH
    private var rs = false
    private var isChangeNumpos = true
    private var numPos = ""
    private var RADIUS = Utils.convertDp2Px(9, context)
    private var RANGE = Utils.convertDp2Px(20, context)
    private var mHeightText = 0
    private var currentLengthSound1 = RADIUS.toDouble()
    private var currentLengthSound2 = RADIUS.toDouble()
    private var maxLength = RADIUS.toDouble()
    private var textGetX = 0f

    /*    private var rangeCircleProgress1 = 0f
        private var rangeCircleProgress2 = 0f*/
    private var circleProgressY1 = 0f
    private var circleProgressY2 = 0f
    private val DISTANCE = Utils.convertDp2Px(20, context)
    private var currentXCircleVolume1 = 0f
    private var currentXCircleVolume2 = 0f
    private var isChangeLengtDuration = 0
    private var textName1 = ""
    private var textName2 = ""
    private var ratioSound1 = "100%"
    private var ratioSound2 = "100%"
    private var maxDistance = 0.0
    private var position = 0



    init {
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
        currentXCircleVolume1 = mWidth / 1.3f + RANGE
        currentXCircleVolume2 = mWidth / 1.3f + RANGE
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
            currentLengthSound1.toFloat() + RADIUS,
            mHeight / 2 - DISTANCE
        )

        rectDuration2 = RectF(
            RADIUS,
            mHeightText + rectDuration1.height() + Utils.convertDp2Px(10, context),
            currentLengthSound2.toFloat() + RADIUS,
            (mHeight / 2 - DISTANCE) * 2 - Utils.convertDp2Px(10, context)
        )

        canvas.drawRoundRect(rectDuration1, RADIUS_RECT, RADIUS_RECT, mPaint2)
        canvas.drawRoundRect(rectDuration2, RADIUS_RECT, RADIUS_RECT, mPaint2)

        drawTextName(canvas, textName1)
        drawTextName2(canvas, textName2)

        drawBitmap(canvas, rectDuration1.top + rectText1.height() + RANGE * 3 / 2)
        drawBitmap2(canvas, rectDuration2.top + rectText2.height() + RANGE * 3 / 2)

        rectSeekbarSound1 = RectF(rectImageDst1.width() + RANGE * 2, rectImageDst1.top + RADIUS, mWidth / 1.3f + RANGE, rectImageDst1.top + RADIUS + 10)

        rectSeekbarSound2 = RectF(rectImageDst2.width() + RANGE * 2, rectImageDst2.top + RADIUS, mWidth / 1.3f + RANGE, rectImageDst2.top + RADIUS + 10)

        if (currentXCircleVolume1 == 0f || currentXCircleVolume2 == 0f) {
            currentXCircleVolume1 = (rectImageDst1.width() + RANGE * 2)
            currentXCircleVolume2 = (rectImageDst1.width() + RANGE * 2)
//            currentXCircle1 =mWidth / 1.3f + RANGE
//            currentXCircle2 = mWidth / 1.3f + RANGE
        }
        rectProgressSeekbar1 = RectF(rectImageDst1.width() + RANGE * 2, rectImageDst1.top + RADIUS, currentXCircleVolume1, rectImageDst1.top + RADIUS + 10)
        rectProgressSeekbar2 = RectF(rectImageDst2.width() + RANGE * 2, rectImageDst2.top + RADIUS, currentXCircleVolume2, rectImageDst2.top + RADIUS + 10)
        canvas.drawRoundRect(rectSeekbarSound1, 5f, 5f, mPaint5)
        canvas.drawRoundRect(rectSeekbarSound2, 5f, 5f, mPaint5)
        canvas.drawRoundRect(rectProgressSeekbar1, 5f, 5f, mPaint)
        canvas.drawRoundRect(rectProgressSeekbar2, 5f, 5f, mPaint)

        /**draw circle of seekbar audio file**/
        canvas.drawCircle(currentXCircleVolume1, rectImageDst1.top + RADIUS + 3, RADIUS, mPaint)
        canvas.drawCircle(currentXCircleVolume2, rectImageDst2.top + RADIUS + 3, RADIUS, mPaint)

        /*rangeCircleProgress1 = rectImageDst1.width() + RANGE * 2
        rangeCircleProgress2 = rectImageDst2.width() + RANGE * 2*/
        circleProgressY1 = rectImageDst1.top + RADIUS + 3
        circleProgressY2 = rectImageDst2.top + RADIUS + 3

        drawTextSound(ratioSound1, canvas, rectSeekbarSound1.right + RANGE, rectSeekbarSound1)
        drawTextSound(ratioSound2, canvas, rectSeekbarSound2.right + RANGE, rectSeekbarSound2)
    }

    private fun initLine(canvas: Canvas) {
        /** draw line seekbar file audio custom
         *          draw circle change folow event touchmove
         *          draw text duration audio
         *          draw text change position
         **/
        canvas.drawLine(
            currentLineX.toFloat() + RADIUS,
            0f,
            currentLineX.toFloat() + RADIUS,
            mHeight * 1f - (RANGE + RADIUS),
            mPaint
        )
        canvas.drawCircle(
            currentLineX.toFloat() + RADIUS,
            (mHeight * 1f) - (RANGE + RADIUS),
            RADIUS,
            mPaint
        )

        drawTextDurationMax(canvas, DEFAULT_TIME_POSITION, 0f + RADIUS, mPaint6)
        try {
            rs = durationAudio1 > durationAudio2
            if (!rs) {
                drawTextDurationMax(
                    canvas,
                    getTimeAudio(durationAudio2.toLong()),
                    getRightTextDurationMax(durationAudio2.toLong()),
                    mPaint6
                )
                drawTextDurationMin(
                    canvas,
                    getTimeAudio(audioFile1.duration)
                )
            } else {
                drawTextDurationMax(
                    canvas,
                    getTimeAudio(durationAudio1.toLong()),
                    getRightTextDurationMax(durationAudio1.toLong()),
                    mPaint6
                )
                drawTextDurationMin(
                    canvas, getTimeAudio(audioFile2.duration)
                )

            }
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }

        /**text number position*/


        mPaint.getTextBounds(numPos, 0, numPos.length, rect)
        /***check currentPosision max distance**/
        currentLineTextPos = getCurrentLineTextPos()
        drawTextPosition(
            canvas,
            numPos,
            currentLineTextPos.toFloat() - rect.width() / 2.5f,
            mPaint
        )

    }


    private fun drawTextSound(ratioSound1: String, canvas: Canvas, x: Float, y: RectF) {
        canvas.drawText(ratioSound1, x, y.top + RADIUS / 2.1f, mPaint6)
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
        canvas.drawText(name, RADIUS * 2, rectDuration2.top + rectText2.height() + RADIUS, mPaint4)
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


    private fun getRightTextDurationMax(duration: Long): Float {
        return if (Utils.chooseTimeFormat(duration) == Utils.TIME_FORMAT_INCLUDED_HOUR_TWO_ZERO) {
            mWidth - RANGE * 3
        } else {
            mWidth - RANGE * 2
        }

    }

    private fun getCurrentLineTextPos(): Int {
        var index: Int = 0
        if (
            (currentLineX + rect.width() / 2) < mWidth) {
            index = currentLineX
        }
        if (
            (currentLineX + rect.width() / 2) >= mWidth) {
            index = mWidth - rect.width() / 2
        }
        if ((currentLineX - rect.left) < RADIUS * 2) {
            index = (RADIUS * 2.2).toInt()
        }

        return index
    }


    /* @SuppressLint("SimpleDateFormat")
     private fun convertTime(time: Int): String {
         if (time < 0) return "00:00.0"
         val df = SimpleDateFormat("mm:ss.S")
         return df.format(time)
     }*/


    private fun drawBitmap(canvas: Canvas, fl: Float) {
        val imageSound = BitmapFactory.decodeResource(resources, R.drawable.ic_sound_mixing)
        rectImage = Rect(0, 0, imageSound.width, imageSound.height)
        rectImageDst1 = Rect(
            (RADIUS * 2).toInt(),
            fl.toInt(),
            (RADIUS * 2 + RANGE).toInt(),
            (fl + RANGE).toInt()
        )
        canvas.drawBitmap(imageSound, rectImage, rectImageDst1, null)
    }

    private fun drawBitmap2(canvas: Canvas, fl: Float) {
        val imageSound = BitmapFactory.decodeResource(resources, R.drawable.ic_sound_mixing)
        rectImage = Rect(0, 0, imageSound.width, imageSound.height)
        rectImageDst2 = Rect(
            (RADIUS * 2).toInt(),
            fl.toInt(),
            (RADIUS * 2 + RANGE).toInt(),
            (fl + RANGE).toInt()
        )
        canvas.drawBitmap(imageSound, rectImage, rectImageDst2, null)
    }

    private fun drawTextDurationMax(canvas: Canvas, text: String, getX: Float, mPaint: Paint) {
        mPaint.getTextBounds(text, 0, text.length, rect)
        canvas.drawText(text, getX, rect.height().toFloat(), mPaint)

    }

    private fun drawTextPosition(canvas: Canvas, text: String, pos: Float, mPaint: Paint) {
//        mPaint.getTextBounds(text, 0, text.length, rect)
        canvas.drawText(text, pos, mHeight * 1f - 3, mPaint)

    }

    private fun drawTextDurationMin(canvas: Canvas, text: String) {
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
        } else if (textGetX >= (mWidth - rect.width() - (RADIUS * 4))) {
            textGetX = mWidth - rect.width() * 2 - RANGE
        }
        canvas.drawText(text, textGetX, rect.height().toFloat(), mPaint6)
    }

    fun getW(): Int {
        return mWidth
    }


    private fun checkRangeRatio(ratio: Int) {
        if (ratio > 100) {
            this.ratio = 100
        } else if (ratio < 0) {
            this.ratio = 0
        }
    }

    private fun getDurationMin(): Int {
        return if (durationAudio1 > durationAudio2) {
            audioFile2.duration.toInt()
        } else {
            audioFile1.duration.toInt()
        }
    }

    private fun drawLineTouch(x: Float) {
        try {
            currentLineX = x.toInt()
            if (currentLineX > maxDistance) {
                currentLineX = maxDistance.toInt()
            }
            val value = Utils.convertValue(
                0.0,
                mWidth - RADIUS.toDouble(),
                0.0,
                duration.toDouble(),
                currentLineX.toDouble()
            )
            numPos = getTimeAudio(value = value.toLong())
            invalidate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getTimeAudio(value: Long): String {
        return Utils.toTimeStrToMiliSecond(
            value, Utils.chooseTimeFormat(value)
        )
    }


/*
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val count = Utils.convertDp2Px(18, context)
        setPadding(count.toInt(), 0, 20, 0)
    }
*/

    fun setPosition(position: Int) {
        val pos = Utils.convertValue(0.0, duration.toDouble(), 0.0, mWidth - RADIUS.toDouble(), position.toDouble())
        this.position = position
        currentLineX = pos.toInt()

        /**check pos>maxdistance ->pauseAudio*/
        if (currentLineX > maxDistance) {
            currentLineX = maxDistance.toInt()
            mCallback.endAudioAtMaxdistance()
            isChangeNumpos = false
        }

        if (currentLineX >= mWidth - RADIUS) {
            currentLineX = mWidth - RADIUS.toInt()
            isChangeNumpos = false
        }

        numPos = getTimeAudio(this.position.toLong())
        if (isChangeNumpos) {
            invalidate()
        }
        isChangeNumpos = true
    }

    fun setFileAudio(audioFile1: AudioFile, audioFile2: AudioFile) {
        this.audioFile1 = audioFile1
        this.audioFile2 = audioFile2

        durationAudio1 = audioFile1.duration.toInt()
        durationAudio2 = audioFile2.duration.toInt()
        val rs = durationAudio1 > durationAudio2
        duration = if (!rs) {
            audioFile2.duration.toInt()
        } else {
            audioFile1.duration.toInt()
        }
        durationTmp = duration
    }

    fun setLengthAudio(durAudio1: String?, durAudio2: String?) {

        val isCompare = durAudio1!!.toInt() > durAudio2!!.toInt()
        if (isCompare) {
            currentLengthSound1 = convertValueDuration(durAudio1)
            currentLengthSound2 =
                (durAudio2.toDouble() / durAudio1.toDouble()) * currentLengthSound1

        } else {
            currentLengthSound2 = convertValueDuration(durAudio2)
            currentLengthSound1 =
                (durAudio1.toDouble() / durAudio2.toDouble()) * currentLengthSound2
        }
        if (currentLengthSound1 + RADIUS > mWidth) {
            currentLengthSound1 = mWidth - RADIUS.toDouble()
        }
        if (currentLengthSound2 + RADIUS > mWidth) {
            currentLengthSound2 = mWidth - RADIUS.toDouble()
        }

        invalidate()
    }

    private fun convertValueDuration(duration: String): Double {
        return Utils.convertValue(
            0.0,
            duration.toDouble(),
            0.0,
            mWidth.toDouble(),
            duration.toDouble()
        )
    }


    fun setDuration(duration: String) {
        /**
        setjust the parameters
         */
//        this.duration = duration.toInt()
        numPos = DEFAULT_TIME_POSITION
        maxDistance = Utils.convertValue(
            0.0,
            this.duration.toDouble(),
            0.0,
            mWidth.toDouble(),
            duration.toDouble()
        )
        currentLineX = 0
        mCallback.changeDuration()
        invalidate()
    }

    fun setShortedLength() {

        position = 0
        durationTmp = getDurationMin()
        if (currentLengthSound1 > currentLengthSound2) {
            isChangeLengtDuration = 1
            maxLength = currentLengthSound1
            currentLengthSound1 = currentLengthSound2

        } else if (currentLengthSound1 < currentLengthSound2) {
            isChangeLengtDuration = 2
            maxLength = currentLengthSound2
            currentLengthSound2 = currentLengthSound1


        }
    }

    fun setLonggestLenght() {
        position = 0
        durationTmp = duration
        if (isChangeLengtDuration != 0) {
            when (isChangeLengtDuration) {
                1 -> {
                    currentLengthSound1 = maxLength
                }
                2 -> {
                    currentLengthSound2 = maxLength
                }
            }
        }
    }

    fun seekNext5S(moreDuration: Int) {

        if (currentLineX > maxDistance) {
            currentLineX = maxDistance.toInt()
            position = durationTmp
        } else if (isChangeNumpos) {
            position += moreDuration
        }
        if (position > durationTmp) {
            position = durationTmp
        }
        if (position < durationTmp) {
            invalidate()
            mCallback.onLineChange(audioFile1, audioFile2, position)
        }

    }

    fun seekPrev5S(preDuration: Int) {
        if (currentLineX <= RADIUS || currentLineX <= RADIUS) {
            currentLineX = RADIUS.toInt()
        } else {
            position -= preDuration
        }
        if (position < 0) {
            position = 0
        }
        mCallback.onLineChange(audioFile1, audioFile2, position)
        invalidate()
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
                        currentXCircleVolume1 = x
                        if (x < (rectImageDst1.width() + RANGE * 2)) {
                            currentXCircleVolume1 = (rectImageDst1.width() + RANGE * 2)
                        } else if (x > (mWidth / 1.3f + RANGE)) {
                            currentXCircleVolume1 = (mWidth / 1.3f + RANGE)
                        }
                        mCallback.setVolumeAudio1(
                            x,
                            (rectImageDst1.width() + RANGE * 2),
                            (mWidth / 1.3f + RANGE)
                        )
                        val newValueSound = Utils.convertValue(
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
                        currentXCircleVolume2 = x
                        if (x < (rectImageDst2.width() + RANGE * 2)) {
                            currentXCircleVolume2 = (rectImageDst2.width() + RANGE * 2)
                        } else if (x > (mWidth / 1.3f + RANGE)) {
                            currentXCircleVolume2 = (mWidth / 1.3f + RANGE)
                        }
                        mCallback.setVolumeAudio2(
                            x,
                            (rectImageDst2.width() + RANGE * 2),
                            (mWidth / 1.3f + RANGE)
                        )
                        val newValueSound = Utils.convertValue(
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
                    else -> {
                        //do nothing
                    }
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                when (isTouch) {
                    TOUCHITEM.SEEKBAR -> {
                        isTouch = TOUCHITEM.NONTOUCH
                        val pos = Utils.convertValue(
                            0.0,
                            mWidth - RADIUS.toDouble(),
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
                } else if (event.y < circleProgressY1 + RANGE && event.y > circleProgressY1 - RANGE) {
                    isTouch = TOUCHITEM.SEEKBARSOUND1
                    true
                } else if (event.y < circleProgressY2 + RANGE && event.y > circleProgressY2 - RANGE) {
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