package com.hangyeolee.androidpdfwriter.pdf;

import android.graphics.RectF;
import android.graphics.Typeface;

import com.hangyeolee.androidpdfwriter.components.PDFLayout;
import com.hangyeolee.androidpdfwriter.utils.FontMetrics;
import com.hangyeolee.androidpdfwriter.utils.Zoomable;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class BinarySerializer {
    private final BinaryObjectManager manager;
    // 페이지의 루트 컴포넌트
    private PDFLayout rootComponent;
    private final Map<Typeface, String> fontResourceMap = new HashMap<>();
    private int nextFontNumber = 1;
    private int nextImageNumber = 1;

    // 페이지 수
    int pageCount;
    int pageWidth, pageHeight;
    // 페이지의 콘텐츠 영역 (여백을 제외한 실제 내용이 그려지는 영역)
    RectF contentRect;

    private float currentY = 0; // 현재 그리기 Y 위치
    private BinaryPages pages;
    private BinaryPage currentPage;
    private BinaryResources resources;
    private StringBuilder contentStream;

    /**
     * BinaryPage 생성자
     * @param root 페이지의 루트 컴포넌트
     * @param pageCount 총 페이지 수
     */
    public BinarySerializer(PDFLayout root, int pageCount){
        this.rootComponent = root;
        this.pageCount = pageCount;
        this.manager = new BinaryObjectManager();
    }

    /**
     * 페이지의 콘텐츠 영역 설정
     * @param contentRect 콘텐츠 영역 정보
     */
    public void setContentRect(RectF contentRect){
        this.contentRect = contentRect;
        this.pageWidth = Math.round(contentRect.right+contentRect.left);
        this.pageHeight = Math.round(contentRect.bottom+contentRect.top);
    }

    public void saveTo(OutputStream fos){
        StringBuilder tmp;
        byte[] tmp_b;

        // 2. Info 딕셔너리 작성
        manager.createObject(BinaryInfo::new);

        // 객체 생성
        // 3. Pages 딕셔너리 작성
        pages = manager.createObject(BinaryPages::new);
        // 1 0 obj Pages
        // 4. Catalog 딕셔너리 작성
        manager.createObject(n -> new BinaryCatalog(n, pages));
        // 2 0 obj Catalog

        rootComponent.draw(this, newPage());

        try {
            BufferedOutputStream bufos = new BufferedOutputStream(fos);

            // 1. PDF 헤더 작성
            writePDFHeader(bufos);
            manager.writeAllObjects(bufos);

            manager.writeXref(bufos);
            manager.wrtieTrailer(bufos);

            bufos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public StringBuilder newPage(){
        // 현재 페이지의 컨텐츠 스트림 저장
        if (currentPage != null) {
            // 9. 콘텐츠 스트림 객체 생성
            BinaryObject contents = manager.createObject(n ->
                    new BinaryContentStream(n, contentStream.toString()));
            currentPage.setContents(contents);
        }

        // 페이지 리소스 생성
        resources = manager.createObject(BinaryResources::new);
        // 페이지 생성 및 리소스 설정
        currentPage = manager.createObject(BinaryPage::new);
        currentPage.setResources(resources);
        pages.addPage(currentPage);
        // 새로운 컨텐츠 스트림 생성
        contentStream = new StringBuilder();

        // Y 위치 초기화
        currentY = 0;

        return contentStream;
    }

    public boolean shouldCreateNewPage(float componentHeight) {
        return (currentY + componentHeight) > getPageHeight();
    }

    public void updateYPosition(float height) {
        currentY += height;
    }

    public float getCurrentY() {
        return currentY;
    }

    /**
     * PDF 헤더 작성
     * PDF 버전 1.4 명시
     */
    private void writePDFHeader(BufferedOutputStream bufos) throws IOException {
        byte[] bytes = "%PDF-1.4\n".getBytes(BinaryObjectManager.US_ASCII);
        manager.addXRef(65535, (byte) 'f');
        manager.byteLength += bytes.length;
        bufos.write(bytes);
    }

    /**
     * 폰트를 PDF 리소스로 등록
     */
    public String registerFont(Typeface typeface, FontMetrics metrics) {
        // 이미 등록된 폰트인지 확인
        String fontId = fontResourceMap.get(typeface);
        if (fontId != null) {
            return fontId;
        }

        // 새로운 폰트 ID 생성
        fontId = "F" + nextFontNumber++;
        fontResourceMap.put(typeface, fontId);

        // FontDescriptor 객체 생성
        BinaryFontDescriptor fontDesc = manager.createObject(BinaryFontDescriptor::new);
        fontDesc.setMetrics(metrics);

        // Font 객체 생성
        BinaryFont font = manager.createObject(n -> new BinaryFont(n, fontDesc));
        font.setBaseFont(typeface.toString());
        font.setWidths(metrics.charWidths);

        // Resources에 폰트 추가
        resources.addFont(fontId, font);

        return fontId;
    }

    /**
     * 이미지를 PDF 리소스로 등록
     */
    public String registerImage(android.graphics.Bitmap bitmap) {
        String imageId = "Im" + nextImageNumber++;

        // 이미지 객체 생성
        BinaryImage image = manager.createObject(n -> new BinaryImage(n, bitmap, 85));

        // Resources에 이미지 추가
        resources.addXObject(imageId, image);

        return imageId;
    }

    public int getPageHeight(){
        return Math.round(Zoomable.getInstance().getContentRect().height());
    }
}
