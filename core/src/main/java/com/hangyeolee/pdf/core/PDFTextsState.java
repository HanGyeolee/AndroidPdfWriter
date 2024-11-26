package com.hangyeolee.pdf.core;

/**
 * PDF 그래픽스 상태를 저장하고 복원하는 헬퍼 클래스
 */
class PDFTextsState {
    public static void save(StringBuilder content) {
        content.append("BT\r\n"); // Save graphics state
    }

    public static void restore(StringBuilder content) {
        content.append("ET\r\n"); // Restore graphics state
    }
}
