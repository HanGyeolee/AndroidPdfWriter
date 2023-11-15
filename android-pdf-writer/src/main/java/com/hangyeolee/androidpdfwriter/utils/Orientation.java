package com.hangyeolee.androidpdfwriter.utils;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Orientation {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Row, Column})
    public @interface OrientationInt {}
    /**
     * 가로
     */
    public static final int Row = 0;
    /**
     * 세로
     */
    public static final int Column = 1;
}
