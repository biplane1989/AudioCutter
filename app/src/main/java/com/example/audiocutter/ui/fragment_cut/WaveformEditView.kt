package com.example.audiocutter.ui.fragment_cut

import android.content.Context
import android.graphics.*
import android.media.MediaMetadataRetriever
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import androidx.dynamicanimation.animation.FlingAnimation
import androidx.dynamicanimation.animation.FloatValueHolder
import com.example.audiocutter.R
import com.example.audiocutter.ui.fragment_cut.WaveformLoader.Companion.get
import com.example.audiocutter.util.Utils
import kotlin.math.abs
import kotlin.math.sqrt

class WaveformEditView : View {
    private var widthView = 0
    private var heightView = 0
    private var waveformLineWidth = 0
    private var waveformLineSpace = 0
    private var waveformWidth = 0
    private var waveformDataIndex = 0
    private lateinit var waveformData: DoubleArray
    private var minScale = 0f
    private var scale = 0f
    private var translate = 0f
    private var lastScale = 0f
    private var waveformPaint: Paint? = null
    private var detector: GestureDetector? = null
    private var fling: FlingAnimation? = null
    private var distanceStart = 0f
    private var scaleStart = 0f
    private var translateStart = 0f
    private var startX = 0f
    private var selectRect: RectF? = null
    private var leftRect: RectF? = null
    private var rightRect: RectF? = null
    private var selectRectPaint: Paint? = null
    private var selectColor = 0
    private var selectTextColor = 0
    private var playLineRect: RectF? = null
    private var playLinePaint: Paint? = null
    private var playLineWidth = 0f
    private var cursorLeftRect: RectF? = null
    private var cursorRightRect: RectF? = null
    private var cursorPaint: Paint? = null
    private var cursorColorFilter: ColorFilter? = null
    private var cursorLeftBitmap: Bitmap? = null
    private var cursorRightBitmap: Bitmap? = null
    private var cursorSize = 0f
    private var cursorLeftPortion = 0f
    private var cursorRightPortion = 0f
    private var selectingCursor = 0
    private var selecting = false
    private var movingPlayLine = false
    private var startTimeMs: Long = 0
    private var endTimeMs: Long = 0
    private var trackDurationMs: Long = 0
    private var playPositionMs: Long = 0
    private var listener: WaveformEditListener? = null
    private var maxWaveHeight = 0.0
    private var centerLinePaint: Paint? = null
    private var timeMarkPaint: Paint? = null
    private var timeMarkTextMargin = 0f
    private var timeMarkNewAlpha = 0
    private var timeRange: Long = 0
    private var upperTimeRange: Long = 0
    private var lastTimeRangeIndex = -1
    private var a = 0
    private var scaleDefault = 0f
    private lateinit var paintLineSpace: Paint

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    private fun init(context: Context) {
        paintLineSpace = Paint(Paint.ANTI_ALIAS_FLAG)
        paintLineSpace.color = DEFAULT_LINE_SPACE_COLOR
        paintLineSpace.style = Paint.Style.FILL

        waveformLineWidth = dp2px(WAVEFORM_LINE_WITH)
        waveformLineSpace = dp2px(WAVEFORM_LINE_SPACE)
        waveformPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        waveformPaint!!.color = WAVEFORM_COLOR
        waveformPaint!!.style = Paint.Style.STROKE
        waveformPaint!!.strokeWidth = waveformLineWidth.toFloat()
        waveformPaint!!.strokeCap = Paint.Cap.ROUND
        waveformPaint!!.isAntiAlias = true
        centerLinePaint = Paint()
        centerLinePaint!!.color = CENTER_LINE_COLOR
        val centerLineWidth = Utils.dpToPx(context, CENTER_LINE_WIDTH)
        centerLinePaint!!.strokeWidth = centerLineWidth
        centerLinePaint!!.isAntiAlias = true
        selectColor = DEFAULT_SELECT_COLOR
        selectTextColor = DEFAULT_SELECT_TEXT_COLOR
        selectRectPaint = Paint()
        selectRectPaint!!.color = selectColor
        selectRectPaint!!.isAntiAlias = true
        selectRect = RectF()
        leftRect = RectF()
        rightRect = RectF()
        playLineRect = RectF()
        playLinePaint = Paint()
        playLinePaint!!.color = DEFAULT_PLAY_LINE_COLOR
        playLinePaint!!.isAntiAlias = true
        playLineWidth = Utils.dpToPx(getContext(), DEFAULT_PLAY_LINE_WIDTH_DP)
        playLinePaint!!.strokeWidth = playLineWidth
        cursorLeftRect = RectF()
        cursorRightRect = RectF()
        cursorLeftBitmap = BitmapFactory.decodeResource(resources, R.drawable.wave_view_left_ic)
        cursorRightBitmap = BitmapFactory.decodeResource(resources, R.drawable.wave_view_right_ic)
        cursorSize = Utils.dpToPx(getContext(), DEFAULT_CURSOR_SIZE_DP)
        cursorLeftPortion = 0f
        cursorRightPortion = 1f
        cursorPaint = Paint()
        cursorPaint!!.isAntiAlias = true
        cursorColorFilter = PorterDuffColorFilter(selectTextColor, PorterDuff.Mode.SRC_ATOP)
        timeMarkPaint = Paint()
        timeMarkPaint!!.isAntiAlias = true
        timeMarkPaint!!.textAlign = Paint.Align.CENTER
        timeMarkPaint!!.color = DEFAULT_SELECT_TEXT_COLOR
        timeMarkPaint!!.textSize = Utils.spToPx(
            getContext(),
            DEFAULT_TIME_TEXT_SIZE
        )
        timeMarkTextMargin = Utils.dpToPx(getContext(), DEFAULT_TIME_TEXT_MARGIN)
        setGestureDetector(context)
    }

