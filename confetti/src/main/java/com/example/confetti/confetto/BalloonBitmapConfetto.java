package com.example.confetti.confetto;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;

import com.example.confetti.Utils;

public class BalloonBitmapConfetto extends Confetto {
    private final RectF initViewRect = new RectF();
    private final Paint mExternalPaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mInternalPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float initialRadius = 0f;

    public BalloonBitmapConfetto(Context appContext) {
        mExternalPaint1.setColor(Color.parseColor("#E3A94E"));
        mExternalPaint1.setStyle(Paint.Style.FILL);
        mExternalPaint1.setAlpha(80);
        initialRadius = Utils.convertDp2Px(20, appContext);

        mInternalPaint2.setColor(Color.WHITE);
        scaleRadius(1);

    }

    @Override
    public void setInitialScale(float initialScale) {
        super.setInitialScale(initialScale);
        scaleRadius(initialScale);
    }

    private void scaleRadius(float scaleValue) {
        if (scaleValue <= 0) {
            return;
        }
        BlurMaskFilter filter = new BlurMaskFilter(scaleValue * initialRadius, BlurMaskFilter.Blur.NORMAL);
        mInternalPaint2.setMaskFilter(filter);
    }


    @Override
    public int getWidth() {
        return (int) initViewRect.width();
    }

    @Override
    public int getHeight() {
        return (int) initViewRect.height();
    }

    @Override
    protected void drawInternal(Canvas canvas, Matrix matrix, Paint paint, float x, float y, float rotation, float scale, float percentAnimated) {
        canvas.save();
        canvas.translate(x, y);
        scaleRadius(scale);
        canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, initialRadius * scale, mExternalPaint1);
        canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, initialRadius * scale, mInternalPaint2);
        canvas.restore();
    }
}
