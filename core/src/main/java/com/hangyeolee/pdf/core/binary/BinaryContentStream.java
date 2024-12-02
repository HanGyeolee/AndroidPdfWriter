package com.hangyeolee.pdf.core.binary;

import android.util.Log;
import com.hangyeolee.pdf.core.PDFBuilder;

import java.io.IOException;

/**
 * PDF 콘텐츠 스트림을 위한 객체
 */
class BinaryContentStream extends BinaryDictionary {
    private static final String TAG = "BinaryContentStream";
    public BinaryContentStream(int objectNumber, String content) {
        this(objectNumber, content, false);
    }
    public BinaryContentStream(int objectNumber, String content, boolean forceCompress) {
        super(objectNumber);
        byte[] compressed;

        if(PDFBuilder.DEBUG && !forceCompress) {
            compressed = BinaryConverter.toBytes(content);
            dictionary.put("/Length", compressed.length);
            writeContent(compressed);
        } else {
            try {
                {// 컨텐츠 압축
                    byte[] stream = BinaryConverter.toBytes(content);
                    compressContent(stream);
                    dictionary.put("/Length1", stream.length);
                }

                int length = getTemporaryLength();
                // 딕셔너리에 압축 관련 항목 추가
                dictionary.put("/Filter", "/" + FLATE_DECODE);
                dictionary.put("/Length", length);
            }catch (Throwable e){
                // 압축 실패
                compressed = BinaryConverter.toBytes(content);
                dictionary.put("/Length", compressed.length);
                writeContent(compressed);
            }
        }
    }
    public BinaryContentStream(int objectNumber, byte[] stream) {
        this(objectNumber, stream, false);
    }
    public BinaryContentStream(int objectNumber, byte[] stream, boolean forceCompress) {
        super(objectNumber);

        if(PDFBuilder.DEBUG && !forceCompress) {
            dictionary.put("/Length", stream.length);
            writeContent(stream);
        } else {
            try {
                compressContent(stream);
                // 컨텐츠 압축
                dictionary.put("/Length1", stream.length);

                int length = getTemporaryLength();
                // 딕셔너리에 압축 관련 항목 추가
                dictionary.put("/Filter", "/" + FLATE_DECODE);
                dictionary.put("/Length", length);
            }catch (Throwable e){
                // 압축 실패
                dictionary.put("/Length", stream.length);
                writeContent(stream);
            }
        }
    }

    @Override
    public byte[] getStreamData() {
        try {
            byte[] content = readTemporaryStream();
            deleteTemp();
            return content;
        } catch (IOException e){
            Log.e(TAG, "Can not Read Temporary File", e);
        }
        return null;
    }

    @Override
    protected void finalize() throws Throwable {
        deleteTemp();
        super.finalize();
    }
}