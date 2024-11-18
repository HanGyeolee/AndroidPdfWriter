package com.hangyeolee.androidpdfwriter.components;

import android.graphics.RectF;

import com.hangyeolee.androidpdfwriter.binary.BinarySerializer;
import com.hangyeolee.androidpdfwriter.utils.Border;
import com.hangyeolee.androidpdfwriter.utils.Orientation;

import java.util.ArrayList;
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
        // 테두리와 패딩을 제외한 실제 사용 가능한 너비 계산
        float availableWidth = measureWidth - border.size.left - padding.left
                - border.size.right - padding.right;
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
                child.setSize(availableWidth, childHeight);
            } else {
                child.setSize(availableWidth, null);
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
        float availableWidth = measureWidth - border.size.left - padding.left
                - border.size.right - padding.right;
        float maxHeight = 0;
        float currentX = 0;

        // weight 총합 계산
        float totalWeight = 0;
        for (Float weight : weights) {
            totalWeight += weight;
        }

        // 자식 컴포넌트들의 기본 너비 총합 계산
        float totalChildrenWidth = 0;
        if (!fitChildrenToLayout) {
            for (PDFComponent child : children) {
                totalChildrenWidth += child.width;
            }
        }

        // 자식 컴포넌트들 측정
        for (int i = 0; i < children.size(); i++) {
            PDFComponent child = children.get(i);
            float childWeight = weights.get(i);

            // 너비 계산: fitChildrenToLayout이 true이거나 총 너비가 레이아웃 너비를 초과할 경우 weight 적용
            if (fitChildrenToLayout || totalChildrenWidth > availableWidth) {
                float childWidth = (availableWidth * childWeight) / totalWeight;
                child.setSize(childWidth, fitChildrenToLayout ? measureHeight : null);
            } else {
                child.setSize(child.width, null);
            }

            child.measure(currentX, 0);
            maxHeight = Math.max(maxHeight, child.getTotalHeight());
            currentX += child.getTotalWidth();
        }

        // fitChildrenToLayout이 false일 때 최대 높이로 레이아웃 크기 조정
        if (!fitChildrenToLayout && maxHeight > height) {
            updateHeight(maxHeight - height);

            // 높이가 변경되었으므로 자식들 재측정
            currentX = 0;
            for (PDFComponent child : children) {
                child.measure(currentX, 0);
                currentX += child.getTotalWidth();
            }
        }
    }


    @Override
    public StringBuilder draw(BinarySerializer serializer) {
        super.draw(serializer);

        for(int i = 0; i < children.size(); i++) {
            children.get(i).draw(serializer);
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
     * @param weight 자식 컴포넌트가 차지할 크기 비율.<br/>
     *               만약 {@link #orientation} 이 {@link Orientation#Horizontal} 라면 width, {@link Orientation#Vertical} 이라면 height 를 넣으면 된다.<br/>
     *               Ratio of the size of the child component.<br/>
     *               If {@link #orientation} is {@link Orientation#Horizontal} then width, {@link Orientation#Vertical} then height.
     * @return 자기자신
     */
    public PDFLinearLayout addChild(PDFComponent component, float weight){
        if(weight < 0) weight = 0;
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
    public PDFLinearLayout setSize(Float width, Float height) {
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
