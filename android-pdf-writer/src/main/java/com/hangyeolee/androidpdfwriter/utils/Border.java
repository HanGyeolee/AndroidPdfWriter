package com.hangyeolee.androidpdfwriter.utils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

public class Border {
    public RectF size;
    public ColorRect color;

    /**
     * 테두리 굵기 및 색상 지정 <br>
     * Specify border thickness and color
     */
    public Border(){
        size = new RectF(0,0,0,0);
        color = new ColorRect(Color.WHITE,Color.WHITE,Color.WHITE,Color.WHITE);
    }

    /**
     * 테두리 굵기 및 색상 지정 <br>
     * Specify border thickness and color
     * @param size 테두리 굵기, thickness
     * @param color 테두리 색상, color
     */
    public Border(float size,@ColorInt int color){
        this.size = new RectF(
                size * Zoomable.getInstance().density,
                size * Zoomable.getInstance().density,
                size * Zoomable.getInstance().density,
                size * Zoomable.getInstance().density);
        this.color = new ColorRect(color, color, color, color);
    }

    /**
     * 테두리 굵기 및 색상 지정 <br>
     * Specify border thickness and color
     * @param size 테두리 굵기, thickness
     * @param color 테두리 색상, color
     */
    public Border(RectF size, ColorRect color){
        size.left *= Zoomable.getInstance().density;
        size.top *= Zoomable.getInstance().density;
        size.right *= Zoomable.getInstance().density;
        size.bottom *= Zoomable.getInstance().density;
        this.size = size;
        this.color = color;
    }


    /**
     * 테두리 굵기 및 색상 지정 <br>
     * Specify border thickness and color
     * @param size 테두리 굵기, thickness
     * @param color 테두리 색상, color
     */
    public Border(RectF size,@ColorInt int color){
        size.left *= Zoomable.getInstance().density;
        size.top *= Zoomable.getInstance().density;
        size.right *= Zoomable.getInstance().density;
        size.bottom *= Zoomable.getInstance().density;
        this.size = size;
        this.color = new ColorRect(color, color, color, color);
    }

    public void copy(@Nullable Border b){
        if(b != null){
            size = b.size;
            color = b.color;
        }
    }

    public Border setLeft(float size,@ColorInt int color){
        this.size.left = size * Zoomable.getInstance().density;
        this.color.left = color;
        return this;
    }
    public Border setTop(float size,@ColorInt int color){
        this.size.top = size * Zoomable.getInstance().density;
        this.color.top = color;
        return this;
    }
    public Border setRight(float size,@ColorInt int color){
        this.size.right = size * Zoomable.getInstance().density;
        this.color.right = color;
        return this;
    }
    public Border setBottom(float size,@ColorInt int color){
        this.size.bottom = size * Zoomable.getInstance().density;
        this.color.bottom = color;
        return this;
    }

    public void draw(Canvas canvas, float measureX, float measureY, int measureWidth, int measureHeight){
        Paint paint = new Paint();
        paint.setFlags(TextPaint.FILTER_BITMAP_FLAG | TextPaint.LINEAR_TEXT_FLAG | TextPaint.ANTI_ALIAS_FLAG);
        float gap;
        if(size.left > 0) {
            gap = size.left*0.5f;
            paint.setStrokeWidth(size.left);
            paint.setColor(color.left);
            canvas.drawLine(measureX+gap, measureY,
                    measureX+gap, measureY+measureHeight, paint);
        }
        if(size.top > 0) {
            gap = size.top*0.5f;
            paint.setStrokeWidth(size.top);
            paint.setColor(color.top);
            canvas.drawLine(measureX, measureY+gap,
                    measureX+measureWidth, measureY+gap, paint);
        }
        if(size.right > 0) {
            gap = size.right*0.5f;
            paint.setStrokeWidth(size.right);
            paint.setColor(color.right);
            canvas.drawLine(measureX+measureWidth-gap, measureY,
                    measureX+measureWidth-gap, measureY+measureHeight, paint);
        }
        if(size.bottom > 0) {
            gap = size.bottom*0.5f;
            paint.setStrokeWidth(size.bottom);
            paint.setColor(color.bottom);
            canvas.drawLine(measureX, measureY+measureHeight-gap,
                    measureX+measureWidth, measureY+measureHeight-gap, paint);
        }
    }

    @Override
    public String toString() {
        return size.toString() + ", " + color.toString();
    }

    /**
     * 왼쪽 테두리만 나타나도록 설정<br>
     * 테두리 굵기 및 색상 지정 <br>
     * Set so that only the left border appears<br>
     * Specify border thickness and color
     * @param size 테두리 굵기, thickness
     * @param color 테두리 색상, color
     */
    public static Border BorderLeft(float size,@ColorInt int color){
        return new Border(new RectF(size * Zoomable.getInstance().density,0,0, 0), new ColorRect(color, Color.WHITE, Color.WHITE, Color.WHITE));
    }
    /**
     * 위쪽 테두리만 나타나도록 설정<br>
     * 테두리 굵기 및 색상 지정 <br>
     * Set so that only the top border appears<br>
     * Specify border thickness and color
     * @param size 테두리 굵기, thickness
     * @param color 테두리 색상, color
     */
    public static Border BorderTop(float size,@ColorInt int color){
        return new Border(new RectF(0,size * Zoomable.getInstance().density,0, 0), new ColorRect(Color.WHITE, color, Color.WHITE, Color.WHITE));
    }
    /**
     * 오른쪽 테두리만 나타나도록 설정<br>
     * 테두리 굵기 및 색상 지정 <br>
     * Set so that only the right border appears<br>
     * Specify border thickness and color
     * @param size 테두리 굵기, thickness
     * @param color 테두리 색상, color
     */
    public static Border BorderRight(float size,@ColorInt int color){
        return new Border(new RectF(0,0,size * Zoomable.getInstance().density, 0), new ColorRect(Color.WHITE, Color.WHITE, color, Color.WHITE));
    }
    /**
     * 아래쪽 테두리만 나타나도록 설정<br>
     * 테두리 굵기 및 색상 지정 <br>
     * Set so that only the bottom border appears<br>
     * Specify border thickness and color
     * @param size 테두리 굵기, thickness
     * @param color 테두리 색상, color
     */
    public static Border BorderBottom(float size,@ColorInt int color){
        return new Border(new RectF(0,0,0, size * Zoomable.getInstance().density), new ColorRect(Color.WHITE, Color.WHITE, Color.WHITE, color));
    }
}
