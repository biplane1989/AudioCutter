package com.example.confetti.confetto;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;

import com.example.confetti.Utils;

public class StarBitmapConfetto extends Confetto {
    private final RectF initViewRect = new RectF();
    private final Path starPath = new Path();
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public StarBitmapConfetto(Context appContext) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.parseColor("#E3A94E"));
        float width = Utils.convertDp2Px(24, appContext);
        float height = Utils.convertDp2Px(24, appContext);
        initViewRect.set(0, 0, width, height);
        scalePath(1f);

    }

    @Override
    public void setInitialScale(float initialScale) {
        super.setInitialScale(initialScale);
        scalePath(initialScale);
    }

    private void scalePath(float scaleValue) {
        float newWidth = scaleValue * initViewRect.width();
        float newHeight = scaleValue * initViewRect.height();

        float left = initViewRect.left + (initViewRect.width() - newWidth) / 2f;
        float top = initViewRect.top + (initViewRect.height() - newHeight) / 2f;
        float right = left + newWidth;
        float bottom = top + newHeight;

        float rectWidth = newWidth / 5f;
        float rectHeight = newHeight / 5f;


        float centerX = (left + right) / 2f;
        float centerY = (top + bottom) / 2f;
        starPath.reset();
        starPath.moveTo(centerX, top);
        starPath.lineTo(centerX - rectWidth / 2f, centerY - rectHeight / 2f);
        starPath.lineTo(left, centerY);
        starPath.lineTo(centerX - rectWidth / 2f, centerY + rectHeight / 2f);
        starPath.lineTo(centerX , top  + newHeight);
        starPath.lineTo(centerX  + rectWidth/2f, centerY + rectHeight / 2f);
        starPath.lineTo(left  + newWidth, centerY);
        starPath.lineTo(centerX + rectWidth/2f , centerY - rectHeight/2f);
        starPath.close();
    }


    @Override
    public int getWidth() {
        return (int)initViewRect.width();
    }

    @Override
    public int getHeight() {
        return (int)initViewRect.height();
    }

    @Override
    protected void drawInternal(Canvas canvas, Matrix matrix, Paint paint, float x, float y, float rotation, float scale, float percentAnimated) {
        canvas.save();
        canvas.translate(x, y);
        scalePath(scale);
        canvas.rotate(rotation, initViewRect.centerX(), initViewRect.centerY());
        canvas.drawPath(starPath, mPaint);
        canvas.restore();
    }
}
