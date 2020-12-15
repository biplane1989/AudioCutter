package com.example.waveform.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Color.parseColor
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.waveform.Utils
import com.example.waveform.soundfile.AudioDecoder
import com.example.waveform.soundfile.AudioDecoderBuilder
import com.example.waveform.soundfile.ProgressListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WaveformView1 : View, ProgressListener {
    companion object {
        val WAVEFORM_COLOR = parseColor("#FDAA74")
        const val WAVEFORM_LINE_WITH = 2f //dp
        const val DEFAULT_CURSOR_SIZE_DP = 28f
        const val DEFAULT_RANGE_TIME_TEXT_SIZE = 12f
        const val DEFAULT_RANGE_DURATION_TEXT_SIZE = 13f
        const val MIN_RANGE_SELECTION_IN_SECONDS = 1
        val DEFAULT_RANGE_SELECTION_COLOR = parseColor("#66FFFFFF")
        val DEFAULT_LINE_SELECTION_COLOR = parseColor("#D8D8D8")
        val DEFAULT_LINE_PLAYING_COLOR = parseColor("#FF6161")
        val DEFAULT_SELECT_TEXT_COLOR = parseColor("#707489")
        const val RANGE_SELECTION_TOTAL_INTERVAL = 6

    }

    private lateinit var mWaveformDrawer: WaveformDrawer
    private lateinit var mRangeDrawer: RangeDrawer
    private var waveformLineWidth = 0f
    private val mWaveformViewListener: WaveformViewListener? = null
    private var playPositionMs: Long = 0
    private var mFPS = 0
    private var mDuration: Long = 0
    private var mAudioDecoder: AudioDecoder? = null
    private lateinit var mTouchEventHandler: WaveformTouchEventHandler
    private var isDettachedWindow: Boolean = false
    override fun reportProgress(fractionComplete: Double): Boolean {
        return !isDettachedWindow
    }

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

    fun isInitialized(): Boolean {
        return mWaveformDrawer.isInitialized
    }

    private fun init(context: Context) {
        waveformLineWidth = Utils.dpToPx(context, WAVEFORM_LINE_WITH)
        mWaveformDrawer = WaveformDrawer(this, WAVEFORM_COLOR, DEFAULT_LINE_PLAYING_COLOR)
        mTouchEventHandler = WaveformTouchEventHandler(this)
        mRangeDrawer =
            RangeDrawer(
                this,
                DEFAULT_RANGE_SELECTION_COLOR,
                DEFAULT_LINE_SELECTION_COLOR,
                DEFAULT_SELECT_TEXT_COLOR
            )
    }

    private fun isReadyToDraw(): Boolean {
        return width > 0 && height > 0
    }

    fun zoomIn() {
        if (!isInitialized()) {
            return
        }
        val factor = mWaveformDrawer.zoomIn()
        if (factor != -1f) {
            mTouchEventHandler.onWaveformZoomIn(factor)
            setOffset(getOffset())
            mRangeDrawer.onWaveformZoomIn(factor)
            invalidate()
        }

    }

    fun zoomOut() {
        if (!isInitialized()) {
            return
        }
        val factor = mWaveformDrawer.zoomOut()
        if (factor != -1f) {
            mTouchEventHandler.onWaveformZoomOut(factor)
            setOffset(getOffset())
            mRangeDrawer.onWaveformZoomOut(factor)
            invalidate()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mTouchEventHandler.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event)

    }


    suspend fun setDataSource(filePath: String, duration: Long) = withContext(Dispatchers.Default) {
        mAudioDecoder = null
        mTouchEventHandler.reset()
        mWaveformDrawer.reset()
        postInvalidate()
        mWaveformViewListener?.let {
            it.onCountAudioSelected(duration, true)
            it.onPlayPositionChanged(playPositionMs.toInt(), false)
            it.onEndTimeChanged(duration)
        }

        mDuration = duration
        mAudioDecoder = AudioDecoderBuilder.build(filePath, this@WaveformView1)
        mAudioDecoder?.let {
            if (mDuration != 0L) {
                mFPS = (it.numFrames / (mDuration / 1000)).toInt()
            }
            post {
                mWaveformDrawer.computeDoublesForAllZoomLevels(it)
                mTouchEventHandler.init()
                mRangeDrawer.init()
                invalidate()
            }
        }

    }

    override fun onDraw(canvas: Canvas) {
        mWaveformDrawer.onDraw(canvas)
        mRangeDrawer.onDraw(canvas)
        mTouchEventHandler.onWaveformDraw()
    }

    protected fun setOffset(offset: Int): Boolean {
        if (mWaveformDrawer.setOffset(offset)) {
            mRangeDrawer.onChangeOffset()
            return true;
        }
        return false;
    }

    protected fun getOffset(): Int {
        return mWaveformDrawer.mOffset
    }

    protected fun maxPos(): Int {
        return mWaveformDrawer.maxPos()
    }

    protected fun getPlayPos(): Int {
        return mTouchEventHandler.mPlaybackPos
    }

    protected fun getSelectionStart(): Int {
        return mTouchEventHandler.mStartPos
    }

    protected fun getSelectionEnd(): Int {
        return mTouchEventHandler.mEndPos
    }

    override fun onDetachedFromWindow() {
        isDettachedWindow = true
        super.onDetachedFromWindow()
    }

    protected fun getWaveformHeight(): Int {
        return (0.7f * this.measuredHeight).toInt()
    }

    protected fun getWaveformWidth(): Int {
        return this.measuredWidth
    }

    protected fun getDrawingStartY(): Int {
        return ((this.measuredHeight - 0.8f * this.measuredHeight) / 2f).toInt()
    }

    protected fun getDrawingEndY(): Int {
        return getDrawingStartY() + (0.8f * this.measuredHeight).toInt()
    }

    protected fun secondsToPixels(seconds: Double): Int {
        mAudioDecoder?.let {
            val z = mWaveformDrawer.mZoomFactorByZoomLevel.get(mWaveformDrawer.mZoomLevel)
            return (z * seconds * it.sampleRate / it.samplesPerFrame + 0.5).toInt()
        }
        return -1
    }

    protected fun pixelsToSeconds(pixels: Int): Double {
        mAudioDecoder?.let {
            if (it.sampleRate != 0) {
                val z = mWaveformDrawer.mZoomFactorByZoomLevel.get(mWaveformDrawer.mZoomLevel)
                return (pixels * it.samplesPerFrame / (it.sampleRate * z)).toDouble()
            }
        }
        return -1.0
    }

    protected fun millisecsToPixels(msecs: Int): Int {
        mAudioDecoder?.let {
            val z = mWaveformDrawer.mZoomFactorByZoomLevel.get(mWaveformDrawer.mZoomLevel)
            return (msecs * 1.0 * it.sampleRate * z / (1000.0 * it.samplesPerFrame) + 0.5).toInt()
        }
        return -1
    }

    protected fun pixelsToMillisecs(pixels: Int): Int {
        mAudioDecoder?.let {
            val z = mWaveformDrawer.mZoomFactorByZoomLevel.get(mWaveformDrawer.mZoomLevel)
            return (pixels * (1000.0 * it.samplesPerFrame) / (it.sampleRate * z) + 0.5).toInt()
        }
        return -1
    }

    protected fun fps(): Int {
        return mFPS
    }

    protected fun getCursorLeftRect(): RectF {
        return mRangeDrawer.mCursorLeftRect
    }

    protected fun getCursorRightRect(): RectF {
        return mRangeDrawer.mCursorRightRect
    }

    protected fun duration(): Long {
        return mDuration
    }


}