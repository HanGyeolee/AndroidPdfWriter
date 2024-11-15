package com.hangyeolee.androidpdfwriter.utils;

import java.lang.annotation.Native;

public enum Paper{
    // 840  x1188mm
    A0(840f,1188f, PaperUnit.MM),
    // 594  x840mm
    A1(594f,840f, PaperUnit.MM),
    // 420  x594mm
    A2(420f,594f, PaperUnit.MM),
    // 297  x420mm
    A3(297f,420f, PaperUnit.MM),
    // 210  x297mm
    A4(210f,297f, PaperUnit.MM),
    // 148.5x210mm
    A5(148.5f, 210f, PaperUnit.MM),
    // 1028  1456
    B0(1028f,1456f, PaperUnit.MM),
    // 728  1028
    B1(728f,1028f, PaperUnit.MM),
    // 514  728
    B2(514f,728f, PaperUnit.MM),
    // 364  514
    B3(364f,514f, PaperUnit.MM),
    // 257  364
    B4(257f,364f, PaperUnit.MM),
    // 182  x257
    B5(182f,257f, PaperUnit.MM),
    // 8.5  X11inch
    Letter(8.5f, 11f, PaperUnit.INCH),
    // 8.5  X14inch
    Legal(8.5f, 14f, PaperUnit.INCH);

    private float width;
    private float height;
    private PaperUnit unit;

    Paper(float width, float height, PaperUnit unit){
        this.width = width;
        this.height = height;
        this.unit = unit;
    }

    public void setCustom(float width, float height, PaperUnit unit){
        this.width = width;
        this.height = height;
        this.unit = unit;
    }

    public float getHeight() {
        return (float) (height * unit.toPt());
    }

    public float getWidth() {
        return (float) (width * unit.toPt());
    }

    public Paper Landscape(){
        float w = getWidth();
        width = getHeight();
        height = w;
        return this;
    }
}
