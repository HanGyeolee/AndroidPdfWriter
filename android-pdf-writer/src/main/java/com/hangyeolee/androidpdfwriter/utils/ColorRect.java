package com.hangyeolee.androidpdfwriter.utils;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

public class ColorRect{
    @ColorInt
    public int left;
    @ColorInt
    public int top;
    @ColorInt
    public int right;
    @ColorInt
    public int bottom;

    public ColorRect(@ColorInt int left, @ColorInt int top,
                     @ColorInt int right,@ColorInt int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public ColorRect(@Nullable ColorRect r) {
        if (r == null) {
            left = top = right = bottom = 0;
        } else {
            left = r.left;
            top = r.top;
            right = r.right;
            bottom = r.bottom;
        }
    }

    /**
     * Sets the color of the rectangle to the specified value.
     *
     * @param left   The color of the left side of the rectangle
     * @param top    The color of the top of the rectangle
     * @param right  The color of the right side of the rectangle
     * @param bottom The color of the bottom of the rectangle
     */
    public void set(int left, int top, int right, int bottom) {
        this.left   = left;
        this.top    = top;
        this.right  = right;
        this.bottom = bottom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ColorRect r = (ColorRect) o;
        return left == r.left && top == r.top && right == r.right && bottom == r.bottom;
    }

    @Override
    public int hashCode() {
        int result = left;
        result = 31 * result + top;
        result = 31 * result + right;
        result = 31 * result + bottom;
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(32);
        sb.append("ColorRect("); sb.append(String.format("%X", left)); sb.append(", ");
        sb.append(String.format("%X", top)); sb.append(", "); sb.append(String.format("%X", right));
        sb.append(", "); sb.append(String.format("%X", bottom)); sb.append(")");
        return sb.toString();
    }
}

