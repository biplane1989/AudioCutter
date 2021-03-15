package com.example.waveform.views;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import com.example.waveform.R;
import com.example.waveform.Utils;
import com.example.waveform.soundfile.AudioDecoder;

import java.util.Locale;

class WaveformDrawer {
    private static final float MAX_ZOOM_VALUE = 4.0f;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private AudioDecoder mAudioDecoder;
    private WaveformView mWaveformView;

    private float range;
    private float scaleFactor;
    private float minGain;




    /*protected int mZoomLevel;
    protected int mNumZoomLevels;*/

    /*protected int[] mLenByZoomLevel;
    protected float[] mZoomFactorByZoomLevel;*/
    private float mZoomValue = -1f;
    private float mMinZoomValue = -1f;

    private boolean mInitialized;
    int mOffset;
    private final Rect textBounds = new Rect();
    private final Paint mLinePlayingPaint;
    private final int mWaveformSelectedColor, mWaveformUnselectedColor;
    private final float cursorSize;

    WaveformDrawer(WaveformView waveformView, int waveformSelectedColor, int waveformUnselectedColor, int linePlayingColor) {
        mWaveformSelectedColor = waveformSelectedColor;
        mWaveformUnselectedColor = waveformUnselectedColor;
        mWaveformView = waveformView;
        mOffset = 0;
        mPaint.setAntiAlias(false);

        mLinePlayingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePlayingPaint.setColor(linePlayingColor);
        mLinePlayingPaint.setAntiAlias(true);
        mLinePlayingPaint.setStyle(Paint.Style.STROKE);
        mLinePlayingPaint.setStrokeWidth(4f);

        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextSize(Utils.Companion.dpToPx(mWaveformView.getContext(), 16));
        cursorSize = Utils.Companion.dpToPx(mWaveformView.getContext(), WaveformView.DEFAULT_CURSOR_SIZE_DP);
       /* mLenByZoomLevel = null;
        mZoomFactorByZoomLevel = null;*/

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

      /*  mNumZoomLevels = 4;
        mLenByZoomLevel = new int[4];
        mZoomFactorByZoomLevel = new float[4];*/

        float ratio = mWaveformView.getMeasuredWidth() / (float) numFrames;
        if (ratio < 1) {
            mMinZoomValue = ratio;
            mZoomValue = ratio;
          /*  mLenByZoomLevel[0] = Math.round(numFrames * ratio);
            mZoomFactorByZoomLevel[0] = ratio;

            mLenByZoomLevel[1] = numFrames;
            mZoomFactorByZoomLevel[1] = 1.0f;

            mLenByZoomLevel[2] = numFrames * 2;
            mZoomFactorByZoomLevel[2] = 2.0f;

            mLenByZoomLevel[3] = numFrames * 3;
            mZoomFactorByZoomLevel[3] = 3.0f;

            mZoomLevel = 0;*/
        } else {
            mMinZoomValue = 1f;
            mZoomValue = mMinZoomValue;
            while (mZoomValue <= MAX_ZOOM_VALUE) {
                if (mZoomValue * numFrames - mWaveformView.getMeasuredWidth() > 0) {
                    break;
                }
                mZoomValue += 0.5f;
            }
           /* mLenByZoomLevel[0] = numFrames;
            mZoomFactorByZoomLevel[0] = 1.0f;

            mLenByZoomLevel[1] = numFrames * 2;
            mZoomFactorByZoomLevel[1] = 2f;

            mLenByZoomLevel[2] = numFrames * 3;
            mZoomFactorByZoomLevel[2] = 3.0f;

            mLenByZoomLevel[3] = numFrames * 4;
            mZoomFactorByZoomLevel[3] = 4.0f;

            mZoomLevel = 0;
            for (int i = 0; i < 4; i++) {
                if (mLenByZoomLevel[mZoomLevel] - mWaveformView.getMeasuredWidth() > 0) {
                    if (mLenByZoomLevel[mZoomLevel] > mWaveformView.getMeasuredWidth() && mZoomLevel > 0) {
                        mZoomLevel--;
                    }
                    break;
                } else {
                    mZoomLevel = i;
                }
            }*/
        }
        mInitialized = true;
    }

