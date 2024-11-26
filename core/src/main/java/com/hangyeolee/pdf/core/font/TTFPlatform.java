package com.hangyeolee.pdf.core.font;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class TTFPlatform {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            PLATFORM_UNICODE,
            PLATFORM_MAC,
            PLATFORM_ISO,
            PLATFORM_WINDOWS
    })
    public @interface ID{}
    public static final int PLATFORM_UNICODE = 0;
    public static final int PLATFORM_MAC = 1;
    public static final int PLATFORM_ISO = 2;
    public static final int PLATFORM_WINDOWS = 3;
}
