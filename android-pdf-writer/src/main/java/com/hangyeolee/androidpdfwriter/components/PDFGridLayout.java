package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Rect;
import android.graphics.RectF;

import androidx.annotation.IntRange;

import com.hangyeolee.androidpdfwriter.exceptions.TableCellHaveNotIndexException;
import com.hangyeolee.androidpdfwriter.binary.BinarySerializer;
import com.hangyeolee.androidpdfwriter.utils.Border;

import java.util.ArrayList;
import com.hangyeolee.androidpdfwriter.listener.Action;
import com.hangyeolee.androidpdfwriter.utils.Zoomable;

public class PDFGridLayout extends PDFLayout{
    int rows, columns;

    ArrayList<Integer> span;

    final float[] gaps;
    final RectF childMargin = new RectF(0,0,0,0);

    public PDFGridLayout(int rows, int columns){
        super();
        if(rows < 1) rows = 1;
        if(columns < 1) columns = 1;
        this.rows = rows;
        this.columns = columns;
        int length = rows*columns;
        child = new ArrayList<>(length);
        span = new ArrayList<>(length);
        gaps = new float[rows];
        for(int i = 0; i < length; i++){
            child.add(PDFEmpty.build().setParent(this));
            span.add(i);
        }
    }

    @Override
    public void measure(float x, float y) {
        super.measure(x, y);

        int i, j;
        float totalHeight = 0;
        float gapWidth = 0;
        int index;

        int zero_count = 0;
        float lastWidth =  (measureWidth - border.size.left - padding.left
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

        float[] maxHeights = new float[columns];
        int columnSpanCount;
        // Cell 당 높이 계산
        for(i = 0; i < columns; i++){
            float totalWidth = 0;
            for(j = 0; j < rows; j++){
                index = i*rows + j;
                gapWidth = gaps[j];
                if(index == span.get(index)) {
                    columnSpanCount = 1;
                    // 가로 span
                    for(int xx = j+1; xx < rows; xx++){
                        if(index == span.get(i*rows + xx)){
                            gapWidth += gaps[xx] + childMargin.left + childMargin.right;
                        }else break;
                    }
                    // 세로 span 개수
                    for(int yy = i+1; yy < columns; yy++){
                        if(index == span.get((yy)*rows + j)){
                            columnSpanCount++;
                        }else break;
                    }

                    child.get(index).width = gapWidth;
                    // 자식 컴포넌트의 Margin 무시
                    child.get(i).margin.set(childMargin);
                    if(columnSpanCount == 1) {
                        child.get(index).measure(totalWidth, totalHeight);
                        if (maxHeights[i] < child.get(index).measureHeight) {
                            maxHeights[i] = child.get(index).measureHeight;
                        }
                    }
                    totalWidth += gapWidth + childMargin.left + childMargin.right;
                }else{
                    index = span.get(index);
                    int origin_x = index % rows;
                    if(origin_x == j) {
                        int origin_y = index / rows;
                        // 가로 span
                        for(int xx = j+1; xx < rows; xx++){
                            if(index == span.get(origin_y*rows + xx)){
                                gapWidth += gaps[xx] + childMargin.left + childMargin.right;
                            }else break;
                        }
                        totalWidth += gapWidth + childMargin.left + childMargin.right;
                    }
                }
            }
            totalHeight += maxHeights[i] + childMargin.top + childMargin.bottom;
            measureHeight = (totalHeight);
        }

        totalHeight = 0;
        float maxHeight;
        float maxSpanHeight = 0;
        float lastTotalHeight = 0;
        boolean repeatIfor = false;
        for(i = 0; i < columns; i++) {
            float totalWidth = 0;
            maxSpanHeight = 0;
            // 세로 Span 존재 탐지
            for(j = 0; j < rows; j++){
                index = i*rows + j;
                maxHeight = maxHeights[i];
                for(int yy = i+1; yy < columns; yy++){
                    if(index == span.get((yy)*rows + j)){
                        maxHeight += maxHeights[yy] + childMargin.top + childMargin.bottom;
                    }else break;
                }
                if(maxHeight > maxSpanHeight) maxSpanHeight = maxHeight;
            }

            // 페이지 넘기는 Grid Cell 이 있는 경우 다음 페이지로
            float zoomHeight = Zoomable.getInstance().getContentRect().height();
            float lastHeight = totalHeight + measureY;
            while(lastHeight > zoomHeight){
                lastHeight -= zoomHeight;
            }
            lastTotalHeight = totalHeight;
            if(lastHeight < zoomHeight && zoomHeight < lastHeight + maxSpanHeight + childMargin.top + childMargin.bottom){
                float gap = zoomHeight - lastHeight;
                if(i == 0) {
                    margin.top += (gap);
                    updateHeight(gap);
                    float d = 0;
                    if (parent != null)
                        d += parent.measureY + parent.border.size.top + parent.padding.top;
                    measureY = relativeY + margin.top + d;
                }
                else
                    totalHeight += gap;
            }

            for(j = 0; j < rows; j++){
                index = i*rows + j;
                gapWidth = gaps[j];
                if(index == span.get(index)) {
                    columnSpanCount = 1;
                    //Span 계산
                    maxHeight = maxHeights[i];
                    for(int xx = j+1; xx < rows; xx++){
                        if(index == span.get(i*rows + xx)){
                            gapWidth += gaps[xx] + childMargin.left + childMargin.right;
                        }else break;
                    }
                    for(int yy = i+1; yy < columns; yy++){
                        if(index == span.get((yy)*rows + j)){
                            columnSpanCount++;
                            maxHeight += maxHeights[yy] + childMargin.top + childMargin.bottom;
                        }else break;
                    }

                    child.get(index).width = gapWidth;

                    //measure
                    child.get(index).measure(totalWidth, totalHeight);

                    //Span 이후 span 된 컴포넌트의 높이가 주어진 높이보다 클 경우
                    if(child.get(index).height > maxHeight){
                        float gapHeight = child.get(index).height - maxHeight;

                        zero_count = columnSpanCount;
                        for(int yy = i; yy < i+columnSpanCount; yy++) {
                            maxHeights[yy] += gapHeight / zero_count;
                            gapHeight -= gapHeight / zero_count;
                            zero_count -= 1;
                        }
                        repeatIfor = true;
                        break;
                    }

                    child.get(index).force(gapWidth, maxHeight, childMargin);
                    totalWidth += gapWidth + childMargin.left + childMargin.right;
                }else{
                    index = span.get(index);
                    int origin_x = index % rows;
                    if(origin_x == j) {
                        int origin_y = index / rows;
                        // 가로 span
                        for(int xx = j+1; xx < rows; xx++){
                            if(index == span.get(origin_y*rows + xx)){
                                gapWidth += gaps[xx] + childMargin.left + childMargin.right;
                            }else break;
                        }
                        totalWidth += gapWidth + childMargin.left + childMargin.right;
                    }
                }
            }

            if(repeatIfor){
                i--;
                totalHeight = lastTotalHeight;
                repeatIfor = false;
                continue;
            }

            totalHeight += maxHeights[i] + childMargin.top + childMargin.bottom;
        }
        measureHeight = (totalHeight + border.size.top + border.size.bottom + padding.top + padding.bottom);
    }

    @Override
    public StringBuilder draw(BinarySerializer serializer) {
        super.draw(serializer);

        for(int i = 0; i < child.size(); i++) {
            // Span에 의해 늘어난 만큼 관련 없는 부분은 draw 하지 않는 다.
            if(i == span.get(i)) {
                child.get(i).draw(serializer);
            }
        }

        return null;
    }

    /**
     * 자식 컴포넌트가 차지하는 가로 길이 설정
     * @param xIndex 설정할 Row
     * @param width 가로 길이
     * @return 자기자신
     */
    public PDFGridLayout setChildWidth(int xIndex, float width){
        if(width < 0) width = 0;
        if(xIndex < gaps.length)
            gaps[xIndex] = width;
        return this;
    }

    public PDFGridLayout setChildSpan(int x, int y, @IntRange(from = 1) int rowSpan, @IntRange(from = 1) int columnSpan)
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
            throw new ArrayIndexOutOfBoundsException("Span Size is Out of Bounds");
        }
        return this;
    }


    public PDFGridLayout setChildMargin(Rect margin) {
        this.childMargin.set(margin);
        return this;
    }

    public PDFGridLayout setChildMargin(int left, int top, int right, int bottom) {
        this.childMargin.set(left, top, right, bottom);
        return this;
    }

    public PDFGridLayout setChildMargin(int all) {
        return setChildMargin(all, all, all, all);
    }

    public PDFGridLayout setChildMargin(int horizontal, int vertical) {
        return setChildMargin(horizontal, vertical, horizontal, vertical);
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
    public PDFGridLayout addChild(int x, int y, @IntRange(from = 1) int rowSpan, @IntRange(from = 1) int columnSpan, PDFComponent component)
            throws ArrayIndexOutOfBoundsException{
        int index = y*rows + x;
        component.setParent(this);
        if(index < child.size() && (y+columnSpan-1)*rows + x+rowSpan-1 < child.size()) {
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
    public PDFGridLayout setMargin(RectF margin) {
        super.setMargin(margin);
        return this;
    }

    @Override
    public PDFGridLayout setMargin(float left, float top, float right, float bottom) {
        super.setMargin(left, top, right, bottom);
        return this;
    }

    @Override
    public PDFGridLayout setMargin(float all) {
        super.setMargin(all);
        return this;
    }

    @Override
    public PDFGridLayout setMargin(float horizontal, float vertical) {
        super.setMargin(horizontal, vertical);
        return this;
    }

    @Override
    public PDFGridLayout setPadding(float all) {
        super.setPadding(all);
        return this;
    }

    @Override
    public PDFGridLayout setPadding(float horizontal, float vertical) {
        super.setPadding(horizontal, vertical);
        return this;
    }

    @Override
    public PDFGridLayout setPadding(RectF padding) {
        super.setPadding(padding);
        return this;
    }

    @Override
    public PDFGridLayout setPadding(float left, float top, float right, float bottom) {
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
