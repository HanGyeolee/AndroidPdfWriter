package com.hangyeolee.androidpdfwriter.pdf;

/**
 * XObject 객체 (이미지나 폼 등)
 */
abstract class BinaryXObject extends BinaryObject {
    public BinaryXObject(int objectNumber) {
        super(objectNumber);
        dictionary.put("/Type", "/XObject");
    }
}

