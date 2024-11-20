package com.hangyeolee.androidpdfwriter.components;

import android.graphics.RectF;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;

import com.hangyeolee.androidpdfwriter.binary.BinaryConverter;
import com.hangyeolee.androidpdfwriter.binary.BinarySerializer;
import com.hangyeolee.androidpdfwriter.exceptions.LayoutChildrenFitDeniedException;
import com.hangyeolee.androidpdfwriter.utils.Anchor;
import com.hangyeolee.androidpdfwriter.utils.Border;
import com.hangyeolee.androidpdfwriter.utils.Orientation;

import java.util.ArrayList;
import java.util.Locale;

import com.hangyeolee.androidpdfwriter.listener.Action;
import com.hangyeolee.androidpdfwriter.utils.Zoomable;

public class PDFLinearLayout extends PDFLayout {
    @Orientation.OrientationInt
    int orientation = Orientation.Vertical;
    private final ArrayList<Float> weights = new ArrayList<Float>();

    public PDFLinearLayout(){
        super();
    }
    public PDFLinearLayout(int length){
        this(length, 1.0f);
    }
    public PDFLinearLayout(int length, float defaultWeight){
        super(length);
        // Initialize weights for empty components
        for (int i = 0; i < length; i++) {
            weights.add(defaultWeight);
        }
    }

    @Override
    public void measure(float x, float y) {
        super.measure(x, y);

        // 방향에 따라 적절한 측정 메서드 호출
        if (orientation == Orientation.Vertical) {
            measureVertical();
        } else {
            measureHorizontal();
        }
    }

    private void measureVertical() {
        if (fitChildrenToLayout && height <= 0) {
            throw new LayoutChildrenFitDeniedException(
                    "In Orientation.Vertical, height must be specified to fit the child into"+this.getClass().getName()+".");
        }
        // 테두리와 패딩을 제외한 실제 사용 가능한 너비 계산
        float contentHeight = Zoomable.getInstance().getContentHeight();
        float currentY = 0;

        // fitChildrenToLayout이 true일 경우 전체 weight 합 계산
        float totalWeight = 0;
        if (fitChildrenToLayout) {
            for (Float weight : weights) {
                totalWeight += weight;
            }
        }

        float defaultHeight = measureHeight;
        // 자식 컴포넌트 측정 및 페이지 분할 처리
        for (int i = 0; i < children.size(); i++) {
            int stack = 0;
            PDFComponent child = children.get(i);
            float childWeight = weights.get(i);

            // 자식 컴포넌트의 높이 계산
            if (fitChildrenToLayout) {
                // weight 비율에 따른 높이 계산
                float childHeight = (defaultHeight * childWeight) / totalWeight;
                child.setSize(null, childHeight);
            }

            // PDFLayout은 페이지네이션 체크 건너뛰기
            if (child instanceof PDFLayout) {
                child.measure(0, currentY);
                currentY += child.getTotalHeight();
                continue;
            }

            for(;;) {
                // 자식 컴포넌트 위치 측정
                child.measure(0, currentY);
                float maxW = measureWidth - child.margin.left - child.margin.right;
                float maxH = measureHeight - currentY
                        - border.size.top - border.size.bottom
                        - padding.top - padding.bottom
                        - child.margin.top - child.margin.bottom;
                childReanchor(child, maxW, maxH);

                // 페이지 경계 확인 및 조정
                float childEndY = currentY + child.getTotalHeight();
                int currentPage = (int) (currentY / contentHeight);
                int endPage = (int) (childEndY / contentHeight);

                if (currentPage != endPage) {
                    stack++;
                    if(stack > 2) {
                        // 스택 오버 플로우 체크
                        // 페이지 경계를 두 번 이상 넘어서게 되면
                        // 자식 컴포넌트가 페이지보다 크기가 크다고 판단.
                        throw new StackOverflowError("Height of child component Too Large");
                    }

                    // 다음 페이지 시작점으로 이동
                    float newY = (endPage) * contentHeight;
                    float yDiff = newY - currentY;
                    child.updateHeight(yDiff);

                    // 변경된 위치로 다시 해당 컴포넌트 재 연산
                    currentY = newY;
                }else {
                    break;
                }
            }

            // 다음 컴포넌트 위치 계산
            currentY += child.getTotalHeight();
        }
    }


