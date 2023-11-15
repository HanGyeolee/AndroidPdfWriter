package com.hangyeolee.androidpdfwriter.utils;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class Border {
    public Rect size;
    public Rect color;

    /**
     * 테두리 굵기 및 색상 지정 <br>
     * Specify border thickness and color
     */
    public Border(){
        size = new Rect(0,0,0,0);
        color = new Rect(0,0,0,0);
    }

    /**
     * 테두리 굵기 및 색상 지정 <br>
     * Specify border thickness and color
     * @param size 테두리 굵기, thickness
     * @param color 테두리 색상, color
     */
    public Border(int size, int color){
        this.size = new Rect(size, size, size, size);
        this.color = new Rect(color, color, color, color);
    }

    /**
     * 테두리 굵기 및 색상 지정 <br>
     * Specify border thickness and color
     * @param size 테두리 굵기, thickness
     * @param color 테두리 색상, color
     */
    public Border(Rect size, Rect color){
        this.size = new Rect(size);
        this.color = new Rect(color);
    }

    /**
     * 테두리 굵기 및 색상 지정 <br>
     * Specify border thickness and color
     * @param size 테두리 굵기, thickness
     * @param color 테두리 색상, color
     */
    public Border(Rect size, int color){
        this.size = new Rect(size);
        this.color = new Rect(color, color, color, color);
    }
    public Border setLeft(int size, int color){
        this.size.left = size;
        this.color.left = color;
        return this;
    }
    public Border setTop(int size, int color){
        this.size.top = size;
        this.color.top = color;
        return this;
    }
    public Border setRight(int size, int color){
        this.size.right = size;
        this.color.right = color;
        return this;
    }
    public Border setBottom(int size, int color){
        this.size.bottom = size;
        this.color.bottom = color;
        return this;
    }

    public void draw(Canvas canvas, int measureX, int measureY, int measureWidth, int measureHeight){
        Paint paint = new Paint();
        if(size.left > 0) {
            paint.setStrokeWidth(size.left);
            paint.setColor(color.left);
            canvas.drawLine(measureX, measureY, measureX, measureY+measureHeight, paint);
        }
        if(size.top > 0) {
            paint.setStrokeWidth(size.top);
            paint.setColor(color.top);
            canvas.drawLine(measureX, measureY, measureX+measureWidth, measureY, paint);
        }
        if(size.right > 0) {
            paint.setStrokeWidth(size.right);
            paint.setColor(color.right);
            canvas.drawLine(measureX+measureWidth, measureY, measureX+measureWidth, measureY+measureHeight, paint);
        }
        if(size.bottom > 0) {
            paint.setStrokeWidth(size.bottom);
            paint.setColor(color.bottom);
            canvas.drawLine(measureX, measureY+measureHeight, measureX+measureWidth, measureY+measureHeight, paint);
        }
    }

    /**
     * 왼쪽 테두리만 나타나도록 설정<br>
     * 테두리 굵기 및 색상 지정 <br>
     * Set so that only the left border appears<br>
     * Specify border thickness and color
     * @param size 테두리 굵기, thickness
     * @param color 테두리 색상, color
     */
    public static Border BorderLeft(int size, int color){
        return new Border(new Rect(size,0,0, 0), new Rect(color, 0, 0, 0));
    }
    /**
     * 위쪽 테두리만 나타나도록 설정<br>
     * 테두리 굵기 및 색상 지정 <br>
     * Set so that only the top border appears<br>
     * Specify border thickness and color
     * @param size 테두리 굵기, thickness
     * @param color 테두리 색상, color
     */
    public static Border BorderTop(int size, int color){
        return new Border(new Rect(0,size,0, 0), new Rect(0, color, 0, 0));
    }
    /**
     * 오른쪽 테두리만 나타나도록 설정<br>
     * 테두리 굵기 및 색상 지정 <br>
     * Set so that only the right border appears<br>
     * Specify border thickness and color
     * @param size 테두리 굵기, thickness
     * @param color 테두리 색상, color
     */
    public static Border BorderRight(int size, int color){
        return new Border(new Rect(0,0,size, 0), new Rect(0, 0, color, 0));
    }
    /**
     * 아래쪽 테두리만 나타나도록 설정<br>
     * 테두리 굵기 및 색상 지정 <br>
     * Set so that only the bottom border appears<br>
     * Specify border thickness and color
     * @param size 테두리 굵기, thickness
     * @param color 테두리 색상, color
     */
    public static Border BorderBottom(int size, int color){
        return new Border(new Rect(0,0,0, size), new Rect(0, 0, 0, color));
    }
}
