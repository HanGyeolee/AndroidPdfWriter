package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.StaticLayout;
import android.text.TextPaint;

import com.hangyeolee.androidpdfwriter.exceptions.LayoutSizeException;
import com.hangyeolee.androidpdfwriter.utils.Anchor;

public class PDFText extends PDFComponent{
    String text = null;
    TextPaint paint = null;
    StaticLayout layout;

    @Override
    public void measure(int x, int y) {
        super.measure(x, y);

        int width = measureWidth - border.size.left - padding.left
                - border.size.right - padding.right;
        int height = measureHeight - border.size.top - padding.top
                - border.size.bottom - padding.bottom;

        if(text != null && paint != null) {
            layout = StaticLayout.Builder.obtain(text,
                    0,
                    text.length(),
                    paint,
                    width).build();

            if (buffer != null && !buffer.isRecycled()) buffer.recycle();

            int updatedHeight = layout.getHeight();
            Bitmap origin = Bitmap.createBitmap(width, updatedHeight, Bitmap.Config.ARGB_8888);
            bufferPaint = new Paint();
            // 텍스트를 캔버스를 통해 비트맵에 그린다.
            layout.draw(new Canvas(buffer));
            /*
            layout.getHeight 가 measureHeight 보다 크다면?
            상위 컴포넌트의 Height를 업데이트 한다.
            */
            if (updatedHeight > height) {
                updateHeight(updatedHeight - height);
                width = measureWidth - border.size.left - padding.left
                        - border.size.right - padding.right;
                height = measureHeight - border.size.top - padding.top
                        - border.size.bottom - padding.bottom;
            }
            buffer = Bitmap.createBitmap(origin,
                    0, 0,
                    width, height);
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

    public PDFText(PDFComponent parent){
        super(parent);
    }
    public PDFText(PDFComponent parent, String text){
        super(parent);
        this.setText(text).setTextPaint(null);
    }
    public PDFText(PDFComponent parent, String text, TextPaint paint){
        super(parent);
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
}

