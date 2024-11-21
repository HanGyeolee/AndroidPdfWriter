package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Color;

import com.hangyeolee.androidpdfwriter.binary.BinarySerializer;
import com.hangyeolee.androidpdfwriter.utils.Anchor;
import com.hangyeolee.androidpdfwriter.utils.Border;
import com.hangyeolee.androidpdfwriter.utils.Zoomable;

import java.util.ArrayList;

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

    @Override
    public StringBuilder draw(BinarySerializer serializer) {
        float pageHeight = Zoomable.getInstance().getContentHeight();
        float remainingHeight = measureHeight;

        // 시작 페이지와 끝 페이지 계산
        int startPage = calculatePageIndex(measureY);
        int endPage = calculatePageIndex(measureY, measureHeight);

        // 첫 페이지의 Y 좌표 조정
        float y = measureY - startPage * pageHeight;
        if(y < 0) y = 0;

        float currentY = y;

        // 페이지별로 분할하여 그리기
        int currentPage = 0;
        while (remainingHeight > 0) {
            StringBuilder content = serializer.getPage(startPage + currentPage);

            // 현재 페이지에 그릴 수 있는 높이 계산
            float availableHeight = pageHeight - (currentY % pageHeight);
            float heightToDraw = Math.min(availableHeight, remainingHeight);

            // 현재 페이지에 배경과 테두리 그리기
            drawPageSection(content, currentY, heightToDraw,
                    currentPage == 0, currentPage == endPage - startPage);

            remainingHeight -= heightToDraw;
            currentY = 0; // 다음 페이지에서는 최상단부터 시작
            currentPage++;
        }
        return null;
    }

    private void drawPageSection(StringBuilder content,
                                 float startY, float height,
                                 boolean isFirstPage, boolean isLastPage) {

        // 배경 그리기
        if (backgroundColor != Color.TRANSPARENT && measureWidth > 0) {
            // 그래픽스 상태 저장
            PDFGraphicsState.save(content);

            setColorInPDF(content, backgroundColor);
            drawRectInPDF(content,
                    measureX, startY, measureWidth, height,
                    true, false);

            // 그래픽스 상태 복원
            PDFGraphicsState.restore(content);
        }

        // 테두리 그리기 - 페이지 경계에서 적절히 분할
        Border sectionBorder = new Border();
        sectionBorder.copy(border);

        // 페이지 경계에서 테두리 처리
        if (!isFirstPage) {
            sectionBorder.setTop(0, Color.TRANSPARENT);
        }
        if (!isLastPage) {
            sectionBorder.setBottom(0, Color.TRANSPARENT);
        }

        sectionBorder.draw(content, measureX, startY,
                measureWidth, height);
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

    /**
     * 레이아웃의 모든 하위 구성 요소를 부모 크기에 맞출지 설정
     */
    public PDFLayout setFitChildrenToLayout(boolean fit) {
        this.fitChildrenToLayout = fit;
        return this;
    }
}

