package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;

import androidx.annotation.IntDef;

import com.hangyeolee.androidpdfwriter.utils.Fit;

public class  PDFImage extends PDFComponent{
    Bitmap origin = null;
    @Fit.FitType
    int fit = Fit.FILL;

    @Override
    public void measure(int x, int y) {
        super.measure(x, y);

        if(buffer != null && !buffer.isRecycled()) buffer.recycle();

        bufferPaint = new Paint();

        double aspectRatio = (double) origin.getHeight() / (double) origin.getWidth();

        int resizeW = measureWidth;
        int resizeH = measureHeight;
        int gapX;
        int gapY;
        Bitmap scaled;
        Canvas canvas;
        if (fit == Fit.SCALE_DOWN){
            fit = Fit.CONTAIN;
            if (origin.getWidth() > origin.getHeight()) {
                if(resizeW > origin.getWidth()){
                    fit = Fit.NONE;
                }
            } else if(resizeH > origin.getHeight()){
                fit = Fit.NONE;
            }
        }

        switch (fit) {
            case Fit.FILL:
                buffer = Bitmap.createScaledBitmap(origin,
                        measureWidth, measureHeight, false);
                break;
            case Fit.CONTAIN:
                if (origin.getWidth() > origin.getHeight()) {
                    resizeH = (int) (resizeW * aspectRatio);
                    gapX = 0;
                    gapY = (measureHeight - resizeH) >> 1;
                } else {
                    resizeW = (int) (resizeH / aspectRatio);
                    gapX = (measureWidth - resizeW) >> 1;
                    gapY = 0;
                }
                scaled = Bitmap.createScaledBitmap(origin,
                        resizeW, resizeH, false);
                buffer = Bitmap.createBitmap(scaled,
                        gapX, gapY,
                        measureWidth, measureHeight);
                scaled.recycle();
                break;
            case Fit.COVER:
                if (origin.getWidth() > origin.getHeight()) {
                    resizeW = (int) (measureHeight * aspectRatio);
                    gapX = (measureWidth - resizeW) >> 1;
                    gapY = 0;
                } else {
                    resizeH = (int) (measureWidth / aspectRatio);
                    gapX = 0;
                    gapY = (measureHeight - resizeH) >> 1;
                }
                scaled = Bitmap.createScaledBitmap(origin,
                        resizeW, resizeH, false);
                buffer = Bitmap.createBitmap(scaled,
                        gapX, gapY,
                        measureWidth + gapX, measureHeight + gapY);
                scaled.recycle();
                break;
            case Fit.NONE:
                resizeH = origin.getHeight();
                resizeW = origin.getWidth();
                gapX = (resizeW - measureWidth) >> 1;
                gapY = (resizeH - measureHeight) >> 1;
                buffer = Bitmap.createBitmap(origin,
                        -gapX, -gapY,
                        measureWidth, measureHeight);
                break;
            case Fit.SCALE_DOWN:
                break;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawBitmap(buffer,
                measureX + border.size.left + padding.left,
                measureY + border.size.top + padding.top, bufferPaint);
    }

    public PDFImage(PDFComponent parent){
        super(parent);
    }
    public PDFImage(PDFComponent parent, Bitmap bitmap){
        super(parent);
        setImage(bitmap);
    }
    public PDFImage(PDFComponent parent, Bitmap bitmap, @Fit.FitType int fit){
        super(parent);
        setImage(bitmap).setFit(fit);
    }
    public PDFImage setImage(Bitmap bitmap){
        this.origin = bitmap;
        width = bitmap.getWidth();
        height = bitmap.getHeight();
        return this;
    }
    public PDFImage setFit(@Fit.FitType int fit){
        this.fit = fit;
        return this;
    }

    @Override
    protected void finalize() throws Throwable {
        if(origin != null && !origin.isRecycled())
            this.origin.recycle();
        super.finalize();
    }
}
