package com.hangyeolee.androidpdfwriter.binary;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * PDF 객체를 표현하는 클래스
 */
class BinaryObject {
    protected static final String FLATE_DECODE = "FlateDecode";
    protected static final String DCT_DECODE = "DCTDecode";
    private final int objectNumber;

    public BinaryObject(int objectNumber) {
        this.objectNumber = objectNumber;
    }

    /**
     * 문자열 컨텐츠를 FlateDecode로 압축
     */
    protected byte[] compressContent(byte[] content) {
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

    /**
     * 스트림이 있는 경우 오버라이드
     */
    public byte[] getStreamData() { return null; }

    public int getObjectNumber() { return objectNumber; }
}
