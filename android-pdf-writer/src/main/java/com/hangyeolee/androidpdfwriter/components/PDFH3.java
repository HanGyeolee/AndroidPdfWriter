package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Typeface;
import android.text.TextPaint;

import com.hangyeolee.androidpdfwriter.utils.Zoomable;

public class PDFH3 extends PDFText{
    public static float fontSize = 18.72f;

    public PDFH3(String text){
        super(text);
        this.bufferPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        this.bufferPaint.setTextSize(fontSize * Zoomable.getInstance().density);
    }
    public PDFH3(String text, TextPaint paint){
        super(text, paint);
    }

    public static PDFH3 build(String text){return new PDFH3(text);}
}
