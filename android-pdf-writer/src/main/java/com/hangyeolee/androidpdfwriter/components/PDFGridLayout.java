package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.hangyeolee.androidpdfwriter.exceptions.TableCellHaveNotIndexException;
import com.hangyeolee.androidpdfwriter.utils.Border;

import java.util.ArrayList;
import com.hangyeolee.androidpdfwriter.listener.Action;
import com.hangyeolee.androidpdfwriter.utils.Zoomable;

public class PDFGridLayout extends PDFLayout{
    int rows, columns;

    ArrayList<Integer> span;

    final int[] gaps;
    final Rect childMargin = new Rect(0,0,0,0);

    public PDFGridLayout(int rows, int columns){
        super();
        if(rows < 1) rows = 1;
        if(columns < 1) columns = 1;
        this.rows = rows;
        this.columns = columns;
        int length = rows*columns;
        child = new ArrayList<>(length);
        span = new ArrayList<>(length);
        gaps = new int[rows];
        for(int i = 0; i < length; i++){
            child.add(PDFEmpty.build().setParent(this));
            span.add(i);
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

        int[] maxHeights = new int[columns];
        int rowSpanCount;
        int columnSpanCount;
        for(i = 0; i < columns; i++){
            int totalWidth = 0;
            for(j = 0; j < rows; j++){
                index = i*rows + j;
                if(index == span.get(index)) {
                    rowSpanCount = 1;
                    columnSpanCount = 1;
                    for(int xx = j+1; xx < rows; xx++){
                        if(index == span.get(i*rows + xx)){
                            rowSpanCount++;
                        }else break;
                    }
                    gapWidth = gaps[j];
                    for(int xx = j+1; xx < j+rowSpanCount; xx++){
                        gapWidth += gaps[xx] + childMargin.left + childMargin.right;
                    }
                    for(int yy = i+1; yy < columns; yy++){
                        if(index == span.get((yy)*rows + j)){
                            columnSpanCount++;
                        }else break;
                    }
                    child.get(index).width = gapWidth;
                    // 자식 컴포넌트의 Margin 무시
                    child.get(i).margin.set(childMargin);
                    child.get(index).measure(totalWidth, totalHeight);
                    if (columnSpanCount == 1 && maxHeights[i] < child.get(index).measureHeight) {
                        maxHeights[i] = child.get(index).measureHeight;
                    }
                    totalWidth += gapWidth + childMargin.left + childMargin.right;
                }
            }
            totalHeight += maxHeights[i] + childMargin.top + childMargin.bottom;
            measureHeight = totalHeight;
        }
        totalHeight = 0;
        for(i = 0; i < columns; i++) {
            int totalWidth = 0;
            for(j = 0; j < rows; j++){
                index = i*rows + j;
                if(index == span.get(index)) {
                    rowSpanCount = 1;
                    columnSpanCount = 1;
                    int maxHeight = maxHeights[i];
                    for(int xx = j+1; xx < rows; xx++){
                        if(index == span.get(i*rows + xx)){
                            rowSpanCount++;
                        }else break;
                    }
                    gapWidth = gaps[j];
                    for(int xx = j+1; xx < j+rowSpanCount; xx++){
                        gapWidth += gaps[xx] + childMargin.left + childMargin.right;
                    }
                    for(int yy = i+1; yy < columns; yy++){
                        if(index == span.get((yy)*rows + j)){
                            columnSpanCount++;
                        }else break;
                    }
                    for(int yy = i+1; yy < i+columnSpanCount; yy++){
                        maxHeight += maxHeights[yy] + childMargin.top + childMargin.bottom;
                    }
                    child.get(index).width = gapWidth;
                    child.get(index).height = maxHeight;
                    child.get(index).measure(totalWidth, totalHeight);

                    if(child.get(index).height > maxHeight){
                        int gapHeight = child.get(index).height - maxHeight;

                        zero_count = columnSpanCount;
                        for(int yy = i; yy < i+columnSpanCount; yy++) {
                            maxHeights[yy] += gapHeight / zero_count;
                            gapHeight -= gapHeight / zero_count;
                            zero_count -= 1;
                        }
                        j = -1;
                        totalWidth = 0;
                        continue;
                    }

                    child.get(index).force(gapWidth, maxHeight, childMargin);
                    totalWidth += gapWidth + childMargin.left + childMargin.right;
                }
            }
            totalHeight += maxHeights[i] + childMargin.top + childMargin.bottom;
        }
        measureHeight = (int) (totalHeight + border.size.top + border.size.bottom + padding.top + padding.bottom);
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
    public PDFGridLayout setChildWidth(int xIndex, int width){
        if(width < 0) width = 0;
        if(xIndex < gaps.length)
            gaps[xIndex] = Math.round(width * Zoomable.getInstance().density);
        return this;
    }

    public PDFGridLayout setChildSpan(int x, int y, int rowSpan, int columnSpan)
            throws ArrayIndexOutOfBoundsException{
        int index = y*rows + x;
        if(index < child.size() && (y+columnSpan-1)*rows + x+rowSpan-1 < child.size()) {
            int spanIdx;
            for(int i = 0; i < columnSpan; i++){
                for(int j = 0; j < rowSpan; j++){
                    spanIdx = (y+i)*rows + x+j;
                    span.set(spanIdx, index);
                }
            }
        }
        else {
            throw new ArrayIndexOutOfBoundsException();
        }
        return this;
    }

    @Override
    @Deprecated
    public PDFGridLayout addChild(PDFComponent component) throws TableCellHaveNotIndexException {
        throw new TableCellHaveNotIndexException();
    }

    /**
     * 레이아웃에 자식 추가
     * @param component 자식 컴포넌트
     * @param x x위치, 0부터
     * @param y y위치, 0부터
     * @return 자기자신
     */
    public PDFGridLayout addChild(int x, int y, PDFComponent component)
            throws ArrayIndexOutOfBoundsException{
        return addChild(x, y, 1, 1, component);
    }

    /**
     * 레이아웃에 자식 추가
     * @param component 자식 컴포넌트
     * @param x x위치, 0부터
     * @param y y위치, 0부터
     * @return 자기자신
     */
    public PDFGridLayout addChild(int x, int y, int rowSpan, int columnSpan, PDFComponent component)
            throws ArrayIndexOutOfBoundsException{
        int index = y*rows + x;
        component.setParent(this);
        if(index < child.size() && (columnSpan-1)*rows + rowSpan-1 < child.size()) {
            child.set(index, component);
        }
        else {
            throw new ArrayIndexOutOfBoundsException();
        }
        return setChildSpan(x,y,rowSpan,columnSpan);
    }

    @Override
    public PDFGridLayout setSize(Float width, Float height) {
        super.setSize(width, height);
        return this;
    }

    @Override
    public PDFGridLayout setBackgroundColor(int color) {
        super.setBackgroundColor(color);
        return this;
    }

    @Override
    public PDFGridLayout setMargin(Rect margin) {
        super.setMargin(margin);
        return this;
    }

    public PDFGridLayout setChildMargin(Rect margin) {
        this.childMargin.set(margin);
        return this;
    }

    @Override
    public PDFGridLayout setMargin(int left, int top, int right, int bottom) {
        super.setMargin(left, top, right, bottom);
        return this;
    }

    @Override
    public PDFGridLayout setPadding(Rect padding) {
        super.setPadding(padding);
        return this;
    }

    @Override
    public PDFGridLayout setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        return this;
    }

    @Override
    public PDFGridLayout setBorder(Action<Border, Border> action) {
        super.setBorder(action);
        return this;
    }

    @Override
    public PDFGridLayout setAnchor(Integer horizontal, Integer vertical) {
        super.setAnchor(horizontal, vertical);
        return this;
    }

    @Override
    protected PDFGridLayout setParent(PDFComponent parent) {
        super.setParent(parent);
        return this;
    }

    public static PDFGridLayout build(int rows, int columns){return new PDFGridLayout(rows, columns);}
}
