package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Typeface;
import android.text.TextPaint;

public class PDFH1 extends PDFText{
    public static float fontSize = 32;

    public PDFH1(String text){
        super(text);
        this.bufferPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        this.bufferPaint.setTextSize(fontSize);
    }
    public PDFH1(String text, TextPaint paint){
        super(text, paint);
    }

    public static PDFH1 build(String text){return new PDFH1(text);}
}

