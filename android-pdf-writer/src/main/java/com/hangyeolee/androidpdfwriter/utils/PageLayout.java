package com.hangyeolee.androidpdfwriter.utils;

import android.graphics.RectF;

// 페이지 레이아웃 관련 인터페이스 정의
public interface PageLayout {
    RectF getPageRect();
    RectF getPadding();
    float getContentWidth();
    float getContentHeight();
    float transform2PDFHeight(float y);
    float transform2PDFWidth(float x);
}
