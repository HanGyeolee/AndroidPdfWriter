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
        dictionary.put("/Subtype", "/TrueType");
        if(descriptor != null)
            dictionary.put("/FontDescriptor", descriptor);
        if(encoding != null)
            dictionary.put("/Encoding", encoding);
        if(cmap != null)
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
