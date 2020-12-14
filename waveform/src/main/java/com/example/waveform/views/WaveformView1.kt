package com.example.waveform.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
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
        val WAVEFORM_COLOR = Color.parseColor("#FDAA74")
        const val WAVEFORM_LINE_WITH = 2f //dp
    }

    private lateinit var mWaveformDrawer: WaveformDrawer
    private var waveformLineWidth = 0f
    private val mWaveformViewListener: WaveformViewListener? = null
    private var playPositionMs: Long = 0
    private var mDuration: Long = 0
    private var mAudioDecoder: AudioDecoder? = null
    private lateinit var mTouchEventHandler: WaveformTouchEventHandler
    override fun reportProgress(fractionComplete: Double): Boolean {
        return true
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

    private fun init(context: Context) {
        waveformLineWidth = Utils.dpToPx(context, WAVEFORM_LINE_WITH)
        mWaveformDrawer = WaveformDrawer(this, WAVEFORM_COLOR, waveformLineWidth)
        mTouchEventHandler = WaveformTouchEventHandler(this);
    }

    private fun isReadyToDraw(): Boolean {
        return width > 0 && height > 0
    }

    fun zoomIn() {
        if(!mTouchEventHandler.isReady){
            return
        }
        mWaveformDrawer.zoomIn()
        mTouchEventHandler.onWaveformZoomIn()
    }

    fun zoomOut() {
        if(!mTouchEventHandler.isReady){
            return
        }
        mWaveformDrawer.zoomOut()
        mTouchEventHandler.onWaveformZoomOut()
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
            post {
                mWaveformDrawer.computeDoublesForAllZoomLevels(it)
                mTouchEventHandler.init()
                invalidate()
            }
        }

    }

    override fun onDraw(canvas: Canvas) {
        mWaveformDrawer.onDraw(canvas)
    }

    fun setParameters(start: Int, end: Int, offset: Int) {
        mWaveformDrawer.setParameters(start, end, offset)
    }

    fun getOffset(): Int {
        return mWaveformDrawer.mOffset
    }
    fun maxPos():Int{
        return mWaveformDrawer.maxPos()
    }

}