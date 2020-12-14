package com.example.waveform.views;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

class WaveformTouchEventHandler {
    private ScaleGestureDetector mScaleGestureDetector;
    protected GestureDetector mGestureDetector;
    private boolean mTouchDragging = false;
    private float mTouchStart;
    private float mTouchInitialOffset;
    private int mOffset;
    private float mInitialScaleSpan;
    private int mFlingVelocity;
    private long mWaveformTouchStartMsec;

    private int mMaxPos;
    private int mStartPos;
    private int mEndPos;
    protected int mOffsetGoal;

    private boolean mInitialized = false;
    private WaveformView1 mWaveformView;

    WaveformTouchEventHandler(WaveformView1 waveformView) {
        mWaveformView = waveformView;
        mScaleGestureDetector = new ScaleGestureDetector(
                mWaveformView.getContext(),
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    public boolean onScaleBegin(ScaleGestureDetector d) {
                        mInitialScaleSpan = Math.abs(d.getCurrentSpanX());
                        return true;
                    }

                    public boolean onScale(ScaleGestureDetector d) {
                        float scale = Math.abs(d.getCurrentSpanX());
                        if (scale - mInitialScaleSpan > 40) {
                            //mListener.waveformZoomIn();
                            mWaveformView.zoomIn();

                            mInitialScaleSpan = scale;
                        }
                        if (scale - mInitialScaleSpan < -40) {
                            //mListener.waveformZoomOut();
                            mWaveformView.zoomOut();
                            mInitialScaleSpan = scale;
                        }
                        return true;
                    }
                });
       /* mGestureDetector = new GestureDetector(
                waveformView.getContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    public boolean onFling(
                            MotionEvent e1, MotionEvent e2, float vx, float vy) {
                        waveformFling(vx);
                        return true;
                    }
                });*/
    }

    void reset() {
        mInitialized = false;
    }

    void init() {
        mMaxPos = mWaveformView.maxPos();
        mInitialized = true;

    }

    boolean isReady() {
        return mInitialized;
    }

    boolean onTouchEvent(MotionEvent event) {
        if (!mInitialized) {
            return false;
        }
        mScaleGestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                waveformTouchStart(event.getX());
                break;
            case MotionEvent.ACTION_MOVE:
                waveformTouchMove(event.getX());
                break;
            case MotionEvent.ACTION_UP:
                waveformTouchEnd();
                break;
        }
        return true;
    }

    private void waveformFling(float vx) {
        mTouchDragging = false;
        mOffsetGoal = mOffset;
        mFlingVelocity = (int) (-vx);
        updateDisplay();
    }

    private void waveformTouchStart(float x) {
        mTouchDragging = true;
        mTouchStart = x;
        mTouchInitialOffset = mOffset;
        mFlingVelocity = 0;
        mWaveformTouchStartMsec = System.currentTimeMillis();
    }

    private void waveformTouchMove(float x) {
        Log.d("taihhhhh", "waveformTouchMove mTouchInitialOffset" + mTouchInitialOffset + " mTouchStart " + mTouchStart + " x " + x);
        changeOffset(trap((int) (mTouchInitialOffset + (mTouchStart - x))));
        updateDisplay();
    }

    private void waveformTouchEnd() {
        mTouchDragging = false;
        // mOffsetGoal = mOffset;
        long elapsedMsec = System.currentTimeMillis() - mWaveformTouchStartMsec;
    }

    private int trap(int pos) {
        if (pos < 0)
            return 0;
        if (pos > mMaxPos)
            return mMaxPos;
        return pos;
    }

    private void changeOffset(int newOffset) {
        mOffset = newOffset;
        Log.d("taihhhhh", " mOffset " + mOffset);
    }

    private void updateDisplay() {
        /*if(!mTouchDragging){
            int offsetDelta;

            if(mFlingVelocity != 0){
                offsetDelta = mFlingVelocity / 30;
                if (mFlingVelocity > 80) {
                    mFlingVelocity -= 80;
                } else if (mFlingVelocity < -80) {
                    mFlingVelocity += 80;
                } else {
                    mFlingVelocity = 0;
                }

                mOffset += offsetDelta;
            }else{
                offsetDelta = mOffsetGoal - mOffset;

                if (offsetDelta > 10)
                    offsetDelta = offsetDelta / 10;
                else if (offsetDelta > 0)
                    offsetDelta = 1;
                else if (offsetDelta < -10)
                    offsetDelta = offsetDelta / 10;
                else if (offsetDelta < 0)
                    offsetDelta = -1;
                else
                    offsetDelta = 0;

                mOffset += offsetDelta;
            }
        }*/
        mWaveformView.setParameters(mStartPos, mEndPos, mOffset);
        mWaveformView.invalidate();
    }

    void onWaveformZoomIn() {
        changeOffset(mWaveformView.getOffset());
        mMaxPos = mWaveformView.maxPos();
    }

    void onWaveformZoomOut() {
        changeOffset(mWaveformView.getOffset());
        mMaxPos = mWaveformView.maxPos();
    }

}