    private int getLenByZoomLevel() {
        return (int) (mZoomValue * mAudioDecoder.getNumFrames());
    }

    void onDraw(final Canvas canvas) {
        if (isInitialized()) {
            int measuredWidth = mWaveformView.getMeasuredWidth();
            int measuredHeight = mWaveformView.getWaveformHeight();
            int start = mOffset;
            /*  int width = mLenByZoomLevel[mZoomLevel] - start;*/
            int width = getLenByZoomLevel() - start;
            int ctr = (mWaveformView.getDrawingStartY() + mWaveformView.getDrawingEndY()) / 2;

            if (width > measuredWidth)
                width = measuredWidth;

            int i = 0;
            while (i < width) {
                if (isPositionSelected(i + start)) {
                    mPaint.setColor(mWaveformSelectedColor);
                } else {
                    mPaint.setColor(mWaveformUnselectedColor);
                }
                drawWaveform(canvas, i, start, measuredHeight, ctr, mPaint);
                i++;
            }
        } else {
            drawLoadingText(canvas);
        }
    }

    private boolean isPositionSelected(int position) {
        if (position >= mWaveformView.getSelectionStart() && position <= mWaveformView.getSelectionEnd()) {
            return true;
        }
        return false;
    }

    private void drawLoadingText(Canvas canvas) {
        int measuredWidth = mWaveformView.getMeasuredWidth();
        int measuredHeight = (int) (mWaveformView.getMeasuredHeight());
        if (measuredHeight > 0 && measuredWidth > 0) {
            String waitingText = String.format(Locale.getDefault(), "%s %d", mWaveformView.getResources().getString(R.string.waiting_for_loading_waveform), mWaveformView.getLoadingPercent()) + "%";
            mTextPaint.getTextBounds(waitingText, 0, waitingText.length(), textBounds);
            float x = (measuredWidth - textBounds.width() - mWaveformView.getPaddingLeft() + mWaveformView.getPaddingRight()) / 2f;
            float y = (measuredHeight - textBounds.height() - mWaveformView.getPaddingTop() + mWaveformView.getPaddingBottom()) / 2f;
            canvas.drawText(waitingText, x, y, mTextPaint);
        }

    }

