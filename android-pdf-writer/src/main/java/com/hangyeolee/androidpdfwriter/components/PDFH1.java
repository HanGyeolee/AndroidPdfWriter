package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Typeface;
import android.text.TextPaint;

public class PDFH1 extends PDFText{
    public static float fontSize = 32;

    public PDFH1(PDFComponent parent, String text){
        super(parent, text);
        this.paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        this.paint.setTextSize(fontSize);
    }
    public PDFH1(PDFComponent parent, String text, TextPaint paint){
        super(parent, text, paint);
    }
}

