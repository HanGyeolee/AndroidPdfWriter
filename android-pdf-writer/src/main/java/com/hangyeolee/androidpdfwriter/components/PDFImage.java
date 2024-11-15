package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;

import com.hangyeolee.androidpdfwriter.binary.BinarySerializer;
import com.hangyeolee.androidpdfwriter.utils.Anchor;
import com.hangyeolee.androidpdfwriter.utils.Border;
import com.hangyeolee.androidpdfwriter.utils.Fit;

import com.hangyeolee.androidpdfwriter.listener.Action;

import java.util.Locale;

public class  PDFImage extends PDFResourceComponent{
    Bitmap origin = null;
    @Fit.FitInt
    int fit = Fit.NONE;

    float resizeW;
    float resizeH;
    float gapX;
    float gapY;

    @Override
    public void measure(float x, float y) {
        super.measure(x, y);
        float _width =  (measureWidth - border.size.left - padding.left
                - border.size.right - padding.right);
        float _height =  (measureHeight - border.size.top - padding.top
                - border.size.bottom - padding.bottom);
        if(height > 0){
            /*
            height 가 measureHeight 보다 크다면?
            상위 컴포넌트의 Height를 업데이트 한다.
            */
            while (height > _height) {
                updateHeight(height - _height);
                _height =  (measureHeight - border.size.top - padding.top
                        - border.size.bottom - padding.bottom);
            }
        }

        fitting(_width, _height);

        if(height < 1) {
            /*
            resizeH 가 measureHeight 보다 크다면?
            상위 컴포넌트의 Height를 업데이트 한다.
            */
            while (resizeH > _height) {
                updateHeight(resizeH - _height);
                _height = (measureHeight - border.size.top - padding.top
                        - border.size.bottom - padding.bottom);
            }

            fitting(_width, _height);
        }
    }

    private void fitting(float _width, float _height){
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
                    resizeH =  (_width * aspectRatio);
                    gapX = 0;
                    gapY = (_height - resizeH);
                } else {
                    resizeW =  (_height / aspectRatio);
                    gapX = (_width - resizeW);
                    gapY = 0;
                }
                // Measure X Anchor and Y Anchor
                gapX = Anchor.getDeltaPixel(anchor.horizontal, gapX);
                gapY = Anchor.getDeltaPixel(anchor.vertical, gapY);
                break;
            case Fit.CONTAIN:
                if (this.origin.getWidth() > this.origin.getHeight()) {
                    resizeW =  (_height / aspectRatio);
                    gapX = (_width - resizeW);
                    gapY = 0;
                } else {
                    resizeH =  (_width * aspectRatio);
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
    public void registerResources(BinarySerializer page) {
        if (origin != null) {
            resourceId = page.registerImage(origin);
        }
    }

    @Override
    public StringBuilder draw(BinarySerializer serializer) {
        StringBuilder content = super.draw(serializer);

        float originalWidth = origin.getWidth();
        float originalHeight = origin.getHeight();
        float _width = measureWidth - border.size.left - padding.left
                - border.size.right - padding.right;
        float _height = measureHeight - border.size.top - padding.top
                - border.size.bottom - padding.bottom;

        // 이미지가 그려질 실제 위치 계산
        float x = measureX + border.size.left + padding.left + gapX;
        float y = serializer.getPageHeight() - (measureY + border.size.top + padding.top + gapY + resizeH);

        // 그래픽스 상태 저장 (이미지 변환을 위해 필요)
        PDFGraphicsState.save(content);
        float scaleX = 1;
        float scaleY = 1;

        switch (fit) {
            case Fit.FILL:
                // 1. 먼저 원하는 크기로 스케일 조정
                scaleX = _width / originalWidth;
                scaleY = _height / originalHeight;
                break;

            case Fit.COVER:
            case Fit.CONTAIN:
                // 1. 비율 유지하며 스케일 계산
                scaleX = resizeW / originalWidth;
                scaleY = resizeH / originalHeight;
                break;

            default:
                break;
        }
        // 지정된 크기에 맞게 늘리기
        content.append(String.format(Locale.getDefault(),
                "%.2f 0 0 %.2f %.2f %.2f cm\n",
                scaleX, scaleY, x, y));
        content.append("/").append(resourceId).append(" Do\n");

        PDFGraphicsState.restore(content);
        return null;
    }

    /**
     * GridLayout 안에 있는 이미지의 크기는 같은 열에 이미지 하나만 있는 경우에 적용된다.<br>
     * 같은 열에 더 큰 크기의 컴포넌트가 존재한다면, 해당 컴포넌트의 크기에 따라 Fit 된다.<br>
     * The size of the image in GridLayout is applied when there is only one image in the same column.<br>
     * If a larger component exists in the same column, it will fit according to the size of that component.
     * @deprecated 이 메소드는 더 이상 사용되지 않습니다.<br/>This method is no longer used.
     * @see PDFImage#setSize(Float)
     * @param width 가로 크기
     * @param height 세로 크기
     * @return 자기 자신
     */
    @Override
    @Deprecated
    public PDFImage setSize(Float width, Float height) {
        super.setSize(width, height);
        return this;
    }
    /**
     * Layout 안에 있는 이미지의 가로 길이는 부모의 가로 길이로 무조건 적용된다.<br>
     * The width of the image in the layout is unconditionally applied as the horizontal length of the parent.
     * @param height 세로 크기
     * @return 자기 자신
     */
    public PDFImage setSize(Float height) {
        super.setSize(null, height);
        return this;
    }
    @Override
    public PDFImage setBackgroundColor(int color) {
        super.setBackgroundColor(color);
        return this;
    }

    @Override
    public PDFImage setMargin(RectF margin) {
        super.setMargin(margin);
        return this;
    }

    @Override
    public PDFImage setMargin(float left, float top, float right, float bottom) {
        super.setMargin(left, top, right, bottom);
        return this;
    }

    @Override
    public PDFImage setMargin(float all) {
        super.setMargin(all);
        return this;
    }

    @Override
    public PDFImage setMargin(float horizontal, float vertical) {
        super.setMargin(horizontal, vertical);
        return this;
    }

    @Override
    public PDFImage setPadding(float all) {
        super.setPadding(all);
        return this;
    }

    @Override
    public PDFImage setPadding(float horizontal, float vertical) {
        super.setPadding(horizontal, vertical);
        return this;
    }

    @Override
    public PDFImage setPadding(RectF padding) {
        super.setPadding(padding);
        return this;
    }

    @Override
    public PDFImage setPadding(float left, float top, float right, float bottom) {
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
