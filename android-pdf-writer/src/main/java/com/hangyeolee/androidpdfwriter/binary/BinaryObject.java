package com.hangyeolee.androidpdfwriter.binary;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    private static final String TAG = "BinaryObject";
    protected static final String FLATE_DECODE = "FlateDecode";
    protected static final String DCT_DECODE = "DCTDecode";
    protected static final int CHUNK_SIZE = 524288; // 0.5MB
    private final int objectNumber;

    public BinaryObject(int objectNumber) {
        this.objectNumber = objectNumber;
    }

    /**
     * 문자열 컨텐츠를 FlateDecode로 압축
     */
    protected byte[] compressContent(byte[] content) throws OutOfMemoryError, IOException {
        if(content.length < CHUNK_SIZE) {
            return compressContentDirectly(content);
        }
        return compressContentInChunks(content);
    }

    private byte[] compressContentDirectly(byte[] content) throws OutOfMemoryError, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(content.length);

        // DeflaterOutputStream 사용 - zlib 형식 준수
        Deflater deflater = new Deflater(5);
        DeflaterOutputStream zlibStream = new DeflaterOutputStream(outputStream, deflater);

        // 데이터 쓰기
        zlibStream.write(content);
        zlibStream.finish();
        zlibStream.close();

        // 리소스 정리
        deflater.end();

        return outputStream.toByteArray();
    }
    private byte[] compressContentInChunks(byte[] content) throws OutOfMemoryError, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // 중간 압축 데이터를 위한 작은 크기의 버퍼
        ByteArrayOutputStream tempStream = new ByteArrayOutputStream(CHUNK_SIZE);
        // DeflaterOutputStream 사용 - zlib 형식 준수
        Deflater deflater = new Deflater(Deflater.BEST_SPEED);
        DeflaterOutputStream zlibStream = new DeflaterOutputStream(tempStream, deflater);

        // 청크 단위로 쓰고 flush
        for (int offset = 0; offset < content.length; offset += CHUNK_SIZE) {
            int length = Math.min(CHUNK_SIZE, content.length - offset);
            zlibStream.write(content, offset, length);

            // tempStream의 데이터를 finalStream으로 이동
            zlibStream.flush();
            tempStream.writeTo(outputStream);
            tempStream.reset();
        }

        // 남은 데이터 처리
        zlibStream.finish();
        tempStream.writeTo(outputStream);

        // 리소스 정리
        zlibStream.close();
        tempStream.close();
        deflater.end();

        return outputStream.toByteArray();
    }

    /**
     * 스트림이 있는 경우 오버라이드
     */
    public byte[] getStreamData() { return null; }

    public int getObjectNumber() { return objectNumber; }
}
