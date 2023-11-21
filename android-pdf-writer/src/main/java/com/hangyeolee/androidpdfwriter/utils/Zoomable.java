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

    /**
     * PDF 내에 들어가는 요소를 전부 이미지로 변환 후 집어 넣기 때문에<br>
     * density 를 통해 전체적으로 이미지 파일 크기 자체를 키워서<br>
     * 디바이스에 구애 받지 않고 dpi 를 늘리는 효과를 만든다. <br>
     * Since all elements in the PDF are converted into images and inserted,<br>
     * Through density, the image file size itself is increased as a whole,<br>
     * creating the effect of increasing dpi regardless of device.
     */
    public float density = 1.0f;

    private RectF contentRect = null;

    public void setContentRect(RectF contentRect){
        this.contentRect = contentRect;
    }

    public RectF getContentRect() {
        return contentRect;
    }
    public float getZoomWidth() {
        return contentRect.width() * density;
    }
    public float getZoomHeight() {
        return contentRect.height() * density;
    }
}
