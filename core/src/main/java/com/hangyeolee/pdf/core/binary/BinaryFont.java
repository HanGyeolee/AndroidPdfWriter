package com.hangyeolee.pdf.core.binary;


import androidx.annotation.Nullable;

import com.hangyeolee.pdf.core.PDFBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Font 객체
 */
class BinaryFont extends BinaryDictionary {
    private final List<BinaryObject> descendantFonts = new ArrayList<>();
    public BinaryFont(
            int objectNumber,
            @Nullable String encoding) {
        super(objectNumber);
        dictionary.put("/Type", "/Font");
        // 기본 14 폰트용 공통 설정
        if (encoding != null) {
            dictionary.put("/Encoding", "/" + encoding);
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

    public void setCAMP(BinaryObject cmap){
        dictionary.put("/ToUnicode", cmap); // ToUnicode CMap 추가
    }

    public void setFontDescriptor(BinaryFontDescriptor descriptor){
        dictionary.put("/FontDescriptor", descriptor);
    }

    public void setBaseFont(String name) {
        dictionary.put("/BaseFont", "/" + name);
    }

    public void setWidths(String widths) {
//        dictionary.put("/FontMatrix", "[0.001 0 0 0.001 0 0]");
        dictionary.put("/Widths", widths);
    }

    public void setW(Map<Character, Integer> map, Map<Character, Integer> glyphIds) {
        if(!map.isEmpty()) {
            StringBuilder sb = new StringBuilder("[");
            for (Map.Entry<Character, Integer> entry : map.entrySet()) {
                Integer glyphId = glyphIds.get(entry.getKey());
                if(glyphId != null)
                    // 글리프, 가로길이
                    sb.append(String.format(Locale.getDefault(), "%d[%d]",
                            glyphId & 0xffff,
                            entry.getValue()));
            }
            sb.append("]");
            dictionary.put("/W", sb.toString());
//            dictionary.put("/DW", 1000);
        }
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
