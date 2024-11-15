package com.hangyeolee.androidpdfwriter.binary;


/**
 * Font 객체
 */
class BinaryFont extends BinaryObject {
    public BinaryFont(int objectNumber, BinaryFontDescriptor descriptor, String encoding, BinaryObject cmap) {
        super(objectNumber);
        dictionary.put("/Type", "/Font");
        dictionary.put("/Subtype", "/TrueType");
        dictionary.put("/FontDescriptor", descriptor);
        // UTF-8 인코딩을 위한 설정
        dictionary.put("/Encoding", encoding);
        dictionary.put("/ToUnicode", cmap); // ToUnicode CMap 추가
        addDependency(descriptor);
    }

    public void setBaseFont(String name) {
        dictionary.put("/BaseFont", "/" + name);
    }

    public void setWidths(float[] widths) {
        StringBuilder sb = new StringBuilder("[");
        for (float width : widths) {
            sb.append(width).append(" ");
        }
        sb.append("]");
        dictionary.put("/Widths", sb.toString());
    }
}
