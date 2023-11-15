package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;

public class PDFH3 extends PDFText{
    public static float fontSize = 18.72f;

    public PDFH3(String text){
        super(text);
        this.bufferPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        this.bufferPaint.setTextSize(fontSize);
    }
    public PDFH3(String text, TextPaint paint){
        super(text, paint);
    }

    public static PDFH3 build(String text){return new PDFH3(text);}
}
