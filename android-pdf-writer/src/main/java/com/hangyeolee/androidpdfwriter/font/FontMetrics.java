package com.hangyeolee.androidpdfwriter.font;


import android.graphics.RectF;

// 폰트 메트릭 정보를 전달하기 위한 클래스
public class FontMetrics {
    public final int flags;
    public final RectF fontBBox;
    public final int italicAngle;
    public final float ascent;
    public final float descent;
    public final float capHeight;
    public final float stemV;
    public final float[] charWidths;

    public FontMetrics(int flags, int italicAngle, float ascent, float descent, float capHeight,
                       float stemV, RectF fontBBox, float[] charWidths) {
        this.flags = flags;
        this.italicAngle = italicAngle;
        this.ascent = ascent;
        this.descent = descent;
        this.capHeight = capHeight;
        this.stemV = stemV;
        this.fontBBox = fontBBox;
        this.charWidths = charWidths;
    }
}
