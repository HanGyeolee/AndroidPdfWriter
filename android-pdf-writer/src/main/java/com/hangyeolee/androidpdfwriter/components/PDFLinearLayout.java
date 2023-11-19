package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.hangyeolee.androidpdfwriter.utils.Border;
import com.hangyeolee.androidpdfwriter.utils.Orientation;

import java.util.ArrayList;
import com.hangyeolee.androidpdfwriter.listener.Action;
import com.hangyeolee.androidpdfwriter.utils.Zoomable;

public class PDFLinearLayout extends PDFLayout {
    @Orientation.OrientationInt
    int orientation = Orientation.Column;

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
    public void measure(float x, float y) {
        super.measure(x, y);

        int i;
        int gap = 0;
        int totalAxis = 0;
        int lastWidth = (int) (measureWidth - border.size.left - padding.left
                - border.size.right - padding.right);
        switch (orientation){
            case Orientation.Column:
                for(i = 0; i < child.size(); i++){
                    child.get(i).measure(0,totalAxis);
                    gap = child.get(i).getTotalHeight();
                    child.get(i).force(lastWidth ,null, null);
                    totalAxis += gap;
                }
                break;
            case Orientation.Row:
                int zero_count = 0;
                int maxHeight = 0;
                // 가로 길이 자동 조절
                for(i = 0; i < child.size(); i++) {
                    if(gaps.get(i) > lastWidth) gaps.set(i, 0);
                    lastWidth -= child.get(i).margin.left + child.get(i).margin.right;
                    if(lastWidth < 0) gaps.set(i, 0);
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
                    if (maxHeight < child.get(i).measureHeight) {
                        maxHeight = child.get(i).measureHeight;
                    }
                    totalAxis += gap + child.get(i).margin.left + child.get(i).margin.right;
                }
                totalAxis = 0;
                for(i = 0; i < child.size(); i++){
                    gap = gaps.get(i);
                    child.get(i).width = gap;
                    child.get(i).height = maxHeight;
                    child.get(i).measure(totalAxis,0);
                    child.get(i).force(gap, maxHeight, null);
                    totalAxis += gap + child.get(i).margin.left + child.get(i).margin.right;
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

    /**
     * 레이아웃에 자식 추가<br>
     * Add children to layout
     * @param component 자식 컴포넌트
     * @return 자기자신
     */
    @Override
    public PDFLinearLayout addChild(PDFComponent component){
        return addChild(component, 0);
    }

    /**
     * 레이아웃에 자식 추가<br>
     * Add children to layout
     * @param component 자식 컴포넌트
     * @param width 자식 컴포넌트가 차지하는 가로 길이
     * @return 자기자신
     */
    public PDFLinearLayout addChild(PDFComponent component, int width){
        if(width < 0) width = 0;
        gaps.add(Math.round(width * Zoomable.getInstance().density));
        super.addChild(component);
        return this;
    }

    /**
     * 레이아웃의 방향 설정<br>
     * Setting the orientation of the layout
     * @param orientation 방향
     * @return 자기자신
     */
    public PDFLinearLayout setOrientation(@Orientation.OrientationInt int orientation){
        this.orientation = orientation;
        return this;
    }

    @Override
    public PDFLinearLayout setSize(Float width, Float height) {
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
    public PDFLinearLayout setBorder(Action<Border, Border> action) {
        super.setBorder(action);
        return this;
    }

    @Override
    public PDFLinearLayout setAnchor(Integer vertical, Integer horizontal) {
        super.setAnchor(vertical, horizontal);
        return this;
    }
    @Override
    protected PDFLinearLayout setParent(PDFComponent parent) {
        super.setParent(parent);
        return this;
    }

    public static PDFLinearLayout build(){return new PDFLinearLayout();}
}
