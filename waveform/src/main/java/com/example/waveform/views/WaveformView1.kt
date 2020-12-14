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
    }

    private fun isReadyToDraw(): Boolean {
        return width > 0 && height > 0
    }

    fun zoomIn() {
        mWaveformDrawer.zoomIn()
    }

    fun zoomOut() {
        mWaveformDrawer.zoomOut()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mWaveformDrawer.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event)

    }


    suspend fun setDataSource(filePath: String, duration: Long) = withContext(Dispatchers.Default) {
        mWaveformViewListener?.let {
            it.onCountAudioSelected(duration, true)
            it.onPlayPositionChanged(playPositionMs.toInt(), false)
            it.onEndTimeChanged(duration)
        }

        mDuration = duration
        mAudioDecoder = AudioDecoderBuilder.build(filePath, this@WaveformView1)
        if (isReadyToDraw()) {
            mWaveformDrawer.computeDoublesForAllZoomLevels(mAudioDecoder)
            postInvalidate()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w != oldw || h != oldh) {
            if (isReadyToDraw() && mAudioDecoder != null && !mWaveformDrawer.isInitialized) {
                mWaveformDrawer.computeDoublesForAllZoomLevels(mAudioDecoder)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        mWaveformDrawer.onDraw(canvas)
    }

}