package com.hangyeolee.pdf.core.binary;

import android.graphics.RectF;

import java.util.Locale;

/**
 * Page 객체 (단일 페이지)
 */
class BinaryPage extends BinaryDictionary {
    private BinaryPages parent;
    private BinaryResources resources;
    private StringBuilder contentStream ;

    public BinaryPage(int objectNumber) {
        super(objectNumber);
        dictionary.put("/Type", "/Page");
        contentStream  = new StringBuilder();
    }

    public void setParent(BinaryPages parent) {
        this.parent = parent;
        dictionary.put("/Parent", parent);
    }

    public void setResources(BinaryResources resources) {
        this.resources = resources;
        dictionary.put("/Resources", resources);
    }

    public StringBuilder getContents(){
        return contentStream ;
    }

    // draw 완료 후 호출하여 컨텐츠를 ContentStream 객체로 변환
    public void finalizeContent(BinaryObjectManager manager) {
        if (contentStream != null && contentStream.length() > 0) {
            BinaryObject contents = manager.createObject(n ->
                    new BinaryContentStream(n, contentStream.toString()));
            dictionary.put("/Contents", contents);
            contentStream.setLength(0);
            contentStream = null;
        }
    }

    public void setMediaBox(RectF dimensions) {
        dictionary.put("/MediaBox", String.format(Locale.getDefault(), "[%s %s %s %s]",
                BinaryConverter.formatNumber(dimensions.left),
                BinaryConverter.formatNumber(dimensions.bottom),
                BinaryConverter.formatNumber(dimensions.right),
                BinaryConverter.formatNumber(dimensions.top)
        ));
    }
}
