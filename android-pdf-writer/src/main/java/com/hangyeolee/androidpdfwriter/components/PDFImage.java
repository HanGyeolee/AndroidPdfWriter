package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;

import com.hangyeolee.androidpdfwriter.utils.Anchor;
import com.hangyeolee.androidpdfwriter.utils.Border;
import com.hangyeolee.androidpdfwriter.utils.Fit;

import com.hangyeolee.androidpdfwriter.listener.Action;

public class  PDFImage extends PDFComponent{
    Bitmap origin = null;
    @Fit.FitInt
    int fit = Fit.NONE;

    @Override
    public void measure(float x, float y) {
        super.measure(x, y);
        int dx = 0, dy = 0;
        // 이미지에서는 gap을 직접 연산함으로 상단 super.measure()에서 연산된 measureX,Y 를 다시 잡아준다.
        if(parent != null){
            dx += parent.measureX + parent.border.size.left + parent.padding.left;
            dy += parent.measureY + parent.border.size.top + parent.padding.top;
        }
        measureX = relativeX + margin.left + dx;
        measureY = relativeY + margin.top + dy;

        if(buffer != null && !buffer.isRecycled()) buffer.recycle();

        float aspectRatio = 1;
        if(this.origin.getWidth() > 0)
            aspectRatio = (float)this.origin.getHeight() / this.origin.getWidth();

        int resizeW = measureWidth;
        int resizeH = measureHeight;
        float gapX;
        float gapY;
        Bitmap scaled;
        if (fit == Fit.SCALE_DOWN){
            fit = Fit.CONTAIN;
            if (this.origin.getWidth() > this.origin.getHeight()) {
                if(resizeW > this.origin.getWidth()){
                    fit = Fit.NONE;
                }
            } else if(resizeH > this.origin.getHeight()){
                fit = Fit.NONE;
            }
        }
        /*
        height 가 measureHeight 보다 크다면?
        상위 컴포넌트의 Height를 업데이트 한다.
        */
        if (height > measureHeight) {
            updateHeight(height - measureHeight);
        }
        buffer = Bitmap.createBitmap(measureWidth, measureHeight, Bitmap.Config.ARGB_8888);
        Canvas transparentCanvas = new Canvas(buffer);
        transparentCanvas.drawColor(Color.TRANSPARENT);

        switch (fit) {
            case Fit.FILL:
                buffer = Bitmap.createScaledBitmap(origin,
                        measureWidth, measureHeight, false);
                break;
            case Fit.CONTAIN:
                if (this.origin.getWidth() > this.origin.getHeight()) {
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
                transparentCanvas.drawBitmap(scaled,
                        (int)gapX, (int)gapY, null);
                scaled.recycle();
                break;
            case Fit.COVER:
                if (this.origin.getWidth() > this.origin.getHeight()) {
                    resizeW = (int) (measureHeight / aspectRatio);
                    gapX = (measureWidth - resizeW);
                    gapY = 0;
                } else {
                    resizeH = (int) (measureWidth * aspectRatio);
                    gapX = 0;
                    gapY = (measureHeight - resizeH);
                }
                // Measure X Anchor and Y Anchor
                gapX = Anchor.getDeltaPixel(anchor.horizontal, gapX);
                gapY = Anchor.getDeltaPixel(anchor.vertical, gapY);
                scaled = Bitmap.createScaledBitmap(origin,
                        resizeW, resizeH, false);
                transparentCanvas.drawBitmap(scaled,
                        (int)gapX, (int)gapY, null);
                scaled.recycle();
                break;
            case Fit.NONE:
                gapX = (this.origin.getWidth() - measureWidth);
                gapY = (this.origin.getHeight() - measureHeight);
                // Measure X Anchor and Y Anchor
                gapX = Anchor.getDeltaPixel(anchor.horizontal, gapX);
                gapY = Anchor.getDeltaPixel(anchor.vertical, gapY);
                transparentCanvas.drawBitmap(origin,
                        (int)-gapX, (int)-gapY, null);
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
    public PDFImage setSize(Integer width, Integer height) {
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
    public PDFImage setBorder(Action<Border, Border> action) {
        super.setBorder(action);
        return this;
    }

    @Override
    public PDFImage setAnchor(Integer vertical, Integer horizontal) {
        super.setAnchor(vertical, horizontal);
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
    public PDFImage(Bitmap bitmap, @Fit.FitInt int fit){
        setImage(bitmap).setFit(fit);
    }

    /**
     * 이미지 컴포넌트의 크기는 기본적으로 이미지의 크기를 가진다.<br>
     * 기본적으로 고정점은 중앙이다.<br>
     * The size of the image component basically has the size of the image.<br>
     * Basically, the anchor is the center.
     * @param bitmap 이미지
     * @return 자기자신
     */
    private PDFImage setImage(Bitmap bitmap){
        this.origin = bitmap;
        width = this.origin.getWidth();
        height = this.origin.getHeight();
        anchor.vertical = Anchor.Center;
        anchor.horizontal = Anchor.Center;
        return this;
    }

    /**
     * 컴포넌트의 크기를 기준으로 이미지 확대, 축소 조건 설정<br>
     * Set image enlargement and reduction conditions based on component size
     * @param fit 조건
     * @return 자기자신
     */
    public PDFImage setFit(@Fit.FitInt int fit){
        this.fit = fit;
        return this;
    }

    @Override
    protected void finalize() throws Throwable {
        if(origin != null && !origin.isRecycled())
            this.origin.recycle();
        super.finalize();
    }

    public static PDFImage build(Bitmap bitmap){return new PDFImage(bitmap);}
}
