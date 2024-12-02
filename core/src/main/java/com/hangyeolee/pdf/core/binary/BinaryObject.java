package com.hangyeolee.pdf.core.binary;

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
    private static final File tempPath = new File(System.getProperty("java.io.tmpdir", "."));
    private static final String TAG = "BinaryObject";
    protected static final String FLATE_DECODE = "FlateDecode";
    protected static final String DCT_DECODE = "DCTDecode";
    private final int objectNumber;
    private final int hash;

    public BinaryObject(int objectNumber) {
        this.objectNumber = objectNumber;
        hash = this.hashCode();
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
        File tempFile = new File(tempPath, "pdf_"+this.hash+"_"+hash+".tmp");
        tempFile.delete();
        return new BufferedOutputStream(new FileOutputStream(tempFile));
    }
    protected byte[] readTemporaryStream() throws IOException{
        return readTemporaryStream(0);
    }
    protected byte[] readTemporaryStream(int hash) throws IOException {
        File tempFile = new File(tempPath, "pdf_"+this.hash+"_"+hash+".tmp");
        try (FileInputStream is = new FileInputStream(tempFile)) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
                return is.readAllBytes();
            } else {
                // 파일 크기로 버퍼 할당
                int fileSize = (int) tempFile.length();
                byte[] buffer = new byte[fileSize];

                int offset = 0;
                int numRead;
                while (offset < buffer.length &&
                        (numRead = is.read(buffer, offset, buffer.length - offset)) >= 0) {
                    offset += numRead;
                }

                // 모든 데이터를 읽었는지 확인
                if (offset < buffer.length) {
                    Log.e(TAG,"Could not completely read file " + tempFile.getName());
                }
                return buffer;
            }
        }
    }
    protected long getTemporaryLength() {
        return getTemporaryLength(0);
    }
    protected long getTemporaryLength(int hash) {
        File tempFile = new File(tempPath, "pdf_"+this.hash+"_"+hash+".tmp");
        return tempFile.length();
    }
    protected void deleteTemp(){
        deleteTemp(0);
    }
    protected void deleteTemp(int... hashes){
        for(int hash:hashes){
            File tempFile = new File(tempPath, "pdf_"+this.hash+"_"+hash+".tmp");
            if(tempFile.exists()){
                tempFile.delete();
            }
        }
    }

    /**
     * 스트림이 있는 경우 오버라이드
     */
    public byte[] getStreamData() { return null; }

    public int getObjectNumber() { return objectNumber; }
}