    private fun initTimeMarks() {
        updateDividerTimeRange()
        upperTimeRange = timeRange
        invalidate()
    }

    fun setDataSource(path: String) {
        val duration = getDuration(path)
        trackDurationMs = duration
        invalidate()
        if (duration > WaveformLoader.PERIOD_IN_FRAMES) {
            val numberSample = (duration / WaveformLoader.PERIOD_IN_FRAMES).toInt()
            waveformDataIndex = 0
            waveformData = DoubleArray(numberSample)
            waveformWidth =
                numberSample * waveformLineWidth + (numberSample - 1) * waveformLineSpace
            Log.d(TAG, String.format("numberSample = %d", numberSample))
            initWaveformProperties()
            get()!!.extractor(path, object : WaveformLoader.OnWaveformLoaderCallBack {
                override fun onWaveformUpdate(db: DoubleArray?, chanelCount: Int) {
                    if (db != null) {
                        var i = 0
                        while (i < db.size) {
                            val value: Double = if (chanelCount == 2) {
                                (db[i] + db[i + 1]) / 2.0
                            } else {
                                db[i]
                            }
                            if (waveformDataIndex < waveformData.size) {
                                waveformData[waveformDataIndex] = value
                                waveformDataIndex++
                            }
                            i += chanelCount
                        }
                        invalidate()
                    }
                }

                override fun onWaveformFinish() {
                    invalidate()
                }
            })
        } else {
            waveformWidth = 0
        }
    }

