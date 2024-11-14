package com.hangyeolee.androidpdfwriter.utils;


import android.graphics.RectF;

// 폰트 메트릭 정보를 전달하기 위한 클래스
public class FontMetrics {
    public final float ascent;
    public final float descent;
    public final float capHeight;
    public final float stemV;
    public final RectF fontBBox;
    public final float[] charWidths;

    public FontMetrics(float ascent, float descent, float capHeight,
                       float stemV, RectF fontBBox, float[] charWidths) {
        this.ascent = ascent;
        this.descent = descent;
        this.capHeight = capHeight;
        this.stemV = stemV;
        this.fontBBox = fontBBox;
        this.charWidths = charWidths;
    }
}
