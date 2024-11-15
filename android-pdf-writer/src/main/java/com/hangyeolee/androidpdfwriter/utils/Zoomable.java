package com.hangyeolee.androidpdfwriter.utils;

import android.graphics.RectF;

public class Zoomable {
    private static Zoomable instance = null;

    public static Zoomable getInstance(){
        if(instance == null){
            instance = new Zoomable();
        }
        return instance;
    }

    private RectF contentRect = null;

    public void setContentRect(RectF contentRect){
        this.contentRect = contentRect;
    }

    public RectF getContentRect() {
        return contentRect;
    }
}
