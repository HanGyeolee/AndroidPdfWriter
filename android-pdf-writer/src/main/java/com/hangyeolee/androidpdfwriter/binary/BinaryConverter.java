package com.hangyeolee.androidpdfwriter.binary;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class BinaryConverter {

    public static byte[] toBytes(String input) {
        byte[] bytes = new byte[input.length()];
        for (int i = 0; i < input.length(); i++) {
            bytes[i] = (byte)(input.charAt(i) & 0xFF);
        }
        return bytes;
    }

    public static String formatArray(int[] array) {
        if (array == null || array.length == 0) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int value : array) {
            sb.append(formatNumber(value)).append(" ");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 단일 숫자에 대한 포맷팅
     * - 0인 경우: "0"
     * - 0이 아닌 경우: 소수점 아래 유효숫자만 표시
     */
    public static String formatNumber(float number) {
        return formatNumber(number, 6);
    }

    /**
     * 단일 숫자에 대한 포맷팅
     * - 0인 경우: "0"
     * - 0이 아닌 경우: 소수점 아래 유효숫자만 표시
     */
    public static String formatNumber(float number, int digit) {
        if (number == 0f) {
            return "0";
        }

        // 숫자를 문자열로 변환 (지수 표기법 없이)
        String format = "%."+digit+"f";
        String str = String.format(Locale.getDefault(), format, number);

        // 후행 0 제거
        str = str.replaceAll("0*$", "");

        // 소수점이 마지막 문자인 경우 제거
        if (str.endsWith(".")) {
            str = str.substring(0, str.length() - 1);
        }

        return str;
    }

    public static boolean isBase14Font(String name) {
        return name.equals("Times-Roman") ||
                name.equals("Times-Bold") ||
                name.equals("Times-Italic") ||
                name.equals("Times-BoldItalic") ||
                name.equals("Helvetica") ||
                name.equals("Helvetica-Bold") ||
                name.equals("Helvetica-Oblique") ||
                name.equals("Helvetica-BoldOblique") ||
                name.equals("Courier") ||
                name.equals("Courier-Bold") ||
                name.equals("Courier-Oblique") ||
                name.equals("Courier-BoldOblique") ||
                name.equals("Symbol") ||
                name.equals("ZapfDingbats");
    }
}
