package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Typeface;
import android.text.TextPaint;

import com.hangyeolee.androidpdfwriter.font.PDFFont;

public class PDFH5 extends PDFText{
    public static float fontSize = 13.28f;


    public PDFH5(String text){
        super(text, PDFFont.HELVETICA_BOLD);
        this.bufferPaint.setTextSize(fontSize);
    }
    public PDFH5(String text, TextPaint paint){
        super(text, paint, PDFFont.HELVETICA_BOLD);
    }


    public static PDFH5 build(String text){return new PDFH5(text);}
}
