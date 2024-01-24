package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;

import com.hangyeolee.androidpdfwriter.utils.Anchor;
import com.hangyeolee.androidpdfwriter.utils.Border;
import com.hangyeolee.androidpdfwriter.utils.Fit;

import com.hangyeolee.androidpdfwriter.listener.Action;

public class  PDFImage extends PDFComponent{
    Bitmap origin = null;
    @Fit.FitInt
    int fit = Fit.NONE;

    int resizeW;
    int resizeH;
    float gapX;
    float gapY;

    @Override
    public void measure(float x, float y) {
        super.measure(x, y);
        int _width = Math.round (measureWidth - border.size.left - padding.left
                - border.size.right - padding.right);
        int _height = Math.round (measureHeight - border.size.top - padding.top
                - border.size.bottom - padding.bottom);

        fitting(_width, _height);

        /*
        height 가 measureHeight 보다 크다면?
        상위 컴포넌트의 Height를 업데이트 한다.
        */
        while (resizeH > _height) {
            updateHeight(resizeH - _height);
            _height = Math.round (measureHeight - border.size.top - padding.top
                    - border.size.bottom - padding.bottom);
        }

        fitting(_width, _height);
    }

    private void fitting(int _width, int _height){
        float aspectRatio = 1;
        if(this.origin.getWidth() > 0)
            aspectRatio = (float)this.origin.getHeight() / this.origin.getWidth();

        resizeW = _width;
        resizeH = _height;

        if(resizeW < 1 && resizeH > 0){
            resizeW = this.origin.getWidth() * resizeH/this.origin.getHeight();
        } else if(resizeH < 1 && resizeW > 0){
            resizeH = this.origin.getHeight() * resizeW/this.origin.getWidth();
        } else if(resizeW < 1){
            resizeW = this.origin.getWidth();
            resizeH = this.origin.getHeight();
        }

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

        switch (fit) {
            case Fit.FILL:
            case Fit.SCALE_DOWN:
                break;
            case Fit.COVER:
                if (this.origin.getWidth() > this.origin.getHeight()) {
                    resizeH = Math.round (_width * aspectRatio);
                    gapX = 0;
                    gapY = (_height - resizeH);
                } else {
                    resizeW = Math.round (_height / aspectRatio);
                    gapX = (_width - resizeW);
                    gapY = 0;
                }
                // Measure X Anchor and Y Anchor
                gapX = Anchor.getDeltaPixel(anchor.horizontal, gapX);
                gapY = Anchor.getDeltaPixel(anchor.vertical, gapY);
                break;
            case Fit.CONTAIN:
                if (this.origin.getWidth() > this.origin.getHeight()) {
                    resizeW = Math.round (_height / aspectRatio);
                    gapX = (_width - resizeW);
                    gapY = 0;
                } else {
                    resizeH = Math.round (_width * aspectRatio);
                    gapX = 0;
                    gapY = (_height - resizeH);
                }
                // Measure X Anchor and Y Anchor
                gapX = Anchor.getDeltaPixel(anchor.horizontal, gapX);
                gapY = Anchor.getDeltaPixel(anchor.vertical, gapY);
                break;
            case Fit.NONE:
                gapX = (this.origin.getWidth() - _width);
                gapY = (this.origin.getHeight() - _height);
                // Measure X Anchor and Y Anchor
                gapX = Anchor.getDeltaPixel(anchor.horizontal, gapX);
                gapY = Anchor.getDeltaPixel(anchor.vertical, gapY);
                break;
        }
    }

