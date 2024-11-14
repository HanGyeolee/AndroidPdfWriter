package com.hangyeolee.androidpdfwriter.utils;

/**
 * PDF 폰트 리소스를 관리하기 위한 내부 클래스
 */
public class PDFFontResource extends PDFResource {
    public PDFFontResource(int objectNumber, int fontId) {
        super(objectNumber, "F" + fontId);
    }
}