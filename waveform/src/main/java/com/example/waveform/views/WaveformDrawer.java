package com.example.waveform.views;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.example.waveform.R;
import com.example.waveform.Utils;
import com.example.waveform.soundfile.AudioDecoder;

class WaveformDrawer {
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private AudioDecoder mAudioDecoder;
    private View mView;

    private float range;
    private float scaleFactor;
    private float minGain;

    private int mZoomLevel;
    private int mNumZoomLevels;

    private int[] mLenByZoomLevel;
    private float[] mZoomFactorByZoomLevel;

    private boolean mInitialized;
    int mOffset;
    private final String waitingForLoadingText;
    private final Rect textBounds = new Rect();


    WaveformDrawer(View view, int waveformColor, float waveformLineWidth) {
        mView = view;
        mOffset = 0;
        mPaint.setAntiAlias(false);
        mPaint.setColor(view.getResources().getColor(R.color.waveform_selected));

        mPaint.setColor(waveformColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(waveformLineWidth);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextSize(Utils.Companion.dpToPx(mView.getContext(), 16));

        waitingForLoadingText = mView.getResources().getString(R.string.waiting_for_loading_waveform);

        mLenByZoomLevel = null;
        mZoomFactorByZoomLevel = null;

    }

    void reset() {
        mInitialized = false;
    }

    void computeDoublesForAllZoomLevels(AudioDecoder audioDecoder) {
        mInitialized = false;
        this.mAudioDecoder = audioDecoder;
        int numFrames = mAudioDecoder.getNumFrames();

        float maxGain = 1.0f;
        for (int i = 0; i < numFrames; ++i) {
            float gain = getGain(i, numFrames, mAudioDecoder.getFrameGains());
            if (gain > maxGain) {
                maxGain = gain;
            }
        }
        scaleFactor = 1.0f;
        if (maxGain > 255.0) {
            scaleFactor = 255 / maxGain;
        }

        maxGain = 0;
        int gainHist[] = new int[256];
        for (int i = 0; i < numFrames; ++i) {
            int smoothedGain = (int) (getGain(i, numFrames, mAudioDecoder.getFrameGains()) * scaleFactor);
            if (smoothedGain < 0) {
                smoothedGain = 0;
            }
            if (smoothedGain > 255) {
                smoothedGain = 255;
            }
            if (smoothedGain > maxGain)
                maxGain = smoothedGain;
            gainHist[smoothedGain]++;
        }

        // Re-calibrate the min to be 5%
        minGain = 0;
        int sum = 0;
        while (minGain < 255 && sum < numFrames / 20) {
            sum += gainHist[(int) minGain];
            minGain++;
        }

        // Re-calibrate the max to be 99%
        sum = 0;
        while (maxGain > 2 && sum < numFrames / 100) {
            sum += gainHist[(int) maxGain];
            maxGain--;
        }

        range = maxGain - minGain;

        mNumZoomLevels = 4;
        mLenByZoomLevel = new int[4];
        mZoomFactorByZoomLevel = new float[4];

        float ratio = mView.getMeasuredWidth() / (float) numFrames;
        if (ratio < 1) {
            mLenByZoomLevel[0] = Math.round(numFrames * ratio);
            mZoomFactorByZoomLevel[0] = ratio;

            mLenByZoomLevel[1] = numFrames;
            mZoomFactorByZoomLevel[1] = 1.0f;

            mLenByZoomLevel[2] = numFrames * 2;
            mZoomFactorByZoomLevel[2] = 2.0f;

            mLenByZoomLevel[3] = numFrames * 3;
            mZoomFactorByZoomLevel[3] = 3.0f;

            mZoomLevel = 0;
        } else {
            mLenByZoomLevel[0] = numFrames;
            mZoomFactorByZoomLevel[0] = 1.0f;

            mLenByZoomLevel[1] = numFrames * 2;
            mZoomFactorByZoomLevel[1] = 2f;

            mLenByZoomLevel[2] = numFrames * 3;
            mZoomFactorByZoomLevel[2] = 3.0f;

            mLenByZoomLevel[3] = numFrames * 4;
            mZoomFactorByZoomLevel[3] = 4.0f;

            mZoomLevel = 0;
            for (int i = 0; i < 4; i++) {
                if (mLenByZoomLevel[mZoomLevel] - mView.getMeasuredWidth() > 0) {
                    break;
                } else {
                    mZoomLevel = i;
                }
            }
        }
        mInitialized = true;
    }

    void onDraw(final Canvas canvas) {
        if (isInitialized()) {
            int measuredWidth = mView.getMeasuredWidth();
            int measuredHeight = (int) (mView.getMeasuredHeight() * 0.8f);
            int start = mOffset;
            int width = mLenByZoomLevel[mZoomLevel] - start;
            int ctr = mView.getMeasuredHeight() / 2;

            if (width > measuredWidth)
                width = measuredWidth;

            int i = 0;
            while (i < width) {
                drawWaveform(canvas, i, start, measuredHeight, ctr, mPaint);
                i++;
            }
        } else {
            drawLoadingText(canvas);
        }
    }

    private void drawLoadingText(Canvas canvas) {
        int measuredWidth = mView.getMeasuredWidth();
        int measuredHeight = (int) (mView.getMeasuredHeight());
        if (measuredHeight > 0 && measuredWidth > 0) {
            mTextPaint.getTextBounds(waitingForLoadingText, 0, waitingForLoadingText.length(), textBounds);
            float x = (measuredWidth - textBounds.width() - mView.getPaddingLeft() + mView.getPaddingRight()) / 2f;
            float y = (measuredHeight - textBounds.height() - mView.getPaddingTop() + mView.getPaddingBottom()) / 2f;
            canvas.drawText(waitingForLoadingText, x, y, mTextPaint);
        }

    }

    private void drawWaveform(final Canvas canvas, final int i, final int start, final int measuredHeight, final int ctr, final Paint paint) {
        int h = (int) (getScaledHeight(mZoomFactorByZoomLevel[mZoomLevel], start + i) * measuredHeight / 2);
        drawWaveformLine(canvas, i, ctr - h, ctr + 1 + h, paint);

    }

    private void drawWaveformLine(Canvas canvas, int x, int y0, int y1, Paint paint) {
        canvas.drawLine(x, y0, x, y1, paint);
    }

    private float getGain(int i, int numFrames, int[] frameGains) {
        int x = Math.min(i, numFrames - 1);
        if (numFrames < 2) {
            return frameGains[x];
        } else {
            if (x == 0) {
                return (frameGains[0] / 2.0f) + (frameGains[1] / 2.0f);
            } else if (x == numFrames - 1) {
                return (frameGains[numFrames - 2] / 2.0f) + (frameGains[numFrames - 1] / 2.0f);
            } else {
                return (frameGains[x - 1] / 3.0f) + (frameGains[x] / 3.0f) + (frameGains[x + 1] / 3.0f);
            }
        }
    }

    private float getScaledHeight(float zoomLevel, int i) {
        if (zoomLevel == 1.0) {
            return getNormalHeight(i);
        } else if (zoomLevel < 1.0) {
            return getZoomedOutHeight(zoomLevel, i);
        }
        return getZoomedInHeight(zoomLevel, i);
    }

    private float getNormalHeight(int i) {
        return getHeight(i, mAudioDecoder.getNumFrames(), mAudioDecoder.getFrameGains(), scaleFactor, minGain, range);
    }

    private float getHeight(int i, int numFrames, int[] frameGains, float scaleFactor, float minGain, float range) {
        float value = (getGain(i, numFrames, frameGains) * scaleFactor - minGain) / range;
        if (value < 0.0)
            value = 0.0f;
        if (value > 1.0)
            value = 1.0f;
        return value;
    }

    private float getZoomedInHeight(float zoomLevel, int i) {
        int f = (int) zoomLevel;
        if (i == 0) {
            return 0.5f * getHeight(0, mAudioDecoder.getNumFrames(), mAudioDecoder.getFrameGains(), scaleFactor, minGain, range);
        }
        if (i == 1) {
            return getHeight(0, mAudioDecoder.getNumFrames(), mAudioDecoder.getFrameGains(), scaleFactor, minGain, range);
        }
        if (i % f == 0) {
            float x1 = getHeight(i / f - 1, mAudioDecoder.getNumFrames(), mAudioDecoder.getFrameGains(), scaleFactor, minGain, range);
            float x2 = getHeight(i / f, mAudioDecoder.getNumFrames(), mAudioDecoder.getFrameGains(), scaleFactor, minGain, range);
            return 0.5f * (x1 + x2);
        } else if ((i - 1) % f == 0) {
            return getHeight((i - 1) / f, mAudioDecoder.getNumFrames(), mAudioDecoder.getFrameGains(), scaleFactor, minGain, range);
        }
        return 0;
    }

    private float getZoomedOutHeight(float zoomLevel, int i) {
        int f = (int) (i / zoomLevel);
        float x1 = getHeight(f, mAudioDecoder.getNumFrames(), mAudioDecoder.getFrameGains(), scaleFactor, minGain, range);
        float x2 = getHeight(f + 1, mAudioDecoder.getNumFrames(), mAudioDecoder.getFrameGains(), scaleFactor, minGain, range);
        return 0.5f * (x1 + x2);
    }

    public boolean isInitialized() {
        return mInitialized;
    }

    void zoomIn() {
        if (canZoomIn()) {
            mZoomLevel++;
            float factor = mLenByZoomLevel[mZoomLevel] / (float) mLenByZoomLevel[mZoomLevel - 1];

            int offsetCenter = mOffset + (int) (mView.getMeasuredWidth() / factor);
            offsetCenter *= factor;
            mOffset = offsetCenter - (int) (mView.getMeasuredWidth() / factor);
            if (mOffset < 0) {
                mOffset = 0;
            }
            mView.invalidate();
        }
    }

    void zoomOut() {
        if (canZoomOut()) {
            mZoomLevel--;
            float factor = mLenByZoomLevel[mZoomLevel + 1] / (float) mLenByZoomLevel[mZoomLevel];
            int offsetCenter = (int) (mOffset + mView.getMeasuredWidth() / factor);
            offsetCenter /= factor;
            mOffset = offsetCenter - (int) (mView.getMeasuredWidth() / factor);
            if (mOffset < 0)
                mOffset = 0;
            mView.invalidate();
        }
    }

    void setParameters(int start, int end, int offset) {
        float zoomFactor = mZoomFactorByZoomLevel[mZoomLevel];
        if ((mAudioDecoder.getNumFrames() * zoomFactor - offset) < mView.getMeasuredWidth()) {
            mOffset = (int) (mAudioDecoder.getNumFrames() * zoomFactor - mView.getMeasuredWidth());
        } else {
            mOffset = offset;
        }

    }

    boolean canZoomIn() {
        return (mZoomLevel < mNumZoomLevels - 1);
    }

    boolean canZoomOut() {
        return (mZoomLevel > 0);
    }

    public int maxPos() {
        return mLenByZoomLevel[mZoomLevel];
    }
}
