package com.hangyeolee.androidpdfwriter.utils;

import java.lang.annotation.Native;

public enum Paper{
    // 840  x1188mm
    A0(2381.103648f,3367.5608736f),
    // 594  x840mm
    A1(1683.7804368f,2381.103648f),
    // 420  x594mm
    A2(1190.551824f,1683.7804368f),
    // 297  x420mm
    A3(841.8902184f,1190.551824f),
    // 210  x297mm
    A4(595.275912f, 841.8902184f),
    // 148.5x210mm
    A5(595.275912f, 841.8902184f),
    // 1028  1456
    B0(2914.0173216f, 4127.2463232f),
    // 728  1028
    B1(2063.6231616f, 2914.0173216f),
    // 514  728
    B2(1457.0086608f, 2063.6231616f),
    // 364  514
    B3(1031.8115808f, 1457.0086608f),
    // 257  364
    B4(728.5043304f, 1031.8115808f),
    // 182  x257mm
    B5(515.9057904f, 728.5043304f),
    // 8.5  X11inch
    Letter(612f, 792f),
    // 8.5  X14inch
    Legal(612f, 1008f);
    //*2.8346472

    private float width;
    private float height;

    Paper(float width, float height){
        this.width = width;
        this.height = height;
    }

    public void setCustom(float width, float height){
        this.width = width;
        this.height = height;
    }

    public float getHeight() {
        return height;
    }

    public float getWidth() {
        return width;
    }

    public Paper Landscape(){
        float w = getWidth();
        width = getHeight();
        height = w;
        return this;
    }
}
