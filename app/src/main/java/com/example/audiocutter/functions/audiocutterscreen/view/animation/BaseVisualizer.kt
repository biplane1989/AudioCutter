package com.example.audiocutter.functions.audiocutterscreen.view.animation

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.media.audiofx.Visualizer
import android.media.audiofx.Visualizer.OnDataCaptureListener
import android.util.AttributeSet
import android.view.View
import androidx.annotation.Nullable
import com.example.audiocutter.R


abstract class BaseVisualizer : View {
    object AVConstants {
        const val DEFAULT_DENSITY = 0.25f
        const val DEFAULT_COLOR = Color.BLACK
        const val DEFAULT_STROKE_WIDTH = 6.0f
        const val MAX_ANIM_BATCH_COUNT = 4
    }

    lateinit var mRawAudioBytes: ByteArray
    protected var mPaint: Paint? = null
    lateinit var mVisualizer: Visualizer
    protected var mColor: Int = AVConstants.DEFAULT_COLOR
    protected var mPaintStyle: PaintStyle = PaintStyle.FILL
    protected var mPositionGravity: PositionGravity = PositionGravity.BOTTOM
    protected var mStrokeWidth: Float = AVConstants.DEFAULT_STROKE_WIDTH
    protected var mDensity: Float = AVConstants.DEFAULT_DENSITY
    protected var mAnimSpeed = AnimSpeed.MEDIUM
    protected var isVisualizationEnabled = true

    constructor(context: Context) : super(context) {
        init(context, null)
        init()
    }

    constructor(context: Context, @Nullable attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
        init()
    }

    constructor(
        context: Context,
        @Nullable attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
        init()
    }

    private fun init(context: Context, attrs: AttributeSet?) {

        //get the attributes specified in attrs.xml using the name we included
        val typedArray = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.BaseVisualizer, 0, 0
        )
        if (typedArray != null && typedArray.length() > 0) {
            try {
                //get the text and colors specified using the names in attrs.xml
                mDensity = typedArray.getFloat(
                    R.styleable.BaseVisualizer_avDensity,
                    AVConstants.DEFAULT_DENSITY
                )
                mColor = typedArray.getColor(
                    R.styleable.BaseVisualizer_avColor,
                    AVConstants.DEFAULT_COLOR
                )
                mStrokeWidth = typedArray.getDimension(
                    R.styleable.BaseVisualizer_avWidth,
                    AVConstants.DEFAULT_STROKE_WIDTH
                )
                val paintType = typedArray.getString(R.styleable.BaseVisualizer_avType)
                if (paintType != null && paintType != "") mPaintStyle =
                    if (paintType.toLowerCase() == "outline") PaintStyle.OUTLINE else PaintStyle.FILL
                val gravityType = typedArray.getString(R.styleable.BaseVisualizer_avGravity)
                if (gravityType != null && gravityType != "") mPositionGravity =
                    if (gravityType.toLowerCase() == "top") PositionGravity.TOP else PositionGravity.BOTTOM
                val speedType = typedArray.getString(R.styleable.BaseVisualizer_avSpeed)
                if (speedType != null && speedType != "") {
                    mAnimSpeed = AnimSpeed.MEDIUM
                    if (speedType.toLowerCase() == "slow") mAnimSpeed =
                        AnimSpeed.SLOW else if (speedType.toLowerCase() == "fast") mAnimSpeed =
                        AnimSpeed.FAST
                }
            } finally {
                typedArray.recycle()
            }
        }
        mPaint = Paint()
        mPaint!!.color = mColor
        mPaint!!.strokeWidth = mStrokeWidth
        if (mPaintStyle === PaintStyle.FILL) mPaint!!.style = Paint.Style.FILL else {
            mPaint!!.style = Paint.Style.STROKE
        }
    }

    /**
     * Set color to visualizer with color resource id.
     *
     * @param color color resource id.
     */
    fun setColor(color: Int) {
        mColor = color
        mPaint!!.color = mColor
    }

    /**
     * Set the density of the visualizer
     *
     * @param density density for visualization
     */
    fun setDensity(density: Float) {
        //TODO: Check dynamic density change, may cause crash
        synchronized(this) {
            mDensity = density
            init()
        }
    }

    /**
     * Sets the paint style of the visualizer
     *
     * @param paintStyle style of the visualizer.
     */
    fun setPaintStyle(paintStyle: PaintStyle) {
        mPaintStyle = paintStyle
        mPaint!!.style =
            if (paintStyle === PaintStyle.FILL) Paint.Style.FILL else Paint.Style.STROKE
    }

    /**
     * Sets the position of the Visualization[PositionGravity]
     *
     * @param positionGravity position of the Visualization
     */
    fun setPositionGravity(positionGravity: PositionGravity) {
        mPositionGravity = positionGravity
    }

    /**
     * Sets the Animation speed of the visualization[AnimSpeed]
     *
     * @param animSpeed speed of the animation
     */
    open fun setAnimationSpeed(animSpeed: AnimSpeed) {
        mAnimSpeed = animSpeed
    }

    /**
     * Sets the width of the outline [PaintStyle]
     *
     * @param width style of the visualizer.
     */
    fun setStrokeWidth(width: Float) {
        mStrokeWidth = width
        mPaint!!.strokeWidth = width
    }

    /**
     * Sets the audio bytes to be visualized form [Visualizer] or other sources
     *
     * @param bytes of the raw bytes of music
     */
    fun setRawAudioBytes(bytes: ByteArray) {
        mRawAudioBytes = bytes
        this.invalidate()
    }

    /**
     * Sets the audio session id for the currently playing audio
     *
     * @param audioSessionId of the media to be visualised
     */
    fun setAudioSessionId(audioSessionId: Int) {
        release()

        mVisualizer = Visualizer(audioSessionId)
        mVisualizer.captureSize = Visualizer.getCaptureSizeRange()[1]
        mVisualizer.setDataCaptureListener(object : OnDataCaptureListener {
            override fun onWaveFormDataCapture(
                visualizer: Visualizer, bytes: ByteArray,
                samplingRate: Int
            ) {
                mRawAudioBytes = bytes
                invalidate()
            }

            override fun onFftDataCapture(
                visualizer: Visualizer, bytes: ByteArray,
                samplingRate: Int
            ) {
            }
        }, Visualizer.getMaxCaptureRate() / 2, true, false)
        mVisualizer!!.enabled = true
    }

    /**
     * Releases the visualizer
     */
    fun release() {
        mVisualizer.release()
    }

    /**
     * Enable Visualization
     */
    fun show() {
        isVisualizationEnabled = true
    }

    /**
     * Disable Visualization
     */
    fun hide() {
        isVisualizationEnabled = false
    }

    protected abstract fun init()
}

enum class AnimSpeed {
    SLOW, MEDIUM, FAST
}

enum class PaintStyle {
    OUTLINE, FILL
}

enum class PositionGravity {
    TOP, BOTTOM
}

