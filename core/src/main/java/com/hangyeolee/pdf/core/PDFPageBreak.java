package com.hangyeolee.pdf.core;


import com.hangyeolee.androidpdfwriter.binary.BinarySerializer;
import com.hangyeolee.androidpdfwriter.utils.Zoomable;

/**
 * 강제 페이지 나누기를 위한 컴포넌트.<br>
 * 이 컴포넌트 이후의 모든 내용은 새로운 페이지에서 시작됩니다.<br>
 * Components for forced page splitting.<br>
 * Everything after this component begins on a new page.<br>
 */
public class PDFPageBreak extends PDFComponent {
    public PDFPageBreak() {super();}

    @Override
    public void measure(float x, float y) {
        super.measure(x, y);

        // 현재 페이지의 남은 공간을 모두 차지하도록 높이 조정
        float pageHeight = Zoomable.getInstance().getContentHeight();
        int currentPage = calculatePageIndex(measureY);
        float nextPageY = (currentPage + 1) * pageHeight;

        // 다음 페이지 시작점까지의 거리를 높이로 설정
        float remainingHeight = nextPageY - measureY;
        height = remainingHeight;
        float _height = getTotalHeight();
        // 부모 컴포넌트의 높이도 업데이트
        while (remainingHeight > _height) {
            updateHeight(remainingHeight - _height);
            _height = getTotalHeight();
        }
    }

    @Override
    public void draw(BinarySerializer serializer) {
        // draw nothing
    }

    /**
     * {@link PDFPageBreak}를 생성합니다.
     * @return 새로운 PDFPageBreak 인스턴스
     */
    public static PDFPageBreak build() { return new PDFPageBreak(); }
}
