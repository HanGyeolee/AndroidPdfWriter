package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Typeface;
import android.text.TextPaint;

import com.hangyeolee.androidpdfwriter.font.PDFFont;

public class PDFH2 extends PDFText{
    public static float fontSize = 24;

    public PDFH2(String text){
        super(text, PDFFont.HELVETICA_BOLD);
        this.bufferPaint.setTextSize(fontSize);
    }
    public PDFH2(String text, TextPaint paint){
        super(text, paint, PDFFont.HELVETICA_BOLD);
    }


    public static PDFH2 build(String text){return new PDFH2(text);}
}
