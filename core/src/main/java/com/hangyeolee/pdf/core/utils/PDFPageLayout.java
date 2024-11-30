package com.hangyeolee.pdf.core.utils;

import android.graphics.RectF;

// 구현체
public class PDFPageLayout implements PageLayout {
    private final RectF pageRect;
    private final RectF padding;

    protected PDFPageLayout(RectF pageRect, RectF padding) {
        this.pageRect = new RectF(pageRect);
        this.padding = new RectF(padding);
    }

    @Override
    public RectF getPageRect() {
        return new RectF(pageRect);
    }

    @Override
    public RectF getPadding() {
        return new RectF(padding);
    }

    @Override
    public float getContentWidth() {
        return pageRect.width() - padding.left - padding.right;
    }

    @Override
    public float getContentHeight() {
        return pageRect.height() - padding.top - padding.bottom;
    }

    @Override
    public float transform2PDFHeight(float y) {
        return pageRect.height() - padding.top - y;
    }

    @Override
    public float transform2PDFWidth(float x) {
        return padding.left + x;
    }
}