    private fun setGestureDetector(context: Context) {
        detector = GestureDetector(context, object : SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (selectingCursor == 0) {
                    cancelFling()
                    fling = FlingAnimation(FloatValueHolder(translate))
                    fling!!.setStartVelocity(-velocityX)
                        .setMinValue(0f)
                        .setMaxValue(waveformWidth * scale - widthView)
                        .setFriction(1.1f)
                        .addUpdateListener { animation, value, velocity ->
                            translate = value
                            updateCursors()
                            invalidate()
                        }
                        .start()
                }
                return true
            }
        })
    }

    private fun getDuration(path: String): Long {
        return try {
            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(path)
            val durationStr =
                mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durationStr!!.toLong()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            0
        }
    }

    private fun initWaveformProperties() {
        if (widthView > 0 && waveformWidth > 0) {
            scale = widthView.toFloat() / waveformWidth.toFloat()
            minScale = scale
            lastScale = scale
            translate = 0f
            invalidate()
        }
    }

    private fun initDimensions() {
        maxWaveHeight = heightView - 4 * (timeMarkPaint!!.textSize + timeMarkTextMargin).toDouble()
        cursorLeftRect!![0f, Utils.dpToPx(context, 32f), cursorSize] =
            cursorSize + Utils.dpToPx(context, 32f)
        cursorRightRect!![widthView - cursorSize, heightView - cursorSize - Utils.dpToPx(
            context,
            32f
        ), widthView.toFloat()] =
            heightView.toFloat() - Utils.dpToPx(context, 32f)
        selectRect!![cursorLeftRect!!.left, (heightView / 2 - maxWaveHeight / 2).toFloat(), cursorRightRect!!.right] =
            heightView - cursorSize / 2
        playLineRect!![-cursorSize / 2, (heightView / 2 - maxWaveHeight / 2).toFloat(), cursorSize / 2] =
            heightView - cursorSize / 2
        initTimeMarks()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w != oldw || h != oldh) {
            widthView = w
            heightView = h
            initWaveformProperties()
            initDimensions()
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (widthView > 0 && heightView > 0 && waveformWidth > 0) {
            drawCenterLine(canvas)
            drawTimeMark(canvas)
            drawWaveform(canvas)
            drawSelectRect(canvas)
            drawPlayLine(canvas)
            drawSelectCursors(canvas)
            if (scaleDefault == 0f) {
                scaleDefault = scale
            }
        }
    }

    fun zoomOut() {
        if (scale < 1f) {
            scale = (scale + 0.1).toFloat()
            if (scale >= 1f) {
                scale = 1f
            }
        }
        updateCursors()
        updateStartTime()
        updateEndTime()
        updateDividerTimeRange()
        lastScale = scale
        invalidate()
    }

    fun zoomInt() {
        if (scale > scaleDefault) {
            scale = (scale - 0.1).toFloat()
            if (scale <= scaleDefault) {
                scale = scaleDefault
                translate = 0f
            }
            updateCursors()
            updateStartTime()
            updateEndTime()
            updateDividerTimeRange()
            lastScale = scale
            invalidate()
        }
    }

    private fun drawCenterLine(canvas: Canvas) {
        canvas.drawLine(
            0f,
            heightView / 2f,
            widthView.toFloat(),
            heightView / 2f,
            centerLinePaint!!
        )
    }

    private fun drawTimeMark(canvas: Canvas) {
        if (trackDurationMs > 0 && timeRange > 0) {
            var startX =
                (translate / (waveformWidth * scale) * trackDurationMs - timeRange).toLong()
            startX -= startX % timeRange
            var stopX =
                ((widthView + translate) / (waveformWidth * scale) * trackDurationMs + timeRange).toLong()
            stopX -= stopX % timeRange
            var i = startX
            a = 0
            while (i < stopX) {
                val x = i * 1f / trackDurationMs * waveformWidth * scale - translate
                timeMarkPaint!!.alpha =
                    if (i % upperTimeRange / 1000 == 0L) 255 else Math.abs(timeMarkNewAlpha)
                if (x >= 0) {
                    val fl = Utils.getWidthText(Utils.longMsToString(i), context) / 2
                    canvas.drawText(
                        Utils.longMsToString(i),
                        if (a == 0) x + fl else if (a == 1) x else x - fl,
                        selectRect!!.top - timeMarkTextMargin - timeMarkPaint!!.textSize,
                        timeMarkPaint!!
                    )
//                    canvas.drawLine(
//                        if (a == 0) x + fl else if (a == 1) x else x - fl,
//                        selectRect!!.top,
//                        if (a == 0) x + fl else if (a == 1) x else x - fl,
//                        selectRect!!.top + cursorSize / 2,
//                        timeMarkPaint!!
//                    )
                    a++
                }
                i += timeRange
            }
        }
    }

    private fun drawWaveform(canvas: Canvas) {
        val startIndex = (translate / scale / (waveformLineWidth + waveformLineSpace)).toInt()
        var endIndex =
            startIndex + (widthView / scale / (waveformLineWidth + waveformLineSpace).toFloat()).toInt()
        endIndex = Math.min(endIndex, waveformData.size)
        val step = (endIndex - startIndex).toFloat() / widthView.toFloat()
        if (step > 1) {
            var max: Double
            var k: Int
            var l: Int
            for (index in 0 until widthView) {
                max = 0.0
                k = (startIndex + index * step).toInt()
                l = (startIndex + (index + 1) * step).toInt()
                for (i in k until l) {
                    if (i < waveformDataIndex) {
                        val value = waveformData[i] - WaveformLoader.DB_MIN
                        if (value > max) {
                            max = value
                        }
                    }
                }
                if (max > 0) {
                    val lineHeight = calculatorLineHeight(max)
                    val startY = (heightView / 2f + lineHeight / 2f)
                    val stopY = (heightView / 2f - lineHeight / 2f)
                    waveformPaint!!.strokeWidth = 1f
                    canvas.drawLine(
                        index.toFloat(),
                        startY,
                        index.toFloat(),
                        stopY,
                        waveformPaint!!
                    )
                }
            }
        } else {
            canvas.save()
            canvas.translate(-translate, 0f)
            canvas.scale(scale, 1f, 0f, 0f)
            endIndex = if (endIndex > waveformDataIndex) waveformDataIndex else endIndex
            for (index in startIndex until endIndex) {
                val value = waveformData[index] - WaveformLoader.DB_MIN
                val lineHeight = calculatorLineHeight(value)
                val centerX =
                    index * (waveformLineWidth + waveformLineSpace) + waveformLineWidth / 2f
                val startY = (heightView / 2f + lineHeight / 2f)
                val stopY = (heightView / 2f - lineHeight / 2f)
                waveformPaint!!.strokeWidth = waveformLineWidth.toFloat()
                canvas.drawLine(centerX, startY, centerX, stopY, waveformPaint!!)
            }
            canvas.scale(1f, 1f, widthView / 2f, 0f)
            canvas.translate(0f, 0f)
            canvas.restore()
        }
    }

    private fun drawSelectCursors(canvas: Canvas) {
        timeMarkPaint!!.color = selectTextColor
        canvas.drawText(
            Utils.longDurationMsToStringMs((cursorLeftPortion * trackDurationMs).toLong()),
            selectRect!!.left,
            selectRect!!.top - timeMarkTextMargin,
            timeMarkPaint!!
        )
        canvas.drawText(
            Utils.longDurationMsToStringMs((cursorRightPortion * trackDurationMs).toLong()),
            selectRect!!.right,
            selectRect!!.top - timeMarkTextMargin,
            timeMarkPaint!!
        )
//        cursorPaint!!.colorFilter = if (selectingCursor == 1) cursorColorFilter else null
        canvas.drawBitmap(cursorLeftBitmap!!, null, cursorLeftRect!!, cursorPaint)
//        cursorPaint!!.colorFilter = if (selectingCursor == 2) cursorColorFilter else null
        canvas.drawBitmap(cursorRightBitmap!!, null, cursorRightRect!!, cursorPaint)
    }

    private fun drawSelectRect(canvas: Canvas) {
//        canvas.drawRect(selectRect!!, selectRectPaint!!)
        leftRect!!.set(
            0f,
            selectRect!!.top,
            selectRect!!.left,
            selectRect!!.bottom - cursorSize / 2f
        )
        rightRect!!.set(
            selectRect!!.right,
            selectRect!!.top,
            measuredWidth.toFloat(),
            selectRect!!.bottom - cursorSize / 2f
        )
        canvas.drawRect(leftRect!!, selectRectPaint!!)
        canvas.drawRect(rightRect!!, selectRectPaint!!)

        canvas.drawLine(
            leftRect!!.right,
            leftRect!!.top,
            leftRect!!.right,
            leftRect!!.bottom,
            paintLineSpace
        )
        canvas.drawLine(
            rightRect!!.left,
            rightRect!!.top,
            rightRect!!.left,
            rightRect!!.bottom,
            paintLineSpace
        )
    }

    private fun drawPlayLine(canvas: Canvas) {
        playLinePaint!!.color =
            if (movingPlayLine) Color.WHITE else DEFAULT_PLAY_LINE_COLOR
        canvas.drawLine(
            playLineRect!!.centerX(),
            playLineRect!!.top,
            playLineRect!!.centerX(),
            playLineRect!!.bottom - cursorSize / 2f,
            playLinePaint!!
        )
        canvas.drawLine(
            playLineRect!!.centerX(),
            playLineRect!!.top,
            playLineRect!!.centerX(),
            playLineRect!!.bottom - cursorSize / 2f,
            playLinePaint!!
        )
    }

    private fun cancelFling() {
        if (fling != null) {
            fling!!.cancel()
            fling = null
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchCount = event.pointerCount
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_1_DOWN, MotionEvent.ACTION_POINTER_2_DOWN -> {
                selecting = false
                movingPlayLine = false
                cancelFling()
                if (touchCount >= 2) {
                    distanceStart =
                        distance(event.getX(0), event.getX(1), event.getY(0), event.getY(1))
                    scaleStart = scale
                    translateStart = -1f
                } else {
                    if (!checkSelecting(event) && !checkMovingPlayLine(event)) {
                        translateStart = translate
                        startX = event.x
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> if (touchCount >= 2) {
                Log.e(TAG, "onTouchEvent0: $touchCount")
                val centerPointX = (event.getX(0) + event.getX(1)) / 2f
                val centerPointWaveformX = (centerPointX + translate) / scale
                val distanceEnd =
                    distance(event.getX(0), event.getX(1), event.getY(0), event.getY(1))
                val tmpScale = Math.min(1f, distanceEnd / distanceStart * scaleStart)
                if (tmpScale < minScale) {
                    Log.e(TAG, "onTouchEvent1: $touchCount")
                    scale = minScale
                    translate = 0f
                    updateCursors()
                } else {
                    Log.e(TAG, "onTouchEvent2: $touchCount")
                    scale = tmpScale
                    val tempTranslate = centerPointWaveformX * scale - centerPointX
                    correctTranslate(tempTranslate)
                }
                updateDividerTimeRange()
                lastScale = scale
                invalidate()
                return true
            } else {
                if (selecting) {
                    changeSelectRange(event)
                } else if (movingPlayLine) {
                    movePlayLine(event)
                } else {
                    if (translateStart >= 0) {
                        val distance = startX - event.x
                        val tempTranslate = translateStart + distance
                        correctTranslate(tempTranslate)
                        invalidate()
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                selectingCursor = 0
                selecting = false
                movingPlayLine = false
            }
        }
        if (selectingCursor == 0 && detector != null) detector?.onTouchEvent(event)
        return true
    }

    private fun checkSelecting(event: MotionEvent): Boolean {
        if (cursorLeftRect!!.contains(event.x, event.y)) {
            selectingCursor = 1
            selecting = true
        } else if (cursorRightRect!!.contains(event.x, event.y)) {
            selectingCursor = 2
            selecting = true
        } else {
            selecting = false
        }
        return selecting
    }

    private fun checkMovingPlayLine(event: MotionEvent): Boolean {
        movingPlayLine = playLineRect!!.contains(event.x, event.y)
        return movingPlayLine
    }

    private fun changeSelectRange(event: MotionEvent) {
        if (selectingCursor == 1) {
            if (event.x - cursorSize / 2 >= 0 && event.x + cursorSize / 2 <= cursorRightRect!!.right) {
                cursorLeftRect!![event.x - cursorSize / 2, +Utils.dpToPx(
                    context,
                    32f
                ), event.x + cursorSize / 2] =
                    cursorSize + Utils.dpToPx(context, 32f)
                cursorLeftPortion = (cursorLeftRect!!.left + translate) / scale / waveformWidth
                updateStartTime()
                invalidate()
            }
        } else if (selectingCursor == 2) {
            if (event.x + cursorSize / 2 <= widthView && event.x - cursorSize / 2 >= cursorLeftRect!!.left) {
                cursorRightRect!![event.x - cursorSize / 2, heightView - cursorSize - Utils.dpToPx(
                    context,
                    32f
                ), event.x + cursorSize / 2] =
                    heightView.toFloat() - Utils.dpToPx(
                        context,
                        32f
                    )
                cursorRightPortion = (cursorRightRect!!.right + translate) / scale / waveformWidth
                updateEndTime()
                invalidate()
            }
        }
        selectRect!![cursorLeftRect!!.left, selectRect!!.top, cursorRightRect!!.right] =
            heightView - cursorSize / 2
        invalidate()
    }

    private fun movePlayLine(event: MotionEvent) {
        playLineRect!![event.x - cursorSize / 2, (heightView / 2 - maxWaveHeight / 2).toFloat(), event.x + cursorSize / 2] =
            heightView - cursorSize / 2
        playPositionMs =
            ((playLineRect!!.centerX() + translate) / scale / waveformWidth * trackDurationMs).toLong()
        invalidate()
        if (listener != null) {
            listener!!.onPlayPositionChanged(playPositionMs.toInt())
        }
    }

    private fun updateStartTime() {
        startTimeMs =
            (abs(cursorLeftRect!!.left + translate) / scale / waveformWidth * trackDurationMs).toLong()
        if (listener != null) {
            listener!!.onStartTimeChanged(startTimeMs)
        }
    }

    private fun updateEndTime() {
        endTimeMs =
            (abs(cursorRightRect!!.right + translate) / scale / waveformWidth * trackDurationMs).toLong()
        if (listener != null) {
            listener!!.onEndTimeChanged(endTimeMs)
        }
    }

    private fun calculatorLineHeight(value: Double): Float {
        return if (value > 35f) ((value - 35f) / (WaveformLoader.DB_MIN + 35f) * maxWaveHeight).toFloat() else 0f
    }

    private fun correctTranslate(tempTranslate: Float) {
        translate = if (tempTranslate < 0) {
            0f
        } else if (tempTranslate > waveformWidth * scale - widthView) {
            waveformWidth * scale - widthView
        } else {
            tempTranslate
        }
        updateCursors()
        updateStartTime()
        updateEndTime()
    }

    private fun updateCursors() {
        val cursorLeftX = cursorLeftPortion * waveformWidth * scale - translate
        cursorLeftRect!![cursorLeftX, +Utils.dpToPx(context, 32f), cursorLeftX + cursorSize] =
            cursorSize + Utils.dpToPx(context, 32f)
        val cursorRightX = cursorRightPortion * waveformWidth * scale - translate
        cursorRightRect!![cursorRightX - cursorSize, cursorRightRect!!.top, cursorRightX] =
            cursorRightRect!!.bottom
        selectRect!![cursorLeftRect!!.left, selectRect!!.top, cursorRightRect!!.right] =
            heightView - cursorSize / 2
        val playLineX = playPositionMs * 1f / trackDurationMs * waveformWidth * scale - translate
        playLineRect!![playLineX - cursorSize / 2, (heightView / 2 - maxWaveHeight / 2).toFloat(), playLineX + cursorSize / 2] =
            heightView - cursorSize / 2
    }

    private fun distance(x0: Float, x1: Float, y0: Float, y1: Float): Float {
        val x = x0 - x1
        val y = y0 - y1
        return sqrt(x * x + y * y.toDouble()).toFloat()
    }

    private fun dp2px(dp: Float): Int {
        return (resources.displayMetrics.density * dp + 0.5f).toInt()
    }

    fun setStartTimeMs(startTimeMs: Int) {
        this.startTimeMs = startTimeMs.toLong()
        cursorLeftPortion = startTimeMs * 1f / trackDurationMs
        updateCursors()
    }

    fun setEndTimeMs(endTimeMs: Int) {
        this.endTimeMs = endTimeMs.toLong()
        cursorRightPortion = endTimeMs * 1f / trackDurationMs
        updateCursors()
    }

    fun setListener(listener: WaveformEditListener?) {
        this.listener = listener
    }

    private fun cancelLoad() {
        get()!!.cancel()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancelLoad()
    }

    fun setPlayPositionMs(currentPositionMs: Int) {
        if (!movingPlayLine && currentPositionMs > 0) {
            playPositionMs = currentPositionMs.toLong()
            val x = playPositionMs * 1f / trackDurationMs * waveformWidth * scale - translate
            playLineRect!![x - cursorSize / 2, (heightView / 2 - maxWaveHeight / 2).toFloat(), x + cursorSize / 2] =
                heightView - cursorSize / 2
            invalidate()
            Log.d(
                TAG,
                "setPlayPositionMs: $playPositionMs   ===== $trackDurationMs ==== $x  ===  $translate"
            )
        }
    }

    private fun calculateDivideTimeRange(scaledDuration: Double): Int {
        return if (scaledDuration > TIME_RANGE_STEPS[0] * 2) {
            0
        } else if (scaledDuration > TIME_RANGE_STEPS[1] * 2) {
            1
        } else if (scaledDuration > TIME_RANGE_STEPS[2] * 2) {
            2
        } else if (scaledDuration > TIME_RANGE_STEPS[3] * 2) {
            3
        } else if (scaledDuration > TIME_RANGE_STEPS[4] * 2) {
            4
        } else if (scaledDuration > TIME_RANGE_STEPS[5] * 2) {
            5
        } else if (scaledDuration > TIME_RANGE_STEPS[6] * 2) {
            6
        } else if (scaledDuration > TIME_RANGE_STEPS[7] * 2) {
            7
        } else if (scaledDuration > TIME_RANGE_STEPS[8] * 2) {
            8
        } else if (scaledDuration > TIME_RANGE_STEPS[9] * 2) {
            9
        } else {
            10
        }
    }

    private fun updateDividerTimeRange() {
        val portion = waveformWidth * scale / widthView
        val scaledTime = trackDurationMs / portion.toDouble()
        val stepIndex = calculateDivideTimeRange(scaledTime)
        val tmpTimeRange = TIME_RANGE_STEPS[stepIndex]
        if (stepIndex < 1) {
            timeRange = TIME_RANGE_STEPS[stepIndex]
            upperTimeRange = timeRange
            timeMarkNewAlpha = 255
            lastTimeRangeIndex = -1
        } else {
            if (stepIndex != lastTimeRangeIndex) {
                upperTimeRange = TIME_RANGE_STEPS[stepIndex - 1]
                timeRange = tmpTimeRange
                lastTimeRangeIndex = stepIndex
                timeMarkNewAlpha = if (lastScale < scale) 0 else 255
            } else {
                if (lastScale < scale) {
                    if (timeMarkNewAlpha < 255) {
                        timeMarkNewAlpha++
                    }
                } else {
                    if (timeMarkNewAlpha > 0) {
                        timeMarkNewAlpha--
                    }
                }
            }
        }
    }

    interface WaveformEditListener {
        fun onStartTimeChanged(startTimeMs: Long)
        fun onEndTimeChanged(endTimeMs: Long)
        fun onPlayPositionChanged(positionMs: Int)
    }

    companion object {
        val TAG = WaveformEditView::class.java.simpleName
        val TIME_RANGE_STEPS = longArrayOf(
            1800000,
            900000,
            600000,
            300000,
            120000,
            60000,
            30000,
            10000,
            5000,
            2500,
            1250
        )
        const val WAVEFORM_LINE_WITH = 2f //dp
        const val WAVEFORM_LINE_SPACE = 1f //dp
        const val CENTER_LINE_WIDTH = 1f //dp
        val WAVEFORM_COLOR = Color.parseColor("#FDAA74")
        const val CENTER_LINE_COLOR = -0xf6f6f7 //dp
        const val DEFAULT_CURSOR_SIZE_DP = 28f //in dp
        val DEFAULT_SELECT_COLOR = Color.parseColor("#66FFFFFF")
        const val DEFAULT_TIME_TEXT_SIZE = 12f //sp
        const val DEFAULT_PLAY_LINE_WIDTH_DP = 1f //dp
        const val DEFAULT_TIME_TEXT_MARGIN = 4f //sp
        private val DEFAULT_SELECT_TEXT_COLOR = Color.parseColor("#707489")
        private val DEFAULT_PLAY_LINE_COLOR = Color.parseColor("#D81B60")
        private val DEFAULT_LINE_SPACE_COLOR = Color.parseColor("#D8D8D8")
    }
}