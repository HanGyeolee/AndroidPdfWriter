package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.hangyeolee.androidpdfwriter.listener.Action;
import com.hangyeolee.androidpdfwriter.utils.Border;

public class PDFEmpty extends PDFComponent{
    public static PDFEmpty build(){return new PDFEmpty();}

    @Override
    public void measure(float x, float y) {
        // measure nothing
    }

    @Override
    public void draw(Canvas canvas) {
        // draw nothing
    }
}

