package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import androidx.annotation.ColorInt;

import com.hangyeolee.androidpdfwriter.binary.BinarySerializer;
import com.hangyeolee.androidpdfwriter.utils.Anchor;
import com.hangyeolee.androidpdfwriter.utils.Border;

import com.hangyeolee.androidpdfwriter.listener.Action;

import java.util.Locale;

public abstract class PDFComponent{
    PDFComponent parent = null;

    // margin을 뺀 나머지 길이
    float width = 0;
    float height = 0;
    @ColorInt
    int backgroundColor = Color.TRANSPARENT;
    final RectF margin = new RectF(0,0,0,0);
    final RectF padding = new RectF(0,0,0,0);
    final Border border = new Border();
    final Anchor anchor = new Anchor();

    Paint bufferPaint = null;

    float relativeX = 0;
    float relativeY = 0;
    // Absolute Position
    protected float measureX = 0;
    protected float measureY = 0;
    // margin을 뺀 나머지 길이
    protected float measureWidth = -1;
    protected float measureHeight = -1;

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
        float gapX = 0;
        float gapY = 0;

        float left = margin.left;
        float top = margin.top;
        float right = margin.right;
        float bottom = margin.bottom;
        // Measure Width and Height
        if(parent == null){
            measureWidth = (width - left - right);
            measureHeight = (height - top - bottom);
        }
        else{
            // Get Max Width and Height from Parent
            float maxW = parent.measureWidth
                    - parent.border.size.left - parent.border.size.right
                    - parent.padding.left - parent.padding.right
                    - left - right;
            float maxH = parent.measureHeight
                    - parent.border.size.top - parent.border.size.bottom
                    - parent.padding.top - parent.padding.bottom
                    - top - bottom;
            if(maxW < 0) maxW = 0;
            if(maxH < 0) maxH = 0;

            // 설정한 Width 나 Height 가 최대값을 넘지 않으면, 설정한 값으로
            if(0 < width && width + relativeX <= maxW) measureWidth = width;
                // 설정한 Width 나 Height 가 최대값을 넘으면, 최대 값으로 Width 나 Height를 설정
            else measureWidth =  (maxW - relativeX);
            if(0 < height && height + relativeY <= maxH) measureHeight = height;
            else measureHeight =  (maxH - relativeY);

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
    protected void force(Float width, Float height, RectF forceMargin) {
        float max;
        float gap = 0;
        float d;
        if (width != null) {
            if(forceMargin == null)
                max = (width - margin.left - margin.right);
            else
                max = width;
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
            if(forceMargin == null)
                max = (height - margin.top - margin.bottom);
            else
                max = height;
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

    /**
     * PDF 컨텐츠 스트림에 색상 설정
     */
    protected void setColorInPDF(StringBuilder content, int color) {
        float red = Color.red(color) / 255f;
        float green = Color.green(color) / 255f;
        float blue = Color.blue(color) / 255f;
        float alpha = Color.alpha(color) / 255f;

        // RGB 색상 설정
        if (alpha == 1.0f) {
            content.append(String.format(Locale.getDefault(), "%.3f %.3f %.3f RG\n", red, green, blue)); // 선 색상
            content.append(String.format(Locale.getDefault(), "%.3f %.3f %.3f rg\n", red, green, blue)); // 채움 색상
        } else {
            // 투명도가 있는 경우 ExtGState 사용
            // Note: ExtGState는 리소스로 등록되어야 함
            content.append("/GS1 gs\n"); // 알파값이 설정된 그래픽스 상태 사용
            content.append(String.format(Locale.getDefault(), "%.3f %.3f %.3f RG\n", red, green, blue));
            content.append(String.format(Locale.getDefault(), "%.3f %.3f %.3f rg\n", red, green, blue));
        }
    }

    /**
     * PDF 컨텐츠 스트림에 사각형 그리기
     */
    protected void drawRectInPDF(BinarySerializer page, StringBuilder content, float x, float y, float width, float height,
                                 boolean fill, boolean stroke) {
        content.append(String.format(Locale.getDefault(), "%.2f %.2f %.2f %.2f re\n",
                x, page.getPageHeight() - (y + height), // PDF 좌표계로 변환
                width, height));

        if (fill && stroke) {
            content.append("B\n"); // 채우기 및 테두리
        } else if (fill) {
            content.append("f\n"); // 채우기만
        } else if (stroke) {
            content.append("S\n"); // 테두리만
        }
    }

    /**
     * 컴포넌트의 리소스를 등록하고 PDF 오브젝트를 생성
     * @param serializer BinaryPage 인스턴스
     */
    public StringBuilder draw(BinarySerializer serializer){
        float componentHeight = getTotalHeight();

        // 페이지 체크
        int requiredPage = serializer.calculatePageIndex(measureY, componentHeight);
        measureY -= requiredPage * serializer.getPageHeight();
        StringBuilder content = serializer.getPage(requiredPage);

        // 그래픽스 상태 저장
        PDFGraphicsState.save(content);

        // 배경 그리기
        if (backgroundColor != Color.TRANSPARENT && measureWidth > 0 && measureHeight > 0) {
            setColorInPDF(content, backgroundColor);
            drawRectInPDF(serializer, content, measureX, measureY, measureWidth, measureHeight, true, false);
        }

        //--------------테두리 그리기-------------//
        border.draw(serializer, content, measureX, measureY, measureWidth, measureHeight);

        // 그래픽스 상태 복원
        PDFGraphicsState.restore(content);
        return content;
    }

    /**
     * 하위 컴포넌트로 상위 컴포넌트 업데이트 <br>
     * Update parent components to child components
     * @param heightGap
     */
    protected void updateHeight(float heightGap){
        float top = margin.top;
        float bottom = margin.bottom;
        if(parent == null){
            height += heightGap;
            measureHeight = (height - top - bottom);
        }
        else{
            parent.updateHeight(heightGap);
            float maxH = parent.measureHeight
                    - parent.border.size.top - parent.border.size.bottom
                    - parent.padding.top - parent.padding.bottom
                    - top - bottom;
            if(0 < height && height + relativeY <= maxH) measureHeight = height;
            else measureHeight = maxH - relativeY;
        }
    }

    /**
     * 계산된 전체 너비를 구한다.<br>
     * Get the calculated total width.
     * @return 전체 너비
     */
    public float getTotalWidth(){
        return measureWidth + margin.right + margin.left;
    }

    /**
     * 계산된 전체 높이를 구한다.<br>
     * Get the calculated total height.
     * @return 전체 높이
     */
    public float getTotalHeight(){
        return measureHeight + margin.top + margin.bottom;
    }

    /**
     * 컴포넌트 내의 내용(content)의 크기 설정 <br>
     * Setting the size of content within a component
     * @param width 가로 크기
     * @param height 세로 크기
     * @return 컴포넌트 자기자신
     */
    public PDFComponent setSize(Float width, Float height){
        if(width != null){
            if(width < 0) width = 0f;
            this.width = (width);
        }
        if(height != null){
            if(height < 0) height = 0f;
            this.height = (height);
        }
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
    public PDFComponent setMargin(RectF margin){
        this.margin.set(margin);
        return this;
    }

    /**
     * 컴포넌트 밖의 여백 설정<br>
     * Setting margins outside of components
     * @param all 여백
     * @return 컴포넌트 자기자신
     */
    public PDFComponent setMargin(float all){
        return setMargin(all, all, all, all);
    }

    /**
     * 컴포넌트 밖의 여백 설정<br>
     * Setting margins outside of components
     * @param horizontal 가로 여백
     * @param vertical 세로 여백
     * @return 컴포넌트 자기자신
     */
    public PDFComponent setMargin(float horizontal, float vertical){
        return setMargin(horizontal, vertical, horizontal, vertical);
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
    public PDFComponent setMargin(float left, float top, float right, float bottom){
        this.margin.set(left, top, right, bottom);
        return this;
    }

    /**
     * 컴포넌트 내의 내용(content)과 테두리(border) 사이의 간격 설정<br>
     * Setting the interval between content and border within a component
     * @param padding 패딩
     * @return 컴포넌트 자기자신
     */
    public PDFComponent setPadding(RectF padding){
        this.padding.set(padding);
        return this;
    }

    /**
     * 컴포넌트 내의 내용(content)과 테두리(border) 사이의 간격 설정<br>
     * Setting the interval between content and border within a component
     * @param all 패딩
     * @return 컴포넌트 자기자신
     */
    public PDFComponent setPadding(float all){
        this.padding.set(all, all, all, all);
        return this;
    }

    /**
     * 컴포넌트 내의 내용(content)과 테두리(border) 사이의 간격 설정<br>
     * Setting the interval between content and border within a component
     * @param horizontal 가로 패딩
     * @param vertical 세로 패딩
     * @return 컴포넌트 자기자신
     */
    public PDFComponent setPadding(float horizontal, float vertical){
        this.padding.set(horizontal, vertical, horizontal, vertical);
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
    public PDFComponent setPadding(float left, float top, float right, float bottom){
        this.padding.set(left, top, right, bottom);
        return this;
    }

    /**
     * 테두리 굵기 및 색상 지정<br>
     * Specify border thickness and color
     * @param action 테두리 변경 함수
     */
    public PDFComponent setBorder(Action<Border, Border> action){
        border.copy(action.invoke(border));
        return this;
    }

    /**
     * 컴포넌트의 고정점 지정 <br>
     * Anchoring components
     * @param vertical 세로 고정점
     * @param horizontal 가로 고정점
     * @return 자기자신
     */
    public PDFComponent setAnchor(@Anchor.AnchorInt Integer horizontal, @Anchor.AnchorInt Integer vertical){
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
        super.finalize();
    }
}
