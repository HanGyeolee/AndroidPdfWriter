package com.hangyeolee.androidpdfwriter.utils;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Anchor{
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Start, Center, End})
    public @interface Type {}
    public static final int Start = -1;
    public static final int Center = 0;
    public static final int End = 1;

    @Type
    public int horizontal = Start;
    @Type
    public int vertical = Start;

    public Anchor(){}
    public Anchor(@Type int vertical,@Type int horizontal){
        this.vertical = vertical;
        this.horizontal = horizontal;
    }

    public static float getDeltaPixel(@Type int axis,float gap){
        switch (axis){
            case Start:
                return 0;
            case Center:
                return (gap * 0.5f);
            case End:
                return gap;
        }
        return 0;
    }

    private String getName(@Type int axis){
        switch (axis){
            case Start:
                return "Start";
            case Center:
                return "Center";
            case End:
                return "End";
        }
        return "Null";
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(32);
        sb.append("Anchor("); sb.append(getName(horizontal)); sb.append(", ");
        sb.append(getName(vertical)); sb.append(")");
        return sb.toString();
    }
}
