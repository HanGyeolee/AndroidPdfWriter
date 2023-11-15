package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import androidx.annotation.ColorInt;

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

    Bitmap buffer = null;
    Paint bufferPaint = null;

    float relativeX = 0;
    float relativeY = 0;
    // Absolute Position
    protected float measureX = 0;
    protected float measureY = 0;
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
    public void measure(float x, float y){
        if(width < 0) {
            width = 0;
        }
        if(height < 0) {
            height = 0;
        }

        relativeX = x;
        relativeY = y;
        float dx, dy;
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
            int maxW = (int) (parent.measureWidth
                    - parent.border.size.left - parent.border.size.right
                    - parent.padding.left - parent.padding.right
                    - left - right);
            int maxH = (int) (parent.measureHeight
                    - parent.border.size.top - parent.border.size.bottom
                    - parent.padding.top - parent.padding.bottom
                    - top - bottom);
            if(maxW < 0) maxW = 0;
            if(maxH < 0) maxH = 0;

            // 설정한 Width 나 Height 가 최대값을 넘지 않으면, 설정한 값으로
            if(0 < width && width + relativeX <= maxW) measureWidth = width;
                // 설정한 Width 나 Height 가 최대값을 넘으면, 최대 값으로 Width 나 Height를 설정
            else measureWidth = (int) (maxW - relativeX);
            if(0 < height && height + relativeY <= maxH) measureHeight = height;
            else measureHeight = (int) (maxH - relativeY);

            gapX = maxW - measureWidth;
            gapY = maxH - measureHeight;
        }
        dx = Anchor.getDeltaPixel(anchor.horizontal, gapX);
        dy = Anchor.getDeltaPixel(anchor.vertical, gapY);
        if(parent != null){
            dx += parent.measureX + parent.border.size.left + parent.padding.left;
            dy += parent.measureY + parent.border.size.top + parent.padding.top;
        }
        measureX = relativeX + left + dx;
        measureY = relativeY + top + dy;
    }

    /**
     * 부모 컴포넌트에서 부모 컴포넌트의 연산에 맞게<br>
     * 자식 컴포넌트를 강제로 수정해야할 떄 사용<br>
     * Used when a parent component needs to force a child component ~<br>
     * to be modified to match the parent component's operations
     * @param width
     * @param height
     */
    protected void force(Integer width, Integer height) {
        int max;
        int gap = 0;
        float d;
        if (width != null) {
            max = (int) (width - margin.left - margin.right);
            gap = max - measureWidth;
            // Measure X Anchor and Y Anchor
            d = Anchor.getDeltaPixel(anchor.horizontal, gap);
            if (parent != null)
                d += parent.measureX + parent.border.size.left + parent.padding.left;
            // Set Absolute Position From Parent
            measureWidth = max;
            measureX = relativeX + margin.left + d;
        }
        if(height != null) {
            max = (int) (height - margin.top - margin.bottom);
            gap = max - measureHeight;
            // Measure X Anchor and Y Anchor
            d = Anchor.getDeltaPixel(anchor.vertical, gap);
            if (parent != null)
                d += parent.measureY + parent.border.size.top + parent.padding.top;
            // Set Absolute Position From Parent
            measureHeight = max;
            measureY = relativeY + margin.top + d;
        }
    }

    public void draw(Canvas canvas){
        //---------------배경 그리기-------------//
        Paint background = new Paint();
        background.setColor(backgroundColor);
        background.setStyle(Paint.Style.FILL);
        float left = relativeX + margin.left;
        float top = relativeY + margin.top;
        if (parent != null) {
            left += parent.measureX + parent.border.size.left + parent.padding.left;
            top += parent.measureY + parent.border.size.top + parent.padding.top;
        }
        if(measureWidth > 0 && measureHeight > 0) {
            canvas.drawRect(left, top,
                    left + measureWidth, top + measureHeight,
                    background);
        }

        //--------------테두리 그리기-------------//
        border.draw(canvas, left, top, measureWidth, measureHeight);
    }

    /**
     * 하위 컴포넌트로 상위 컴포넌트 업데이트 <br>
     * Update parent components to child components
     * @param heightGap
     */
    protected void updateHeight(float heightGap){
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
        return measureWidth + margin.right + margin.left;
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
    public PDFComponent setSize(Integer width, Integer height){
        if(width != null)
            this.width = width;
        if(height != null)
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

    /**
     * 컴포넌트의 고정점 지정 <br>
     * Anchoring components
     * @param vertical 세로 고정점
     * @param horizontal 가로 고정점
     * @return 자기자신
     */
    public PDFComponent setAnchor(@Anchor.AnchorInt Integer vertical, @Anchor.AnchorInt Integer horizontal){
        if(vertical != null)
            anchor.vertical = vertical;
        if(horizontal != null)
            anchor.horizontal = horizontal;
        return  this;
    }

    /**
     * 해당 컴포넌트의 부모 추가<br>
     * Add the parent of that component
     * @param parent 부모
     * @return 자기자신
     */
    protected PDFComponent setParent(PDFComponent parent){
        this.parent = parent;
        return this;
    }

    @Override
    protected void finalize() throws Throwable {
        if(buffer != null && !buffer.isRecycled())
            buffer.recycle();
        super.finalize();
    }
}
