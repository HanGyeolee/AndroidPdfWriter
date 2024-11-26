package com.hangyeolee.pdf.core.font;


import android.graphics.Rect;

// 폰트 메트릭 정보를 전달하기 위한 클래스
public class FontMetrics {
    public final int flags;
    public final Rect fontBBox;
    public final int italicAngle;
    public final int ascent;
    public final int descent;
    public final int capHeight;
    public final int stemV;
    public final int xHeight;
    public final int stemH;
    public final int[] charWidths;

    public FontMetrics(int flags, int italicAngle, int ascent, int descent, int capHeight,
                       int stemV, int xHeight, int stemH, Rect fontBBox, int[] charWidths) {
        this.flags = flags;
        this.italicAngle = italicAngle;
        this.ascent = ascent;
        this.descent = descent;
        this.capHeight = capHeight;
        this.stemV = stemV;
        this.xHeight = xHeight;
        this.stemH = stemH;
        this.fontBBox = fontBBox;
        this.charWidths = charWidths;
    }
}
