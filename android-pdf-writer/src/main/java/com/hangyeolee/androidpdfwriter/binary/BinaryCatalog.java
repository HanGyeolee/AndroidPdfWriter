package com.hangyeolee.androidpdfwriter.binary;

/**
 * Catalog 객체
 */
class BinaryCatalog extends BinaryObject {
    public BinaryCatalog(int objectNumber) {
        super(objectNumber);
        dictionary.put("/Type", "/Catalog");
    }
    public void setPages(BinaryPages pages){
        dictionary.put("/Pages", pages);  // Pages 객체 참조
    }
}
