package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.hangyeolee.androidpdfwriter.utils.Anchor;
import com.hangyeolee.androidpdfwriter.utils.Border;
import com.hangyeolee.androidpdfwriter.utils.Fit;

import java.util.function.Function;

public class  PDFImage extends PDFComponent{
    Bitmap origin = null;
    @Fit.Type
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
                    gapY = (measureHeight - resizeH);
                } else {
                    resizeW = (int) (resizeH / aspectRatio);
                    gapX = (measureWidth - resizeW);
                    gapY = 0;
                }
                // Measure X Anchor and Y Anchor
                gapX = Anchor.getDeltaPixel(anchor.horizontal, gapX);
                gapY = Anchor.getDeltaPixel(anchor.vertical, gapY);
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
                    gapX = (measureWidth - resizeW);
                    gapY = 0;
                } else {
                    resizeH = (int) (measureWidth / aspectRatio);
                    gapX = 0;
                    gapY = (measureHeight - resizeH);
                }
                // Measure X Anchor and Y Anchor
                gapX = Anchor.getDeltaPixel(anchor.horizontal, gapX);
                gapY = Anchor.getDeltaPixel(anchor.vertical, gapY);
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
                gapX = (resizeW - measureWidth);
                gapY = (resizeH - measureHeight);
                // Measure X Anchor and Y Anchor
                gapX = Anchor.getDeltaPixel(anchor.horizontal, gapX);
                gapY = Anchor.getDeltaPixel(anchor.vertical, gapY);
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

    @Override
    public PDFImage setSize(int width, int height) {
        super.setSize(width, height);
        return this;
    }

    @Override
    public PDFImage setBackgroundColor(int color) {
        super.setBackgroundColor(color);
        return this;
    }

    @Override
    public PDFImage setMargin(Rect margin) {
        super.setMargin(margin);
        return this;
    }

    @Override
    public PDFImage setMargin(int left, int top, int right, int bottom) {
        super.setMargin(left, top, right, bottom);
        return this;
    }

    @Override
    public PDFImage setPadding(Rect padding) {
        super.setPadding(padding);
        return this;
    }

    @Override
    public PDFImage setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        return this;
    }

    @Override
    public PDFImage setBorder(Function<Border, Border> action) {
        super.setBorder(action);
        return this;
    }

    @Override
    public PDFImage setAnchor(int vertical, int horizontal) {
        super.setAnchor(vertical, horizontal);
        return this;
    }

    @Override
    public PDFImage setAnchor(int axis, boolean isHorizontal) {
        super.setAnchor(axis, isHorizontal);
        return this;
    }

    @Override
    protected PDFImage setParent(PDFComponent parent) {
        super.setParent(parent);
        return this;
    }

    public PDFImage(Bitmap bitmap){
        setImage(bitmap);
    }
    public PDFImage(Bitmap bitmap, @Fit.Type int fit){
        setImage(bitmap).setFit(fit);
    }
    public PDFImage setImage(Bitmap bitmap){
        this.origin = bitmap;
        width = bitmap.getWidth();
        height = bitmap.getHeight();
        return this;
    }
    public PDFImage setFit(@Fit.Type int fit){
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
