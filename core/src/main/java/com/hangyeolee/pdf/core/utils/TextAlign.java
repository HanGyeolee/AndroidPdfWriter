package com.hangyeolee.pdf.core.utils;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class TextAlign {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Start, Center, End})
    public @interface TextAlignInt {}
    public static final int Start = 0;
    public static final int Center = 1;
    public static final int End = 2;
}
