package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Typeface;
import android.text.TextPaint;

public class PDFH2 extends PDFText{
    public static float fontSize = 24;

    public PDFH2(String text){
        super(text);
        this.bufferPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        this.bufferPaint.setTextSize(fontSize);
    }
    public PDFH2(String text, TextPaint paint){
        super(text, paint);
    }

    public static PDFH2 build(String text){return new PDFH2(text);}
}
