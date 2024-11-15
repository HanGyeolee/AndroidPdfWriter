package com.hangyeolee.androidpdfwriter.pdf;

/**
 * PDF 그래픽스 상태를 저장하고 복원하는 헬퍼 클래스
 */
public class PDFTextsState {
    public static void save(StringBuilder content) {
        content.append("BT\n"); // Save graphics state
    }

    public static void restore(StringBuilder content) {
        content.append("ET\n"); // Restore graphics state
    }
}
