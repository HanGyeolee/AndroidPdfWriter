package com.hangyeolee.pdf.core.components;

import android.graphics.Typeface;
import android.text.TextPaint;

import com.hangyeolee.pdf.core.font.PDFFont;

public class PDFH6 extends PDFText{
    public static float fontSize = 10.72f;

    protected PDFH6(String text){
        super(text, PDFFont.HELVETICA_BOLD);
        setFontsize(fontSize);
    }
    protected PDFH6(String text, TextPaint paint){
        super(text, paint, PDFFont.HELVETICA_BOLD);
    }

    public static PDFH6 build(String text){return new PDFH6(text);}
    public static PDFH6 build(String text, TextPaint paint){return new PDFH6(text, paint);}
}
