package com.hangyeolee.pdf.core.components;

import android.graphics.Typeface;
import android.text.TextPaint;

import com.hangyeolee.pdf.core.font.PDFFont;

public class PDFH3 extends PDFText{
    public static float fontSize = 18.72f;

    protected PDFH3(String text){
        super(text, PDFFont.HELVETICA_BOLD);
        setFontsize(fontSize);
    }
    protected PDFH3(String text, TextPaint paint){
        super(text, paint, PDFFont.HELVETICA_BOLD);
    }


    public static PDFH3 build(String text){return new PDFH3(text);}
    public static PDFH3 build(String text, TextPaint paint){return new PDFH3(text, paint);}
}
