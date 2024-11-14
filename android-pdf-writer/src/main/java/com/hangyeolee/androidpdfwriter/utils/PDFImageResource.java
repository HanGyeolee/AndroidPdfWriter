package com.hangyeolee.androidpdfwriter.utils;

/**
 * PDF 폰트 리소스를 관리하기 위한 내부 클래스
 */
public class PDFImageResource extends PDFResource {
    public PDFImageResource(int objectNumber, int imageId) {
        super(objectNumber, "Im" + imageId);
    }
}