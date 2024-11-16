package com.hangyeolee.androidpdfwriter.font;

import androidx.annotation.IntDef;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class EncodingMac {
    // Mac Platform Encodings
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            ENCODING_MAC_ROMAN,        // 0: 로마자 문자
            ENCODING_MAC_JAPANESE,     // 1: 일본어 (ShiftJIS 기반)
            ENCODING_MAC_TRAD_CHINESE, // 2: 중국어 번체 (Big5)
            ENCODING_MAC_KOREAN,       // 3: 한국어 (EUC-KR)
            ENCODING_MAC_ARABIC,       // 4: 아랍어
            ENCODING_MAC_HEBREW,       // 5: 히브리어
            ENCODING_MAC_GREEK,        // 6: 그리스어
            ENCODING_MAC_RUSSIAN,      // 7: 러시아어 (KOI8-R)
            ENCODING_MAC_RSYMBOL,      // 8: RSymbol
            ENCODING_MAC_DEVANAGARI,   // 9: 데바나가리
            ENCODING_MAC_GURMUKHI,     // 10: 구르무키
            ENCODING_MAC_GUJARATI,     // 11: 구자라티
            ENCODING_MAC_ORIYA,        // 12: 오리야
            ENCODING_MAC_BENGALI,      // 13: 벵골어
            ENCODING_MAC_TAMIL,        // 14: 타밀어
            ENCODING_MAC_TELUGU,       // 15: 텔루구
            ENCODING_MAC_KANNADA,      // 16: 칸나다
            ENCODING_MAC_MALAYALAM,    // 17: 말라얄람
            ENCODING_MAC_SINHALESE,    // 18: 싱할라
            ENCODING_MAC_BURMESE,      // 19: 버마어
            ENCODING_MAC_KHMER,        // 20: 크메르어
            ENCODING_MAC_THAI,         // 21: 태국어
            ENCODING_MAC_LAOTIAN,      // 22: 라오스어
            ENCODING_MAC_GEORGIAN,     // 23: 조지아어
            ENCODING_MAC_ARMENIAN,     // 24: 아르메니아어
            ENCODING_MAC_SIMP_CHINESE, // 25: 중국어 간체 (GB2312)
            ENCODING_MAC_TIBETAN,      // 26: 티베트어
            ENCODING_MAC_MONGOLIAN,    // 27: 몽골어
            ENCODING_MAC_ETHIOPIC,     // 28: 에티오피아어
            ENCODING_MAC_GEEZ,         // 29: 게에즈어
            ENCODING_MAC_SLAVIC,       // 30: 슬라브어
            ENCODING_MAC_VIETNAMESE,   // 31: 베트남어
            ENCODING_MAC_SINDHI,       // 32: 신디어
            ENCODING_MAC_UNINTERP      // 33: 미해석
    })
    public @interface ID {}
    public static final int ENCODING_MAC_ROMAN = 0;
    public static final int ENCODING_MAC_JAPANESE = 1;
    public static final int ENCODING_MAC_TRAD_CHINESE = 2;
    public static final int ENCODING_MAC_KOREAN = 3;
    public static final int ENCODING_MAC_ARABIC = 4;
    public static final int ENCODING_MAC_HEBREW = 5;
    public static final int ENCODING_MAC_GREEK = 6;
    public static final int ENCODING_MAC_RUSSIAN = 7;
    public static final int ENCODING_MAC_RSYMBOL = 8;
    public static final int ENCODING_MAC_DEVANAGARI = 9;
    public static final int ENCODING_MAC_GURMUKHI = 10;
    public static final int ENCODING_MAC_GUJARATI = 11;
    public static final int ENCODING_MAC_ORIYA = 12;
    public static final int ENCODING_MAC_BENGALI = 13;
    public static final int ENCODING_MAC_TAMIL = 14;
    public static final int ENCODING_MAC_TELUGU = 15;
    public static final int ENCODING_MAC_KANNADA = 16;
    public static final int ENCODING_MAC_MALAYALAM = 17;
    public static final int ENCODING_MAC_SINHALESE = 18;
    public static final int ENCODING_MAC_BURMESE = 19;
    public static final int ENCODING_MAC_KHMER = 20;
    public static final int ENCODING_MAC_THAI = 21;
    public static final int ENCODING_MAC_LAOTIAN = 22;
    public static final int ENCODING_MAC_GEORGIAN = 23;
    public static final int ENCODING_MAC_ARMENIAN = 24;
    public static final int ENCODING_MAC_SIMP_CHINESE = 25;
    public static final int ENCODING_MAC_TIBETAN = 26;
    public static final int ENCODING_MAC_MONGOLIAN = 27;
    public static final int ENCODING_MAC_ETHIOPIC = 28;
    public static final int ENCODING_MAC_GEEZ = 29;
    public static final int ENCODING_MAC_SLAVIC = 30;
    public static final int ENCODING_MAC_VIETNAMESE = 31;
    public static final int ENCODING_MAC_SINDHI = 32;
    public static final int ENCODING_MAC_UNINTERP = 33;

    public static String getCharset(@ID int encodingId) {
        switch (encodingId) {
            case ENCODING_MAC_ROMAN:
                return "x-MacRoman";
            case ENCODING_MAC_JAPANESE:
                return "x-MacJapanese";
            case ENCODING_MAC_TRAD_CHINESE:
                return "x-MacChineseTrad";
            case ENCODING_MAC_KOREAN:
                return "x-MacKorean";
            case ENCODING_MAC_ARABIC:
                return "x-MacArabic";
            case ENCODING_MAC_HEBREW:
                return "x-MacHebrew";
            case ENCODING_MAC_GREEK:
                return "x-MacGreek";
            case ENCODING_MAC_RUSSIAN:
                return "x-MacCyrillic";
            case ENCODING_MAC_RSYMBOL:
                return "x-MacSymbol";
            case ENCODING_MAC_DEVANAGARI:
                return "x-MacDevanagari";
            case ENCODING_MAC_GURMUKHI:
                return "x-MacGurmukhi";
            case ENCODING_MAC_GUJARATI:
                return "x-MacGujarati";
            case ENCODING_MAC_ORIYA:
                return "x-MacOriya";
            case ENCODING_MAC_BENGALI:
                return "x-MacBengali";
            case ENCODING_MAC_TAMIL:
                return "x-MacTamil";
            case ENCODING_MAC_TELUGU:
                return "x-MacTelugu";
            case ENCODING_MAC_KANNADA:
                return "x-MacKannada";
            case ENCODING_MAC_MALAYALAM:
                return "x-MacMalayalam";
            case ENCODING_MAC_SINHALESE:
                return "x-MacSinhalese";
            case ENCODING_MAC_BURMESE:
                return "x-MacBurmese";
            case ENCODING_MAC_KHMER:
                return "x-MacKhmer";
            case ENCODING_MAC_THAI:
                return "x-MacThai";
            case ENCODING_MAC_LAOTIAN:
                return "x-MacLaotian";
            case ENCODING_MAC_GEORGIAN:
                return "x-MacGeorgian";
            case ENCODING_MAC_ARMENIAN:
                return "x-MacArmenian";
            case ENCODING_MAC_SIMP_CHINESE:
                return "x-MacChineseSimp";
            case ENCODING_MAC_TIBETAN:
                return "x-MacTibetan";
            case ENCODING_MAC_MONGOLIAN:
                return "x-MacMongolian";
            case ENCODING_MAC_ETHIOPIC:
                return "x-MacEthiopic";
            case ENCODING_MAC_GEEZ:
                return "x-MacGeez";
            case ENCODING_MAC_SLAVIC:
                return "x-MacCroatian";  // Mac Slavic는 일반적으로 Croatian 인코딩을 사용
            case ENCODING_MAC_VIETNAMESE:
                return "x-MacVietnamese";
            case ENCODING_MAC_SINDHI:
                return "x-MacSindhi";
            case ENCODING_MAC_UNINTERP:
                return "UTF-8";  // 해석되지 않은 인코딩의 경우 UTF-8을 기본값으로 사용
            default:
                return "UTF-8";  // 알 수 없는 인코딩의 경우 UTF-8을 기본값으로 사용
        }
    }
}
