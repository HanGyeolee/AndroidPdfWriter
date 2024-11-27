package com.hangyeolee.androidpdfwriter.components;

import android.graphics.RectF;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;

import com.hangyeolee.androidpdfwriter.binary.BinaryConverter;
import com.hangyeolee.androidpdfwriter.binary.BinarySerializer;
import com.hangyeolee.androidpdfwriter.exceptions.LayoutChildrenFitDeniedException;
import com.hangyeolee.androidpdfwriter.utils.Border;
import com.hangyeolee.androidpdfwriter.utils.Orientation;

import java.util.ArrayList;
import java.util.Locale;

import com.hangyeolee.androidpdfwriter.listener.Action;
import com.hangyeolee.androidpdfwriter.utils.Zoomable;

public class PDFLinearLayout extends PDFLayout {
    protected ArrayList<PDFComponent> children;
    @Orientation.OrientationInt
    int orientation = Orientation.Vertical;
    private final ArrayList<Float> weights = new ArrayList<>();

    protected PDFLinearLayout(){
        super();
        children = new ArrayList<>();
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
            for (int i = 0; i < children.size(); i++) {
                totalWeight += weights.get(i);
            }
        }

        float defaultHeight = measureHeight;
        // 하위 구성 요소 측정 및 페이지 분할 처리
        for (int i = 0; i < children.size(); i++) {
            PDFComponent child = children.get(i);
            float childWeight = weights.get(i);

            // 하위 구성 요소의 높이 계산
            if (fitChildrenToLayout) {
                // weight 비율에 따른 높이 계산
                float childHeight = (defaultHeight * childWeight) / totalWeight;
                child.setSize(null, childHeight);
            }

            // 하위 구성 요소 위치 측정
            child.measure(0, currentY);
            if (!(child instanceof PDFLayout)) {
                float maxW = measureWidth
                        - border.size.left - border.size.right
                        - padding.left - padding.right
                        - child.margin.left - child.margin.right;
                float maxH = measureHeight - currentY
                        - border.size.top - border.size.bottom
                        - padding.top - padding.bottom
                        - child.margin.top - child.margin.bottom;
                childReanchor(child, maxW, maxH);
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
        // 테두리와 패딩을 제외한 실제 사용 가능한 너비 계산
        float contentHeight = Zoomable.getInstance().getContentHeight();
        float availableWidth = measureWidth - border.size.left - padding.left
                - border.size.right - padding.right;
        float maxHeight = 0;
        float currentX = 0;

        // weight 총합 계산
        float totalWeight = 0;
        for (int i = 0; i < children.size(); i++) {
            totalWeight += weights.get(i);
        }

        // 하위 구성 요소들 측정
        for (int i = 0; i < children.size(); i++) {
            PDFComponent child = children.get(i);
            float childWidth = child.width;
            float childWeight = weights.get(i);
            float cellWidth = (availableWidth * childWeight) / totalWeight;
            child.setSize(
                    childWidth <= 0 ? cellWidth : null,
                    fitChildrenToLayout ? measureHeight : null
            );

            // 하위 구성 요소 위치 측정
            child.measure(currentX, 0);
            // PDFLayout은 페이지네이션 체크 건너뛰기
            if (!(child instanceof PDFLayout)) {
                float maxW = cellWidth - child.margin.left - child.margin.right;
                float maxH = measureHeight
                        - border.size.top - border.size.bottom
                        - padding.top - padding.bottom
                        - child.margin.top - child.margin.bottom;
                childReanchor(child, maxW, maxH);
            }

            maxHeight = Math.max(maxHeight, child.getTotalHeight());
            currentX += cellWidth;
        }

        // fitChildrenToLayout이 false일 때 최대 높이로 레이아웃 크기 조정
        if (!fitChildrenToLayout) {
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

                currentX += cellWidth;
            }
        }
    }


    @Override
    public void draw(BinarySerializer serializer) {
        super.draw(serializer);

        for(int i = 0; i < children.size(); i++) {
            PDFComponent child = children.get(i);

            pagenationDrawStart(serializer, child,
                (content, component, x, y, width, height, currentPage, startPage, endPage) -> {
                    // 현재 페이지 내에서의 좌표 계산
                    float pdfX = Zoomable.getInstance().transform2PDFWidth(
                            x
                    );
                    float pdfY = Zoomable.getInstance().transform2PDFHeight(
                            y + height
                    );

                    // 그래픽스 상태 저장
                    PDFGraphicsState.save(content);

                    // 클리핑 영역 설정 - 컴포넌트의 전체 영역
                    // W 클리핑 패스 설정
                    // n 패스를 그리지 않고 클리핑만 적용
                    content.append(String.format(Locale.getDefault(),
                            "%s %s %s %s re W n\r\n",
                            BinaryConverter.formatNumber(pdfX),
                            BinaryConverter.formatNumber(pdfY),
                            BinaryConverter.formatNumber(
                                    width),
                            BinaryConverter.formatNumber(
                                    height))
                    );
                }
            );

            // 하위 구성 요소 그리기
            child.draw(serializer);

            pagenationDrawEnd(serializer, child, content -> {
                // 그래픽스 상태 복원
                PDFGraphicsState.restore(content);
                return null;
            });
        }
    }

    /**
     * 레이아웃에 자식 추가<br>
     * Add children to layout
     * @param component 하위 구성 요소
     * @return 자기자신
     */
    public PDFLinearLayout addChild(PDFComponent component){
        return addChild(component, 1);
    }

    /**
     * 레이아웃에 자식 추가<br>
     * Add children to layout<br>
     * @param component 하위 구성 요소
     * @param weight 하위 구성 요소가 차지할 크기 비율. 반드시 1.0f보다 커야한다.<br/>
     *               만약 {@link #orientation} 이 {@link Orientation#Horizontal} 라면 width, {@link Orientation#Vertical} 이라면 height 를 넣으면 된다.<br/>
     *               Ratio of the size of the child component. Must bigger than 1.0f<br/>
     *               If {@link #orientation} is {@link Orientation#Horizontal} then width, {@link Orientation#Vertical} then height.
     * @return 자기자신
     */
    public PDFLinearLayout addChild(PDFComponent component, @FloatRange(from = 1.0) float weight){
        if(weight < 1) weight = 1;
        if(children.size() == weights.size())
            weights.add(weight);
        component.setParent(this);
        children.add(component);
        return this;
    }

    /**
     * 이전에 설정한 가중치 값들을 지우고, 지정한 가중치로 변경합니다.<br>
     * 하위 구성 요소의 개수보다 많은 가중치는 무시됩니다. 하위 구성 요소의 개수 보다 적은 가중치를 입력하면 나머지를 1.0f으로 변경합니다.<br>
     * Clear the previously set weights values and change them to the specified weights.<br>
     * Weights greater than the number of child components are ignored. If you enter weights less than the child component, change the rest to 1.0f.<br>
     * @param weights 가중치
     * @return
     */
    public PDFLinearLayout setWeights(float... weights){
        this.weights.clear();
        for(float weight : weights){
            this.weights.add(weight);
        }
        while(children.size() > this.weights.size()){
            this.weights.add(1.0f);
        }
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

    public static PDFLinearLayout build(@Orientation.OrientationInt int orientation){return new PDFLinearLayout().setOrientation(orientation);}
}
