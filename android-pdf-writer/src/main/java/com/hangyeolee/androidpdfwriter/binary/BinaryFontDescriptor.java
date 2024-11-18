package com.hangyeolee.androidpdfwriter.binary;

import com.hangyeolee.androidpdfwriter.font.FontMetrics;

import java.util.Locale;

/**
 * FontDescriptor 객체
 */
class BinaryFontDescriptor extends BinaryObject {
    public BinaryFontDescriptor(int objectNumber) {
        super(objectNumber);
        dictionary.put("/Type", "/FontDescriptor");
    }

    public void setMetrics(FontMetrics metrics) {
        dictionary.put("/Flags", metrics.flags);
        dictionary.put("/ItalicAngle", metrics.italicAngle);
        dictionary.put("/Ascent", metrics.ascent);
        dictionary.put("/Descent", metrics.descent);
        dictionary.put("/CapHeight", metrics.capHeight);
        dictionary.put("/StemV", metrics.stemV);
        dictionary.put("/XHeight", metrics.xHeight);
        dictionary.put("/StemH", metrics.stemH);
        dictionary.put("/FontBBox", String.format(Locale.getDefault(),
                "[%d %d %d %d]",
                metrics.fontBBox.left, metrics.fontBBox.bottom,
                metrics.fontBBox.right, metrics.fontBBox.top));
    }

    public void setFontName(String name) {
        dictionary.put("/FontName", "/" + name);
    }
    public void setFontFile3(BinaryObject fontfile3) {
        dictionary.put("/FontFile3", fontfile3);
    }
    public void setFontFile2(BinaryObject fontfile3) {
        dictionary.put("/FontFile2", fontfile3);
    }
}
