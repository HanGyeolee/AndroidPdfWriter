package com.hangyeolee.androidpdfwriter.binary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * PDF 객체를 표현하는 클래스
 */
class BinaryObject {
    private final int objectNumber;

    public BinaryObject(int objectNumber) {
        this.objectNumber = objectNumber;
    }
    /**
     * 스트림이 있는 경우 오버라이드
     */
    public byte[] getStreamData() { return null; }

    public int getObjectNumber() { return objectNumber; }
}
