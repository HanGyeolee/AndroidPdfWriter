package com.hangyeolee.androidpdfwriter.utils;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class DPI {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({D2, Standard, M2, M4, M5, M8, M10})
    public @interface DPIInt {}
    public static final int D2 = 36;
    /**
     * PDF 파일의 기본 DPI <br>
     * Default DPI in PDF file
     */
    public static final int Standard = 72;
    public static final int M2 = 144;
    public static final int M4 = 288;
    public static final int M5 = 360;
    public static final int M8 = 576;
    public static final int M10 = 720;
}
