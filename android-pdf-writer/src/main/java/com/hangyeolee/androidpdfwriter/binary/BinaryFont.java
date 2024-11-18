package com.hangyeolee.androidpdfwriter.binary;


import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Font 객체
 */
class BinaryFont extends BinaryObject {
    private final List<BinaryObject> descendantFonts = new ArrayList<>();
    public BinaryFont(
            int objectNumber,
            @Nullable String encoding,
            @Nullable BinaryObject cmap) {
        super(objectNumber);
        dictionary.put("/Type", "/Font");
        // 기본 14 폰트용 공통 설정
        if (encoding != null) {
            dictionary.put("/Encoding", "/" + encoding);
        }
        if(cmap != null) {
            dictionary.put("/ToUnicode", cmap); // ToUnicode CMap 추가
        }
    }

    public void addDescendantFont(BinaryObject font) {
        descendantFonts.add(font);
    }

    public void setBase14Font(){
        dictionary.put("/Encoding", "/WinAnsiEncoding");
        dictionary.put("/FirstChar", 32);      // 시작 문자 (space)
        dictionary.put("/LastChar", 255);      // 마지막 문자
    }

    public void setFontDescriptor(BinaryFontDescriptor descriptor){
        dictionary.put("/FontDescriptor", descriptor);
    }

    public void setBaseFont(String name) {
        dictionary.put("/BaseFont", "/" + name);
    }

    public void setWidths(int[] widths) {
        StringBuilder sb = new StringBuilder("[");
        for (float width : widths) {
            sb.append(formatNumber(width)).append(" ");
        }
        sb.append("]");
        dictionary.put("/Widths", sb.toString());
    }

    public void setW(int[] widths) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < widths.length; i++) {
            sb.append(i).append(" [").append(formatNumber(widths[i])).append("] ");
        }
        sb.append("]");
        dictionary.put("/W", sb.toString());
    }

    @Override
    public String toDictionaryString() {
        if(!descendantFonts.isEmpty()) {
            StringBuilder fontDict = new StringBuilder("[ ");
            for (var object : descendantFonts) {
                fontDict.append(object.getObjectNumber()).append(" 0 R ");
            }
            fontDict.append("]");
            dictionary.put("/DescendantFonts", fontDict.toString());
        }
        return super.toDictionaryString();
    }
}
