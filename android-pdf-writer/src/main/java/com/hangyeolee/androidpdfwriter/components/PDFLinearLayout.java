package com.hangyeolee.androidpdfwriter.components;

import android.graphics.RectF;

import com.hangyeolee.androidpdfwriter.binary.BinarySerializer;
import com.hangyeolee.androidpdfwriter.utils.Border;
import com.hangyeolee.androidpdfwriter.utils.Orientation;

import java.util.ArrayList;
import com.hangyeolee.androidpdfwriter.listener.Action;

public class PDFLinearLayout extends PDFLayout {
    @Orientation.OrientationInt
    int orientation = Orientation.Column;

    private final ArrayList<Float> gaps = new ArrayList<Float>();

    public PDFLinearLayout(){
        super();
        child = new ArrayList<>();
    }
    public PDFLinearLayout(int length){
        super();
        child = new ArrayList<>(length);
        for(int i = 0; i < length; i++){
            child.add(PDFEmpty.build().setParent(this));
        }
    }

    @Override
    public void measure(float x, float y) {
        super.measure(x, y);

        int i;
        float gap = 0;
        float totalAxis = 0;
        float lastWidth = measureWidth - border.size.left - padding.left
                - border.size.right - padding.right;
        switch (orientation){
            case Orientation.Column:
                for(i = 0; i < child.size(); i++){
                    child.get(i).width = lastWidth - child.get(i).margin.left - child.get(i).margin.right;
                    child.get(i).measure(0,totalAxis);
                    gap = child.get(i).getTotalHeight();
                    child.get(i).force(lastWidth ,gap, null);
                    totalAxis += gap;
                }
                break;
            case Orientation.Row:
                int zero_count = 0;
                float maxHeight = 0;
                // 가로 길이 자동 조절
                for(i = 0; i < child.size(); i++) {
                    if(gaps.get(i) > lastWidth) gaps.set(i, 0.0f);
                    lastWidth -= child.get(i).margin.left + child.get(i).margin.right;
                    if(lastWidth < 0) gaps.set(i, 0.0f);
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
                    child.get(i).measure(totalAxis,0);
                    child.get(i).force(gap, maxHeight, null);
                    totalAxis += gap + child.get(i).margin.left + child.get(i).margin.right;
                }
                break;
        }
    }


    @Override
    public StringBuilder draw(BinarySerializer serializer) {
        super.draw(serializer);

        for(int i = 0; i < child.size(); i++) {
            child.get(i).draw(serializer);
        }
        return null;
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
    public PDFLinearLayout addChild(PDFComponent component, float width){
        if(width < 0) width = 0;
        gaps.add(width);
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
    public PDFLinearLayout setMargin(RectF margin) {
        super.setMargin(margin);
        return this;
    }

    @Override
    public PDFLinearLayout setMargin(float left, float top, float right, float bottom) {
        super.setMargin(left, top, right, bottom);
        return this;
    }

    @Override
    public PDFLinearLayout setMargin(float all) {
        super.setMargin(all);
        return this;
    }

    @Override
    public PDFLinearLayout setMargin(float horizontal, float vertical) {
        super.setMargin(horizontal, vertical);
        return this;
    }

    @Override
    public PDFLinearLayout setPadding(float all) {
        super.setPadding(all);
        return this;
    }

    @Override
    public PDFLinearLayout setPadding(float horizontal, float vertical) {
        super.setPadding(horizontal, vertical);
        return this;
    }

    @Override
    public PDFLinearLayout setPadding(RectF padding) {
        super.setPadding(padding);
        return this;
    }

    @Override
    public PDFLinearLayout setPadding(float left, float top, float right, float bottom) {
        super.setPadding(left, top, right, bottom);
        return this;
    }

    @Override
    public PDFLinearLayout setBorder(Action<Border, Border> action) {
        super.setBorder(action);
        return this;
    }

    @Override
    public PDFLinearLayout setAnchor(Integer horizontal, Integer vertical) {
        super.setAnchor(horizontal, vertical);
        return this;
    }
    @Override
    protected PDFLinearLayout setParent(PDFComponent parent) {
        super.setParent(parent);
        return this;
    }

    public static PDFLinearLayout build(){return new PDFLinearLayout();}
}