    private void drawWaveform(final Canvas canvas, final int i, final int start, final int measuredHeight, final int ctr, final Paint paint) {
        /*int h = (int) (getScaledHeight(mZoomFactorByZoomLevel[mZoomLevel], start + i) * measuredHeight / 2);*/
        int h = (int) (getScaledHeight(mZoomValue, start + i) * measuredHeight / 2);
        drawWaveformLine(canvas, i, ctr - h, ctr + 1 + h, paint);

        if (i + start == mWaveformView.getPlayPos()) {
            int x = Math.min(i, mWaveformView.getSelectionEnd() - start);
            canvas.drawLine(x, mWaveformView.getDrawingStartY() - cursorSize, x, mWaveformView.getDrawingEndY() + cursorSize, mLinePlayingPaint);
        }
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
        //int f = (int) zoomLevel;
        int f = (int) zoomLevel;
        if (i == 0) {
            return 0.5f * getHeight(0, mAudioDecoder.getNumFrames(), mAudioDecoder.getFrameGains(), scaleFactor, minGain, range);
        }
        if (i == 1) {
            return getHeight(0, mAudioDecoder.getNumFrames(), mAudioDecoder.getFrameGains(), scaleFactor, minGain, range);
        }
       /* if (i % f == 0) {
            float x1 = getHeight(i / f - 1, mAudioDecoder.getNumFrames(), mAudioDecoder.getFrameGains(), scaleFactor, minGain, range);
            float x2 = getHeight(i / f, mAudioDecoder.getNumFrames(), mAudioDecoder.getFrameGains(), scaleFactor, minGain, range);
            return 0.5f * (x1 + x2);
        } else if ((i - 1) % f == 0) {
            return getHeight((i - 1) / f, mAudioDecoder.getNumFrames(), mAudioDecoder.getFrameGains(), scaleFactor, minGain, range);
        }*/
        if (i % f == 0) {
            float x1 = getHeight((int) (i / zoomLevel - 1), mAudioDecoder.getNumFrames(), mAudioDecoder.getFrameGains(), scaleFactor, minGain, range);
            float x2 = getHeight((int) (i / zoomLevel), mAudioDecoder.getNumFrames(), mAudioDecoder.getFrameGains(), scaleFactor, minGain, range);
            return 0.5f * (x1 + x2);
        } else if ((i - 1) % f == 0) {
            return getHeight((int) ((i - 1) / zoomLevel), mAudioDecoder.getNumFrames(), mAudioDecoder.getFrameGains(), scaleFactor, minGain, range);
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

    float zoom(int offset, float scaleFactor) {
        float currZoomValue = mZoomValue;
        float newZoomValue = Math.min(MAX_ZOOM_VALUE, mZoomValue * scaleFactor);
        newZoomValue = Math.max(mMinZoomValue, newZoomValue);
        float factor = newZoomValue / currZoomValue;

        mZoomValue = newZoomValue;
        mOffset = (int) (offset * factor);
        if (mOffset < 0) {
            mOffset = 0;
        }
        mWaveformView.invalidate();
        return factor;
    }

    float getFactorForZoom(float scaleFactor) {
        float currZoomValue = mZoomValue;
        float newZoomValue = Math.min(MAX_ZOOM_VALUE, mZoomValue * scaleFactor);
        newZoomValue = Math.max(mMinZoomValue, newZoomValue);
        return newZoomValue / currZoomValue;

    }

    float zoomIn(int offset) {
        if (canZoomIn()) {
            float currZoomValue = mZoomValue;
            float newZoomValue = Math.min(MAX_ZOOM_VALUE, mZoomValue + 1);
            float factor = newZoomValue / currZoomValue;
            mZoomValue = newZoomValue;
            Log.d("taihhhhh", "zoomIn: mZoomValue " + mZoomValue);
            //float factor = getFactorForZoomIn();
            /* mZoomLevel++;*/

            mOffset = (int) (offset * factor);
            if (mOffset < 0) {
                mOffset = 0;
            }
            mWaveformView.invalidate();
            return factor;
        }
        return -1;
    }

   /* float getFactorForZoomOut() {
        if (canZoomOut()) {
            return mLenByZoomLevel[mZoomLevel] / (float) mLenByZoomLevel[mZoomLevel - 1];
        }
        return -1;
    }

    float getFactorForZoomIn() {
        if (canZoomIn()) {
            return mLenByZoomLevel[mZoomLevel + 1] / (float) mLenByZoomLevel[mZoomLevel];
        }
        return -1;
    }*/


    float zoomOut(int offset) {
        if (canZoomOut()) {
            float currZoomValue = mZoomValue;
            float newZoomValue = Math.max(mMinZoomValue, mZoomValue - 1);
            float factor = currZoomValue / newZoomValue;
            mZoomValue = newZoomValue;
            Log.d("taihhhhh", "zoomOut: mZoomValue " + mZoomValue);
          /*  float factor = getFactorForZoomOut();
            mZoomLevel--;*/
            mOffset = (int) (offset / factor);
            if (mOffset < 0)
                mOffset = 0;
            mWaveformView.invalidate();
            return factor;
        }
        return -1;
    }

    boolean setOffset(int offset) {
        boolean isChanged = false;
        /* float zoomFactor = mZoomFactorByZoomLevel[mZoomLevel];*/
        float zoomFactor = mZoomValue;
        if ((mAudioDecoder.getNumFrames() * zoomFactor - offset) < mWaveformView.getMeasuredWidth()) {
            offset = (int) (mAudioDecoder.getNumFrames() * zoomFactor - mWaveformView.getMeasuredWidth());
            offset = Math.max(0, offset);
        }
        if (mOffset != offset) {
            mOffset = offset;
            isChanged = true;
        }
        return isChanged;
    }

    boolean canZoomIn() {
        /* return (mZoomLevel < mNumZoomLevels - 1);*/
        return (mZoomValue < MAX_ZOOM_VALUE);
    }

    boolean canZoomOut() {
        /* return (mZoomLevel > 0);*/
        return (mZoomValue > mMinZoomValue);
    }

    public int maxPos() {
        /* return mLenByZoomLevel[mZoomLevel];*/
        return getLenByZoomLevel();
    }

    public float getZoomValue() {
        return mZoomValue;
    }
}
