package com.hangyeolee.androidpdfwriter.binary;

import com.hangyeolee.androidpdfwriter.PDFBuilder;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * PDF 콘텐츠 스트림을 위한 객체
 */
class BinaryContentStream extends BinaryDictionary {
    private final byte[] compressedContent;

    public BinaryContentStream(int objectNumber, String content) {
        this(objectNumber, content, false);
    }
    public BinaryContentStream(int objectNumber, String content, boolean forceCompress) {
        super(objectNumber);

        if(PDFBuilder.DEBUG && !forceCompress) {
            this.compressedContent = BinaryConverter.toBytes(content);
            dictionary.put("/Length", compressedContent.length);
        } else {
            // 컨텐츠 압축
            byte[] stream = BinaryConverter.toBytes(content);
            dictionary.put("/Length1", stream.length);
            this.compressedContent = compressContent(stream);

            // 딕셔너리에 압축 관련 항목 추가
            dictionary.put("/Filter", "/"+FLATE_DECODE);
            dictionary.put("/Length", compressedContent.length);
        }
    }
    public BinaryContentStream(int objectNumber, byte[] stream) {
        this(objectNumber, stream, false);
    }
    public BinaryContentStream(int objectNumber, byte[] stream, boolean forceCompress) {
        super(objectNumber);

        if(PDFBuilder.DEBUG && !forceCompress) {
            this.compressedContent = stream;
            dictionary.put("/Length", compressedContent.length);
        } else {
            // 컨텐츠 압축
            dictionary.put("/Length1", stream.length);
            this.compressedContent = compressContent(stream);

            // 딕셔너리에 압축 관련 항목 추가
            dictionary.put("/Filter", "/"+FLATE_DECODE);
            dictionary.put("/Length", compressedContent.length);
        }
    }

    @Override
    public byte[] getStreamData() {
        return compressedContent;
    }
}