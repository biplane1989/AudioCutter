package com.example.waveform.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.animation.LinearInterpolator;

class WaveformTouchEventHandler {
    private static final int NONE_PENDING_PLAYBACK_POS = -1;
    public static final int NONE_RANGE_BUTTON_SELECTED = 0;
    public static final int LEFT_RANGE_BUTTON_SELECTED = 1 << 1;
    public static final int RIGHT_RANGE_BUTTON_SELECTED = 1 << 2;
    protected GestureDetector mGestureDetector;
    private boolean mTouchDragging = false;
    private float mTouchStart;
    private float mTouchInitialOffset;
    private int mOffset;
    private float mInitialScaleSpan;
    private int mFlingVelocity;
    private long mWaveformTouchStartMsec;

    protected float mMaxPos;
    protected float mStartPos;
    protected float mEndPos;
    protected float mOffsetGoal;
    private float mPlaybackPos;

    private int mMinFrameDistance;
    private WaveformView mWaveformView;
    private WaveformDrawer mWaveformDrawer;
    protected float mTouchInitialStartPos;
    protected float mTouchInitialEndPos;
    private boolean mIsPlayingLineSliding = false;
    private int pendingPlaybackPos = NONE_PENDING_PLAYBACK_POS;

    private int mRangeButtonSelected = NONE_RANGE_BUTTON_SELECTED;
    private ValueAnimator mAnimator = null;
    private boolean isAnimationUsed = true;
    private boolean isScaling = false;
    private final ScaleGestureDetectorHandler mScaleGestureDetectorHandler;

    class ScaleGestureDetectorHandler {
        private float focusX = -1;

        private ScaleGestureDetector mScaleGestureDetector = new ScaleGestureDetector(
                mWaveformView.getContext(),
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    public boolean onScaleBegin(ScaleGestureDetector d) {
                        mInitialScaleSpan = Math.abs(d.getCurrentSpanX());
                        isScaling = true;
                        //focusX = d.getCurrentSpanX();
                        return true;
                    }

                    public boolean onScale(ScaleGestureDetector d) {
                        if(d.getScaleFactor() != 1f){
                            float deltaX = d.getFocusX();
                            float factor = mWaveformDrawer.getFactorForZoom(d.getScaleFactor());
                            int newOffset = (int) (mOffset + (1 - 1 / factor) * deltaX);
                            Log.d("taihhh", "zoom: " + factor + "  getScaleFactor" + d.getScaleFactor() + " newOffset " + newOffset);
                            mWaveformView.zoom(newOffset, factor);
                        }

                        //float scale = Math.abs(d.getCurrentSpanX());
                       /* if (scale - mInitialScaleSpan > 40) {
                            float deltaX = focusX;
                            float factor = mWaveformDrawer.getFactorForZoomIn();
                            int newOffset = (int) (mOffset + (1 - 1 / factor) * deltaX);
                            Log.d("taihhh", "onScale: relative value" + newOffset + " abs value " + (newOffset * factor));
                            mWaveformView.zoomIn(newOffset);
                            mInitialScaleSpan = scale;
                        }
                        if (scale - mInitialScaleSpan < -40) {
                            float deltaX = focusX;
                            float factor = mWaveformDrawer.getFactorForZoomOut();
                            int newOffset = (int) (mOffset + (1 - 1 / factor) * deltaX);
                            mWaveformView.zoomOut(newOffset);
                            mInitialScaleSpan = scale;
                        }*/
                        isScaling = true;
                        return true;
                    }

                    @Override
                    public void onScaleEnd(ScaleGestureDetector detector) {
                        super.onScaleEnd(detector);
                    }
                });

