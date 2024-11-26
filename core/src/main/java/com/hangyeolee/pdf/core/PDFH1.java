package com.hangyeolee.pdf.core;

import android.text.TextPaint;

import com.hangyeolee.pdf.core.font.PDFFont;

public class PDFH1 extends PDFText{
    public static float fontSize = 32;

    protected PDFH1(String text){
        super(text, PDFFont.HELVETICA_BOLD);
        setFontsize(fontSize);
    }
    protected PDFH1(String text, TextPaint paint){
        super(text, paint, PDFFont.HELVETICA_BOLD);
    }

    public static PDFH1 build(String text){return new PDFH1(text);}
    public static PDFH1 build(String text, TextPaint paint){return new PDFH1(text, paint);}
}

