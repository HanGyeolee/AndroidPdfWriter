package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Canvas;
import android.graphics.Rect;

import androidx.annotation.IntDef;

import com.hangyeolee.androidpdfwriter.utils.Border;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PDFLinearLayout extends PDFLayout{
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Row, Column})
    public @interface Orientation {}
    /**
     * 가로
     */
    public static final int Row = 0;
    /**
     * 세로
     */
    public static final int Column = 1;

    @Orientation
    int orientation = Column;

    private final ArrayList<Integer> gaps = new ArrayList<>();

    public PDFLinearLayout(){
        super();
        child = new ArrayList<>();
    }
    public PDFLinearLayout(int length){
        super();
        child = new ArrayList<>(length);
        for(int i = 0; i < length; i++){
            child.add(null);
        }
    }

    @Override
    public void measure(int x, int y) {
        super.measure(x, y);
        int i;
        int gap = 0;
        int totalAxis = 0;
        switch (orientation){
            case Column:
                for(i = 0; i < child.size(); i++){
                    child.get(i).measure(0,totalAxis);
                    child.get(i).measureAnchor(true);
                    gap = child.get(i).getTotalHeight();
                    totalAxis += gap;
                }
                break;
            case Row:
                int zero_count = 0;
                int lastWidth = measureWidth;
                // 가로 길이 자동 조절
                for(i = 0; i < child.size(); i++) {
                    if(gaps.get(i) == 0){
                        zero_count += 1;
                    }else{
                        lastWidth -= gaps.get(i);
                    }
                }
                for(i = 0; i < child.size(); i++) {
                    if(gaps.get(i) == 0){
                        gaps.set(i, lastWidth/zero_count);
                        lastWidth -= lastWidth/zero_count;
                        zero_count -= 1;
                    }
                }

                for(i = 0; i < child.size(); i++){
                    gap = gaps.get(i);
                    child.get(i).width = gap;
                    child.get(i).measure(totalAxis,0);
                    child.get(i).measureAnchor(false);
                    totalAxis += gap;
                }
                break;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        for(int i = 0; i < child.size(); i++) {
            child.get(i).draw(canvas);
        }
    }

    @Override
    public PDFLinearLayout addChild(PDFComponent component){
        gaps.add(0);
        super.addChild(component);
        return this;
    }
    public PDFLinearLayout addChild(PDFComponent component, int gap){
        gaps.add(gap);
        super.addChild(component);
        return this;
    }

    public PDFLinearLayout setOrientation(@Orientation int orientation){
        this.orientation = orientation;
        return this;
    }

    @Override
    public PDFLinearLayout setSize(int width, int height) {
        super.setSize(width, height);
        return this;
    }

    @Override
    public PDFLinearLayout setBackgroundColor(int color) {
        super.setBackgroundColor(color);
        return this;
    }

    @Override
    public PDFLinearLayout setMargin(Rect margin) {
        super.setMargin(margin);
        return this;
    }

    @Override
    public PDFLinearLayout setMargin(int left, int top, int right, int bottom) {
        super.setMargin(left, top, right, bottom);
        return this;
    }

    @Override
    public PDFLinearLayout setPadding(Rect padding) {
        super.setPadding(padding);
        return this;
    }

    @Override
    public PDFLinearLayout setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        return this;
    }

    @Override
    public PDFLinearLayout setBorder(Function<Border, Border> action) {
        super.setBorder(action);
        return this;
    }

    @Override
    public PDFLinearLayout setAnchor(int vertical, int horizontal) {
        super.setAnchor(vertical, horizontal);
        return this;
    }

    @Override
    public PDFLinearLayout setAnchor(int axis, boolean isHorizontal) {
        super.setAnchor(axis, isHorizontal);
        return this;
    }

    @Override
    protected PDFLinearLayout setParent(PDFComponent parent) {
        super.setParent(parent);
        return this;
    }

    public static PDFLinearLayout build(){return new PDFLinearLayout();}
}
