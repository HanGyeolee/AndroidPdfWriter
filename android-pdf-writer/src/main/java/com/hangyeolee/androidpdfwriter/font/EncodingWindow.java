package com.hangyeolee.androidpdfwriter.font;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class EncodingWindow {
    // Windows Platform Encodings
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            ENCODING_WINDOWS_SYMBOL,
            ENCODING_WINDOWS_UNICODE_BMP,
            ENCODING_WINDOWS_SHIFTJIS,
            ENCODING_WINDOWS_PRC,
            ENCODING_WINDOWS_BIG5,
            ENCODING_WINDOWS_WANSUNG,
            ENCODING_WINDOWS_JOHAB,
            ENCODING_WINDOWS_UNICODE_FULL,
            ENCODING_WINDOWS_GB18030,
            ENCODING_WINDOWS_OEM,
            ENCODING_WINDOWS_LATIN1,
            ENCODING_WINDOWS_LATIN2,
            ENCODING_WINDOWS_CYRILLIC,
            ENCODING_WINDOWS_GREEK,
            ENCODING_WINDOWS_TURKISH,
            ENCODING_WINDOWS_HEBREW,
            ENCODING_WINDOWS_ARABIC,
            ENCODING_WINDOWS_BALTIC,
            ENCODING_WINDOWS_VIETNAMESE,
            ENCODING_WINDOWS_THAI
    })
    public @interface ID {}
    public static final int ENCODING_WINDOWS_SYMBOL = 0;
    public static final int ENCODING_WINDOWS_UNICODE_BMP = 1;
    public static final int ENCODING_WINDOWS_SHIFTJIS = 2;
    public static final int ENCODING_WINDOWS_PRC = 3;
    public static final int ENCODING_WINDOWS_BIG5 = 4;
    public static final int ENCODING_WINDOWS_WANSUNG = 5;
    public static final int ENCODING_WINDOWS_JOHAB = 6;
    public static final int ENCODING_WINDOWS_UNICODE_FULL = 10;
    public static final int ENCODING_WINDOWS_GB18030 = 11;
    public static final int ENCODING_WINDOWS_OEM = 12;
    public static final int ENCODING_WINDOWS_LATIN1 = 0x4E9F;  // Windows-1252
    public static final int ENCODING_WINDOWS_LATIN2 = 0x4E92;  // Windows-1250
    public static final int ENCODING_WINDOWS_CYRILLIC = 0x4E95; // Windows-1251
    public static final int ENCODING_WINDOWS_GREEK = 0x4E98;    // Windows-1253
    public static final int ENCODING_WINDOWS_TURKISH = 0x4E99;  // Windows-1254
    public static final int ENCODING_WINDOWS_HEBREW = 0x4E9A;   // Windows-1255
    public static final int ENCODING_WINDOWS_ARABIC = 0x4E9B;   // Windows-1256
    public static final int ENCODING_WINDOWS_BALTIC = 0x4E9D;   // Windows-1257
    public static final int ENCODING_WINDOWS_VIETNAMESE = 0x4EA5; // Windows-1258
    public static final int ENCODING_WINDOWS_THAI = 0x4EA3;


    public static String getCharset(@ID int encodingId) {
        switch (encodingId) {
            // 유니코드 인코딩
            case ENCODING_WINDOWS_UNICODE_BMP:
                return "UTF-16LE";  // Windows는 Little Endian을 기본으로 사용
            case ENCODING_WINDOWS_UNICODE_FULL:
                return "UTF-16LE";

            // 아시아 언어 인코딩
            case ENCODING_WINDOWS_SHIFTJIS:
                return "Shift_JIS";  // MS932
            case ENCODING_WINDOWS_PRC:
                return "GBK";        // Windows의 중국어 간체
            case ENCODING_WINDOWS_GB18030:
                return "GB18030";    // 확장된 중국어 인코딩
            case ENCODING_WINDOWS_BIG5:
                return "Big5";       // Windows의 중국어 번체
            case ENCODING_WINDOWS_WANSUNG:
                return "windows-949"; // 한국어 완성형
            case ENCODING_WINDOWS_JOHAB:
                return "x-Johab";    // 한국어 조합형
            case ENCODING_WINDOWS_THAI:
                return "windows-874"; // 태국어

            // 서유럽 및 중부 유럽 인코딩
            case ENCODING_WINDOWS_LATIN1:
                return "windows-1252"; // 서유럽 언어
            case ENCODING_WINDOWS_LATIN2:
                return "windows-1250"; // 중부 유럽 언어

            // 키릴 문자 및 그리스어
            case ENCODING_WINDOWS_CYRILLIC:
                return "windows-1251"; // 러시아어 및 키릴 문자
            case ENCODING_WINDOWS_GREEK:
                return "windows-1253"; // 그리스어

            // 중동 언어
            case ENCODING_WINDOWS_TURKISH:
                return "windows-1254"; // 터키어
            case ENCODING_WINDOWS_HEBREW:
                return "windows-1255"; // 히브리어
            case ENCODING_WINDOWS_ARABIC:
                return "windows-1256"; // 아랍어

            // 기타 인코딩
            case ENCODING_WINDOWS_BALTIC:
                return "windows-1257"; // 발트어
            case ENCODING_WINDOWS_VIETNAMESE:
                return "windows-1258"; // 베트남어
            case ENCODING_WINDOWS_SYMBOL:
                return "x-symbol";     // 심볼
            case ENCODING_WINDOWS_OEM:
                return "IBM437";       // OEM/DOS 인코딩

            // 기본값
            default:
                return "UTF-8";        // 알 수 없는 인코딩의 경우 UTF-8 사용
        }
    }
}
