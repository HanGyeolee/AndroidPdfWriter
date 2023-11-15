package com.hangyeolee.androidpdfwriter.utils;

import android.graphics.Rect;

public class Border {
    public Rect size;
    public Rect color;

    public Border(){
        size = new Rect(0,0,0,0);
        color = null;
    }
    public Border(int size, int color){
        this.size = new Rect(size, size, size, size);
        this.color = new Rect(color, color, color, color);
    }
    public Border(Rect size, Rect color){
        this.size = new Rect(size);
        this.color = new Rect(color);
    }
}
