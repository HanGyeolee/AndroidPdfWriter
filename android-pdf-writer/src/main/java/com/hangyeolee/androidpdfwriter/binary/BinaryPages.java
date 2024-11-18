package com.hangyeolee.androidpdfwriter.binary;

import java.util.ArrayList;
import java.util.List;

/**
 * Pages 객체 (페이지들의 컨테이너)
 */
class BinaryPages extends BinaryObject {
    private final List<BinaryPage> pages = new ArrayList<>();

    public BinaryPages(int objectNumber) {
        super(objectNumber);
        dictionary.put("/Type", "/Pages");
    }

    public void addPage(BinaryPage page) {
        pages.add(page);
        page.setParent(this);
    }

    public BinaryPage getPage(int index){
        if (index < 0) {
            throw new IllegalArgumentException("Page index cannot be negative");
        }
        return pages.get(index);
    }

    public int getPageSize(){
        return pages.size();
    }

    public void finalizeContents(BinaryObjectManager manager, BinaryResources resources){
        for (BinaryPage page : pages) {
            page.setResources(resources);
            page.finalizeContent(manager);
        }
    }

    @Override
    public String toDictionaryString() {
        dictionary.put("/Count", pages.size());
        StringBuilder kids = new StringBuilder("[ ");
        for (BinaryPage page : pages) {
            kids.append(page.getObjectNumber()).append(" 0 R ");
        }
        kids.append("]");
        dictionary.put("/Kids", kids.toString());
        return super.toDictionaryString();
    }
}