    private void measureHorizontal() {
        if (fitChildrenToLayout && width <= 0) {
            throw new LayoutChildrenFitDeniedException(
                    "In Orientation.Horizontal, width must be specified to fit the child into"+this.getClass().getName()+".");
        }
        float availableWidth = measureWidth - border.size.left - padding.left
                - border.size.right - padding.right;
        float maxHeight = 0;
        float currentX = 0;

        // weight 총합 계산
        float totalWeight = 0;
        for (Float weight : weights) {
            totalWeight += weight;
        }

        float _height;
        // 자식 컴포넌트들 측정
        for (int i = 0; i < children.size(); i++) {
            PDFComponent child = children.get(i);
            float childWidth = child.width;
            float childWeight = weights.get(i);
            float cellWidth = (availableWidth * childWeight) / totalWeight;
            child.setSize(
                    childWidth <= 0 ? cellWidth : null,
                    fitChildrenToLayout ? measureHeight : null
            );

            child.measure(currentX, 0);
            float maxW = cellWidth - child.margin.left - child.margin.right;
            float maxH = measureHeight
                    - border.size.top - border.size.bottom
                    - padding.top - padding.bottom
                    - child.margin.top - child.margin.bottom;
            childReanchor(child, maxW, maxH);

            maxHeight = Math.max(maxHeight, child.getTotalHeight());
            currentX += child.getTotalWidth();
        }

        _height = measureHeight;
        // fitChildrenToLayout이 false일 때 최대 높이로 레이아웃 크기 조정
        if (!fitChildrenToLayout && maxHeight > _height) {
            updateHeight(maxHeight - _height);

            // 높이가 변경되었으므로 자식들 재측정
            currentX = 0;
            for (int i = 0; i < children.size(); i++) {
                PDFComponent child = children.get(i);
                float childWeight = weights.get(i);
                float cellWidth = (availableWidth * childWeight) / totalWeight;

                child.measure(currentX, 0);
                float maxW = cellWidth - child.margin.left - child.margin.right;
                float maxH = measureHeight
                        - border.size.top - border.size.bottom
                        - padding.top - padding.bottom
                        - child.margin.top - child.margin.bottom;
                childReanchor(child, maxW, maxH);

                currentX += child.getTotalWidth();
            }
        }
    }

    @Override
    public void childReanchor(PDFComponent child, float maxW, float maxH) {
        // anchor 에 따른 X 위치 오류
        float gapX = maxW - child.measureWidth;
        float gapY = maxH - child.measureHeight;

        // 앵커에 따른 위치 조정
        float dx = Anchor.getDeltaPixel(child.anchor.horizontal, gapX);
        float dy = Anchor.getDeltaPixel(child.anchor.vertical, gapY);

        // 절대 좌표 계산
        dx += measureX + border.size.left + padding.left;
        dy += measureY + border.size.top + padding.top;

        float left = child.margin.left;
        float top = child.margin.top;

        child.measureX = child.relativeX + left + dx;
        child.measureY = child.relativeY + top + dy;
    }


    @Override
    public StringBuilder draw(BinarySerializer serializer) {
        super.draw(serializer);
        StringBuilder content;
        float pageHeight = Zoomable.getInstance().getContentHeight();

        for(int i = 0; i < children.size(); i++) {
            PDFComponent child = children.get(i);

            // 현재 컴포넌트가 위치한 페이지 구하기
            int currentPage = serializer.calculatePageIndex(child.measureY);
            content = serializer.getPage(currentPage);

            // 현재 페이지 내에서의 좌표 계산
            float x = Zoomable.getInstance().transform2PDFWidth(
                    child.measureX
            );
            float y = Zoomable.getInstance().transform2PDFHeight(
                    child.measureY + child.measureHeight
            );

            // 그래픽스 상태 저장
            PDFGraphicsState.save(content);

            // 클리핑 영역 설정 - 컴포넌트의 전체 영역
            // W 클리핑 패스 설정
            // n 패스를 그리지 않고 클리핑만 적용
            content.append(String.format(Locale.US,
                    "%s %s %s %s re W n\r\n",
                    BinaryConverter.formatNumber(x),
                    BinaryConverter.formatNumber(y),
                    BinaryConverter.formatNumber(
                            child.measureWidth),
                    BinaryConverter.formatNumber(
                            child.measureHeight))
            );

            // 자식 컴포넌트 그리기
            child.draw(serializer);

            // 그래픽스 상태 복원
            PDFGraphicsState.restore(content);
        }
        return null;
    }

