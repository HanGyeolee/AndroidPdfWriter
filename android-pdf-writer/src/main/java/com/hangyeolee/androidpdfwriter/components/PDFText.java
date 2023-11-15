package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.StaticLayout;
import android.text.TextPaint;

import com.hangyeolee.androidpdfwriter.exceptions.LayoutSizeException;
import com.hangyeolee.androidpdfwriter.utils.Anchor;

public class PDFText extends PDFComponent{
    String text;
    TextPaint paint;
    StaticLayout layout;

    @Override
    public void measure(int globalX, int globalY, PDFComponent parent) {
        super.measure(globalX, globalY, parent);

        layout = StaticLayout.Builder.obtain(text,
                0,
                text.length(),
                paint,
                measureWidth).build();

        if(buffer != null && !buffer.isRecycled()) buffer.recycle();

        Bitmap origin = Bitmap.createBitmap(measureWidth, layout.getHeight(), Bitmap.Config.ARGB_8888);
        bufferPaint = new Paint();
        layout.draw(new Canvas(buffer));
        buffer = Bitmap.createBitmap(origin,
                0, 0,
                measureWidth, measureHeight);
        origin.recycle();
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(buffer, measureX, measureY, bufferPaint);
    }

    public PDFText(String text){
        setData(text, null);
    }
    public PDFText(String text, TextPaint paint){
        setData(text, paint);
    }
    private void setData(String text, TextPaint paint){
        this.text = text;
        if(paint == null){
            this.paint = new TextPaint();
            this.paint.setTextAlign(Paint.Align.LEFT);
            this.paint.setTextSize(16);
        }else{
            this.paint = paint;
        }
    }
}
