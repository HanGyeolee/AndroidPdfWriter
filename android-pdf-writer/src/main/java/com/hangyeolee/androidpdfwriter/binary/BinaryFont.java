package com.hangyeolee.androidpdfwriter.binary;


import androidx.annotation.Nullable;

/**
 * Font 객체
 */
class BinaryFont extends BinaryObject {
    public BinaryFont(
            int objectNumber,
            @Nullable BinaryFontDescriptor descriptor,
            @Nullable String encoding,
            @Nullable BinaryObject cmap) {
        super(objectNumber);
        dictionary.put("/Type", "/Font");
        if(descriptor != null) {
            dictionary.put("/FontDescriptor", descriptor);
            addDependency(descriptor);
        }
        // 기본 14 폰트용 공통 설정
        if (encoding == null) {
            dictionary.put("/Encoding", "/WinAnsiEncoding");
            dictionary.put("/FirstChar", 32);      // 시작 문자 (space)
            dictionary.put("/LastChar", 255);      // 마지막 문자
        } else {
            dictionary.put("/Encoding", "/" + encoding);
        }
        if(cmap != null) {
            dictionary.put("/ToUnicode", cmap); // ToUnicode CMap 추가
            addDependency(cmap);
        }
    }

    public void setBaseFont(String name) {
        dictionary.put("/BaseFont", "/" + name);
    }

    public void setWidths(int[] widths) {
        StringBuilder sb = new StringBuilder("[");
        for (float width : widths) {
            sb.append(width).append(" ");
        }
        sb.append("]");
        dictionary.put("/Widths", sb.toString());
    }
}
