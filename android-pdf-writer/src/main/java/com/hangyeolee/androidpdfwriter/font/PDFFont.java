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

    public static int getFontFlags(@NonNull @ID String fontName){
        int flags = 0;
        switch (fontName) {
            case TIMES_BOLD, TIMES_BOLDITALIC, TIMES_ROMAN, TIMES_ITALIC:
                // Serif 폰트
                flags |= (1 << 1);  // Serif bit
                flags |= (1 << 5);  // Nonsymbolic bit
                break;
            case HELVETICA_BOLD, HELVETICA_BOLDITALIC, HELVETICA, HELVETICA_ITALIC:
                // Sans-serif 폰트
                flags |= (1 << 5);  // Nonsymbolic bit
                break;
            case COURIER_BOLD, COURIER_BOLDITALIC, COURIER, COURIER_ITALIC:
                // Monospace 폰트
                flags |= 1;  // FixedPitch bit
                flags |= (1 << 5);  // Nonsymbolic bit
                break;
            default:
                // Symbol 폰트
                flags |= (1 << 2);  // Symbolic bit
                break;
        };

        // Italic 플래그 설정
        if (fontName.contains("ITALIC")) {
            flags |= (1 << 6);  // Italic bit
        }

        // Bold 플래그 설정
        if (fontName.contains("BOLD")) {
            flags |= (1 << 18);  // ForceBold bit
        }

        return flags;
    }
}
