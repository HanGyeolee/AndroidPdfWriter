package com.hangyeolee.androidpdfwriter.binary;


/**
 * Font 객체
 */
class BinaryFont extends BinaryObject {
    public BinaryFont(int objectNumber, BinaryFontDescriptor descriptor) {
        super(objectNumber);
        dictionary.put("/Type", "/Font");
        dictionary.put("/Subtype", "/TrueType");
        dictionary.put("/FontDescriptor", descriptor);
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
