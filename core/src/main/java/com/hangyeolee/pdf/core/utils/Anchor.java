package com.hangyeolee.pdf.core.utils;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Anchor{
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Start, Center, End})
    public @interface AnchorInt {}
    public static final int Start = 0;
    public static final int Center = 1;
    public static final int End = 2;

    @AnchorInt
    public int horizontal = Center;
    @AnchorInt
    public int vertical = Center;

    public Anchor(){}
    public Anchor(@AnchorInt int vertical, @AnchorInt int horizontal){
        this.vertical = vertical;
        this.horizontal = horizontal;
    }

    public static float getDeltaPixel(@AnchorInt int axis, float gap){
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

    private String getName(@AnchorInt int axis){
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
        StringBuilder sb = new StringBuilder(16);
        sb.append("Anchor("); sb.append(getName(horizontal)); sb.append(", ");
        sb.append(getName(vertical)); sb.append(")");
        return sb.toString();
    }
}
