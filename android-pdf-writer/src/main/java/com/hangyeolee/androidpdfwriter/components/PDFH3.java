package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Typeface;
import android.text.TextPaint;

import com.hangyeolee.androidpdfwriter.font.PDFFont;

public class PDFH3 extends PDFText{
    public static float fontSize = 18.72f;

    public PDFH3(String text){
        super(text, PDFFont.HELVETICA_BOLD);
        this.bufferPaint.setTextSize(fontSize);
    }
    public PDFH3(String text, TextPaint paint){
        super(text, paint, PDFFont.HELVETICA_BOLD);
    }


    public static PDFH3 build(String text){return new PDFH3(text);}
}
