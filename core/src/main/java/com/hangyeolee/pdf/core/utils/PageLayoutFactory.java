package com.hangyeolee.pdf.core.utils;

import android.graphics.RectF;

import androidx.annotation.FloatRange;
import androidx.annotation.Nullable;

// PDFBuilder 등에서 사용할 Factory 클래스
public class PageLayoutFactory {
    public static PageLayout createDefaultLayout() {
        Paper pageSize = Paper.A4;
        return new PDFPageLayout(new RectF(0, 0, pageSize.getWidth(), pageSize.getHeight()), new RectF());
    }


    /**
     * PDF 페이지 패딩, 세로 및 가로 설정.<br/>
     * 모든 페이지에 지정된 패딩이 적용 됩니다.<br/>
     * set PDF page padding, vertical and horizontal<br/>
     * The specified padding applies to all pages.
     * @param pageSize 페이지 크기
     * @param padding 패딩
     * @return 자기 자신
     */
    public static PageLayout createLayout(Paper pageSize, RectF padding) {
        RectF pageRect = new RectF(0, 0, pageSize.getWidth(), pageSize.getHeight());
        return new PDFPageLayout(pageRect, padding);
    }


    /**
     * PDF 페이지 패딩, 세로 및 가로 설정.<br/>
     * 모든 페이지에 지정된 패딩이 적용 됩니다.<br/>
     * set PDF page padding, vertical and horizontal<br/>
     * The specified padding applies to all pages.
     * @param pageSize 페이지 크기
     * @param vertical 세로 패딩
     * @param horizontal 가로 패딩
     * @return 자기 자신
     */
    public static PageLayout createLayout(Paper pageSize,
                                          @FloatRange(from = 0.0f) float vertical,
                                          @FloatRange(from = 0.0f) float horizontal) {
        if(vertical < 0) vertical = 0;
        if(horizontal < 0) horizontal = 0;
        RectF padding = new RectF(vertical, horizontal, vertical, horizontal);
        return createLayout(pageSize, padding);
    }


    /**
     * PDF 페이지 패딩, 세로 및 가로 설정.<br/>
     * 모든 페이지에 지정된 패딩이 적용 됩니다.<br/>
     * set PDF page padding, vertical and horizontal<br/>
     * The specified padding applies to all pages.
     * @param pageSize 페이지 크기
     * @param left 왼쪽 패딩
     * @param top 위쪽 패딩
     * @param right 오른쪽 패딩
     * @param bottom 아래쪽 패딩
     * @return 자기 자신
     */
    public static PageLayout createLayout(Paper pageSize,
                                          @Nullable Float left, @Nullable Float top,
                                          @Nullable Float right, @Nullable Float bottom) {
        float n_left = 0;
        float n_top = 0;
        float n_right = 0;
        float n_bottom = 0;

        if(left != null && left > 0.0f) n_left = left;
        if(top != null && top > 0.0f) n_top = top;
        if(right != null && right > 0.0f) n_right = right;
        if(bottom != null && bottom > 0.0f) n_bottom = bottom;

        RectF padding = new RectF(n_left, n_top, n_right, n_bottom);
        return createLayout(pageSize, padding);
    }
}
