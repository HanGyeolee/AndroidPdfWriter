package com.hangyeolee.androidpdfwriter.binary;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.Typeface;

import com.hangyeolee.androidpdfwriter.BuildConfig;
import com.hangyeolee.androidpdfwriter.components.PDFComponent;
import com.hangyeolee.androidpdfwriter.components.PDFLayout;
import com.hangyeolee.androidpdfwriter.utils.FontMetrics;
import com.hangyeolee.androidpdfwriter.utils.PDFFontResource;
import com.hangyeolee.androidpdfwriter.utils.PDFImageResource;
import com.hangyeolee.androidpdfwriter.utils.PDFResource;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class BinaryPage {
    private int nextFontId = 1;
    private int nextImageId = 1;

    // 현재 오브젝트 번호 (PDF의 각 요소는 고유한 오브젝트 번호를 가짐)
    int currentNumber = 1;
    // 페이지의 루트 컴포넌트
    private PDFComponent rootComponent;

    // 리소스 딕셔너리에 포함될 폰트 목록
    protected ArrayList<PDFFontResource> fonts;
    protected ArrayList<PDFImageResource> images;

    // 페이지 수
    int pageCount;
    int pageWidth, pageHeight;
    // 페이지의 콘텐츠 영역 (여백을 제외한 실제 내용이 그려지는 영역)
    RectF contentRect;

    /**
     * BinaryPage 생성자
     * @param root 페이지의 루트 컴포넌트
     * @param pageCount 총 페이지 수
     */
    public BinaryPage(PDFComponent root, int pageCount){
        this.rootComponent = root;
        this.pageCount = pageCount;
        this.fonts = new ArrayList<>();
        this.images = new ArrayList<>();
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

        try {
            BufferedOutputStream bufos = new BufferedOutputStream(fos);


            // 1. PDF 헤더 작성
            writePDFHeader(bufos);

            // 2. Info 딕셔너리 작성
            writeInfoDictionary(bufos);

            // 3. Catalog 딕셔너리 작성
            int catalogObjNum = writeCatalogDictionary(bufos);
            // 1 0 obj Catalog

            // 4. Pages 딕셔너리 작성
            int pagesObjNum = writePagesDictionary(bufos);
            // 2 0 obj Pages

            // 모든 리소스 등록
            registerAllResources(bufos);

            // 5. 페이지 컨텐츠 작성
            writePageContents(bufos, pagesObjNum);

            // 6. xref 테이블 작성
            writeXRefTable(bufos);

            // 7. 트레일러 작성
            writeTrailer(bufos, catalogObjNum, pagesObjNum);

            bufos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        fonts.clear();
    }

    /**
     * PDF 헤더 작성
     * PDF 버전 1.4 명시
     */
    private void writePDFHeader(BufferedOutputStream bufos) throws IOException {
        byte[] bytes = "%PDF-1.4\n".getBytes(BinarySingleton.US_ASCII);
        BinarySingleton.getInstance().addXRef(65535, (byte) 'f');
        BinarySingleton.getInstance().byteLength += bytes.length;
        bufos.write(bytes);
    }

    /**
     * Info 딕셔너리 작성
     * 문서의 메타데이터 포함
     */
    private void writeInfoDictionary(BufferedOutputStream bufos) throws IOException {
        byte[] bytes = addObject(addDictionary("<</Producer (Gyeolee/APW v" + BuildConfig.PUBLISH_VERSION + ")>>"));
        BinarySingleton.getInstance().addXRef((byte) 'n');
        BinarySingleton.getInstance().byteLength += bytes.length;
        bufos.write(bytes);
    }

    /**
     * Catalog 딕셔너리 작성
     * 문서의 루트 오브젝트
     * @return Catalog 오브젝트 번호
     */
    private int writeCatalogDictionary(BufferedOutputStream bufos) throws IOException {
        int catalogObjNum = currentNumber + 1;
        String sb = "<< /Type /Catalog\n" +
                "/Pages " + catalogObjNum + " 0 R>>";
        byte[] bytes = addObject(addDictionary(sb));
        BinarySingleton.getInstance().addXRef((byte) 'n');
        BinarySingleton.getInstance().byteLength += bytes.length;
        bufos.write(bytes);
        return catalogObjNum;
    }

    /**
     * Pages 딕셔너리 작성
     * 모든 페이지의 부모 노드
     */
    private int writePagesDictionary(BufferedOutputStream bufos) throws IOException {
        int pagesObjNum = currentNumber + 1;
        String sb = "<< /Type /Pages\n" +
                "/Count " + pageCount + '\n' +
                "/Kids [" + pagesObjNum + " 0 R]\n" +
                ">>";
        byte[] bytes = addObject(addDictionary(sb));
        BinarySingleton.getInstance().addXRef((byte) 'n');
        BinarySingleton.getInstance().byteLength += bytes.length;
        bufos.write(bytes);
        return pagesObjNum;
    }

    /**
     * 페이지 내용 작성
     * 컴포넌트 트리를 순회하며 각 컴포넌트의 내용을 PDF 스트림으로 변환
     */
    private void writePageContents(BufferedOutputStream bufos, int pagesObjNum) throws IOException {
        // 1. 리소스 딕셔너리 생성
        int resourceDictNum = writeResourceDictionary(bufos);

        // 2. 컨텐츠 스트림 생성
        int contentStreamNum = writeContentStream(bufos);

        // 3. 페이지 오브젝트 생성
        writePageObject(bufos, resourceDictNum, contentStreamNum, pagesObjNum);
    }


    /**
     * 리소스 딕셔너리 작성
     * 폰트, 이미지 등의 리소스 정보 포함
     * @return 리소스 딕셔너리 오브젝트 번호
     */
    private int writeResourceDictionary(BufferedOutputStream bufos) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<< /ProcSet [/PDF /Text /ImageB /ImageC /ImageI]\n");

        // 폰트 리소스 추가
        if (!fonts.isEmpty()) {
            sb.append("/Font <<\n");
            for (PDFResource font : fonts) {
                sb.append("/").append(font.getResourceId())
                        .append(" ").append(font.getObjectNumber()).append(" 0 R\n");
            }
            sb.append(">>\n");
        }

        // 이미지 리소스 추가
        if (!images.isEmpty()) {
            sb.append("/XObject <<\n");
            for (PDFResource image : images) {
                sb.append("/").append(image.getResourceId())
                        .append(" ").append(image.getObjectNumber()).append(" 0 R\n");
            }
            sb.append(">>\n");
        }

        sb.append(">>");

        byte[] bytes = addObject(addDictionary(sb.toString()));
        BinarySingleton.getInstance().addXRef((byte) 'n');
        BinarySingleton.getInstance().byteLength += bytes.length;
        bufos.write(bytes);

        return currentNumber - 1;
    }

    /**
     * 컴포넌트 트리의 내용을 PDF 컨텐츠 스트림으로 변환
     * @return 컨텐츠 스트림 오브젝트 번호
     */
    private int writeContentStream(BufferedOutputStream bufos) throws IOException {
        // 컨텐츠 스트림 생성을 위한 StringBuilder
        StringBuilder content = new StringBuilder();

        // 그래픽스 상태 초기화
        content.append("q\n");

        // 컴포넌트 트리 순회하며 PDF 연산자 생성
        processComponent(rootComponent, content);

        // 그래픽스 상태 복원
        content.append("Q\n");

        // 스트림 데이터 생성
        byte[] contentBytes = content.toString().getBytes(BinarySingleton.US_ASCII);

        // 스트림 오브젝트 작성
        byte[] bytes = addObject(addStream("<< /Length " + contentBytes.length + " >>"
                // 스트림 오브젝트 작성
                , contentBytes));
        BinarySingleton.getInstance().addXRef((byte) 'n');
        BinarySingleton.getInstance().byteLength += bytes.length;
        bufos.write(bytes);

        return currentNumber - 1;
    }

    /**
     * 컴포넌트를 PDF 컨텐츠로 변환
     * @param component 처리할 컴포넌트
     * @param content PDF 컨텐츠를 저장할 StringBuilder
     */
    private void processComponent(PDFComponent component, StringBuilder content) {
        // 컴포넌트의 변환 매트릭스 설정
        content.append("q\n"); // 그래픽스 상태 저장

        // 컴포넌트의 위치와 크기에 따른 변환 매트릭스 설정
        float x = component.getMeasureX();
        float y = component.getMeasureY();
        content.append("1 0 0 1 ").append(x).append(" ").append(y).append(" cm\n");

        // 컴포넌트의 PDF 표현 생성
        component.createPDFObject(this, content);

        content.append("Q\n"); // 그래픽스 상태 복원

        // 자식 컴포넌트 처리
        if (component instanceof PDFLayout) {
            PDFLayout layout = (PDFLayout) component;
            for (PDFComponent child : layout.getChild()) {
                processComponent(child, content);
            }
        }
    }

    /**
     * 페이지 오브젝트 작성
     * @param bufos 출력 스트림
     * @param resourceDictNum 리소스 딕셔너리 오브젝트 번호
     * @param contentStreamNum 컨텐츠 스트림 오브젝트 번호
     * @param parentNum 부모 Pages 오브젝트 번호
     */
    private void writePageObject(BufferedOutputStream bufos, int resourceDictNum,
                                 int contentStreamNum, int parentNum) throws IOException {
        String sb = "<<\n/Type /Page\n" +
                "/Parent " + parentNum + " 0 R\n" +
                "/Resources " + resourceDictNum + " 0 R\n" +
                "/Contents " + contentStreamNum + " 0 R\n" +
                "/MediaBox [0 0 " +
                Math.round(contentRect.right + contentRect.left) + " " +
                Math.round(contentRect.bottom + contentRect.top) + "]\n" +
                ">>";

        byte[] bytes = addObject(addDictionary(sb));
        BinarySingleton.getInstance().addXRef((byte) 'n');
        BinarySingleton.getInstance().byteLength += bytes.length;
        bufos.write(bytes);
    }

    private void writeXRefTable(BufferedOutputStream bufos) throws IOException {
        String tmp = "xref\n" +
                "0 " + BinarySingleton.getInstance().getLengthXref() + "\n";
        byte[] tmp_b = tmp.getBytes(BinarySingleton.US_ASCII);
        bufos.write(tmp_b);

        for(XRef b: BinarySingleton.getInstance().XRefs){
            bufos.write(b.write());
        }
    }

    private void writeTrailer(BufferedOutputStream bufos, int catalogObjNum, int pagesObjNum) throws IOException {
        String tmp = "trailer\n" +
                "<< /Size " + BinarySingleton.getInstance().getLengthXref() + "\n" +
                "/Info "+catalogObjNum+" 0 R\n" +
                "/Root "+pagesObjNum+" 0 R\n>>\n" +
                "startxref\n" +
                BinarySingleton.getInstance().byteLength;
        byte[] tmp_b = tmp.getBytes(BinarySingleton.US_ASCII);
        bufos.write(tmp_b);

        bufos.write("\n%%EOF".getBytes(BinarySingleton.US_ASCII));
    }

    private byte[] addDictionary(String s){
        return s.getBytes(BinarySingleton.US_ASCII);
    }
    private byte[] addStream(String dict, byte[] data) {
        // 스트림 헤더
        byte[] header = addDictionary(dict + "\nstream\n");
        // 스트림 끝
        byte[] footer = addDictionary("\nendstream");

        // 전체 스트림 데이터 조합
        byte[] result = new byte[header.length + data.length + footer.length];
        System.arraycopy(header, 0, result, 0, header.length);
        System.arraycopy(data, 0, result, header.length, data.length);
        System.arraycopy(footer, 0, result, header.length + data.length, footer.length);

        return result;
    }

    private byte[] addObject(byte[] data){
        // {객체 번호} {세대 번호} obj
        byte[] header = addDictionary(currentNumber + " 0 obj\n");
        byte[] footer = addDictionary("\nendobj\n");
        byte[] result = new byte[header.length + data.length + footer.length];

        System.arraycopy(header, 0, result, 0, header.length);
        System.arraycopy(data, 0, result, header.length, data.length);
        System.arraycopy(footer, 0, result, header.length + data.length, footer.length);

        currentNumber++;
        return result;
    }

    /**
     * 폰트 리소스 추가
     * @param font 추가할 폰트 리소스
     */
    public void addFont(PDFFontResource font) {
        fonts.add(font);
    }

    /**
     * 이미지 리소스 추가
     * @param image 추가할 폰트 리소스
     */
    public void addImage(PDFImageResource image) {
        images.add(image);
    }

    @Override
    protected void finalize() throws Throwable {

    }

    /**
     * 폰트 등록
     * @param typeface 등록할 폰트
     * @return 리소스 ID
     */
    public String registerFont(Typeface typeface, FontMetrics metrics, BufferedOutputStream bufos) throws IOException {
        int objectNumber = createFontObject(typeface, metrics, bufos);
        PDFFontResource resource = new PDFFontResource(objectNumber, nextFontId++);
        // 폰트 객체 생성 및 번호 할당
        fonts.add(resource);
        return resource.getResourceId();
    }

    /**
     * 이미지 등록
     * @param bitmap 등록할 이미지
     * @return 리소스 ID
     */
    public String registerImage(Bitmap bitmap, BufferedOutputStream bufos) throws IOException {
        // 이미지 객체 생성 및 번호 할당
        int objectNumber = createImageObject(bitmap, bufos);
        PDFImageResource resource = new PDFImageResource(objectNumber, nextImageId++);
        images.add(resource);
        return resource.getResourceId();
    }

    /**
     * 컴포넌트 트리의 모든 리소스를 등록
     */
    private void registerAllResources(BufferedOutputStream bufos) {
        processComponentResources(rootComponent, bufos);
    }

    private void processComponentResources(PDFComponent component, BufferedOutputStream bufos) {
        // 현재 컴포넌트의 리소스 등록
        component.registerResources(this, bufos);

        // 자식 컴포넌트들의 리소스 등록
        if (component instanceof PDFLayout) {
            PDFLayout layout = (PDFLayout) component;
            for (PDFComponent child : layout.getChild()) {
                processComponentResources(child, bufos);
            }
        }
    }

    /**
     * 이미지 객체 생성
     * @param bitmap 변환할 비트맵
     * @return 생성된 PDF 객체 번호
     */
    private int createImageObject(Bitmap bitmap, BufferedOutputStream bufos) throws IOException {
        // JPEG 형식으로 이미지 압축
        ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,
                BinarySingleton.getInstance().quality, imageStream);
        byte[] imageData = imageStream.toByteArray();

        // 이미지 객체 딕셔너리 생성
        String sb = "<< /Type /XObject\n" +
                "/Subtype /Image\n" +
                "/Width " + bitmap.getWidth() + "\n" +
                "/Height " + bitmap.getHeight() + "\n" +
                "/ColorSpace /DeviceRGB\n" +
                "/BitsPerComponent 8\n" +
                "/Filter /DCTDecode\n" +
                "/Length " + imageData.length + ">>\n";

        // 이미지 스트림 작성
        byte[] bytes = addObject(addStream(sb, imageData));
        BinarySingleton.getInstance().addXRef((byte) 'n');
        BinarySingleton.getInstance().byteLength += bytes.length;
        bufos.write(bytes);

        return currentNumber - 1;
    }

    /**
     * TrueType 폰트 객체 생성
     * @param typeface Android Typeface 객체
     * @param bufos 출력 스트림
     * @return 생성된 폰트 객체의 번호
     */
    private int createFontObject(Typeface typeface, FontMetrics metrics, BufferedOutputStream bufos) throws IOException {
        // 1. 폰트 디스크립터 객체 생성
        int fontDescriptorNum = createFontDescriptor(typeface, metrics, bufos);

        // 2. 폰트 객체 생성
        StringBuilder sb = new StringBuilder();
        sb.append("<<\n")
                .append("/Type /Font\n")
                .append("/Subtype /TrueType\n")
                .append("/BaseFont /").append(getFontName(typeface)).append("\n")
                .append("/FirstChar 32\n")  // 공백문자부터 시작
                .append("/LastChar 255\n")
                .append("/Encoding /WinAnsiEncoding\n")  // 기본 인코딩 사용
                .append("/FontDescriptor ").append(fontDescriptorNum).append(" 0 R\n")
                .append("/Widths [");

        // 문자 폭 배열 추가 (32~255)
        for (int i = 32; i <= 255; i++) {
            sb.append(getCharWidth(typeface, (char)i)).append(" ");
        }
        sb.append("]\n>>");

        byte[] bytes = addObject(addDictionary(sb.toString()));
        BinarySingleton.getInstance().addXRef((byte) 'n');
        BinarySingleton.getInstance().byteLength += bytes.length;
        bufos.write(bytes);

        return currentNumber - 1;
    }

    /**
     * 폰트 디스크립터 객체 생성
     */
    private int createFontDescriptor(Typeface typeface, FontMetrics metrics,
                                     BufferedOutputStream bufos) throws IOException {
        String sb = "<<\n" +
                "/Type /FontDescriptor\n" +
                "/FontName /" + getFontName(typeface) + "\n" +
                "/Flags " + getFontFlags(typeface) + "\n" +
                "/FontBBox [" +
                Math.round(metrics.fontBBox.left) + " " +
                Math.round(metrics.fontBBox.bottom) + " " +
                Math.round(metrics.fontBBox.right) + " " +
                Math.round(metrics.fontBBox.top) + "]\n" +
                "/ItalicAngle " + getItalicAngle(typeface) + "\n" +
                "/Ascent " + Math.round(metrics.ascent) + "\n" +
                "/Descent " + Math.round(metrics.descent) + "\n" +
                "/CapHeight " + Math.round(metrics.capHeight) + "\n" +
                "/StemV " + Math.round(metrics.stemV) + "\n" +
                ">>";

        byte[] bytes = addObject(addDictionary(sb));
        BinarySingleton.getInstance().addXRef((byte) 'n');
        BinarySingleton.getInstance().byteLength += bytes.length;
        bufos.write(bytes);

        return currentNumber - 1;
    }

    /**
     * Typeface로부터 PDF 폰트 이름 생성
     */
    private String getFontName(Typeface typeface) {
        // 커스텀 폰트의 경우 고유한 이름 생성
        return "Font-" + typeface.hashCode();
    }

    /**
     * 폰트 플래그 계산
     */
    private int getFontFlags(Typeface typeface) {
        int flags = 0;

        // Symbolic 폰트 플래그
        flags |= 1 << 2;

        // 기타 플래그들
        if (typeface.isBold()) {
            flags |= 1 << 18; // ForceBold
        }
        if (typeface.isItalic()) {
            flags |= 1 << 6; // Italic
        }

        return flags;
    }

    /**
     * 이탤릭 각도 계산
     */
    private int getItalicAngle(Typeface typeface) {
        return typeface.isItalic() ? -12 : 0;  // 일반적인 이탤릭 각도
    }

    /**
     * 문자의 폭 계산
     * 실제 구현에서는 Paint를 사용하여 측정해야 함
     */
    private int getCharWidth(Typeface typeface, char c) {
        Paint paint = new Paint();
        paint.setTypeface(typeface);
        paint.setTextSize(1000);  // PDF 폰트 단위계에 맞춤
        return Math.round(paint.measureText(String.valueOf(c)));
    }
}
