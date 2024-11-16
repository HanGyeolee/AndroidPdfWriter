package com.hangyeolee.androidpdfwriter.font;

import android.graphics.Typeface;

import androidx.annotation.NonNull;
import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class PDFFont {
    // Windows Platform Encodings
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            TIMES_ROMAN,
            TIMES_BOLD,
            TIMES_ITALIC,
            TIMES_BOLDITALIC,
            HELVETICA,
            HELVETICA_BOLD,
            HELVETICA_ITALIC,
            HELVETICA_BOLDITALIC,
            COURIER,
            COURIER_BOLD,
            COURIER_ITALIC,
            COURIER_BOLDITALIC
    })
    public @interface ID {}
    public static final String TIMES_ROMAN = "Times-Roman";
    public static final String TIMES_BOLD = "Times-Bold";
    public static final String TIMES_ITALIC = "Times-Italic";
    public static final String TIMES_BOLDITALIC = "Times-BoldItalic";
    public static final String HELVETICA = "Helvetica";
    public static final String HELVETICA_BOLD = "Helvetica-Bold";
    public static final String HELVETICA_ITALIC = "Helvetica-Italic";
    public static final String HELVETICA_BOLDITALIC = "Helvetica-BoldItalic";
    public static final String COURIER = "Courier";
    public static final String COURIER_BOLD = "Courier-Bold";
    public static final String COURIER_ITALIC = "Courier-Italic";
    public static final String COURIER_BOLDITALIC = "Courier-BoldItalic";

    public static Typeface getTypeface(@NonNull @ID String fontName){
        Typeface type = switch (fontName) {
            case TIMES_BOLD, TIMES_BOLDITALIC, TIMES_ROMAN, TIMES_ITALIC -> Typeface.SERIF;
            case HELVETICA_BOLD, HELVETICA_BOLDITALIC, HELVETICA, HELVETICA_ITALIC -> Typeface.SANS_SERIF;
            case COURIER_BOLD, COURIER_BOLDITALIC, COURIER, COURIER_ITALIC -> Typeface.MONOSPACE;
            default -> Typeface.DEFAULT;
        };
        switch (fontName){
            case TIMES_BOLD :
            case HELVETICA_BOLD:
            case COURIER_BOLD:
                return Typeface.create(type, Typeface.BOLD);
            case TIMES_BOLDITALIC :
            case HELVETICA_BOLDITALIC:
            case COURIER_BOLDITALIC:
                return Typeface.create(type, Typeface.BOLD_ITALIC);
            case TIMES_ITALIC :
            case HELVETICA_ITALIC:
            case COURIER_ITALIC:
                return Typeface.create(type, Typeface.ITALIC);
            case TIMES_ROMAN :
            case HELVETICA:
            case COURIER:
                return type;
        }
        return null;
    }
}
