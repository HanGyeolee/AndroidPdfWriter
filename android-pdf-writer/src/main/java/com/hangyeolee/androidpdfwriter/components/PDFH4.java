package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Typeface;
import android.text.TextPaint;

import com.hangyeolee.androidpdfwriter.font.PDFFont;

public class PDFH4 extends PDFText{
    public static float fontSize = 16f;


    protected PDFH4(String text){
        super(text, PDFFont.HELVETICA_BOLD);
        setFontsize(fontSize);
    }
    protected PDFH4(String text, TextPaint paint){
        super(text, paint, PDFFont.HELVETICA_BOLD);
    }


    public static PDFH4 build(String text){return new PDFH4(text);}
    public static PDFH4 build(String text, TextPaint paint){return new PDFH4(text, paint);}
}
