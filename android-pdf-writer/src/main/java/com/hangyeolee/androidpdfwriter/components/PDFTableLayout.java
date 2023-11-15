package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.hangyeolee.androidpdfwriter.exceptions.TableCellHaveNotIndexException;
import com.hangyeolee.androidpdfwriter.utils.Border;

import java.util.ArrayList;
import java.util.function.Function;

public class PDFTableLayout extends PDFLayout{
    int rows, columns;

    final int[] gaps;
    final Rect childMargin = new Rect(0,0,0,0);

    public PDFTableLayout(int rows, int columns){
        super();
        if(rows < 1) rows = 1;
        if(columns < 1) columns = 1;
        this.rows = rows;
        this.columns = columns;
        int length = rows*columns;
        child = new ArrayList<>(length);
        gaps = new int[rows];
        for(int i = 0; i < length; i++){
            child.add(new PDFEmpty());
        }
    }

    @Override
    public void measure(float x, float y) {
        super.measure(x, y);
        int i, j;
        int totalHeight = 0;
        int gapWidth = 0;
        int index;

        int zero_count = 0;
        int lastWidth = (int) (measureWidth - border.size.left - padding.left
                - border.size.right - padding.right);
        // 가로 길이 자동 조절
        for(i = 0; i < gaps.length ; i++) {
            if(gaps[i] > lastWidth) gaps[i] = 0;
            lastWidth -= childMargin.left + childMargin.right;
            if(lastWidth < 0) gaps[i] = 0;
            if(gaps[i] == 0){
                zero_count += 1;
            }else{
                lastWidth -= gaps[i];
            }
        }
        for(i = 0; i < gaps.length; i++) {
            if(gaps[i] == 0){
                gaps[i] = lastWidth/zero_count;
                lastWidth -= lastWidth/zero_count;
                zero_count -= 1;
            }
        }

        for(i = 0; i < columns; i++){
            int totalWidth = 0;
            int maxHeight = 0;
            for(j = 0; j < rows; j++){
                index = i*rows + j;
                gapWidth = gaps[j];
                child.get(index).width = gapWidth;
                // 자식 컴포넌트의 Margin 무시
                child.get(i).margin.set(childMargin);
                child.get(index).measure(totalWidth, totalHeight);
                if (maxHeight < child.get(index).getTotalHeight()) {
                    maxHeight = child.get(index).getTotalHeight();
                }
                totalWidth += gapWidth + childMargin.left + childMargin.right;
            }
            totalHeight += maxHeight;
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
     * 자식 컴포넌트가 차지하는 가로 길이 설정
     * @param xIndex 설정할 Row
     * @param width 가로 길이
     * @return 자기자신
     */
    public PDFTableLayout setChildWidth(int xIndex, int width){
        if(width < 0) width = 0;
        if(xIndex < gaps.length)
            gaps[xIndex] = width;
        return this;
    }

    @Override
    @Deprecated
    public PDFTableLayout addChild(PDFComponent component) throws TableCellHaveNotIndexException {
        throw new TableCellHaveNotIndexException();
    }

    /**
     * 레이아웃에 자식 추가
     * @param component 자식 컴포넌트
     * @param x x위치, 0부터
     * @param y y위치, 0부터
     * @return 자기자신
     */
    public PDFTableLayout addChild(int x, int y, PDFComponent component)
            throws ArrayIndexOutOfBoundsException{
        int index = y*rows + x;
        component.setParent(this);
        if(index < child.size())
            child.set(index, component);
        else
            throw new ArrayIndexOutOfBoundsException();
        return this;
    }

    @Override
    public PDFTableLayout setSize(int width, int height) {
        super.setSize(width, height);
        return this;
    }

    @Override
    public PDFTableLayout setBackgroundColor(int color) {
        super.setBackgroundColor(color);
        return this;
    }

    @Override
    public PDFTableLayout setMargin(Rect margin) {
        super.setMargin(margin);
        return this;
    }

    public PDFTableLayout setChildMargin(Rect margin) {
        this.childMargin.set(margin);
        return this;
    }

    @Override
    public PDFTableLayout setMargin(int left, int top, int right, int bottom) {
        super.setMargin(left, top, right, bottom);
        return this;
    }

    @Override
    public PDFTableLayout setPadding(Rect padding) {
        super.setPadding(padding);
        return this;
    }

    @Override
    public PDFTableLayout setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        return this;
    }

    @Override
    public PDFTableLayout setBorder(Function<Border, Border> action) {
        super.setBorder(action);
        return this;
    }

    @Override
    public PDFTableLayout setAnchor(int vertical, int horizontal) {
        super.setAnchor(vertical, horizontal);
        return this;
    }

    @Override
    public PDFTableLayout setAnchor(int axis, boolean isHorizontal) {
        super.setAnchor(axis, isHorizontal);
        return this;
    }

    @Override
    protected PDFTableLayout setParent(PDFComponent parent) {
        super.setParent(parent);
        return this;
    }

    public static PDFTableLayout build(int rows, int columns){return new PDFTableLayout(rows, columns);}
}
