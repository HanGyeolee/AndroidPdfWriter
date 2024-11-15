package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Typeface;
import android.text.TextPaint;

public class PDFH6 extends PDFText{
    public static float fontSize = 10.72f;

    public PDFH6(String text){
        super(text);
        this.bufferPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        this.bufferPaint.setTextSize(fontSize);
    }
    public PDFH6(String text, TextPaint paint){
        super(text, paint);
    }

    public static PDFH6 build(String text){return new PDFH6(text);}
}
