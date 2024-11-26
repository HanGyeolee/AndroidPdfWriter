package com.hangyeolee.pdf.core;

import com.hangyeolee.pdf.core.binary.BinarySerializer;

public class PDFEmpty extends PDFComponent{
    public static PDFEmpty build(){return new PDFEmpty();}

    @Override
    public void measure(float x, float y) {
        // measure nothing
    }

    @Override
    public void draw(BinarySerializer serializer) {
        // draw nothing
    }
}
