package com.hangyeolee.pdf.core;


import android.graphics.RectF;

import androidx.annotation.ColorInt;

import com.hangyeolee.pdf.core.binary.BinarySerializer;
import com.hangyeolee.pdf.core.listener.Action;
import com.hangyeolee.pdf.core.utils.Border;
import com.hangyeolee.pdf.core.utils.Zoomable;

/**
 * 강제 페이지 나누기를 위한 컴포넌트.<br>
 * 이 컴포넌트 이후의 모든 내용은 새로운 페이지에서 시작됩니다.<br>
 * Components for forced page splitting.<br>
 * Everything after this component begins on a new page.<br>
 */
public class PDFPageBreak extends PDFComponent {
    public PDFPageBreak() {super();}

    @Override
    public void measure(float x, float y) {
        super.measure(x, y);

        // 현재 페이지의 남은 공간을 모두 차지하도록 높이 조정
        float pageHeight = Zoomable.getInstance().getContentHeight();
        int currentPage = calculatePageIndex(measureY);
        float nextHeight = (currentPage + 1) * pageHeight;
        float remainingHeight = nextHeight - measureY;

        height = remainingHeight;
        // 부모 컴포넌트의 높이도 업데이트
        if (remainingHeight > measureHeight) {
            updateHeight(remainingHeight - measureHeight);
        }
    }

    @Override
    public void draw(BinarySerializer serializer) {
        super.draw(serializer);
        // draw nothing
    }

    /**
     * 페이지 나누기 컴포넌트는 자체적으로 Margin을 가질 수 없다.<br>
     * Cells cannot have Margin on their own.
     */
    @Override
    @Deprecated
    public PDFPageBreak setMargin(RectF margin) {
        return this;
    }

    /**
     * 페이지 나누기 컴포넌트는 자체적으로 Margin을 가질 수 없다.<br>
     * Cells cannot have Margin on their own.
     */
    @Override
    @Deprecated
    public PDFPageBreak setMargin(float left, float top, float right, float bottom) {
        return this;
    }

    /**
     * 페이지 나누기 컴포넌트는 자체적으로 Margin을 가질 수 없다.<br>
     * Cells cannot have Margin on their own.
     */
    @Override
    @Deprecated
    public PDFPageBreak setMargin(float all) {
        return this;
    }

    /**
     * 페이지 나누기 컴포넌트는 자체적으로 Margin을 가질 수 없다.<br>
     * Cells cannot have Margin on their own.
     */
    @Override
    @Deprecated
    public PDFPageBreak setMargin(float horizontal, float vertical) {
        return this;
    }

    /**
     * 페이지 나누기 컴포넌트는 자체적으로 Padding 을 가질 수 없다.<br>
     * Cells cannot have Padding on their own.
     */
    @Override
    @Deprecated
    public PDFPageBreak setPadding(float all) {
        return this;
    }

    /**
     * 페이지 나누기 컴포넌트는 자체적으로 Padding 을 가질 수 없다.<br>
     * Cells cannot have Padding on their own.
     */
    @Override
    @Deprecated
    public PDFPageBreak setPadding(float horizontal, float vertical) {
        return this;
    }

    /**
     * 페이지 나누기 컴포넌트는 자체적으로 Padding 을 가질 수 없다.<br>
     * Cells cannot have Padding on their own.
     */
    @Override
    @Deprecated
    public PDFPageBreak setPadding(RectF padding) {
        return this;
    }

    /**
     * 페이지 나누기 컴포넌트는 자체적으로 Padding 을 가질 수 없다.<br>
     * Cells cannot have Padding on their own.
     */
    @Override
    @Deprecated
    public PDFPageBreak setPadding(float left, float top, float right, float bottom) {
        return this;
    }

    /**
     * 페이지 나누기 컴포넌트는 자체적으로 Border을 가질 수 없다.<br>
     * Cells cannot have Border on their own.
     */
    @Override
    @Deprecated
    public PDFPageBreak setBorder(Action<Border, Border> action) {
        return this;
    }

    /**
     * 페이지 나누기 컴포넌트는 자체적으로 Border을 가질 수 없다.<br>
     * Cells cannot have Border on their own.
     */
    @Override
    @Deprecated
    public PDFPageBreak setBorder(float size, @ColorInt int color) {
        return this;
    }

    /**
     * 페이지 나누기 컴포넌트는 자체적으로 Anchor을 가질 수 없다.<br>
     * Cells cannot have Anchor on their own.
     */
    @Override
    @Deprecated
    public PDFPageBreak setAnchor(Integer horizontal, Integer vertical) {
        super.setAnchor(horizontal, vertical);
        return this;
    }
    
    /**
     * {@link PDFPageBreak}를 생성합니다.
     * @return 새로운 PDFPageBreak 인스턴스
     */
    public static PDFPageBreak build() { return new PDFPageBreak(); }
}
