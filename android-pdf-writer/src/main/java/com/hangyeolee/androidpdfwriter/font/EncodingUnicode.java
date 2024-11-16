package com.hangyeolee.androidpdfwriter.font;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class EncodingUnicode {
    // Unicode Platform Encodings
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            ENCODING_UNICODE_1_0,
            ENCODING_UNICODE_1_1,
            ENCODING_ISO_10646,
            ENCODING_UNICODE_2_0_BMP,
            ENCODING_UNICODE_2_0_FULL,
            ENCODING_UNICODE_VARIATION,
            ENCODING_UNICODE_FULL,
            ENCODING_UNICODE_3_0,
            ENCODING_UNICODE_3_1,
            ENCODING_UNICODE_3_2,
            ENCODING_UNICODE_4_0,
            ENCODING_UNICODE_4_1,
            ENCODING_UNICODE_5_0,
            ENCODING_UNICODE_5_1,
            ENCODING_UNICODE_5_2,
            ENCODING_UNICODE_6_0,
            ENCODING_UNICODE_6_1,
            ENCODING_UNICODE_6_2,
            ENCODING_UNICODE_6_3,
            ENCODING_UNICODE_7_0,
            ENCODING_UNICODE_8_0,
            ENCODING_UNICODE_9_0,
            ENCODING_UNICODE_10_0,
            ENCODING_UNICODE_11_0,
            ENCODING_UNICODE_12_0,
            ENCODING_UNICODE_12_1,
            ENCODING_UNICODE_13_0,
            ENCODING_UNICODE_14_0
    })
    public @interface ID {}
    public static final int ENCODING_UNICODE_1_0 = 0;
    public static final int ENCODING_UNICODE_1_1 = 1;
    public static final int ENCODING_ISO_10646 = 2;
    public static final int ENCODING_UNICODE_2_0_BMP = 3;
    public static final int ENCODING_UNICODE_2_0_FULL = 4;
    public static final int ENCODING_UNICODE_VARIATION = 5;
    public static final int ENCODING_UNICODE_FULL = 6;
    public static final int ENCODING_UNICODE_3_0 = 7;
    public static final int ENCODING_UNICODE_3_1 = 8;
    public static final int ENCODING_UNICODE_3_2 = 9;
    public static final int ENCODING_UNICODE_4_0 = 10;
    public static final int ENCODING_UNICODE_4_1 = 11;
    public static final int ENCODING_UNICODE_5_0 = 12;
    public static final int ENCODING_UNICODE_5_1 = 13;
    public static final int ENCODING_UNICODE_5_2 = 14;
    public static final int ENCODING_UNICODE_6_0 = 15;
    public static final int ENCODING_UNICODE_6_1 = 16;
    public static final int ENCODING_UNICODE_6_2 = 17;
    public static final int ENCODING_UNICODE_6_3 = 18;
    public static final int ENCODING_UNICODE_7_0 = 19;
    public static final int ENCODING_UNICODE_8_0 = 20;
    public static final int ENCODING_UNICODE_9_0 = 21;
    public static final int ENCODING_UNICODE_10_0 = 22;
    public static final int ENCODING_UNICODE_11_0 = 23;
    public static final int ENCODING_UNICODE_12_0 = 24;
    public static final int ENCODING_UNICODE_12_1 = 25;
    public static final int ENCODING_UNICODE_13_0 = 26;
    public static final int ENCODING_UNICODE_14_0 = 27;

    public static String getCharset(@ID int encodingId) {
        // 모든 유니코드 인코딩은 UTF-16BE를 사용
        return "UTF-16BE";
    }
}
