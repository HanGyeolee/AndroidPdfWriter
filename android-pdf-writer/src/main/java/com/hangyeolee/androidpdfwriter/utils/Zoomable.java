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

    private RectF pageRect = null;
    private final RectF padding = new RectF();

    /**
     * PDF 페이지 크기 설정
     * @param pageRect 크기
     */
    public void setPageRect(RectF pageRect){
        this.pageRect = pageRect;
    }

    public RectF getPageRect() {
        return pageRect;
    }

    /**
     * PDF 페이지 패딩
     */
    public RectF getPadding() {
        return padding;
    }
}
