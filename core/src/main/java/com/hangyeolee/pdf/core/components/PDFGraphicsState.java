package com.hangyeolee.pdf.core.components;

import android.graphics.Color;

import androidx.annotation.ColorInt;

import com.hangyeolee.pdf.core.binary.BinaryConverter;

import java.util.Locale;

/**
 * PDF 그래픽스 상태를 저장하고 복원하는 헬퍼 클래스
 */
class PDFGraphicsState {
    public static void save(StringBuilder content) {
        content.append("q\r\n"); // Save graphics state
    }

    public static void addStrokeColor(StringBuilder content, @ColorInt int color) {
        float red =     Color.red(      color) / 255f;
        float green =   Color.green(    color) / 255f;
        float blue =    Color.blue(     color) / 255f;
        content.append(String.format(Locale.getDefault(),"%s %s %s RG\r\n",
                BinaryConverter.formatNumber(red, 3),
                BinaryConverter.formatNumber(green, 3),
                BinaryConverter.formatNumber(blue, 3))
        );
    }

    public static void addFillColor(StringBuilder content, @ColorInt int color) {
        float red =     Color.red(      color) / 255f;
        float green =   Color.green(    color) / 255f;
        float blue =    Color.blue(     color) / 255f;
        content.append(String.format(Locale.getDefault(),"%s %s %s rg\r\n",
                BinaryConverter.formatNumber(red, 3),
                BinaryConverter.formatNumber(green, 3),
                BinaryConverter.formatNumber(blue, 3))
        );
    }

    public static void restore(StringBuilder content) {
        content.append("Q\r\n"); // Restore graphics state
    }
}
