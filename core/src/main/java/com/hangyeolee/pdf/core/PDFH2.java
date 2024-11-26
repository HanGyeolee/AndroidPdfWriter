package com.hangyeolee.pdf.core;

import android.text.TextPaint;

import com.hangyeolee.pdf.core.font.PDFFont;

public class PDFH2 extends PDFText{
    public static float fontSize = 24;

    protected PDFH2(String text){
        super(text, PDFFont.HELVETICA_BOLD);
        setFontsize(fontSize);
    }
    protected PDFH2(String text, TextPaint paint){
        super(text, paint, PDFFont.HELVETICA_BOLD);
    }


    public static PDFH2 build(String text){return new PDFH2(text);}
    public static PDFH2 build(String text, TextPaint paint){return new PDFH2(text, paint);}
}
