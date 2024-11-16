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
        dictionary.put("/FontBBox", String.format(Locale.getDefault(),
                "[%f %f %f %f]",
                metrics.fontBBox.left, metrics.fontBBox.bottom,
                metrics.fontBBox.right, metrics.fontBBox.top));
    }
}
