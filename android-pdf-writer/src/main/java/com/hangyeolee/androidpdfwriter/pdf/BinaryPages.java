package com.hangyeolee.androidpdfwriter.pdf;

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
        addDependency(page);
    }

    @Override
    public String toDictionaryString() {
        dictionary.put("/Count", pages.size());
        StringBuilder kids = new StringBuilder("[");
        for (BinaryPage page : pages) {
            kids.append(page.getObjectNumber()).append(" 0 R ");
        }
        kids.append("]");
        dictionary.put("/Kids", kids.toString());
        return super.toDictionaryString();
    }
}
