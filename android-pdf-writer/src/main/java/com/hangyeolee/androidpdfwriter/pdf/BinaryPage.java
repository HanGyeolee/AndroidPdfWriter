package com.hangyeolee.androidpdfwriter.pdf;

import java.util.Locale;

/**
 * Page 객체 (단일 페이지)
 */
class BinaryPage extends BinaryObject {
    private BinaryPages parent;
    private BinaryResources resources;
    private BinaryObject contents;  // 페이지의 모든 컨텐츠를 포함하는 스트림

    public BinaryPage(int objectNumber) {
        super(objectNumber);
        dictionary.put("/Type", "/Page");
    }

    public void setParent(BinaryPages parent) {
        this.parent = parent;
        dictionary.put("/Parent", parent);
    }

    public void setResources(BinaryResources resources) {
        this.resources = resources;
        dictionary.put("/Resources", resources);
        addDependency(resources);
    }

    /**
     * 페이지의 컨텐츠 스트림 설정
     * @param contents 컨텐츠 스트림 객체
     */
    public void setContents(BinaryObject contents) {
        this.contents = contents;
        dictionary.put("/Contents", contents);
        addDependency(contents);
    }

    public void setMediaBox(float[] dimensions) {
        dictionary.put("/MediaBox", String.format(Locale.getDefault(), "[%f %f %f %f]",
                dimensions[0], dimensions[1], dimensions[2], dimensions[3]));
    }

    /**
     * 현재 페이지의 리소스 객체 반환
     */
    public BinaryResources getResources() {
        return resources;
    }
}
