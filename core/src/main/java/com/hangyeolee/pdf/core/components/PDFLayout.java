package com.hangyeolee.pdf.core.components;

import com.hangyeolee.pdf.core.binary.BinarySerializer;
import com.hangyeolee.pdf.core.utils.Anchor;

public abstract class PDFLayout extends PDFComponent{
    protected boolean fitChildrenToLayout = false;  // 하위 구성 요소 높이 맞춤 옵션

    public PDFLayout(){
        super();
    }

    @Override
    public void measure(float x, float y) {
        super.measure(x, y);

        // 레이아웃은 앵커를 사용하지 않고 항상 좌상단 기준
        float dx = (parent != null) ?
                parent.measureX + parent.border.size.left + parent.padding.left : 0;
        float dy = (parent != null) ?
                parent.measureY + parent.border.size.top + parent.padding.top : 0;

        measureX = relativeX + margin.left + dx;
        measureY = relativeY + margin.top + dy;
    }

    @Override
    protected void updateHeight(float heightGap){
        height += heightGap;
        super.updateHeight(heightGap);
    }

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

    protected float pageEdgeCheck(PDFComponent child, float newY, float currentY){
        // 다음 페이지 시작점으로 이동
        float yDiff = newY - currentY;
        child.updateHeight(yDiff);
        // 변경된 위치로 다시 해당 컴포넌트 재 연산
        return newY - child.parent.measureY;
    }

    /**
     * 레이아웃의 모든 하위 구성 요소를 부모 크기에 맞출지 설정
     */
    public PDFLayout setFitChildrenToLayout(boolean fit) {
        this.fitChildrenToLayout = fit;
        return this;
    }
}

