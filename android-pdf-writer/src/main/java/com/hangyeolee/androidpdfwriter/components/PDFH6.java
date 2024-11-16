package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Typeface;
import android.text.TextPaint;

import com.hangyeolee.androidpdfwriter.font.PDFFont;

public class PDFH6 extends PDFText{
    public static float fontSize = 10.72f;


    public PDFH6(String text){
        super(text, PDFFont.HELVETICA_BOLD);
        this.bufferPaint.setTextSize(fontSize);
    }
    public PDFH6(String text, TextPaint paint){
        super(text, paint, PDFFont.HELVETICA_BOLD);
    }


    public static PDFH6 build(String text){return new PDFH6(text);}
}
