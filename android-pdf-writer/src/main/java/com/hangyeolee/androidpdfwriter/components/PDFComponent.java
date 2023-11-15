package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import androidx.annotation.ColorInt;

import com.hangyeolee.androidpdfwriter.exceptions.LayoutSizeException;
import com.hangyeolee.androidpdfwriter.utils.Anchor;
import com.hangyeolee.androidpdfwriter.utils.Border;

import java.util.function.Function;

public abstract class PDFComponent{
    PDFComponent parent = null;

    int width = 0;
    int height = 0;
    @ColorInt
    int backgroundColor = Color.TRANSPARENT;
    final Rect margin = new Rect(0,0,0,0);
    final Rect padding = new Rect(0,0,0,0);
    final Border border = new Border();
    final Anchor anchor = new Anchor();

    Bitmap buffer;
    Paint bufferPaint;

    int relativeX = 0;
    int relativeY = 0;
    // Absolute Position
    protected int measureX = 0;
    protected int measureY = 0;
    // margin을 뺀 나머지 길이
    protected int measureWidth = -1;
    protected int measureHeight = -1;

    public PDFComponent(){}


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
        if(width < 0) {
            width = 0;
        }
        if(height < 0) {
            height = 0;
        }

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
            measureWidth = width - left - right;
            measureHeight = height - top - bottom;
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
            if(0 <= width + relativeX && width + relativeX < maxW) measureWidth = width;
                // 설정한 Width 나 Height 가 최대값을 넘으면, 최대 값으로 Width 나 Height를 설정
            else measureWidth = maxW;
            if(0 <= height + relativeY && height + relativeY < maxH) measureHeight = height;
            else measureHeight = maxH;

