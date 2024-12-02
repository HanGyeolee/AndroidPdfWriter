package com.hangyeolee.androidpdfwriter.binary;

import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
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
    protected void compressContent(byte[] content) throws OutOfMemoryError, IOException {
        compressContent(content, 0);
    }
    protected void compressContent(byte[] content, int hash) throws OutOfMemoryError, IOException {
        OutputStream outputStream = createTemporaryStream(hash);

        // DeflaterOutputStream 사용 - zlib 형식 준수
        Deflater deflater = new Deflater(5);
        DeflaterOutputStream zlibStream = new DeflaterOutputStream(outputStream, deflater);

        // 데이터 쓰기
        zlibStream.write(content);
        zlibStream.finish();
        zlibStream.close();

        // 리소스 정리
        deflater.end();

        outputStream.close();
    }

    protected void writeContent(byte[] content) throws OutOfMemoryError {
        writeContent(content, 0);
    }
    protected void writeContent(byte[] content, int hash) throws OutOfMemoryError {
        try {
            OutputStream outputStream = createTemporaryStream(hash);
            outputStream.write(content);
            outputStream.close();
        } catch (IOException e){
            Log.e(TAG, "Can not Create Temporary File", e);
        }
    }

    protected OutputStream createTemporaryStream() throws IOException {
        return createTemporaryStream(0);
    }
    protected OutputStream createTemporaryStream(int hash) throws IOException {
        File tempFile = File.createTempFile("pdf_image_"+this.hashCode(), hash+".tmp");
        tempFile.delete();
        return new BufferedOutputStream(new FileOutputStream(tempFile));
    }
    protected byte[] readTemporaryStream() throws IOException{
        return readTemporaryStream(0);
    }
    protected byte[] readTemporaryStream(int hash) throws IOException {
        File tempFile = File.createTempFile("pdf_image_"+this.hashCode(), hash+".tmp");
        try (FileInputStream is = new FileInputStream(tempFile)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return is.readAllBytes();
            } else {
                byte[] buffer = new byte[is.available()];
                is.read(buffer);
                return buffer;
            }
        }
    }
    protected int getTemporaryLength() throws IOException {
        return getTemporaryLength(0);
    }
    protected int getTemporaryLength(int hash) throws IOException {
        File tempFile = File.createTempFile("pdf_image_"+this.hashCode(), hash+".tmp");
        try (FileInputStream is = new FileInputStream(tempFile)) {
            return is.available();
        }
    }
    protected void deleteTemp(){
        deleteTemp(0);
    }
    protected void deleteTemp(int... hashes){
        for(int hash:hashes){
            try {
                File tempFile = File.createTempFile("pdf_image_" + this.hashCode(), hash + ".tmp");
                if(tempFile.exists()){
                    tempFile.delete();
                }
            } catch (IOException e){
                Log.e(TAG, "Can not Delete Temp : pdf_image_" + this.hashCode() + (hash + ".tmp"));
            }
        }
    }

    /**
     * 스트림이 있는 경우 오버라이드
     */
    public byte[] getStreamData() { return null; }

    public int getObjectNumber() { return objectNumber; }
}
