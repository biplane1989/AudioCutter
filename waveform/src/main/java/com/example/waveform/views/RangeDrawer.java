package com.example.waveform.views;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.example.waveform.R;
import com.example.waveform.Utils;

public class RangeDrawer {
    private final int TIME_FORMAT_INCLUDED_HOUR_ONE_ZERO = 1;
    private final int TIME_FORMAT_INCLUDED_HOUR_TWO_ZERO = 2;
    private final int TIME_FORMAT_INCLUDED_MINUTE_ONE_ZERO = 3;
    private final int TIME_FORMAT_INCLUDED_MINUTE_TWO_ZERO = 4;
    private final int TIME_FORMAT_INCLUDED_SECOND_ONE_ZERO = 5;
    private final int TIME_FORMAT_INCLUDED_SECOND_TWO_ZERO = 6;


    private static Bitmap mCursorLeftBitmap;
    private static Bitmap mCursorRightBitmap;
    protected RectF mCursorLeftRect = new RectF();
    protected RectF mCursorRightRect = new RectF();
    protected RectF mLeftSelectionRect = new RectF();
    protected RectF mRightSelectionRect = new RectF();
    private WaveformView mWaveformView;
    private final Paint mRangeSelectionPaint;
    private final Paint mLineSelectionPaint;
    private final Paint mTimeRangePaint;
    private final Paint mDurationPaint;
    private String[] listTimeStrSegments;
    private float[] listTimePosXSegments;
    private int mTimeFormat = TIME_FORMAT_INCLUDED_HOUR_TWO_ZERO;


    private final float cursorSize;

    public RangeDrawer(WaveformView waveformView, int rangeSelectionColor, int lineSelectionColor, int timeRangeColor) {
        mWaveformView = waveformView;
        if (mCursorLeftBitmap == null) {
            mCursorLeftBitmap = BitmapFactory.decodeResource(mWaveformView.getResources(), R.drawable.wave_view_left_ic);
        }
        if (mCursorRightBitmap == null) {
            mCursorRightBitmap = BitmapFactory.decodeResource(mWaveformView.getResources(), R.drawable.wave_view_right_ic);
        }
        cursorSize = Utils.Companion.dpToPx(mWaveformView.getContext(), WaveformView.DEFAULT_CURSOR_SIZE_DP);

        mRangeSelectionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRangeSelectionPaint.setColor(rangeSelectionColor);
        mRangeSelectionPaint.setStyle(Paint.Style.FILL);
        mRangeSelectionPaint.setAntiAlias(true);

        mLineSelectionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLineSelectionPaint.setColor(lineSelectionColor);
        mLineSelectionPaint.setAntiAlias(true);
        mLineSelectionPaint.setStyle(Paint.Style.STROKE);
        mLineSelectionPaint.setStrokeWidth(4f);

        mTimeRangePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTimeRangePaint.setColor(timeRangeColor);
        mTimeRangePaint.setTextSize(Utils.Companion.dpToPx(mWaveformView.getContext(), WaveformView.DEFAULT_RANGE_TIME_TEXT_SIZE));

        mDurationPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDurationPaint.setColor(timeRangeColor);
        mDurationPaint.setTextSize(Utils.Companion.dpToPx(mWaveformView.getContext(), WaveformView.DEFAULT_RANGE_DURATION_TEXT_SIZE));
    }

    private void splitTimeInterval() {
        int offset = mWaveformView.getOffset();
        int waveformWidth = mWaveformView.getWidth();
        long maxTime = mWaveformView.pixelsToMillisecs(offset + waveformWidth);
        int timeFormat = chooseTimeFormat(maxTime);


        int step = waveformWidth / WaveformView.RANGE_SELECTION_TOTAL_INTERVAL;
        for (int i = 0; i < WaveformView.RANGE_SELECTION_TOTAL_INTERVAL; ++i) {
            int startX = i * step;
            int endX = (i + 1) * step;
            float centerX = (startX + endX) / 2f;
            listTimeStrSegments[i] = toTimeStr(mWaveformView.pixelsToMillisecs(offset + (int) ((i + 0.5) * step)), timeFormat);
            listTimePosXSegments[i] = centerX - mTimeRangePaint.measureText(listTimeStrSegments[i]) / 2f;
        }

    }

    public void init() {
        mTimeFormat = chooseTimeFormat(mWaveformView.duration());
        listTimeStrSegments = new String[WaveformView.RANGE_SELECTION_TOTAL_INTERVAL];
        listTimePosXSegments = new float[WaveformView.RANGE_SELECTION_TOTAL_INTERVAL];
        splitTimeInterval();
    }

    private int chooseTimeFormat(long duration) {
        int hours = (int) (duration / (36e5));
        long remainingTime = (long) (duration - hours * 36e5);
        int minutes = (int) (remainingTime / 6e4);
        remainingTime = (long) (remainingTime - minutes * 6e4);
        int seconds = (int) (remainingTime / 1e3);
        if (hours > 0) {
            if (hours > 9) {
                return TIME_FORMAT_INCLUDED_HOUR_TWO_ZERO;
            } else {
                return TIME_FORMAT_INCLUDED_HOUR_ONE_ZERO;
            }
        } else {
            if (minutes > 0) {
                if (minutes > 9) {
                    return TIME_FORMAT_INCLUDED_MINUTE_TWO_ZERO;
                } else {
                    return TIME_FORMAT_INCLUDED_MINUTE_ONE_ZERO;
                }
            } else {
                if (seconds > 9) {
                    return TIME_FORMAT_INCLUDED_SECOND_TWO_ZERO;
                } else {
                    return TIME_FORMAT_INCLUDED_SECOND_ONE_ZERO;
                }
            }
        }
    }

