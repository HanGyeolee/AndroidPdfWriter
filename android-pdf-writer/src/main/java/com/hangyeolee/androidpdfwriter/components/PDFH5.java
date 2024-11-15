package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Typeface;
import android.text.TextPaint;

public class PDFH5 extends PDFText{
    public static float fontSize = 13.28f;

    public PDFH5(String text){
        super(text);
        this.bufferPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        this.bufferPaint.setTextSize(fontSize);
    }
    public PDFH5(String text, TextPaint paint){
        super(text, paint);
    }

    public static PDFH5 build(String text){return new PDFH5(text);}
}
