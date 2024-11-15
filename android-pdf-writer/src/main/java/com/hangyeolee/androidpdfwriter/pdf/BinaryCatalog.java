package com.hangyeolee.androidpdfwriter.pdf;

/**
 * Catalog 객체
 */
class BinaryCatalog extends BinaryObject {
    public BinaryCatalog(int objectNumber, BinaryPages pages) {
        super(objectNumber);
        dictionary.put("/Type", "/Catalog");
        dictionary.put("/Pages", pages);  // Pages 객체 참조
    }
}
