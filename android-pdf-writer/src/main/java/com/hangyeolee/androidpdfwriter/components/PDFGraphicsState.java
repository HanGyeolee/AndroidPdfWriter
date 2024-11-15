package com.hangyeolee.androidpdfwriter.components;

/**
 * PDF 그래픽스 상태를 저장하고 복원하는 헬퍼 클래스
 */
class PDFGraphicsState {
    public static void save(StringBuilder content) {
        content.append("q\n"); // Save graphics state
    }

    public static void restore(StringBuilder content) {
        content.append("Q\n"); // Restore graphics state
    }
}