    /**
     * 레이아웃에 자식 추가<br>
     * Add children to layout
     * @param component 자식 컴포넌트
     * @return 자기자신
     */
    @Override
    public PDFLinearLayout addChild(PDFComponent component){
        return addChild(component, 1);
    }

    /**
     * 레이아웃에 자식 추가<br>
     * Add children to layout<br>
     * @param component 자식 컴포넌트
     * @param weight 자식 컴포넌트가 차지할 크기 비율. 반드시 1.0f보다 커야한다.<br/>
     *               만약 {@link #orientation} 이 {@link Orientation#Horizontal} 라면 width, {@link Orientation#Vertical} 이라면 height 를 넣으면 된다.<br/>
     *               Ratio of the size of the child component. Must bigger than 1.0f<br/>
     *               If {@link #orientation} is {@link Orientation#Horizontal} then width, {@link Orientation#Vertical} then height.
     * @return 자기자신
     */
    public PDFLinearLayout addChild(PDFComponent component, @FloatRange(from = 1.0) float weight){
        if(weight < 1) weight = 1;
        weights.add(weight);
        super.addChild(component);
        return this;
    }

    /**
     * 레이아웃의 방향 설정<br>
     * Setting the orientation of the layout
     * @param orientation 방향
     * @return 자기자신
     */
    public PDFLinearLayout setOrientation(@Orientation.OrientationInt int orientation){
        this.orientation = orientation;
        return this;
    }

    @Override
    public PDFLinearLayout setSize(Number width, Number height) {
        super.setSize(width, height);
        return this;
    }

    @Override
    public PDFLinearLayout setBackgroundColor(int color) {
        super.setBackgroundColor(color);
        return this;
    }

    @Override
    public PDFLinearLayout setMargin(RectF margin) {
        super.setMargin(margin);
        return this;
    }

    @Override
    public PDFLinearLayout setMargin(float left, float top, float right, float bottom) {
        super.setMargin(left, top, right, bottom);
        return this;
    }

    @Override
    public PDFLinearLayout setMargin(float all) {
        super.setMargin(all);
        return this;
    }

    @Override
    public PDFLinearLayout setMargin(float horizontal, float vertical) {
        super.setMargin(horizontal, vertical);
        return this;
    }

    @Override
    public PDFLinearLayout setPadding(float all) {
        super.setPadding(all);
        return this;
    }

    @Override
    public PDFLinearLayout setPadding(float horizontal, float vertical) {
        super.setPadding(horizontal, vertical);
        return this;
    }

    @Override
    public PDFLinearLayout setPadding(RectF padding) {
        super.setPadding(padding);
        return this;
    }

    @Override
    public PDFLinearLayout setPadding(float left, float top, float right, float bottom) {
        super.setPadding(left, top, right, bottom);
        return this;
    }

    @Override
    public PDFLinearLayout setBorder(Action<Border, Border> action) {
        super.setBorder(action);
        return this;
    }

    @Override
    public PDFLinearLayout setBorder(float size, @ColorInt int color) {
        super.setBorder(size, color);
        return this;
    }

    @Override
    public PDFLinearLayout setAnchor(Integer horizontal, Integer vertical) {
        super.setAnchor(horizontal, vertical);
        return this;
    }
    @Override
    protected PDFLinearLayout setParent(PDFComponent parent) {
        super.setParent(parent);
        return this;
    }

    public static PDFLinearLayout build(){return new PDFLinearLayout();}
}
