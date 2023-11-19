package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Rect;

import com.hangyeolee.androidpdfwriter.listener.Action;
import com.hangyeolee.androidpdfwriter.utils.Border;

public class PDFEmpty extends PDFComponent{
    public static PDFEmpty build(){return new PDFEmpty();}

    @Override
    public PDFEmpty setSize(Float width, Float height) {
        super.setSize(width, height);
        return this;
    }

    @Override
    public PDFEmpty setBackgroundColor(int color) {
        super.setBackgroundColor(color);
        return this;
    }

    @Override
    public PDFEmpty setMargin(Rect margin) {
        super.setMargin(margin);
        return this;
    }

    @Override
    public PDFEmpty setMargin(int left, int top, int right, int bottom) {
        super.setMargin(left, top, right, bottom);
        return this;
    }

    @Override
    public PDFEmpty setPadding(Rect padding) {
        super.setPadding(padding);
        return this;
    }

    @Override
    public PDFEmpty setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        return this;
    }

    @Override
    public PDFEmpty setBorder(Action<Border, Border> action) {
        super.setBorder(action);
        return this;
    }

    @Override
    public PDFEmpty setAnchor(Integer vertical, Integer horizontal) {
        super.setAnchor(vertical, horizontal);
        return this;
    }

    @Override
    protected PDFEmpty setParent(PDFComponent parent) {
        super.setParent(parent);
        return this;
    }
}

