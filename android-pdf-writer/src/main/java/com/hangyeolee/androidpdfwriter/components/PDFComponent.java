package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.hangyeolee.androidpdfwriter.exceptions.LayoutSizeException;
import com.hangyeolee.androidpdfwriter.utils.Anchor;
import com.hangyeolee.androidpdfwriter.utils.Border;

public abstract class PDFComponent{
    public int width = -1;
    public int height = -1;
    public int backgroundColor;
    Rect margin = new Rect(0,0,0,0);
    Rect padding = new Rect(0,0,0,0);
    Border border = new Border();
    Anchor anchor = new Anchor();

    Bitmap buffer;
    Paint bufferPaint;

    protected int measureX = 0;
    protected int measureY = 0;
    protected int measureWidth = -1;
    protected int measureHeight = -1;

    public PDFComponent(){}
    public void measure(int globalX, int globalY){
        if(width < 0) throw new LayoutSizeException("width is negative. must have 'parent' parameter to use negative number");
        if(height < 0) throw new LayoutSizeException("height is negative. must have 'parent' parameter to use negative number");
        measure(globalX, globalY, null);
    }
    public void measure(int globalX, int globalY, PDFComponent parent){
        int dx = 0;
        int dy = 0;
        // Measure Width and Height
        if(parent == null){
            measureWidth = width;
            measureHeight = height;
        }
        else{
            int maxW = parent.measureWidth
                    - parent.border.size.left - parent.border.size.right
                    - parent.padding.left - parent.padding.right;
            int maxH = parent.measureHeight
                    - parent.border.size.top - parent.border.size.bottom
                    - parent.padding.top - parent.padding.bottom;
            if(maxW < 0) maxW = 0;
            if(maxH < 0) maxH = 0;

            if(0 <= width && width < maxW) measureWidth = width;
            else measureWidth = maxW;
            if(0 <= height && height < maxH) measureHeight = height;
            else measureHeight = maxH;

            // Measure X and Y
            int gapX = maxW - measureWidth;
            int gapY = maxH - measureHeight;
            dx = Anchor.getDeltaPixel(anchor.horizontal, gapX);
            dy = Anchor.getDeltaPixel(anchor.vertical, gapY);
        }
        measureX = globalX + margin.left + border.size.left + padding.left + dx;
        measureY = globalY + margin.top + border.size.top + padding.top + dy;
    }
    public abstract void draw(Canvas canvas);

    @Override
    protected void finalize() throws Throwable {
        buffer.recycle();
        super.finalize();
    }
}
