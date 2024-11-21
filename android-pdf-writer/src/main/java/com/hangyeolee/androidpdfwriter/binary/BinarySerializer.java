package com.hangyeolee.androidpdfwriter.binary;

import android.graphics.Bitmap;
import android.graphics.RectF;

import androidx.annotation.NonNull;

import com.hangyeolee.androidpdfwriter.components.PDFLayout;
import com.hangyeolee.androidpdfwriter.font.FontExtractor;
import com.hangyeolee.androidpdfwriter.font.FontMetrics;
import com.hangyeolee.androidpdfwriter.utils.Zoomable;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class BinarySerializer {
    private static final String HEADER = "%PDF-1.4\r\n"
            + "%" + (char)0xE2 + (char)0xE3 + (char)0xCF + (char)0xD3 +"\r\n";
    private static final String TO_UNICODE_CMAP_TEMPLATE = """
                    /CIDInit /ProcSet findresource begin\r
                    12 dict begin\r
                    begincmap\r
                    /CIDSystemInfo <</Registry (Adobe)/Ordering (UCS)/Supplement 0>> def\r
                    /CMapName /Adobe-Identity-UCS def\r
                    /CMapType 2 def\r
                    1 begincodespacerange <0000> <FFFF> endcodespacerange\r
                    %s
                    endcmap\r
                    CMapName currentdict /CMap defineresource pop\r
                    end end
                    """;
    /*
                    1 beginbfrange
                    <0000> <FFFF> <0000>
                    endbfrange

    * */

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
        // 1. Catalog 작성 1 0 obj
        BinaryCatalog catalog = manager.createObject(BinaryCatalog::new);
        // 2. Pages 작성
        pages = manager.createObject(BinaryPages::new);
        catalog.setPages(pages);
        // 3. Info 작성
        manager.createObject(BinaryInfo::new);

        rootComponent.draw(this);

        pages.finalizeContents(manager, resources);
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
        byte[] bytes = BinaryConverter.toBytes(HEADER);
        manager.addXRef(65535, (byte) 'f');
        manager.byteLength += bytes.length;
        bufos.write(bytes);
    }

    /**
     * 폰트를 PDF 리소스로 등록
     */
    public String registerFont(@NonNull FontExtractor.FontInfo info, FontMetrics metrics) {
        if(resources == null){
            // 페이지 리소스 생성
            resources = manager.createObject(BinaryResources::new);
        }

        // 이미 등록된 폰트인지 확인
        String fontId = fontResourceMap.get(info);
        if (fontId != null) {
            return fontId;
        }

        // 새로운 폰트 ID 생성
        fontId = "F" + nextFontNumber++;
        fontResourceMap.put(info, fontId);

        BinaryFont font;
        BinaryFontDescriptor fontDesc;
        if(BinaryConverter.isBase14Font(info.postScriptName)){
            // Font 객체 생성
            font = manager.createObject(n -> new BinaryFont(n, null, null));
            font.setBaseFont(info.postScriptName);
            font.setSubtype("Type1");

            font.setWidths(BinaryConverter.formatArray(metrics.charWidths));
            font.setBase14Font();
            // FontDescriptor 객체 생성
            fontDesc = manager.createObject(BinaryFontDescriptor::new);
            fontDesc.setFontName(info.postScriptName);
            font.setFontDescriptor(fontDesc);
        }else {
            if(cmap == null){
                cmap = createToUnicode(info.W.keySet());
            }
            String CIDName = "CIDFont+"+fontId;
            // Font 객체 생성
            font = manager.createObject(n -> new BinaryFont(n, "Identity-H", cmap));
            font.setBaseFont(CIDName);//info.postScriptName); // +"+fontId"
            font.setSubtype("Type0");
//            font.setWidths(metrics.charWidths);

            BinaryFont cidFont = manager.createObject(n -> new BinaryFont(n, null, null));
            cidFont.setSubtype("CIDFontType2");
            cidFont.setBaseFont(CIDName);//info.postScriptName); // +"+fontId"
            cidFont.dictionary.put("/CIDSystemInfo", "<</Registry (Adobe)/Ordering (Identity)/Supplement 0>>");
//            cidFont.dictionary.put("/CMapName", "/Identity-H");
            cidFont.dictionary.put("/CIDToGIDMap", "/Identity");
            cidFont.setW(info.W);
            font.addDescendantFont(cidFont);

            // FontDescriptor 객체 생성
            fontDesc = manager.createObject(BinaryFontDescriptor::new);
            fontDesc.setFontName(CIDName);//info.postScriptName); // +"+fontId"
            cidFont.setFontDescriptor(fontDesc);

            // Font Subsetting
//            byte[] subsetFont = FontSubSetter.fontSubsetting(info.stream, info.W.keySet());

            // Font 파일 생성
            BinaryContentStream fontfile2 = manager.createObject(n ->
                    new BinaryContentStream(n, info.stream, true));
            fontfile2.setSubtype("TrueType"); // Type1C를 TrueType으로 변경
            fontDesc.setFontFile2(fontfile2);
        }
        fontDesc.setMetrics(metrics);

        // Resources에 폰트 추가
        resources.addFont(fontId, font);

        return fontId;
    }

    /**
     * 이미지를 PDF 리소스로 등록
     */
    public String registerImage(android.graphics.Bitmap bitmap) {
        if(resources == null){
            // 페이지 리소스 생성
            resources = manager.createObject(BinaryResources::new);
        }

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

    private BinaryObject createToUnicode(Set<Character> fontBytes) {
        StringBuilder sb = new StringBuilder();
        sb.append(fontBytes.size()).append(" beginbfchar ");
        for(Character c : fontBytes) {
            sb.append(String.format("<%04X> ", c & 0xFFFF));
        }
        sb.append("endbfchar\r");
        final String bfchar = sb.toString();
        // ToUnicode CMap을 스트림 객체로 생성
        return manager.createObject(n -> new BinaryContentStream(n,
                String.format(Locale.getDefault() ,TO_UNICODE_CMAP_TEMPLATE, bfchar)));
    }

    public void setQuality(int quality){
        if(quality < 0) quality = 0;
        else if (quality > 100) quality = 100;
        this.quality = quality;
    }
}
