package com.hangyeolee.androidpdfwriter.utils;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Orientation {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Horizontal, Vertical})
    public @interface OrientationInt {}
    /**
     * 가로
     */
    public static final int Horizontal = 0;
    /**
     * 세로
     */
    public static final int Vertical = 1;
}