    void onDraw(final Canvas canvas) {
        if (!mWaveformView.isInitialized()) {
            return;
        }
        computeParamsToDraw();
        canvas.drawLine(
                mCursorLeftRect.left + 3f, mCursorLeftRect.top,
                mCursorLeftRect.left, mCursorRightRect.bottom,
                mLineSelectionPaint);
        canvas.drawLine(
                mCursorRightRect.right, mCursorLeftRect.top,
                mCursorRightRect.right - 3f, mCursorRightRect.bottom,
                mLineSelectionPaint);
      /*  canvas.drawRect(mLeftSelectionRect, mRangeSelectionPaint);
        canvas.drawRect(mRightSelectionRect, mRangeSelectionPaint);*/

        canvas.drawBitmap(mCursorLeftBitmap, null, mCursorLeftRect, null);
        canvas.drawBitmap(mCursorRightBitmap, null, mCursorRightRect, null);
        String startTimeText = toTimeStr(mWaveformView.pixelsToMillisecs(mWaveformView.getSelectionStart()), mTimeFormat);
        String endTimeText = toTimeStr(mWaveformView.pixelsToMillisecs(mWaveformView.getSelectionEnd()), mTimeFormat);
        canvas.drawText(startTimeText, mCursorLeftRect.left, mCursorLeftRect.top - 20f, mTimeRangePaint);
        canvas.drawText(endTimeText, mCursorRightRect.right - mTimeRangePaint.measureText(endTimeText), mCursorLeftRect.top - 20f, mTimeRangePaint);
        drawTimeDistanceValue(canvas);
        drawTimeInterval(canvas);
    }

    private void drawTimeInterval(final Canvas canvas) {
        for (int i = 0; i < WaveformView.RANGE_SELECTION_TOTAL_INTERVAL; ++i) {
            canvas.drawText(listTimeStrSegments[i], listTimePosXSegments[i], mCursorLeftRect.top - 60f, mTimeRangePaint);
        }
    }

    private void drawTimeDistanceValue(final Canvas canvas) {
        float centerX = mWaveformView.getWaveformWidth() / 2f;
        float y = mWaveformView.getDrawingEndY() + (mWaveformView.getHeight() - (mWaveformView.getDrawingEndY() - mWaveformView.getDrawingStartY())) / 4f;
        String durationStr = toTimeStr(mWaveformView.getPlaybackInMs(), mTimeFormat);
        canvas.drawText(durationStr, centerX - mDurationPaint.measureText(durationStr) / 2f, y, mDurationPaint);
    }

    private void computeParamsToDraw() {
        int selectionStart = mWaveformView.getSelectionStart();
        int offset = mWaveformView.getOffset();
        int selectionEnd = mWaveformView.getSelectionEnd();
        int drawingStartY = mWaveformView.getDrawingStartY();
        int drawingEndY = mWaveformView.getDrawingEndY();

        mWaveformView.getDrawingEndY();
        mCursorLeftRect.left = selectionStart - offset;
        mCursorLeftRect.right = mCursorLeftRect.left + cursorSize;
        mCursorLeftRect.top = drawingStartY - cursorSize;
        mCursorLeftRect.bottom = mCursorLeftRect.top + cursorSize;

        mCursorRightRect.left = selectionEnd - offset - cursorSize;
        mCursorRightRect.right = mCursorRightRect.left + cursorSize;
        mCursorRightRect.top = drawingEndY;
        mCursorRightRect.bottom = mCursorRightRect.top + cursorSize;

        mLeftSelectionRect.left = 0;
        mLeftSelectionRect.right = mCursorLeftRect.left;
        mLeftSelectionRect.top = mCursorLeftRect.top;
        mLeftSelectionRect.bottom = mCursorRightRect.bottom;

        mRightSelectionRect.left = mCursorRightRect.right;
        mRightSelectionRect.right = mWaveformView.getWaveformWidth();
        mRightSelectionRect.top = mCursorLeftRect.top;
        mRightSelectionRect.bottom = mCursorRightRect.bottom;
    }

    private String toTimeStr(long timeInMs, int timeFormat) {
        int hours = (int) (timeInMs / (36e5));
        long remainingTime = (long) (timeInMs - hours * 36e5);
        int minutes = (int) (remainingTime / 6e4);
        remainingTime = (long) (remainingTime - minutes * 6e4);
        int seconds = (int) (remainingTime / 1e3);
        remainingTime = (long) (remainingTime - seconds * 1e3);
        int time = (int) (remainingTime / 10);
        switch (timeFormat) {
            case TIME_FORMAT_INCLUDED_HOUR_ONE_ZERO:
                return String.format("%01d:%02d:%02d.%02d", hours, minutes, seconds, time);
            case TIME_FORMAT_INCLUDED_HOUR_TWO_ZERO:
                return String.format("%02d:%02d:%02d.%02d", hours, minutes, seconds, time);
            case TIME_FORMAT_INCLUDED_MINUTE_ONE_ZERO:
                return String.format("%01d:%02d.%02d", minutes, seconds, time);
            case TIME_FORMAT_INCLUDED_MINUTE_TWO_ZERO:
                return String.format("%02d:%02d.%02d", minutes, seconds, time);
            case TIME_FORMAT_INCLUDED_SECOND_ONE_ZERO:
                return String.format("%01d.%02d", seconds, time);
            case TIME_FORMAT_INCLUDED_SECOND_TWO_ZERO:
                return String.format("%02d.%02d", seconds, time);
        }
        return String.format("%02d:%02d:%02d.%02d", hours, minutes, seconds, time);
    }

    void onWaveformZoom() {
        splitTimeInterval();
    }

    void onChangeOffset() {
        splitTimeInterval();
    }
}