        void onTouchEvent(MotionEvent event) {
            mScaleGestureDetector.onTouchEvent(event);
        }
    }

    WaveformTouchEventHandler(WaveformView waveformView, WaveformDrawer waveformDrawer) {
        mWaveformView = waveformView;
        mWaveformDrawer = waveformDrawer;
        mScaleGestureDetectorHandler = new ScaleGestureDetectorHandler();

        mGestureDetector = new GestureDetector(
                waveformView.getContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    public boolean onFling(
                            MotionEvent e1, MotionEvent e2, float vx, float vy) {
                        waveformFling(vx);
                        return true;
                    }
                });
    }

    void reset() {

    }

    void init() {
        mMaxPos = mWaveformView.maxPos();
        mOffsetGoal = 0;
        mFlingVelocity = 0;
        mOffset = 0;
        mStartPos = 0;
        cancelPlayPosAnimation();
        mPlaybackPos = 0;
        mEndPos = mMaxPos;
        mMinFrameDistance = mWaveformView.millisecsToPixels(WaveformView.MIN_RANGE_SELECTION_IN_SECONDS * 1000);
    }

    void onWaveformDraw() {
        if (mFlingVelocity != 0) {
            updateDisplay();
        }
    }

    boolean onTouchEvent(MotionEvent event) {
        if (!mWaveformView.isInitialized()) {
            return false;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            isScaling = false;
            if (mWaveformView.getCursorLeftRect().contains(event.getX(), event.getY())) {
                mRangeButtonSelected = LEFT_RANGE_BUTTON_SELECTED;
            }
            if (mWaveformView.getCursorRightRect().contains(event.getX(), event.getY())) {
                mRangeButtonSelected = RIGHT_RANGE_BUTTON_SELECTED;
            }
        }

        if (mRangeButtonSelected == NONE_RANGE_BUTTON_SELECTED) {
            mScaleGestureDetectorHandler.onTouchEvent(event);
            if (isScaling) {
                return true;
            }
            if (mGestureDetector.onTouchEvent(event)) {
                return true;
            }
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mRangeButtonSelected == NONE_RANGE_BUTTON_SELECTED) {
                    waveformTouchStart(event.getX());
                } else {
                    markerTouchStart(event.getX());
                }

                break;
            case MotionEvent.ACTION_MOVE:
                if (mRangeButtonSelected == NONE_RANGE_BUTTON_SELECTED) {
                    waveformTouchMove(event.getX());
                } else {
                    markerTouchMove(event.getX());
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mRangeButtonSelected == NONE_RANGE_BUTTON_SELECTED) {
                    waveformTouchEnd();
                } else {
                    markerTouchEnd();
                }
                mRangeButtonSelected = NONE_RANGE_BUTTON_SELECTED;
                if (mIsPlayingLineSliding) {
                    mIsPlayingLineSliding = false;
                    if (pendingPlaybackPos != -1) {
                        onPlaybackChanged(pendingPlaybackPos);
                    }
                    onFinishDraggingPlayPos();
                }
                pendingPlaybackPos = NONE_PENDING_PLAYBACK_POS;
                break;
        }
        return true;
    }

    private void markerTouchStart(float x) {
        mTouchDragging = true;
        mTouchStart = x;
        mTouchInitialStartPos = mStartPos;
        mTouchInitialEndPos = mEndPos;
    }

    private void markerTouchMove(float x) {
        float delta = x - mTouchStart;
        if (mRangeButtonSelected == LEFT_RANGE_BUTTON_SELECTED) {
            changeStartPos(trap((int) (mTouchInitialStartPos + delta)));
        } else {
            changeEndPos(trap((int) (mTouchInitialEndPos + delta)));
        }
    }

    void changeStartPos(int newStartPos) {
        if (newStartPos < 0 || newStartPos >= (mMaxPos - mMinFrameDistance)) {
            return;
        }
        mStartPos = newStartPos;
        if ((mEndPos - mStartPos) < mMinFrameDistance) {
            mEndPos = mStartPos + mMinFrameDistance;
            if (mEndPos > mWaveformView.maxPos()) {
                mEndPos = mWaveformView.maxPos();
            }
        }
        if (mPlaybackPos < mStartPos) {
            mPlaybackPos = mStartPos;
            mIsPlayingLineSliding = true;
        }
        if (mPlaybackPos > mEndPos) {
            mPlaybackPos = mEndPos;
            mIsPlayingLineSliding = true;
        }
        if (mIsPlayingLineSliding) {
            onStartDraggingPlayPos();
        }
        onTimeRangeChanged();
        mWaveformView.invalidate();
    }

    void changeEndPos(int newEndPos) {
        if (newEndPos > mMaxPos) {
            return;
        }
        mEndPos = newEndPos;
        if ((mEndPos - mStartPos) < mMinFrameDistance) {
            mEndPos = mStartPos + mMinFrameDistance;
            if (mEndPos > mWaveformView.maxPos()) {
                mEndPos = mWaveformView.maxPos();
            }
        }
        if (mPlaybackPos < mStartPos) {
            cancelPlayPosAnimation();
            mPlaybackPos = mStartPos;
            mIsPlayingLineSliding = true;
        }
        if (mPlaybackPos > mEndPos) {
            cancelPlayPosAnimation();
            mPlaybackPos = mEndPos;
            mIsPlayingLineSliding = true;
        }
        if (mIsPlayingLineSliding) {
            onStartDraggingPlayPos();
        }
        onTimeRangeChanged();
        mWaveformView.invalidate();
    }

    private void markerTouchEnd() {
        mTouchDragging = false;
        mRangeButtonSelected = NONE_RANGE_BUTTON_SELECTED;
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
        changeOffset(trap((int) (mTouchInitialOffset + (mTouchStart - x))));
        updateDisplay();
    }

    private void waveformTouchEnd() {
        mTouchDragging = false;
        mOffsetGoal = mOffset;
        long elapsedMsec = System.currentTimeMillis() - mWaveformTouchStartMsec;
        // handle clicked event
        if (elapsedMsec < 300) {
            int ms = mWaveformView.pixelsToMillisecs((int) mTouchStart);
            int frameDelta = mWaveformView.millisecsToPixels(ms);
            int newPos = trap(mWaveformView.getOffset() + frameDelta);
            if (newPos < mStartPos || newPos > mEndPos) {
                return;
            }
            cancelPlayPosAnimation();
            mPlaybackPos = newPos;
            mWaveformView.invalidate();
            onPlayingLineClicked();
        }

    }

    private int trap(int pos) {
        if (pos < 0)
            return 0;
        if (pos > mMaxPos)
            return (int) mMaxPos;
        return pos;
    }

    void changeOffset(int newOffset) {
        mOffset = newOffset;
    }

    private void updateDisplay() {
        if (!mTouchDragging) {
            int offsetDelta;

            if (mFlingVelocity != 0) {
                offsetDelta = mFlingVelocity / 30;
                if (mFlingVelocity > 80) {
                    mFlingVelocity -= 80;
                } else if (mFlingVelocity < -80) {
                    mFlingVelocity += 80;
                } else {
                    mFlingVelocity = 0;
                }

                mOffset += offsetDelta;
                if (mOffset + mWaveformView.getMeasuredWidth() / 2 > mMaxPos) {
                    mOffset = (int) (mMaxPos - mWaveformView.getMeasuredWidth() / 2);
                    mFlingVelocity = 0;
                }
                if (mOffset < 0) {
                    mOffset = 0;
                    mFlingVelocity = 0;
                }
                mOffsetGoal = mOffset;
            } else {
                offsetDelta = (int) (mOffsetGoal - mOffset);

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
        }
        if (mWaveformView.setOffset(mOffset)) {
            mWaveformView.invalidate();
        }

    }

    void onWaveformZoomIn(float factor) {
        cancelPlayPosAnimation();
        changeOffset(mWaveformView.getOffset());
        mMaxPos = mWaveformView.maxPos();
        mOffsetGoal = mOffset;
        mStartPos *= factor;
        mEndPos *= factor;
        mPlaybackPos *= factor;
        mMinFrameDistance *= factor;
        Log.d("taihhhh", "onWaveformZoomIn: mEndPos " + mEndPos);
    }

    void onWaveformZoomOut(float factor) {
        cancelPlayPosAnimation();
        changeOffset(mWaveformView.getOffset());
        mMaxPos = mWaveformView.maxPos();
        mOffsetGoal = mOffset;
        mStartPos /= factor;
        mEndPos /= factor;
        mPlaybackPos /= factor;
        mMinFrameDistance /= factor;
        Log.d("taihhhh", "onWaveformZoomOut: mEndPos " + mEndPos);
    }

    void onPlaybackChanged(int newPlayPos) {

        if (newPlayPos >= 0 && newPlayPos <= mMaxPos) {
            if (newPlayPos < mStartPos || newPlayPos > mEndPos) {
                onPlayPosOutOfRange(newPlayPos > mEndPos);
                return;
            }
            movePlaybackPos(newPlayPos);
        }

    }


    private void movePlaybackPos(int newPlayPos) {
      /*  mPlaybackPos = newPlayPos;
        mWaveformView.invalidate();*/
        if (isAnimationUsed && newPlayPos > mPlaybackPos) {
            if (mAnimator != null) {
                pendingPlaybackPos = newPlayPos;
            } else {
                pendingPlaybackPos = NONE_PENDING_PLAYBACK_POS;
                mAnimator = ValueAnimator.ofInt((int) mPlaybackPos, newPlayPos);
                int duration = mWaveformView.pixelsToMillisecs(newPlayPos) - mWaveformView.pixelsToMillisecs((int) mPlaybackPos);
                mAnimator.setDuration(Math.min(duration, 500));
                mAnimator.setInterpolator(mPlayPosInterpolator);
                mAnimator.addUpdateListener(mPlayPosAnimatorUpdateListener);
                mAnimator.addListener(mPlayPosAnimatorListener);
                mAnimator.start();
            }
        } else {
            pendingPlaybackPos = NONE_PENDING_PLAYBACK_POS;
            cancelPlayPosAnimation();
            mPlaybackPos = newPlayPos;
            mWaveformView.invalidate();
        }
    }

    private final LinearInterpolator mPlayPosInterpolator = new LinearInterpolator();


    private final ValueAnimator.AnimatorUpdateListener mPlayPosAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            if (isAnimationUsed) {
                mPlaybackPos = (int) animation.getAnimatedValue();
                mWaveformView.invalidate();
            }

        }
    };
    private final AnimatorListenerAdapter mPlayPosAnimatorListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            mAnimator = null;
            if (pendingPlaybackPos > mPlaybackPos) {
                movePlaybackPos(pendingPlaybackPos);
            }
        }
    };

    private void cancelPlayPosAnimation() {
        isAnimationUsed = false;
        if (mAnimator != null) {
            mAnimator.cancel();
            mAnimator = null;
            if (pendingPlaybackPos != NONE_PENDING_PLAYBACK_POS) {
                mPlaybackPos = pendingPlaybackPos;
                pendingPlaybackPos = NONE_PENDING_PLAYBACK_POS;
            }
        }
        isAnimationUsed = true;
    }

    int getPlaybackPos() {
        return (int) mPlaybackPos;
    }

    private void onPlayPosOutOfRange(boolean isEnd) {
        cancelPlayPosAnimation();
        mWaveformView.onPlayPosOutOfRange(isEnd);
    }

    private void onStartDraggingPlayPos() {
        cancelPlayPosAnimation();
        mWaveformView.onStartDraggingPlayPos();
    }

    private void onFinishDraggingPlayPos() {
        cancelPlayPosAnimation();
        mWaveformView.onFinishDraggingPlayPos();
    }

    private void onPlayingLineClicked() {
        mWaveformView.onPlayingLineClicked();
    }

    private void onTimeRangeChanged() {
        mWaveformView.onTimeRangeChanged();
    }

    void release() {
        cancelPlayPosAnimation();
    }
}