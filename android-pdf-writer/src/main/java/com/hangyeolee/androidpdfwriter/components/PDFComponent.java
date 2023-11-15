package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.hangyeolee.androidpdfwriter.exceptions.LayoutSizeException;
import com.hangyeolee.androidpdfwriter.utils.Anchor;
import com.hangyeolee.androidpdfwriter.utils.Border;

public abstract class PDFComponent{
    PDFComponent parent = null;
    int relativeX = 0;
    int relativeY = 0;

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
    // margin을 뺀 나머지 길이
    protected int measureWidth = -1;
    protected int measureHeight = -1;

    public PDFComponent(){}
    public PDFComponent(PDFComponent parent){
        this.parent = parent;
    }

    /**
     * 상위 컴포넌트에서의 레이아웃 계산 <br>
     * 상위 컴포넌트가 null 인 경우 전체 페이지를 기준 <br>
     * Compute layout on parent components <br>
     * Based on full page when parent component is null
     */
    public void measure(){
        measure(relativeX, relativeY);
    }

    /**
     * 상위 컴포넌트에서의 레이아웃 계산 <br>
     * 상위 컴포넌트가 null 인 경우 전체 페이지를 기준 <br>
     * Compute layout on parent components <br>
     * Based on full page when parent component is null
     * @param x 상대적인 X 위치, Relative X position
     * @param y 상대적인 Y 위치, Relative Y position
     */
    public void measure(int x, int y){
        relativeX = x;
        relativeY = y;
        int dx, dy;
        int gapX = 0;
        int gapY = 0;

        int left = margin.left;
        int top = margin.top;
        int right = margin.right;
        int bottom = margin.bottom;
        // Measure Width and Height
        if(parent == null){
            if(width < 0 || height < 0) throw new LayoutSizeException("전체 페이지를 기준으로 음수의 크기를 가질 수 없습니다.");
            measureWidth = width;
            measureHeight = height;
        }
        else{
            // Get Max Width and Height from Parent
            int maxW = parent.measureWidth
                    - parent.border.size.left - parent.border.size.right
                    - parent.padding.left - parent.padding.right
                    - left - right;
            int maxH = parent.measureHeight
                    - parent.border.size.top - parent.border.size.bottom
                    - parent.padding.top - parent.padding.bottom
                    - top - bottom;
            if(maxW < 0) maxW = 0;
            if(maxH < 0) maxH = 0;

            // 설정한 Width 나 Height 가 최대값을 넘지 않으면, 설정한 값으로
            if(0 <= width && width < maxW) measureWidth = width;
                // 설정한 Width 나 Height 가 최대값을 넘으면, 최대 값으로 Width 나 Height를 설정
            else measureWidth = maxW;
            if(0 <= height && height < maxH) measureHeight = height;
            else measureHeight = maxH;

            gapX = maxW - measureWidth;
            gapY = maxH - measureHeight;
        }
        // Measure X Anchor and Y Anchor
        dx = Anchor.getDeltaPixel(anchor.horizontal, gapX);
        dy = Anchor.getDeltaPixel(anchor.vertical, gapY);
        // Set Absolute Position From Parent
        measureX = x + left + dx;
        measureY = y + top + dy;
    }

    public PDFComponent setMargin(Rect margin){
        this.margin.set(margin);
        return this;
    }
    public PDFComponent setMargin(int left, int top, int right, int bottom){
        this.margin.set(left, top, right, bottom);
        return this;
    }
    public PDFComponent setPadding(Rect margin){
        this.padding.set(margin);
        return this;
    }
    public PDFComponent setPadding(int left, int top, int right, int bottom){
        this.padding.set(left, top, right, bottom);
        return this;
    }


    public void draw(Canvas canvas){
        //TODO draw background Color
        border.draw(canvas, measureX, measureY, measureWidth, measureHeight);
    }

    protected void updateHeight(int heightGap){
        if(parent != null){
            parent.measureHeight += heightGap;
            parent.updateHeight(heightGap);
        }else {
            height += heightGap;
        }
        measure();
    }

    @Override
    protected void finalize() throws Throwable {
        if(buffer != null && !buffer.isRecycled())
            buffer.recycle();
        super.finalize();
    }
}
