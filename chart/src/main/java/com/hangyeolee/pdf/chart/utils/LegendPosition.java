package com.hangyeolee.pdf.chart.utils;

import androidx.annotation.IntDef;

import com.hangyeolee.pdf.chart.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class LegendPosition {
    @IntDef(value = {
            NONE, BOTTOM, TOP, RIGHT, LEFT
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ID {}
    public static final int NONE    = 0x00;
    public static final int BOTTOM  = 0x01;
    public static final int TOP     = 0x02;
    public static final int RIGHT   = 0x04;
    public static final int LEFT    = 0x08;
}
