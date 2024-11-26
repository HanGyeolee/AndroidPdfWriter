package com.hangyeolee.pdf.core.font;

public class TTFSystem {

    // 인코딩 지원 확인 메서드
    public static boolean isUnicodeEncoding(int platformId, int encodingId) {
        if (platformId == TTFPlatform.PLATFORM_UNICODE) {
            return true;
        }
        if (platformId == TTFPlatform.PLATFORM_WINDOWS) {
            return encodingId == EncodingWindow.ENCODING_WINDOWS_UNICODE_BMP ||
                    encodingId == EncodingWindow.ENCODING_WINDOWS_UNICODE_FULL;
        }
        return false;
    }

    // 플랫폼별 적절한 문자셋 반환
    public static String getCharset(int platformId, int encodingId) {
        if (isUnicodeEncoding(platformId, encodingId)) {
            return "UTF-16BE";
        }

        if (platformId == TTFPlatform.PLATFORM_WINDOWS){
            return EncodingWindow.getCharset(encodingId);
        }

        if (platformId == TTFPlatform.PLATFORM_MAC) {
            return EncodingMac.getCharset(encodingId);
        }

        return "UTF-8";  // 기본값
    }
}