    @Override
    protected void createBuffer(){
        int _width = Math.round (measureWidth - border.size.left - padding.left
                - border.size.right - padding.right);
        int _height = Math.round (measureHeight - border.size.top - padding.top
                - border.size.bottom - padding.bottom);

        Bitmap scaled;
        buffer = Bitmap.createBitmap(_width, _height, Bitmap.Config.ARGB_8888);
        Canvas transparentCanvas = new Canvas(buffer);
        transparentCanvas.drawColor(Color.TRANSPARENT);

        Canvas canvas;
        Rect src,dst;

        switch (fit) {
            case Fit.FILL:
                src = new Rect(0, 0, origin.getWidth(), origin.getHeight());
                dst = new Rect(0, 0, resizeW, resizeH);
                transparentCanvas.drawBitmap(origin, src, dst, bufferPaint);
                break;
            case Fit.COVER:
            case Fit.CONTAIN:
                src = new Rect(0, 0, origin.getWidth(), origin.getHeight());
                dst = new Rect(0, 0, resizeW, resizeH);

                scaled = Bitmap.createBitmap( resizeW, resizeH, Bitmap.Config.ARGB_8888);

                canvas = new Canvas(scaled);
                canvas.drawBitmap(origin, src, dst, bufferPaint);

                transparentCanvas.drawBitmap(scaled,
                        Math.round(gapX), Math.round(gapY), bufferPaint);
                scaled.recycle();
                break;

            case Fit.NONE:
                transparentCanvas.drawBitmap(origin,
                        Math.round(-gapX), Math.round(-gapY), bufferPaint);
                break;
            case Fit.SCALE_DOWN:
                break;
        }

        int dx = 0, dy = 0;
        // 이미지에서는 gap을 직접 연산함으로 상단 super.measure()에서 연산된 measureX,Y 를 다시 잡아준다.
        if(parent != null){
            dx += parent.measureX + parent.border.size.left + parent.padding.left;
            dy += parent.measureY + parent.border.size.top + parent.padding.top;
        }
        measureX = relativeX + margin.left + dx;
        measureY = relativeY + margin.top + dy;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        createBuffer();

        canvas.drawBitmap(buffer,
                measureX + border.size.left + padding.left,
                measureY + border.size.top + padding.top, bufferPaint);

        deleteBuffer();
    }

    /**
     * GridLayout 안에 있는 이미지의 크기는 같은 열에 이미지 하나만 있는 경우에 적용된다.<br>
     * 같은 열에 더 큰 크기의 컴포넌트가 존재한다면, 해당 컴포넌트의 크기에 따라 Fir 된다.<br>
     * The size of the image in GridLayout is applied when there is only one image in the same column.<br>
     * If a larger component exists in the same column, it will be fired according to the size of that component.
     * @param width 가로 크기
     * @param height 세로 크기
     * @return 자기 자신
     */
    @Override
    public PDFImage setSize(Float width, Float height) {
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
    public PDFImage setMargin(int all) {
        super.setMargin(all);
        return this;
    }

    @Override
    public PDFImage setMargin(int horizontal, int vertical) {
        super.setMargin(horizontal, vertical);
        return this;
    }

    @Override
    public PDFImage setPadding(int all) {
        super.setPadding(all);
        return this;
    }

    @Override
    public PDFImage setPadding(int horizontal, int vertical) {
        super.setPadding(horizontal, vertical);
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
    public PDFImage setAnchor(Integer horizontal, Integer vertical) {
        super.setAnchor(horizontal, vertical);
        return this;
    }

    @Override
    protected PDFImage setParent(PDFComponent parent) {
        super.setParent(parent);
        return this;
    }

    public PDFImage(Bitmap bitmap){
        bufferPaint = new Paint();
        bufferPaint.setFlags(TextPaint.FILTER_BITMAP_FLAG | TextPaint.LINEAR_TEXT_FLAG | TextPaint.ANTI_ALIAS_FLAG);
        setImage(bitmap);
    }
    public PDFImage(Bitmap bitmap, @Fit.FitInt int fit){
        bufferPaint = new Paint();
        bufferPaint.setFlags(TextPaint.FILTER_BITMAP_FLAG | TextPaint.LINEAR_TEXT_FLAG | TextPaint.ANTI_ALIAS_FLAG);
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
