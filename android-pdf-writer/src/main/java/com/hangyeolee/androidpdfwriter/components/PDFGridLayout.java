package com.hangyeolee.androidpdfwriter.components;

import android.graphics.RectF;

import androidx.annotation.ColorInt;

import com.hangyeolee.androidpdfwriter.binary.BinaryConverter;
import com.hangyeolee.androidpdfwriter.binary.BinarySerializer;
import com.hangyeolee.androidpdfwriter.exceptions.CellOutOfGridLayoutException;
import com.hangyeolee.androidpdfwriter.exceptions.LayoutSizeException;
import com.hangyeolee.androidpdfwriter.listener.Action;
import com.hangyeolee.androidpdfwriter.utils.Border;
import com.hangyeolee.androidpdfwriter.utils.Orientation;
import com.hangyeolee.androidpdfwriter.utils.Zoomable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class PDFGridLayout extends PDFLayout{
    private final ArrayList<PDFGridCell> cells = new ArrayList<>();
    private final Map<Integer, Set<Integer>> occupancyGrid = new HashMap<>();

    @Orientation.OrientationInt
    private int orientation = Orientation.Horizontal;
    private final ArrayList<Float> weights = new ArrayList<>();
    private int count = 1;

    private int currentRow = 0;
    private int currentColumn = 0;
    private int maxRow = 0;
    private int maxColumn = 0;

    protected PDFGridLayout(int count) {
        if(count <= 0) throw new LayoutSizeException();
        this.count = count;
        for(int i = 0; i < count; i++){
            this.weights.add(1.0f);
        }
    }

    @Override
    public void measure(float x, float y) {
        super.measure(x, y);

        for(int i = 0; i < cells.size(); i++){
            cells.get(i).measure(0,0);
        }

        // weight 총합 계산
        float totalWeight = 0;
        for (int i = 0; i < count; i++) {
            totalWeight += weights.get(i);
        }

        if(orientation == Orientation.Vertical) {
            measureVertical(totalWeight);
        } else {
            measureHorizontal(totalWeight);
        }
    }

    private void measureVertical(float totalWeight){
        // 행의 높이 계산 (weight 기반)
        float availableHeight = measureHeight - border.size.top - padding.top
                - border.size.bottom - padding.bottom;
        float availableWidth = measureWidth - border.size.left - padding.left
                - border.size.right - padding.right;
        float[] rowHeights = new float[count];
        for (int i = 0; i < count; i++) {
            rowHeights[i] = (availableHeight * weights.get(i)) / totalWeight;
        }

        for (int i = 0; i < cells.size(); i++) {
            PDFGridCell cell = cells.get(i);
            cell.setParent(this);
            int position = cell.getPosition();
            int column = position/count;
            int row = position % count;

            // X 좌표 계산 (column 위치에 따라)
            float relativeX = availableWidth * column/maxColumn;

            // Y 좌표 계산 (row 위치에 따라)
            float relativeY = 0;
            for (int j = 0; j < row; j++) {
                relativeY += rowHeights[j];
            }

            // cell 너비 계산
            float cellWidth = availableWidth * cell.columnSpan/maxColumn;

            // cell 높이 계산 (rowSpan 고려)
            float cellHeight = 0;
            for (int j = row; j < row + cell.rowSpan; j++) {
                cellHeight += rowHeights[j];
            }

            // 최종 크기 설정 및 측정
            cell.setSize(cellWidth, cellHeight);
            cell.measure(relativeX, relativeY);
        }
    }

    private void measureHorizontal(float totalWeight){
        // 각 열의 너비 계산 (weight 기반)
        float availableWidth = measureWidth - border.size.left - padding.left
                - border.size.right - padding.right;
        float[] columnWidths = new float[count];
        for (int i = 0; i < count; i++) {
            columnWidths[i] = (availableWidth * weights.get(i)) / totalWeight;
        }

        // 각 행의 높이를 계산하기 위한 맵
        // key: row 번호, value: 해당 행의 최대 높이
        Map<Integer, Float> rowHeights = new HashMap<>();

        // 1단계: 각 행의 최대 높이 계산
        for (PDFGridCell cell : cells) {
            int position = cell.getPosition();
            int row = position / count;
            int column = position % count;

            // cell의 너비 계산 (columnSpan 고려)
            float cellWidth = 0;
            for (int i = column; i < column + cell.columnSpan; i++) {
                cellWidth += columnWidths[i];
            }

            // cell 측정을 통해 높이 구하기
            cell.setSize(cellWidth, null);
            cell.measure(0, 0);

            // rowSpan을 고려하여 각 행의 필요한 높이 계산
            float heightPerRow = cell.measureHeight / cell.rowSpan;
            for (int i = row; i < row + cell.rowSpan; i++) {
                Float height = rowHeights.get(i);
                if(height == null){
                    height = 0.0f;
                }
                if(heightPerRow > height){
                    rowHeights.put(i, heightPerRow);
                }
            }
        }

        for (int i = 0; i < cells.size(); i++) {
            PDFGridCell cell = cells.get(i);
            cell.setParent(this);
            int position = cell.getPosition();
            int row = position/count;
            int column = position % count;

            // X 좌표 계산 (column 위치에 따라)
            float relativeX = 0;
            for (int j = 0; j < column; j++) {
                relativeX += columnWidths[j];
            }

            // Y 좌표 계산 (row 위치에 따라)
            float relativeY = 0;
            for (int j = 0; j < row; j++) {
                Float height = rowHeights.get(j);
                if(height == null) height = 0.0f;
                relativeY += height;
            }

            // cell 높이 계산 (rowSpan 고려)
            float cellHeight = 0;
            for (int j = row; j < row + cell.rowSpan; j++) {
                Float height = rowHeights.get(j);
                if(height == null) height = 0.0f;
                cellHeight += height;
            }

            // 최종 크기 설정 및 측정
            cell.setSize(null, cellHeight);
            cell.measure(relativeX, relativeY);
        }
    }

    @Override
    public StringBuilder draw(BinarySerializer serializer) {
        super.draw(serializer);
        float pageHeight = Zoomable.getInstance().getContentHeight();

        StringBuilder content;
        for(int i = 0; i < cells.size(); i++) {
            PDFComponent child = cells.get(i);

            // 현재 컴포넌트가 위치한 페이지 구하기
            int currentPage = calculatePageIndex(child.measureY, child.measureHeight);
            content = serializer.getPage(currentPage);

            // 첫 페이지의 Y 좌표 조정
            float currentY = child.measureY - currentPage * pageHeight;
            if(currentY < 0) currentY = 0;

            // 현재 페이지 내에서의 좌표 계산
            float x = Zoomable.getInstance().transform2PDFWidth(
                    child.measureX
            );
            float y = Zoomable.getInstance().transform2PDFHeight(
                    currentY + child.measureHeight
            );

            // 그래픽스 상태 저장
            PDFGraphicsState.save(content);

            // 클리핑 영역 설정 - 컴포넌트의 전체 영역
            // W 클리핑 패스 설정
            // n 패스를 그리지 않고 클리핑만 적용
            content.append(String.format(Locale.US,
                    "%s %s %s %s re W n\r\n",
                    BinaryConverter.formatNumber(x),
                    BinaryConverter.formatNumber(y),
                    BinaryConverter.formatNumber(
                            child.measureWidth),
                    BinaryConverter.formatNumber(
                            child.measureHeight))
            );

            // 하위 구성 요소 그리기
            child.draw(serializer);

            // 그래픽스 상태 복원
            PDFGraphicsState.restore(content);
        }
        return null;
    }

    /**
     * 레이아웃에 자식 추가<br>
     * Add children to layout<br>
     * @param cell 하위 셀 요소
     * @return 자기자신
     */
    public PDFGridLayout addCell(PDFGridCell cell){
        int position;
        if(orientation == Orientation.Vertical){
            position = getCellPositionV(cell);
        }
        else {
            position = getCellPositionH(cell);
        }
        cell.setPosition(position);
        cells.add(cell);
        return this;
    }

    /**
     * 구획에 자식 추가<br>특정 행과 열에 강제적으로 자식 구성 요소를 배치합니다.<br>
     * 셀의 범위에 의해서 다른 구성 요소와 겹쳐질 수 있습니다.<br>
     * Add children to layout<br>Forced to place child components in specific rows and columns.<br>
     * They can be overlaid with other components by the Span in the cell.<br>
     * @param cell 하위 셀 요소
     * @param row 격자 요소에서의 행
     * @param column 격자 요소에서의 열
     * @return 자기자신
     */
    public PDFGridLayout addCell(int row, int column, PDFGridCell cell){
        int position = getCellPosition(cell, row, column);
        cell.setPosition(position);
        cells.add(cell);
        return this;
    }

    /**
     * 이전에 설정한 가중치 값들을 지우고, 지정한 가중치로 변경합니다.<br>
     * {@link #count}보다 많은 가중치는 무시됩니다. {@link #count}보다 적은 가중치를 입력하면 나머지를 1.0f으로 변경합니다.<br>
     * Clear the previously set weights values and change them to the specified weights.<br>
     * Weights greater than the number of {@link #count} are ignored. If you enter weights less than the number of {@link #count}, change the rest to 1.0f.<br>
     * @param weights 가중치
     * @return
     */
    public PDFGridLayout setWeights(float... weights){
        this.weights.clear();
        for(float weight : weights){
            this.weights.add(weight);
        }
        while(count > this.weights.size()){
            this.weights.add(1.0f);
        }
        return this;
    }


    protected boolean checkOccupancyGridV(int row, int column, int rowSpan, int columnSpan){
        if(row + rowSpan > maxRow) return false;
        for(int j = column; j < column + columnSpan; j++) {
            Set<Integer> rows = occupancyGrid.get(j);
            if (rows != null) {
                int max = row + rowSpan;
                for (int i = row; i < max; i++) {
                    if (rows.contains(i)) return false;
                }
            }
        }
        // 무조건 가능
        return true;
    }
    protected boolean checkOccupancyGridH(int row, int column, int rowSpan, int columnSpan) {
        if (column + columnSpan > maxColumn) return false;
        for(int j = row; j < row + rowSpan; j++) {
            Set<Integer> columns = occupancyGrid.get(j);
            if (columns != null) {
                int max = column + columnSpan;
                for (int i = column; i < max; i++) {
                    if (columns.contains(i)) return false;
                }
            }
        }
        // 무조건 가능
        return true;
    }
    protected void updateOccupancyGridV(int position, int rowSpan){
        int column = position / count;
        int row = position % count;
        Set<Integer> rows = occupancyGrid.get(column);
        if(rows == null){
            rows = new HashSet<>();
            occupancyGrid.put(column, rows);
        }
        int max = row + rowSpan;
        for(int i = row; i < max; i++){
            rows.add(i);
        }
    }
    protected void updateOccupancyGridH(int position, int columnSpan){
        int row = position / count;
        int column = position % count;
        Set<Integer> columns = occupancyGrid.get(row);
        if(columns == null){
            columns = new HashSet<>();
            occupancyGrid.put(row, columns);
        }
        int max = column + columnSpan;
        for(int i = column; i < max; i++){
            columns.add(i);
        }
    }

    /**
     * 격자 구획에 셀을 자동으로 배치합니다.
     * @param cell 자식 셀
     * @return
     */
    protected int getCellPositionV(PDFGridCell cell){
        int position;
        if(checkOccupancyGridV(currentRow, currentColumn, cell.rowSpan, cell.columnSpan)){
            position = currentRow + count * currentColumn;
            currentRow += cell.columnSpan - 1;
            if(currentRow == maxRow){
                currentColumn += currentRow / count;
                currentRow = currentRow % count;
                maxColumn = currentColumn;
            }
        }
        else{
            // 현재 위치에 넣을 수 없음.
            currentRow++;
            if(currentRow == maxRow){
                currentColumn += currentRow / count;
                currentRow = currentRow % count;
                maxColumn = currentColumn;
            }
            position = getCellPositionV(cell);
        }
        updateOccupancyGridV(position, cell.rowSpan);

        return position;
    }
    protected int getCellPositionH(PDFGridCell cell){
        int position;
        if(checkOccupancyGridH(currentRow, currentColumn, cell.rowSpan, cell.columnSpan)){
            position = currentColumn + count * currentRow;
            currentColumn += cell.columnSpan - 1;
            if(currentColumn == maxColumn){
                currentRow += currentColumn / count;
                currentColumn = currentColumn % count;
                maxRow = currentRow;
            }
        }
        else{
            // 현재 위치에 넣을 수 없음.
            currentColumn++;
            if(currentColumn == maxColumn){
                currentRow += currentColumn / count;
                currentColumn = currentColumn % count;
                maxRow = currentRow;
            }
            position = getCellPositionH(cell);
        }
        updateOccupancyGridH(position, cell.columnSpan);

        return position;
    }

    /**
     * 격자 구획에 셀을 강제로 배치합니다.
     * @param cell 자식 셀
     * @param row 행
     * @param column 열
     * @return
     */
    private int getCellPosition(PDFGridCell cell, int row, int column){
        int position;
        if(row > maxRow) maxRow = row;
        if(column > maxColumn) maxColumn = column;
        if(orientation == Orientation.Vertical){
            position = row + count * column;
            row += cell.rowSpan - 1;
            if(row == maxRow){
                column += row / count;
                if(column > maxColumn)
                    maxColumn = column;
            } else if (row > maxRow) {
                throw new CellOutOfGridLayoutException(
                        "The span of the cells exceeded the layout range.");
            }
        }
        else {
            position = column + count * row;
            column += cell.columnSpan - 1;
            if(column == maxColumn){
                row += column / count;
                if(row > maxRow)
                    maxRow = row;
            } else if (column > maxColumn)  {
                throw new CellOutOfGridLayoutException(
                        "The span of the cells exceeded the layout range.");
            }
        }
        return position;
    }

    /**
     * 레이아웃의 방향 설정<br>
     * Setting the orientation of the layout
     * @return 자기자신
     */
    protected PDFGridLayout setHorizontal(){
        this.orientation = Orientation.Horizontal;
        maxColumn = count;
        return this;
    }
    protected PDFGridLayout setVertical(float height){
        if(height <= 0)
            throw new LayoutSizeException("Vertical GridLayout must have a height.");
        this.height = height;
        this.orientation = Orientation.Vertical;
        maxRow = count;
        return this;
    }

    @Override
    public PDFGridLayout setSize(Number width, Number height) {
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
    public PDFGridLayout setBorder(float size, @ColorInt int color) {
        super.setBorder(size, color);
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

    public static PDFGridLayout horizontal(int columnCount){return new PDFGridLayout(columnCount).setHorizontal();}
    public static PDFGridLayout vertical(int rowCount, float height){return new PDFGridLayout(rowCount).setVertical(height);}
}
