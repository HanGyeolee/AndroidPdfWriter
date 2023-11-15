package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.StaticLayout;
import android.text.TextPaint;

import com.hangyeolee.androidpdfwriter.exceptions.LayoutSizeException;
import com.hangyeolee.androidpdfwriter.utils.Anchor;
import com.hangyeolee.androidpdfwriter.utils.Border;

import java.util.function.Function;

public class PDFText extends PDFComponent{
    String text = null;
    TextPaint paint = null;
    StaticLayout layout;

    @Override
    public void measure(int x, int y) {
        super.measure(x, y);

        int _width = measureWidth - border.size.left - padding.left
                - border.size.right - padding.right;
        int _height = measureHeight - border.size.top - padding.top
                - border.size.bottom - padding.bottom;

        if(text != null && paint != null) {
            layout = StaticLayout.Builder.obtain(text,
                    0,
                    text.length(),
                    paint,
                    _width).build();

            if (buffer != null && !buffer.isRecycled()) buffer.recycle();

            int updatedHeight = layout.getHeight();
            Bitmap origin = Bitmap.createBitmap(_width, updatedHeight, Bitmap.Config.ARGB_8888);
            bufferPaint = new Paint();
            // 텍스트를 캔버스를 통해 비트맵에 그린다.
            layout.draw(new Canvas(buffer));
            /*
            상대 위치+updatedHeight 가 measureHeight 보다 크다면?
            상위 컴포넌트의 Height를 업데이트 한다.
            */
            if (y + updatedHeight > _height) {
                updateHeight(y + updatedHeight - _height);
                _width = measureWidth - border.size.left - padding.left
                        - border.size.right - padding.right;
            }
            measureHeight = updatedHeight + border.size.top + padding.top
                    + border.size.bottom + padding.bottom;

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
                measureY + border.size.top + padding.top, bufferPaint);
    }

    @Override
    public PDFText setSize(int width, int height) {
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
    public PDFText setBorder(Function<Border, Border> action) {
        super.setBorder(action);
        return this;
    }

    @Override
    public PDFText setAnchor(int vertical, int horizontal) {
        super.setAnchor(vertical, horizontal);
        return this;
    }

    @Override
    public PDFText setAnchor(int axis, boolean isHorizontal) {
        super.setAnchor(axis, isHorizontal);
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
    public PDFText setText(String Text){
        this.text = text;
        return this;
    }
    public PDFText setTextPaint(TextPaint paint){
        if(paint == null){
            this.paint = new TextPaint();
            this.paint.setTextAlign(Paint.Align.LEFT);
            this.paint.setTextSize(16);
        }else{
            this.paint = paint;
        }
        return this;
    }
    public PDFText setTextAlign(Paint.Align align){
        if(paint == null) setTextPaint(null);
        this.paint.setTextAlign(align);
        return this;
    }

    public static PDFText build(String text){return new PDFText(text);}
}

