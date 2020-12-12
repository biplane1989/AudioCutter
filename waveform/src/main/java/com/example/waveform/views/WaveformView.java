package com.example.waveform.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.waveform.R;
import com.example.waveform.soundfile.AudioDecoder;
import com.example.waveform.soundfile.AudioDecoderBuilder;
import com.example.waveform.soundfile.ProgressListener;

public class WaveformView extends View implements ProgressListener {
    private WaveformDrawer mWaveformDrawer;
    private AudioDecoder mAudioDecoder;

    public WaveformView(Context context) {
        super(context);
        init();
    }

    public WaveformView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        mWaveformDrawer = new WaveformDrawer(this, getResources().getColor(R.color.waveform_selected), 10);
    }

    public void setAudioFile(String filePath) {
        Log.d("taihhhhh", "start setAudioFile");
        mAudioDecoder = AudioDecoderBuilder.build(filePath, this);
        Log.d("taihhhhh", "end setAudioFile ");
        if(isReadyToDraw()){
            mWaveformDrawer.computeDoublesForAllZoomLevels(mAudioDecoder);
            postInvalidate();
        }
    }



    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(isReadyToDraw()){
            mWaveformDrawer.computeDoublesForAllZoomLevels(mAudioDecoder);
        }
    }

    private boolean isReadyToDraw() {
        return getWidth() > 0 && getHeight() > 0;
    }

    @Override
    public boolean reportProgress(double fractionComplete) {
        Log.d("taihhhhh", "reportProgress: " + fractionComplete);
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isReadyToDraw()) {
            return;
        }
        if (mWaveformDrawer.isInitialized()) {
            mWaveformDrawer.onDraw(canvas);
        }
    }
}