            gapX = maxW - measureWidth;
            gapY = maxH - measureHeight;
        }
        dx = Anchor.getDeltaPixel(anchor.horizontal, gapX);
        dy = Anchor.getDeltaPixel(anchor.vertical, gapY);
        if(parent != null){
            dx += parent.measureX;
            dy += parent.measureY;
        }
        measureX = relativeX + left + dx;
        measureY = relativeY + top + dy;
        measureAnchor(true);
        measureAnchor(false);
    }

    protected void measureAnchor(boolean isHorizontal){
        int max;
        int gap = 0;
        int d;
        if(isHorizontal){
            if(parent != null) {
                max = parent.measureWidth
                        - parent.border.size.left - parent.border.size.right
                        - parent.padding.left - parent.padding.right
                        - margin.left - margin.right;
                gap = max - measureWidth;
            }
            // Measure X Anchor and Y Anchor
            d = Anchor.getDeltaPixel(anchor.horizontal, gap);
            if(parent != null)
                d += parent.measureX;
            // Set Absolute Position From Parent
            measureX = relativeX + margin.left + d;
        }else {
            if(parent != null) {
                max = parent.measureHeight
                        - parent.border.size.top - parent.border.size.bottom
                        - parent.padding.top - parent.padding.bottom
                        - margin.top - margin.bottom;
                gap = max - measureHeight;
            }
            // Measure X Anchor and Y Anchor
            d = Anchor.getDeltaPixel(anchor.vertical, gap);
            if(parent != null)
                d += parent.measureY;
            // Set Absolute Position From Parent
            measureY = relativeY + margin.top + d;
        }
    }

    /**
     * 하위 컴포넌트로 상위 컴포넌트 업데이트 <br>
     * Update parent components to child components
     * @param heightGap
     */
    protected void updateHeight(int heightGap){
        if(parent != null){
            parent.updateHeight(heightGap);
        }else {
            height += heightGap;
        }
        measure();
    }

    /**
     * 계산된 전체 너비를 구한다.<br>
     * Get the calculated total width.
     * @return 전체 너비
     */
    public int getTotalWidth(){
        return measureHeight + margin.top + margin.bottom;
    }
    /**
     * 계산된 전체 높이를 구한다.<br>
     * Get the calculated total height.
     * @return 전체 높이
     */
    public int getTotalHeight(){
        return measureHeight + margin.top + margin.bottom;
    }

    /**
     * 컴포넌트 내의 내용(content)의 크기 설정 <br>
     * Setting the size of content within a component
     * @param width 가로 크기
     * @param height 세로 크기
     * @return 컴포넌트 자기자신
     */
    public PDFComponent setSize(int width, int height){
        this.width = width;
        this.height = height;
        return this;
    }

    /**
     * 컴포넌트 내의 배경의 색상 설정<br>
     * Set the color of the background within the component
     * @param color 색상
     * @return 컴포넌트 자기자신
     */
    public PDFComponent setBackgroundColor(int color){
        this.backgroundColor = color;
        return this;
    }

    /**
     * 컴포넌트 밖의 여백 설정<br>
     * Setting margins outside of components
     * @param margin 여백
     * @return 컴포넌트 자기자신
     */
    public PDFComponent setMargin(Rect margin){
        this.margin.set(margin);
        return this;
    }

    /**
     * 컴포넌트 밖의 여백 설정<br>
     * Setting margins outside of components
     * @param left 왼쪽 여백
     * @param top 위쪽 여백
     * @param right 오른쪽 여백
     * @param bottom 아래쪽 여백
     * @return 컴포넌트 자기자신
     */
    public PDFComponent setMargin(int left, int top, int right, int bottom){
        this.margin.set(left, top, right, bottom);
        return this;
    }

    /**
     * 컴포넌트 내의 내용(content)과 테두리(border) 사이의 간격 설정<br>
     * Setting the interval between content and border within a component
     * @param padding 패딩
     * @return 컴포넌트 자기자신
     */
    public PDFComponent setPadding(Rect padding){
        this.padding.set(padding);
        return this;
    }

    /**
     * 컴포넌트 내의 내용(content)과 테두리(border) 사이의 간격 설정<br>
     * Setting the interval between content and border within a component
     * @param left 왼쪽 패딩
     * @param top 위쪽 패딩
     * @param right 오른쪽 패딩
     * @param bottom 아래쪽 패딩
     * @return 컴포넌트 자기자신
     */
    public PDFComponent setPadding(int left, int top, int right, int bottom){
        this.padding.set(left, top, right, bottom);
        return this;
    }

    /**
     * 테두리 굵기 및 색상 지정<br>
     * Specify border thickness and color
     * @param action 테두리 변경 함수
     */
    public PDFComponent setBorder(Function<Border, Border> action){
        border.copy(action.apply(border));
        return this;
    }

    public PDFComponent setAnchor(@Anchor.Type int vertical, @Anchor.Type int horizontal){
        anchor.horizontal = horizontal;
        anchor.vertical = vertical;
        return  this;
    }
    public PDFComponent setAnchor(@Anchor.Type int axis, boolean isHorizontal){
        if(isHorizontal) {
            anchor.horizontal = axis;
        }else {
            anchor.vertical = axis;
        }
        return  this;
    }
    protected PDFComponent setParent(PDFComponent parent){
        this.parent = parent;
        return this;
    }


    public void draw(Canvas canvas){
        int pageWidth = canvas.getWidth();
        int pageHeight = canvas.getHeight();
        Paint background = new Paint();
        background.setColor(backgroundColor);
        background.setStyle(Paint.Style.FILL);
        int right = pageWidth - measureX - measureWidth;
        int bottom = pageHeight - measureY - measureHeight;
        if(right >= 0 && bottom >= 0) {
            canvas.drawRect(measureX, measureY,
                    right, bottom, background);
        }
        border.draw(canvas, measureX, measureY, measureWidth, measureHeight);
    }

    @Override
    protected void finalize() throws Throwable {
        if(buffer != null && !buffer.isRecycled())
            buffer.recycle();
        super.finalize();
    }
}
