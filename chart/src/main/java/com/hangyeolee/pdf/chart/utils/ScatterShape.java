package com.hangyeolee.pdf.chart.utils;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ScatterShape {
    @IntDef(value = {
            SHAPE_CIRCLE, SHAPE_TRIANGLE, SHAPE_SQUARE
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ID {}
    public static final int SHAPE_CIRCLE    = 1;
    public static final int SHAPE_TRIANGLE   = 2;
    public static final int SHAPE_SQUARE      = 3;
}
