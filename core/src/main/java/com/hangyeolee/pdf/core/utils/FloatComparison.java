package com.hangyeolee.pdf.core.utils;

public class FloatComparison {
    private static final float EPSILON = 1E-4f; // 적절한 epsilon 값 설정 float 값 100~1000 사이에 적합

    // 두 float이 실질적으로 같은지 비교
    public static boolean isEqual(float a, float b) {
        return Math.abs(a - b) < EPSILON;
    }

    // a가 b보다 크거나 같은지 비교
    public static boolean isGreater(float a, float b) {
        return a > b && !isEqual(a, b);
    }

    // a가 b보다 크거나 같은지 비교
    public static boolean isGreaterOrEqual(float a, float b) {
        return a > b || isEqual(a, b);
    }

    // a가 b보다 크거나 같은지 비교
    public static boolean isLess(float a, float b) {
        return a < b && !isEqual(a, b);
    }

    // a가 b보다 작거나 같은지 비교
    public static boolean isLessOrEqual(float a, float b) {
        return a < b || isEqual(a, b);
    }
}
