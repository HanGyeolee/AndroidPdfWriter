package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Typeface;
import android.text.TextPaint;

import com.hangyeolee.androidpdfwriter.font.PDFFont;

public class PDFH1 extends PDFText{
    public static float fontSize = 32;

    public PDFH1(String text){
        super(text, PDFFont.HELVETICA_BOLD);
        this.bufferPaint.setTextSize(fontSize);
    }
    public PDFH1(String text, TextPaint paint){
        super(text, paint, PDFFont.HELVETICA_BOLD);
    }

    public static PDFH1 build(String text){return new PDFH1(text);}
}

