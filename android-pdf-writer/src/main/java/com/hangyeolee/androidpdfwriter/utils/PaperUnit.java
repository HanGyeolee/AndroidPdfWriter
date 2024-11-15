package com.hangyeolee.androidpdfwriter.utils;

public enum PaperUnit {
    MM,
    INCH;

    public double toPt(){
        switch (this){
            case MM:
                return 72.0/25.4;
            case INCH:
                return 72.0;
        }
        return 1.0;
    }
}
