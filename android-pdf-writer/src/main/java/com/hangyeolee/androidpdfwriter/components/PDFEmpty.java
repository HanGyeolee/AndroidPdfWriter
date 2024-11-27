package com.hangyeolee.androidpdfwriter.components;

import com.hangyeolee.androidpdfwriter.binary.BinarySerializer;

public class PDFEmpty extends PDFComponent{
    public static PDFEmpty build(){return new PDFEmpty();}

    @Override
    public void draw(BinarySerializer serializer) {
        // draw nothing
    }
}

