package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;

public class PDFH2 extends PDFText{
    public static float fontSize = 24;

    public PDFH2(PDFComponent parent, String text){
        super(parent, text);
        this.paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        this.paint.setTextSize(fontSize);
    }
    public PDFH2(PDFComponent parent, String text, TextPaint paint){
        super(parent, text, paint);
    }
}
