package com.hangyeolee.androidpdfwriter.binary;

import com.hangyeolee.androidpdfwriter.PDFBuilder;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * PDF 콘텐츠 스트림을 위한 객체
 */
class BinaryContentStream extends BinaryObject {
    private final byte[] compressedContent;

    public BinaryContentStream(int objectNumber, String content) {
        super(objectNumber);

        if(PDFBuilder.DEBUG) {
            this.compressedContent = BinaryConverter.toBytes(content);
            dictionary.put("/Length", compressedContent.length);
        } else {
            // 컨텐츠 압축
            this.compressedContent = compressContent(content);

            // 딕셔너리에 압축 관련 항목 추가
            dictionary.put("/Filter", "/FlateDecode");
            dictionary.put("/Length", compressedContent.length);
        }
    }
    public BinaryContentStream(int objectNumber, byte[] stream) {
        super(objectNumber);

        if(PDFBuilder.DEBUG) {
            this.compressedContent = stream;
            dictionary.put("/Length", compressedContent.length);
        } else {
            // 컨텐츠 압축
            dictionary.put("/Length1", stream.length);
            this.compressedContent = compressContent(stream);

            // 딕셔너리에 압축 관련 항목 추가
            dictionary.put("/Filter", "/FlateDecode");
            dictionary.put("/Length", compressedContent.length);
        }
    }

    /**
     * 문자열 컨텐츠를 FlateDecode로 압축
     */
    private byte[] compressContent(String content) {
        try {
            byte[] input = BinaryConverter.toBytes(content);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(input.length);

            // DeflaterOutputStream 사용 - zlib 형식 준수
            Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
            DeflaterOutputStream zlibStream = new DeflaterOutputStream(outputStream, deflater);

            // 데이터 쓰기
            zlibStream.write(input);
            zlibStream.finish();
            zlibStream.close();

            // 리소스 정리
            deflater.end();

            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            // 압축 실패시 원본 반환
            return BinaryConverter.toBytes(content);
        }
    }
    private byte[] compressContent(byte[] content) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(content.length);

            // DeflaterOutputStream 사용 - zlib 형식 준수
            Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
            DeflaterOutputStream zlibStream = new DeflaterOutputStream(outputStream, deflater);

            // 데이터 쓰기
            zlibStream.write(content);
            zlibStream.finish();
            zlibStream.close();

            // 리소스 정리
            deflater.end();

            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            // 압축 실패시 원본 반환
            return content;
        }
    }

    @Override
    public byte[] getStreamData() {
        return compressedContent;
    }
}