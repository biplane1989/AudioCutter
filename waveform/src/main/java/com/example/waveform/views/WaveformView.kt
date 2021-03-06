package com.example.waveform.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color.parseColor
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.example.waveform.Utils
import com.example.waveform.soundfile.AudioDecoder
import com.example.waveform.soundfile.AudioDecoderBuilder
import com.example.waveform.soundfile.ProgressListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.min

class WaveformView : View, ProgressListener {
    companion object {
        val WAVEFORM_SELECTED_COLOR = parseColor("#FDAA74")
        val WAVEFORM_UNSELECTED_COLOR = parseColor("#E7E0DA")
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
    private var mWaveformViewListener: WaveformViewListener? = null
    private var playPositionMs: Long = 0
    private var mFPS = 0
    private var mDuration: Long = 0
    private var mAudioDecoder: AudioDecoder? = null
    private lateinit var mTouchEventHandler: WaveformTouchEventHandler
    private var isDetachedWindow: Boolean = false
    private val mainScope = MainScope()
    private var loadingPercent: Int = 0
    override fun reportProgress(fractionComplete: Double): Boolean {
        loadingPercent = min(100, (fractionComplete * 100).toInt())
        return !isDetachedWindow
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

    fun setWaveformViewListener(waveformViewListener: WaveformViewListener) {
        mWaveformViewListener = waveformViewListener;
    }

    private fun init(context: Context) {
        waveformLineWidth = Utils.dpToPx(context, WAVEFORM_LINE_WITH)
        mWaveformDrawer = WaveformDrawer(
            this,
            WAVEFORM_SELECTED_COLOR,
            WAVEFORM_UNSELECTED_COLOR,
            DEFAULT_LINE_PLAYING_COLOR
        )
        mTouchEventHandler = WaveformTouchEventHandler(this, mWaveformDrawer)
        mRangeDrawer =
            RangeDrawer(
                this,
                DEFAULT_RANGE_SELECTION_COLOR,
                DEFAULT_LINE_SELECTION_COLOR,
                DEFAULT_SELECT_TEXT_COLOR
            )
    }


    fun zoomIn(newOffSet: Int = getSelectionStart()) {
        if (!isInitialized()) {
            return
        }
        val factor = mWaveformDrawer.zoomIn(newOffSet)
        if (factor != -1f) {
            mTouchEventHandler.onWaveformZoomIn(factor)
            setOffset(getOffset())
            mRangeDrawer.onWaveformZoom()
            invalidate()
        }

    }

    fun zoomOut(newOffSet: Int = getSelectionStart()) {
        if (!isInitialized()) {
            return
        }
        val factor = mWaveformDrawer.zoomOut(newOffSet)
        if (factor != -1f) {
            mTouchEventHandler.onWaveformZoomOut(factor)
            setOffset(getOffset())
            mRangeDrawer.onWaveformZoom()
            invalidate()
        }
    }

    fun zoom(newOffSet: Int = getSelectionStart(), scaleFactor:Float) {
        if (!isInitialized()) {
            return
        }
        if(scaleFactor == 1.0f){
            return
        }
        val factor = mWaveformDrawer.zoom(newOffSet, scaleFactor)
        if (factor != -1f) {
            if(scaleFactor > 1f){
                mTouchEventHandler.onWaveformZoomIn(factor)
            }else{
                mTouchEventHandler.onWaveformZoomOut(1f/factor)
            }

            setOffset(getOffset())
            mRangeDrawer.onWaveformZoom()
            invalidate()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mTouchEventHandler.onTouchEvent(event)) {
            return true
        }
        return super.onTouchEvent(event)

    }


    suspend fun setDataSource(filePath: String, duration: Long) = withContext(Dispatchers.Default) {
        loadingPercent = 0
        mAudioDecoder = null
        mTouchEventHandler.reset()
        mWaveformDrawer.reset()
        postInvalidate()
        mWaveformViewListener?.let {
            mainScope.launch {
                it.onPlayPositionChanged(playPositionMs.toInt(), false)
                it.onEndTimeChanged(duration)
            }
        }

        mDuration = duration
        mAudioDecoder = AudioDecoderBuilder.build(filePath, this@WaveformView)
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

    fun updatePlaybackInMs(currPos: Int) {
        val frames: Int = millisecsToPixels(currPos)
        mTouchEventHandler.onPlaybackChanged(frames)
    }

    fun setStartTimeMs(newPos: Int) {
        val frames: Int = millisecsToPixels(newPos)
        mTouchEventHandler.changeStartPos(frames)
    }

    fun setEndTimeMs(newPos: Int) {
        val frames: Int = millisecsToPixels(newPos)
        mTouchEventHandler.changeEndPos(frames)
    }

    fun getStartTimeMs(): Int {
        return pixelsToMillisecs(getSelectionStart())
    }

    fun getEndTimeMs(): Int {
        return pixelsToMillisecs(getSelectionEnd())
    }

    fun getPlaybackInMs(): Int {
        return pixelsToMillisecs(getPlayPos())
    }

    protected fun setOffset(offset: Int): Boolean {
        Log.d("taihhh", "setOffset changeOffset mOffset: ")
        if (mWaveformDrawer.setOffset(offset)) {
            mRangeDrawer.onChangeOffset()
            return true
        }
        mTouchEventHandler.changeOffset(getOffset())
        return false
    }

    protected fun getOffset(): Int {
        return mWaveformDrawer.mOffset
    }

    protected fun maxPos(): Int {
        return mWaveformDrawer.maxPos()
    }

    protected fun getPlayPos(): Int {
        return mTouchEventHandler.getPlaybackPos()
    }

    protected fun getSelectionStart(): Int {
        return mTouchEventHandler.mStartPos.toInt()
    }

    protected fun getSelectionEnd(): Int {
        return mTouchEventHandler.mEndPos.toInt()
    }

    override fun onDetachedFromWindow() {
        isDetachedWindow = true
        super.onDetachedFromWindow()
        mTouchEventHandler.release()
    }

    protected fun getWaveformHeight(): Int {
        return (0.6f * this.measuredHeight).toInt()
    }

    protected fun getWaveformWidth(): Int {
        return this.measuredWidth
    }

    protected fun getDrawingStartY(): Int {
        val marginBottom = 0.1f * this.measuredHeight
        val timeRangeHeight = 0.65f * this.measuredHeight
        return (this.measuredHeight - timeRangeHeight - marginBottom).toInt()
    }

    protected fun getDrawingEndY(): Int {
        val timeRangeHeight = 0.65f * this.measuredHeight
        return getDrawingStartY() + timeRangeHeight.toInt()
    }

    /* protected fun secondsToPixels(seconds: Double): Int {
         mAudioDecoder?.let {
             val z = mWaveformDrawer.mZoomFactorByZoomLevel.get(mWaveformDrawer.mZoomLevel)
             return (z * seconds * it.sampleRate / it.samplesPerFrame + 0.5).toInt()
         }
         return -1
     }*/

    /*protected fun pixelsToSeconds(pixels: Int): Double {
        mAudioDecoder?.let {
            if (it.sampleRate != 0) {
                val z = mWaveformDrawer.mZoomFactorByZoomLevel.get(mWaveformDrawer.mZoomLevel)
                return (pixels * it.samplesPerFrame / (it.sampleRate * z)).toDouble()
            }
        }
        return -1.0
    }*/

    /* protected fun millisecsToPixels(msecs: Int): Int {
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
     }*/

    protected fun millisecsToPixels(msecs: Int): Int {
        mAudioDecoder?.let {
            val z = mWaveformDrawer.zoomValue
            return (msecs * 1.0 * it.sampleRate * z / (1000.0 * it.samplesPerFrame) + 0.5).toInt()
        }
        return -1
    }

    protected fun pixelsToMillisecs(pixels: Int): Int {
        mAudioDecoder?.let {
            val z = mWaveformDrawer.zoomValue
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

    protected fun onPlayPosOutOfRange(isEnd: Boolean) {
        mWaveformViewListener?.onPlayPosOutOfRange(isEnd)
    }

    protected fun onStartDraggingPlayPos() {
        mWaveformViewListener?.onDraggingPlayPos(false)
    }

    protected fun onFinishDraggingPlayPos() {
        mWaveformViewListener?.onDraggingPlayPos(true)
    }

    protected fun onPlayingLineClicked() {
        mWaveformViewListener?.onPlayPositionChanged(pixelsToMillisecs(getPlayPos()), true)
    }

    protected fun onTimeRangeChanged() {
        val startTime = pixelsToMillisecs(getSelectionStart()).toLong()
        val endTime = pixelsToMillisecs(getSelectionEnd()).toLong()
        mWaveformViewListener?.onStartTimeChanged(startTime)
        mWaveformViewListener?.onEndTimeChanged(endTime)
    }

    fun getLoadingPercent(): Int {
        return loadingPercent
    }
}