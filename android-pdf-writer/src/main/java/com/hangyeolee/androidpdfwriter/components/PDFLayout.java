package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Color;

import com.hangyeolee.androidpdfwriter.binary.BinarySerializer;
import com.hangyeolee.androidpdfwriter.utils.Border;

import java.util.ArrayList;

public abstract class PDFLayout extends PDFComponent{
    ArrayList<PDFComponent> child;

    public PDFLayout(){super();}

    public ArrayList<PDFComponent> getChild(){return child;}

    @Override
    public void measure(float x, float y) {
        super.measure(x, y);

        // 레이아웃은 Anchor를 적용하지 않는 다.
        float d = 0;
        if (parent != null)
            d += parent.measureX + parent.border.size.left + parent.padding.left;
        measureX = relativeX + margin.left + d;
        d = 0;
        if (parent != null)
            d += parent.measureY + parent.border.size.top + parent.padding.top;
        measureY = relativeY + margin.top + d;
    }

    @Override
    public StringBuilder draw(BinarySerializer serializer) {
        float pageHeight = serializer.getPageHeight();
        float remainingHeight = getTotalHeight();
        int startPage = serializer.calculatePageIndex(measureY);
        int endPage = serializer.calculatePageIndex(measureY, getTotalHeight());
        measureY -= startPage * serializer.getPageHeight();
        StringBuilder content = serializer.getPage(startPage);
        float currentY = measureY;

        int add = 0;
        while (remainingHeight > 0) {
            // 현재 페이지에 그릴 수 있는 높이 계산
            float availableHeight = pageHeight - (currentY % pageHeight);
            float heightToDraw = Math.min(availableHeight, remainingHeight);

            // 현재 페이지에 배경과 테두리 그리기
            drawPageSection(serializer, content, currentY, heightToDraw, startPage, add, endPage);

            remainingHeight -= heightToDraw;
            currentY = 0; // 다음 페이지에서는 최상단부터 시작

            // 다음 페이지가 필요한 경우
            if (remainingHeight > 0) {
                content = serializer.getPage(startPage+ ++add);
            }
        }
        return content;
    }

    private void drawPageSection(BinarySerializer page, StringBuilder content, float startY, float height, int start, int add, int end) {
        // 그래픽스 상태 저장
        PDFGraphicsState.save(content);

        float left = measureX;
        float top = startY;

        // 배경 그리기
        if (backgroundColor != Color.TRANSPARENT && measureWidth > 0) {
            setColorInPDF(content, backgroundColor);
            drawRectInPDF(page, content, left, top, measureWidth, height, true, false);
        }

        // 테두리 그리기 - 페이지 경계에서 적절히 분할
        Border sectionBorder = new Border();
        sectionBorder.copy(border);

        // 첫 페이지가 아니면 위쪽 테두리 제거
        if (add > 0) {
            sectionBorder.setTop(0, Color.TRANSPARENT);
        }

        // 마지막 섹션이 아니면 아래쪽 테두리 제거
        if (start + add < end) {
            sectionBorder.setBottom(0, Color.TRANSPARENT);
        }

        sectionBorder.draw(page, content, left, top, measureWidth, height);

        // 그래픽스 상태 복원
        PDFGraphicsState.restore(content);
    }

    /**
     * 레이아웃에 자식 추가<br>
     * Add children to layout
     * @param component 자식 컴포넌트
     * @return 자기자신
     */
    public PDFLayout addChild(PDFComponent component){
        component.setParent(this);
        child.add(component);
        return this;
    }
}

