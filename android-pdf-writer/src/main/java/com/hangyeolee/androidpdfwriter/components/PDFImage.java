package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.Log;

import androidx.annotation.ColorInt;

import com.hangyeolee.androidpdfwriter.PDFBuilder;
import com.hangyeolee.androidpdfwriter.binary.BinaryConverter;
import com.hangyeolee.androidpdfwriter.binary.BinarySerializer;
import com.hangyeolee.androidpdfwriter.utils.Anchor;
import com.hangyeolee.androidpdfwriter.utils.Border;
import com.hangyeolee.androidpdfwriter.utils.Fit;

import com.hangyeolee.androidpdfwriter.listener.Action;
import com.hangyeolee.androidpdfwriter.utils.Zoomable;

import java.util.Locale;

public class  PDFImage extends PDFResourceComponent{
    Bitmap origin = null;
    Bitmap resize = null;

    float resizeW;
    float resizeH;
    float gapX;
    float gapY;

    boolean compressEnable = false;

    @Override
    public void measure(float x, float y) {
        super.measure(x, y);
        float _width =  (measureWidth - border.size.left - padding.left
                - border.size.right - padding.right);
        float _height = getTotalHeight();
        if(height > 0){
            /*
            height 가 measureHeight 보다 크다면?
            상위 컴포넌트의 Height를 업데이트 한다.
            */
            while (height > _height) {
                updateHeight(height - _height);
                _height = getTotalHeight();
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
        if(resize != null){
            resourceId = page.registerImage(resize);
        } else if (origin != null) {
            resourceId = page.registerImage(origin);
        }
    }

    @Override
    public StringBuilder draw(BinarySerializer serializer) {
        float originWidth = origin.getWidth();
        float originHeight = origin.getHeight();
        float _width, _height;
        float availableWidth = measureWidth - border.size.left - padding.left
                - border.size.right - padding.right;
        float availableHeight = measureHeight - border.size.top - padding.top
                - border.size.bottom - padding.bottom;
        if(fit == Fit.COVER || fit == Fit.CONTAIN){
            _width = resizeW;
            _height = resizeH;
        }
        else {
            _width = availableWidth;
            _height = availableHeight;
        }

        // 이미지가 그려질 실제 위치 계산
        float x = Zoomable.getInstance().transform2PDFWidth(measureX + border.size.left + padding.left + gapX);
        float y = Zoomable.getInstance().transform2PDFHeight(measureY + border.size.top + padding.top + gapY + _height);

        if(compressEnable) {
            if (_width < originWidth || _height < originHeight) {
                if(resize != null){
                    resize.recycle();
                }
                float scaleX = _width / originWidth;
                float scaleY = _height / originHeight;
                Matrix matrix = new Matrix();
                matrix.setScale(scaleX, scaleY);

                try {
                    resize = Bitmap.createBitmap(
                            origin,
                            0, 0,
                            origin.getWidth(),
                            origin.getHeight(),
                            matrix,
                            true
                    );
                } catch (OutOfMemoryError e) {
                    // 메모리 부족 시 리사이징 실패 처리
                    Log.e(PDFBuilder.TAG, "OutOfMemoryError: PDFImage. " +
                            "The original image is used because there is not enough memory required for image reduction resizing.");
                    if(resize != null){
                        resize.recycle();
                        resize = null;
                    }
                }
            }
        }

        StringBuilder content = super.draw(serializer);
        // 그래픽스 상태 저장 (이미지 변환을 위해 필요)
        PDFGraphicsState.save(content);
        // 지정된 크기에 맞게 늘리기
        content.append(String.format(Locale.getDefault(),
                "%s 0 0 %s %s %s cm\r\n",
                BinaryConverter.formatNumber(_width),
                BinaryConverter.formatNumber(_height),
                BinaryConverter.formatNumber(x),
                BinaryConverter.formatNumber(y))
        );
        content.append("/").append(resourceId).append(" Do\r\n");

        PDFGraphicsState.restore(content);
        return null;
    }

    /**
     * GridLayout 안에 있는 이미지의 크기는 같은 열에 이미지 하나만 있는 경우에 적용된다.<br>
     * 같은 열에 더 큰 크기의 컴포넌트가 존재한다면, 해당 컴포넌트의 크기에 따라 Fit 된다.<br>
     * The size of the image in GridLayout is applied when there is only one image in the same column.<br>
     * If a larger component exists in the same column, it will fit according to the size of that component.
     * @deprecated 이 메소드는 더 이상 사용되지 않습니다.<br/>This method is no longer used.
     * @see PDFImage#setHeight(Float)
     * @param width 가로 크기
     * @param height 세로 크기
     * @return 자기 자신
     */
    @Override
    @Deprecated
    public PDFImage setSize(Number width, Number height) {
        super.setSize(width, height);
        return this;
    }
    /**
     * Layout 안에 있는 이미지의 가로 길이는 부모의 가로 길이로 무조건 적용된다.<br>
     * The width of the image in the layout is unconditionally applied as the horizontal length of the parent.
     * @param height 세로 크기
     * @return 자기 자신
     */
    public PDFImage setHeight(Float height) {
        super.setSize(null, height);
        return this;
    }

    /**
     * 이미지 압축 여부에 대해서 설정한다.<br>
     * compressEnable이 false 면 이미지 리소스에 원본으로 저장한다.<br>
     * true 면, 지정한 크기로 원본 이미지를 축소한 뒤 이미지 리소스에 축소한 이미지를 저장한다.<br>
     * 단, 원본 이미지보다 크게 확대하면 무조건 원본으로 저장한다.<br>
     * Sets whether or not the image is compressed.<br>
     * If compressEnable is false, save it as the source in the image resource.<br>
     * If true, shrink the original image to the specified size and store the reduced image in the image resource.<br>
     * However, if it is enlarged larger than the original image, it will be saved as the original unconditionally.
     * @param compressEnable 압축 허용 여부.<br>
     * @return 자기자신
     */
    public PDFImage setCompress(boolean compressEnable) {
        this.compressEnable = compressEnable;
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
    public PDFImage setBorder(float size, @ColorInt int color) {
        super.setBorder(size, color);
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
