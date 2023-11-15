package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import androidx.annotation.ColorInt;

import com.hangyeolee.androidpdfwriter.utils.TextAlign;
import com.hangyeolee.androidpdfwriter.utils.Border;

import com.hangyeolee.androidpdfwriter.listener.Action;

public class PDFText extends PDFComponent {
    String text = null;
    Layout.Alignment align = null;
    StaticLayout layout = null;

    @Override
    public void measure(float x, float y) {
        super.measure(x, y);

        int _width = (int) (measureWidth - border.size.left - padding.left
                - border.size.right - padding.right);
        int _height = (int) (measureHeight - border.size.top - padding.top
                - border.size.bottom - padding.bottom);

        if(text != null && bufferPaint != null) {
            Rect textRect = new Rect();
            bufferPaint.getTextBounds(text, 0, text.length(), textRect);
            int textWidth = textRect.width();
            int textHeight = textRect.height();
            layout = StaticLayout.Builder.obtain(text,
                            0,
                            text.length(),
                            (TextPaint) bufferPaint,
                            _width)
                    .setAlignment(align)
                    .build();

            if (buffer != null && !buffer.isRecycled()) {
                buffer.recycle();
                buffer = null;
            }

            int updatedHeight = layout.getHeight();
            Bitmap origin = Bitmap.createBitmap(_width, updatedHeight, Bitmap.Config.ARGB_8888);
            // 텍스트를 캔버스를 통해 비트맵에 그린다.
            layout.draw(new Canvas(origin));
            layout = null;
            /*
            상대 위치+updatedHeight 가 measureHeight 보다 크다면?
            상위 컴포넌트의 Height를 업데이트 한다.
            */
            if (updatedHeight > _height) {
                updateHeight(updatedHeight - _height);
                _width = (int) (measureWidth - border.size.left - padding.left
                        - border.size.right - padding.right);
            }
            measureHeight = (int) (updatedHeight + border.size.top + padding.top
                    + border.size.bottom + padding.bottom);
            buffer = Bitmap.createBitmap(origin,
                    0, 0,
                    _width, updatedHeight);
            origin.recycle();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawBitmap(buffer,
                measureX + border.size.left + padding.left,
                measureY + border.size.top + padding.top,
                bufferPaint);
    }

    @Override
    public PDFText setSize(Integer width, Integer height) {
        super.setSize(width, height);
        return this;
    }

    @Override
    public PDFText setBackgroundColor(int color) {
        super.setBackgroundColor(color);
        return this;
    }

    @Override
    public PDFText setMargin(Rect margin) {
        super.setMargin(margin);
        return this;
    }

    @Override
    public PDFText setMargin(int left, int top, int right, int bottom) {
        super.setMargin(left, top, right, bottom);
        return this;
    }

    @Override
    public PDFText setPadding(Rect padding) {
        super.setPadding(padding);
        return this;
    }

    @Override
    public PDFText setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        return this;
    }

    @Override
    public PDFText setBorder(Action<Border, Border> action) {
        super.setBorder(action);
        return this;
    }

    @Override
    public PDFText setAnchor(Integer vertical, Integer horizontal) {
        super.setAnchor(vertical, horizontal);
        return this;
    }

    @Override
    protected PDFText setParent(PDFComponent parent) {
        super.setParent(parent);
        return this;
    }

    public PDFText(String text){
        this.setText(text).setTextPaint(null);
    }
    public PDFText(String text, TextPaint paint){
        this.setText(text).setTextPaint(paint);
    }
    public PDFText setText(String text){
        this.text = text;
        return this;
    }
    public PDFText setTextPaint(TextPaint paint){
        if(paint == null){
            this.bufferPaint = new TextPaint();
            this.align = Layout.Alignment.ALIGN_NORMAL;
            this.bufferPaint.setTextSize(16);
        }else{
            this.bufferPaint = paint;
        }
        return this;
    }
    public PDFText setTextColor(@ColorInt int color){
        if(bufferPaint == null){
            this.bufferPaint = new TextPaint();
            this.align = Layout.Alignment.ALIGN_NORMAL;
            this.bufferPaint.setTextSize(16);
        }
        this.bufferPaint.setColor(color);
        return this;
    }
    public PDFText setTextAlign(@TextAlign.TextAlignInt int align){
        switch (align){
            case TextAlign.Start:
                this.align = Layout.Alignment.ALIGN_NORMAL;
                break;
            case TextAlign.Center:
                this.align = Layout.Alignment.ALIGN_CENTER;
                break;
            case TextAlign.End:
                this.align = Layout.Alignment.ALIGN_OPPOSITE;
                break;
        }
        return this;
    }

    public static PDFText build(String text){return new PDFText(text);}

}
