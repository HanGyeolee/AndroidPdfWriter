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

    public static void clear(){
        getInstance().pageRect = null;
        getInstance().padding.set(0, 0, 0, 0);
    }

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

    public float getContentWidth(){
        return pageRect.width() - padding.left - padding.right;
    }
    public float getContentHeight(){
        return pageRect.height() - padding.top - padding.bottom;
    }

    /**
     *
     * @param y ContentPage 에서 상단으로 부터 거리
     * @return
     */
    public float transform2PDFHeight(float y){
        return pageRect.height() - padding.top - y;
    }
    public float transform2PDFWidth(float x){
        return padding.left + x;
    }
}
