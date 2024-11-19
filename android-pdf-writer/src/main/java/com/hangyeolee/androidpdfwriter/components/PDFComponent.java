package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import androidx.annotation.ColorInt;

import com.hangyeolee.androidpdfwriter.binary.BinaryConverter;
import com.hangyeolee.androidpdfwriter.binary.BinarySerializer;
import com.hangyeolee.androidpdfwriter.utils.Anchor;
import com.hangyeolee.androidpdfwriter.utils.Border;

import com.hangyeolee.androidpdfwriter.listener.Action;
import com.hangyeolee.androidpdfwriter.utils.Zoomable;

import java.util.Locale;

public abstract class PDFComponent{
    // 부모 컴포넌트 참조
    PDFComponent parent = null;

    // 컴포넌트 스타일링
    final RectF margin = new RectF(0,0,0,0);
    final RectF padding = new RectF(0,0,0,0);
    final Border border = new Border();
    final Anchor anchor = new Anchor();
    Paint bufferPaint = null;
    @ColorInt int backgroundColor = Color.TRANSPARENT;

    // 크기 관련
    protected float width = 0;    // 설정된 너비(margin 포함)
    protected float height = 0;   // 설정된 높이(margin 포함)

    // 상대 좌표 (부모 기준)
    protected float relativeX = 0;
    protected float relativeY = 0;

    // 절대 좌표 (첫번째 페이지 기준)
    // 각 페이지의 높이는 ContentRect.Height = PageHeight - Page Top padding - Page Bottom padding 이다.
    /**
     * 해당 절대 좌표를 Page 좌표로 변환한다면 다음과 같다.<br/>
     * Page Left padding + measureX
     */
    protected float measureX = 0;
    /**
     * 해당 절대 좌표를 Page 좌표로 변환한다면 다음과 같다.<br/>
     * PageHeight - Page Top padding - measureY - measureHeight
     */
    protected float measureY = 0;
    protected float measureWidth = -1; // (margin 제외)
    protected float measureHeight = -1; // (margin 제외)

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
        float dx = 0, dy = 0;
        float gapX = 0, gapY = 0;

        // 여백 계산
        float left = margin.left;
        float top = margin.top;
        float right = margin.right;
        float bottom = margin.bottom;

        // 측정된 크기 계산
        if(parent == null){
            // 루트 컴포넌트인 경우
            measureWidth = (width - left - right);
            measureHeight = (height - top - bottom);
        }
        else{
            // 자식 컴포넌트인 경우
            // 부모의 사용 가능한 최대 크기 계산
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

            // 설정된 크기와 최대 크기 중 적절한 값 선택
            if(0 < width && width + relativeX <= maxW) measureWidth = width;
                // 설정한 Width 나 Height 가 최대값을 넘으면, 최대 값으로 Width 나 Height를 설정
            else measureWidth =  (maxW - relativeX);
            if(0 < height && height + relativeY <= maxH) measureHeight = height;
            else measureHeight =  (maxH - relativeY);

            gapX = maxW - measureWidth;
            gapY = maxH - measureHeight;
        }

        // 앵커에 따른 위치 조정
        dx = Anchor.getDeltaPixel(anchor.horizontal, gapX);
        dy = Anchor.getDeltaPixel(anchor.vertical, gapY);

        // 절대 좌표 계산
        if(parent != null){
            dx += parent.measureX + parent.border.size.left + parent.padding.left;
            dy += parent.measureY + parent.border.size.top + parent.padding.top;
        }
        measureX = relativeX + left + dx;
        measureY = relativeY + top + dy;
    }

    /**
     * PDF 컨텐츠 스트림에 색상 설정
     */
    protected void setColorInPDF(StringBuilder content, @ColorInt int color) {
        float alpha = Color.alpha(color) / 255f;

        // RGB 색상 설정
        if (alpha == 1.0f) {
            // 선 색상
            PDFGraphicsState.addStrokeColor(content, color);
            // 채움 색상
            PDFGraphicsState.addFillColor(content, color);
        } else {
            // 알파값이 있는 경우 ExtGState 사용
            // Note: ExtGState는 리소스로 등록되어야 함
            //content.append("/GS1 gs\n"); // 알파값이 설정된 그래픽스 상태 사용
            content.append(String.format(Locale.getDefault(),"/GS%s gs\r\n",
                    BinaryConverter.formatNumber(alpha, 2)
            )); // 알파값에 해당하는 ExtGState 사용
            // 선 색상
            PDFGraphicsState.addStrokeColor(content, color);
            // 채움 색상
            PDFGraphicsState.addFillColor(content, color);
        }
    }

    /**
     * PDF 컨텐츠 스트림에 사각형 그리기
     */
    protected void drawRectInPDF(StringBuilder content,
                                 float x, float y, float width, float height,
                                 boolean fill, boolean stroke) {
        // PDF 좌표계로 변환 (좌하단 기준)
        float pdfX = Zoomable.getInstance().transform2PDFWidth(x);
        float pdfY = Zoomable.getInstance().transform2PDFHeight(y + height);
        content.append(String.format(Locale.getDefault(), "%s %s %s %s re\r\n",
                BinaryConverter.formatNumber(pdfX),
                BinaryConverter.formatNumber(pdfY),
                BinaryConverter.formatNumber(width),
                BinaryConverter.formatNumber(height)
        ));

        if (fill && stroke) {
            content.append("B\r\n"); // 채우기 및 테두리
        } else if (fill) {
            content.append("f\r\n"); // 채우기만
        } else if (stroke) {
            content.append("S\r\n"); // 테두리만
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
        StringBuilder content = serializer.getPage(requiredPage);

        // 배경 그리기
        if (backgroundColor != Color.TRANSPARENT && measureWidth > 0 && measureHeight > 0) {
            // 그래픽스 상태 저장
            PDFGraphicsState.save(content);

            setColorInPDF(content, backgroundColor);
            drawRectInPDF(content,
                    measureX,
                    measureY,
                    measureWidth,
                    measureHeight,
                    true, false);

            // 그래픽스 상태 복원
            PDFGraphicsState.restore(content);
        }

        //--------------테두리 그리기-------------//
        border.draw(content, measureX, measureY, measureWidth, measureHeight);
        return content;
    }

    /**
     * 하위 컴포넌트로 상위 컴포넌트 업데이트 <br>
     * Update parent components to child components
     * @param heightGap
     */
    protected void updateHeight(float heightGap){
        // heightGap이 0이면 업데이트할 필요 없음
        if (heightGap == 0) return;
        float verticalMargins = margin.top + margin.bottom;

        if(parent == null){
            // 루트 컴포넌트인 경우 직접 높이 조정
            height += heightGap;
            measureHeight = height - verticalMargins;
        }
        else{
            // 부모 컴포넌트의 높이를 먼저 업데이트
            parent.updateHeight(heightGap);

            // 부모의 새로운 크기에 맞춰 자신의 크기 재조정
            float maxHeight = parent.measureHeight
                    - parent.border.size.top - parent.border.size.bottom
                    - parent.padding.top - parent.padding.bottom
                    - verticalMargins;

            if(0 < height && height + relativeY <= maxHeight) measureHeight = height;
            else measureHeight = maxHeight - relativeY;
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
     * 테두리 굵기 및 색상 지정<br>
     * Specify border thickness and color
     * @param size 전체 테두리 굵기
     * @param color 전체 테두리 색상
     */
    public PDFComponent setBorder(float size, @ColorInt int color){
        border.setBorder(size, color);
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
