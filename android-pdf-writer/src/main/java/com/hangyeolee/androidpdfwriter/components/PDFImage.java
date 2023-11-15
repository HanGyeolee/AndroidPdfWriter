package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.hangyeolee.androidpdfwriter.utils.Fit;

public class PDFImage extends PDFComponent{
    Bitmap origin;
    int fit = Fit.FILL;

    @Override
    public void measure(int globalX, int globalY, PDFComponent parent) {
        super.measure(globalX, globalY, parent);

        if(buffer != null && !buffer.isRecycled()) buffer.recycle();

        bufferPaint = new Paint();

        double aspectRatio = (double) origin.getHeight() / (double) origin.getWidth();
        if(parent == null){
            buffer = Bitmap.createBitmap(origin,
                    0, 0,
                    measureWidth, measureHeight);
        }
        else {
            int resizeW =  parent.measureWidth
                    - parent.border.size.left - parent.border.size.right
                    - parent.padding.left - parent.padding.right;
            int resizeH = parent.measureHeight
                    - parent.border.size.top - parent.border.size.bottom
                    - parent.padding.top - parent.padding.bottom;
            int gapX;
            int gapY;
            Bitmap scaled;
            Canvas canvas;
            switch (fit) {
                case Fit.FILL:
                    buffer = Bitmap.createScaledBitmap(origin,
                            measureWidth, measureHeight, false);
                    break;
                case Fit.CONTAIN:
                    if (origin.getWidth() > origin.getHeight()) {
                        resizeH = (int) (resizeW * aspectRatio);
                    } else {
                        resizeW = (int) (resizeH / aspectRatio);
                    }
                    buffer = Bitmap.createScaledBitmap(origin,
                            resizeW, resizeH, false);
                    break;
                case Fit.COVER:
                    if (resizeW > resizeH) {
                        resizeH = (int) (resizeW * aspectRatio);
                    } else {
                        resizeW = (int) (resizeH / aspectRatio);
                    }
                    gapX = (resizeW - measureWidth) >> 1;
                    gapY = (resizeH - measureHeight) >> 1;
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
                    gapX = (int)((resizeW - measureWidth) * 0.5);
                    gapY = (int)((resizeH - measureHeight) * 0.5);
                    buffer = Bitmap.createBitmap(measureWidth, measureHeight, Bitmap.Config.ARGB_8888);
                    canvas = new Canvas(buffer);
                    canvas.drawBitmap(origin, -gapX, -gapY, bufferPaint);
                    break;
                case Fit.SCALE_DOWN:
                    int type = Fit.CONTAIN;
                    if (origin.getWidth() > origin.getHeight()) {
                        if(resizeW > origin.getWidth()){
                            type = Fit.NONE;
                        }
                    } else if(resizeH > origin.getHeight()){
                        type = Fit.NONE;
                    }

                    if(type == Fit.NONE){
                        resizeH = origin.getHeight();
                        resizeW = origin.getWidth();
                        gapX = (int)((measureWidth - resizeW) * 0.5);
                        gapY = (int)((measureHeight - resizeH) * 0.5);
                        buffer = Bitmap.createBitmap(measureWidth, measureHeight, Bitmap.Config.ARGB_8888);
                        canvas = new Canvas(buffer);
                        canvas.drawBitmap(origin, gapX, gapY, bufferPaint);
                    }
                    else{
                        if (origin.getWidth() > origin.getHeight()) {
                            resizeH = (int) (resizeW * aspectRatio);
                        } else {
                            resizeW = (int) (resizeH / aspectRatio);
                        }
                        buffer = Bitmap.createScaledBitmap(origin,
                                resizeW, resizeH, false);
                    }
                    break;
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(buffer, measureX, measureY, bufferPaint);
    }

    public PDFImage(Bitmap bitmap){
        this.origin = bitmap;
        width = bitmap.getWidth();
        height = bitmap.getHeight();
    }
    public PDFImage(Bitmap bitmap, int fit){
        this.origin = bitmap;
        width = bitmap.getWidth();
        height = bitmap.getHeight();
        this.fit = fit;
        if(this.fit < 0 || this.fit > 4) this.fit = Fit.FILL;
    }
}
