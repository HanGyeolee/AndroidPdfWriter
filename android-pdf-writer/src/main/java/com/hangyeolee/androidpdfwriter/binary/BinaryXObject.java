package com.hangyeolee.androidpdfwriter.binary;

/**
 * XObject 객체 (이미지나 폼 등)
 */
abstract class BinaryXObject extends BinaryDictionary {
    public BinaryXObject(int objectNumber) {
        super(objectNumber);
        dictionary.put("/Type", "/XObject");
    }
}

