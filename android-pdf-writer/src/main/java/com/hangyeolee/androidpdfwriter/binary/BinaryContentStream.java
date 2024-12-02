package com.hangyeolee.androidpdfwriter.binary;

import com.hangyeolee.androidpdfwriter.PDFBuilder;

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
        byte[] compressed;

        if(PDFBuilder.DEBUG && !forceCompress) {
            compressed = BinaryConverter.toBytes(content);
            dictionary.put("/Length", compressed.length);
        } else {
            // 컨텐츠 압축
            byte[] stream = BinaryConverter.toBytes(content);
            try {
                compressed = compressContent(stream);
                dictionary.put("/Length1", stream.length);

                // 딕셔너리에 압축 관련 항목 추가
                dictionary.put("/Filter", "/" + FLATE_DECODE);
                dictionary.put("/Length", compressed.length);
            }catch (Throwable e){
                // 압축 실패
                compressed = BinaryConverter.toBytes(content);
                dictionary.put("/Length", compressed.length);
            }
        }
        this.compressedContent = compressed;
    }
    public BinaryContentStream(int objectNumber, byte[] stream) {
        this(objectNumber, stream, false);
    }
    public BinaryContentStream(int objectNumber, byte[] stream, boolean forceCompress) {
        super(objectNumber);
        byte[] compressed;

        if(PDFBuilder.DEBUG && !forceCompress) {
            compressed = stream;
            dictionary.put("/Length", compressed.length);
        } else {
            try {
                // 컨텐츠 압축
                compressed = compressContent(stream);
                dictionary.put("/Length1", stream.length);

                // 딕셔너리에 압축 관련 항목 추가
                dictionary.put("/Filter", "/" + FLATE_DECODE);
                dictionary.put("/Length", compressed.length);
            }catch (Throwable e){
                // 압축 실패
                compressed = stream;
                dictionary.put("/Length", compressed.length);
            }
        }
        this.compressedContent = compressed;
    }

    @Override
    public byte[] getStreamData() {
        return compressedContent;
    }
}