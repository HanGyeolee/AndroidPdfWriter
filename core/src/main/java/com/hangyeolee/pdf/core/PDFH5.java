package com.hangyeolee.pdf.core;

import android.text.TextPaint;

import com.hangyeolee.pdf.core.font.PDFFont;

public class PDFH5 extends PDFText{
    public static float fontSize = 13.28f;


    protected PDFH5(String text){
        super(text, PDFFont.HELVETICA_BOLD);
        setFontsize(fontSize);
    }
    protected PDFH5(String text, TextPaint paint){
        super(text, paint, PDFFont.HELVETICA_BOLD);
    }


    public static PDFH5 build(String text){return new PDFH5(text);}
    public static PDFH5 build(String text, TextPaint paint){return new PDFH5(text, paint);}
}
