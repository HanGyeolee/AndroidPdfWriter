package com.hangyeolee.androidpdfwriter.binary;

import android.graphics.Bitmap;
import android.graphics.RectF;

import androidx.annotation.NonNull;

import com.hangyeolee.androidpdfwriter.components.PDFLayout;
import com.hangyeolee.androidpdfwriter.font.FontExtractor;
import com.hangyeolee.androidpdfwriter.font.FontMetrics;
import com.hangyeolee.androidpdfwriter.utils.Zoomable;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class BinarySerializer {
    private static final String HEADER = "%PDF-1.4\r\n" +
            "%" + (char)0xE2 + (char)0xE3 + (char)0xCF + (char)0xD3 +"\r\n";
    private static final String TO_UNICODE_CMAP_TEMPLATE = """
                    /CIDInit /ProcSet findresource begin\r
                    12 dict begin\r
                    begincmap\r
                    /CIDSystemInfo\r
                    <</Registry (Adobe)\r
                    /Ordering (Identity)\r
                    /Supplement 0\r
                    >> def\r
                    /CMapName /Adobe-Identity-UCS def\r
                    /CMapType 2 def\r
                    1 begincodespacerange\r
                    <0000> <FFFF>\r
                    endcodespacerange\r
                    endcmap\r
                    CMapName currentdict /CMap defineresource pop\r
                    end\r
                    end\r
                    """;

    private final BinaryObjectManager manager;
    private final Map<Bitmap, String> imageResourceMap = new HashMap<>();
    private final Map<FontExtractor.FontInfo, String> fontResourceMap = new HashMap<>();
    private int nextImageNumber = 1;
    private int nextFontNumber = 1;

    private int quality;
    private final RectF mediaBox;

    // 페이지의 루트 컴포넌트
    private final PDFLayout rootComponent;

    private BinaryPages pages;
    private BinaryPage currentPage;
    private BinaryResources resources;
    private BinaryObject cmap = null;

    /**
     * BinaryPage 생성자
     * @param root 페이지의 루트 컴포넌트
     */
    public BinarySerializer(PDFLayout root){
        this.rootComponent = root;
        mediaBox = new RectF(
                0, Zoomable.getInstance().getPageRect().bottom,
                Zoomable.getInstance().getPageRect().right, 0);
        this.manager = new BinaryObjectManager();
    }

    public void draw(){
        // 2. Info 딕셔너리 작성
        manager.createObject(BinaryInfo::new);

        // 객체 생성
        // 3. Pages 딕셔너리 작성
        pages = manager.createObject(BinaryPages::new);
        // 1 0 obj Pages
        // 4. Catalog 딕셔너리 작성
        manager.createObject(n -> new BinaryCatalog(n, pages));
        // 2 0 obj Catalog

        // 페이지 리소스 생성
        resources = manager.createObject(BinaryResources::new);
        rootComponent.draw(this);

        pages.finalizeContents(manager);
    }

    public void save(OutputStream os){
        try {
            BufferedOutputStream bufos = new BufferedOutputStream(os);

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
        // 페이지 생성 및 리소스 설정
        currentPage = manager.createObject(BinaryPage::new);
        currentPage.setResources(resources);
        currentPage.setMediaBox(mediaBox);
        pages.addPage(currentPage);

        return currentPage.getContents();
    }

    // 현재 컴포넌트가 몇 번째 페이지에 그려져야 하는지 계산
    public int calculatePageIndex(float measureY, float componentHeight) {
        if (measureY < 0) return 0;
        float pageHeight = Zoomable.getInstance().getContentHeight();
        return (int) Math.floor((measureY + componentHeight) / pageHeight);
    }

    // 현재 컴포넌트가 몇 번째 페이지에 그려져야 하는지 계산
    public int calculatePageIndex(float measureY) {
        float pageHeight = Zoomable.getInstance().getContentHeight();
        return (int) Math.floor((measureY) / pageHeight);
    }

    public StringBuilder getPage(int index){
        if(index < pages.getPageSize()) {
            // 생성된 페이지로 전환
            currentPage = pages.getPage(index);
            return currentPage.getContents();
        } else {
            // 만약 생성된 페이지의 인덱스를 넘어가는 경우,
            return newPage();
        }
    }

    /**
     * PDF 헤더 작성
     * PDF 버전 1.4 명시
     */
    private void writePDFHeader(OutputStream bufos) throws IOException {
        byte[] bytes = HEADER.getBytes(BinaryObjectManager.US_ASCII);
        manager.addXRef(65535, (byte) 'f');
        manager.byteLength += bytes.length;
        bufos.write(bytes);
    }

    /**
     * 폰트를 PDF 리소스로 등록
     */
    public String registerFont(@NonNull FontExtractor.FontInfo info, FontMetrics metrics) {
        // 이미 등록된 폰트인지 확인
        String fontId = fontResourceMap.get(info);
        if (fontId != null) {
            return fontId;
        }

        // 새로운 폰트 ID 생성
        fontId = "F" + nextFontNumber++;
        fontResourceMap.put(info, fontId);

        BinaryFont font;
        // FontDescriptor 객체 생성
        BinaryFontDescriptor fontDesc = manager.createObject(BinaryFontDescriptor::new);
        fontDesc.setMetrics(metrics);
        fontDesc.setFontName(info.postScriptName);
        if(isBase14Font(info.postScriptName)){
            // Font 객체 생성
            font = manager.createObject(n -> new BinaryFont(n, fontDesc,  null, null));
            font.setSubtype("Type1");
        }else {
            if(cmap == null){
                cmap = createToUnicode();
            }
            BinaryContentStream fontfile3 = manager.createObject(n ->
                    new BinaryContentStream(n, true, info.stream));
            fontfile3.setSubtype("Type1C");

            fontDesc.setFontFile3(fontfile3);

            // Font 객체 생성
            font = manager.createObject(n -> new BinaryFont(n, fontDesc, info.encoding, cmap));
            font.setSubtype("TrueType");
            font.setWidths(metrics.charWidths);
        }
        font.setBaseFont(info.postScriptName);

        // Resources에 폰트 추가
        resources.addFont(fontId, font);

        return fontId;
    }

    private boolean isBase14Font(String name) {
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

    /**
     * 이미지를 PDF 리소스로 등록
     */
    public String registerImage(android.graphics.Bitmap bitmap) {
        // 이미 등록된 이미지인지 확인
        String imageId = imageResourceMap.get(bitmap);
        if (imageId != null) {
            return imageId;
        }

        imageId = "Im" + nextImageNumber++;
        imageResourceMap.put(bitmap, imageId);

        // 이미지 객체 생성
        BinaryImage image = manager.createObject(n -> new BinaryImage(n, bitmap, quality));

        // Resources에 이미지 추가
        resources.addXObject(imageId, image);

        return imageId;
    }

    private BinaryObject createToUnicode() {
        // ToUnicode CMap을 스트림 객체로 생성 TODO 압축
        return manager.createObject(n -> new BinaryContentStream(n, false, TO_UNICODE_CMAP_TEMPLATE));
    }

    public void setQuality(int quality){
        if(quality < 0) quality = 0;
        else if (quality > 100) quality = 100;
        this.quality = quality;
    }
}
