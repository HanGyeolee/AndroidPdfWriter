package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;

public class PDFH2 extends PDFText{
    public static float fontSize = 24;

    public PDFH2(String text){
        super(text, null);
        this.paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        this.paint.setTextSize(fontSize);
    }
    public PDFH2(String text, TextPaint paint){
        super(text, paint);
    }
}
