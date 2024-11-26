package com.hangyeolee.pdf.core;

import android.graphics.RectF;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.hangyeolee.pdf.core.binary.BinarySerializer;
import com.hangyeolee.pdf.core.listener.Action;
import com.hangyeolee.pdf.core.utils.Border;
import com.hangyeolee.pdf.core.utils.Zoomable;

public class PDFGridCell extends PDFLayout{
    PDFComponent children;

    int position = -1;
    int rowSpan = 1;
    int columnSpan = 1;

    /**
     * 빌 셀을 생성합니다.<br>
     * Create empty cell
     */
    protected PDFGridCell(){
        this(PDFEmpty.build());
    }
    protected PDFGridCell(int rowSpan, int columnSpan){
        this(PDFEmpty.build(), rowSpan, columnSpan);
    }

    /**
     * 하위 구성 요소를 가지고 있는 셀을 생성합니다.
     * @param content 하위 구성 요소
     */
    protected PDFGridCell(@NonNull PDFComponent content){
        super();
        setChild(content);
    }

    /**
     * 하위 구성 요소를 가지고 있는 셀을 생성합니다.
     * @param content 하위 구성 요소
     */
    protected PDFGridCell(@NonNull PDFComponent content, int rowSpan, int columnSpan){
        super();
        if(rowSpan > 1) this.rowSpan = rowSpan;
        if(columnSpan > 1) this.columnSpan = columnSpan;
        setChild(content);
    }

    @Override
    public void measure(float x, float y) {
        super.measure(x, y);
        if(children != null) {
            // 테두리와 패딩을 제외한 실제 사용 가능한 너비 계산
            float contentHeight = Zoomable.getInstance().getContentHeight();
            // Grid에서 할당 받은 영역 내에서 하위 구성 요소 배치
            if (children instanceof PDFImage) {
                int stack = 0;
                float currentY = 0;
                for (; ; ) {
                    children.measure(0, currentY);
                    float maxW = measureWidth
                            - children.margin.left - children.margin.right;
                    float maxH = measureHeight
                            - children.margin.top - children.margin.bottom;
                    childReanchor(children, maxW, maxH);

                    // 페이지 경계 확인 및 조정
                    int currentPage = calculatePageIndex(children.measureY);
                    int endPage = calculatePageIndex(children.measureY, children.getTotalHeight());

                    if (currentPage != endPage) {
                        stack++;
                        if (stack > 2) {
                            // 스택 오버 플로우 체크
                            // 페이지 경계를 두 번 이상 넘어서게 되면
                            // 하위 구성 요소가 페이지보다 크기가 크다고 판단.
                            throw new StackOverflowError("Height of child component Too Large");
                        }

                        // 다음 페이지 시작점으로 이동
                        float newY = (endPage) * contentHeight;
                        // 변경된 위치로 다시 해당 컴포넌트 재 연산
                        currentY = pageEdgeCheck(children, newY, children.measureY);
                    } else {
                        break;
                    }
                }
            } else {
                // 하위 구성 요소 위치 측정
                children.measure(0, 0);
                if (!(children instanceof PDFLayout)) {
                    float maxW = measureWidth
                            - children.margin.left - children.margin.right;
                    float maxH = measureHeight
                            - children.margin.top - children.margin.bottom;
                    childReanchor(children, maxW, maxH);
                }
            }
        }
    }

    @Override
    public void draw(BinarySerializer serializer) {
        super.draw(serializer);
        if(children != null) {
            children.draw(serializer);
        }
    }

    /**
     * 격자 구획 에서 셀이 추가 되었을 때 위치를 가져 옵니다.
     * 격자 구획 으로 부터 셀의 위치<br>
     * columns 이 3인 Horizontal 격자 구획 에서 position 이 4라면
     * <p>row = Math.floor(4/3), column = 4 - 3 * row</p>
     * Cell's location from GridLayout<br>
     * If position is 4 in Horizontal GridLayout where columns is 3
     * <p>row = Math.floor(4/3), column = 4 - 3 * row</p>
     * @param parent 부모 컴포넌트
     * @return 자기자신
     */
    @Override
    protected PDFGridCell setParent(PDFComponent parent) {
        super.setParent(parent);
        return this;
    }

    /**
     * 셀의 위치 강제 조정<br>
     * Force Cell's Position Adjustment
     * @param position 셀의 위치
     */
    public void setPosition(int position){
        this.position = position;
    }
    public int getPosition(){return this.position;}

    /**
     * 셀의 크기는 무조건 격자 구획에서 셀의 위치와 범위에 의해서 정해진다.
     * @param width 가로 크기
     * @param height 세로 크기
     * @return
     */
    @Override
    public PDFGridCell setSize(Number width, Number height) {
        super.setSize(width, height);
        if(children != null){
            children.setSize(width, height);
        }
        return this;
    }

    @Override
    public PDFGridCell setBackgroundColor(int color) {
        super.setBackgroundColor(color);
        return this;
    }

    public PDFGridCell setChild(@NonNull PDFComponent content){
        content.setParent(this);
        children = content;
        return this;
    }

    public PDFGridCell setColumnSpan(int columnSpan){
        this.columnSpan = columnSpan;
        return this;
    }
    public PDFGridCell setRowSpan(int rowSpan){
        this.rowSpan = rowSpan;
        return this;
    }


    /**
     * 셀은 자체적으로 Margin을 가질 수 없다.<br>
     * Cells cannot have Margin on their own.
     */
    @Override
    @Deprecated
    public PDFGridCell setMargin(RectF margin) {
        return this;
    }

    /**
     * 셀은 자체적으로 Margin을 가질 수 없다.<br>
     * Cells cannot have Margin on their own.
     */
    @Override
    @Deprecated
    public PDFGridCell setMargin(float left, float top, float right, float bottom) {
        return this;
    }

    /**
     * 셀은 자체적으로 Margin을 가질 수 없다.<br>
     * Cells cannot have Margin on their own.
     */
    @Override
    @Deprecated
    public PDFGridCell setMargin(float all) {
        return this;
    }

    /**
     * 셀은 자체적으로 Margin을 가질 수 없다.<br>
     * Cells cannot have Margin on their own.
     */
    @Override
    @Deprecated
    public PDFGridCell setMargin(float horizontal, float vertical) {
        return this;
    }

    /**
     * 셀은 자체적으로 Padding 을 가질 수 없다.<br>
     * Cells cannot have Padding on their own.
     */
    @Override
    @Deprecated
    public PDFGridCell setPadding(float all) {
        return this;
    }

    /**
     * 셀은 자체적으로 Padding 을 가질 수 없다.<br>
     * Cells cannot have Padding on their own.
     */
    @Override
    @Deprecated
    public PDFGridCell setPadding(float horizontal, float vertical) {
        return this;
    }

    /**
     * 셀은 자체적으로 Padding 을 가질 수 없다.<br>
     * Cells cannot have Padding on their own.
     */
    @Override
    @Deprecated
    public PDFGridCell setPadding(RectF padding) {
        return this;
    }

    /**
     * 셀은 자체적으로 Padding 을 가질 수 없다.<br>
     * Cells cannot have Padding on their own.
     */
    @Override
    @Deprecated
    public PDFGridCell setPadding(float left, float top, float right, float bottom) {
        return this;
    }

    /**
     * 셀은 자체적으로 Border을 가질 수 없다.<br>
     * Cells cannot have Border on their own.
     */
    @Override
    @Deprecated
    public PDFGridCell setBorder(Action<Border, Border> action) {
        return this;
    }

    /**
     * 셀은 자체적으로 Border을 가질 수 없다.<br>
     * Cells cannot have Border on their own.
     */
    @Override
    @Deprecated
    public PDFGridCell setBorder(float size, @ColorInt int color) {
        return this;
    }

    /**
     * 셀은 자체적으로 Anchor을 가질 수 없다.<br>
     * Cells cannot have Anchor on their own.
     */
    @Override
    @Deprecated
    public PDFGridCell setAnchor(Integer horizontal, Integer vertical) {
        super.setAnchor(horizontal, vertical);
        return this;
    }

    public static PDFGridCell build(){return new PDFGridCell();}
    public static PDFGridCell build(int rowSpan, int columnSpan){return new PDFGridCell(rowSpan, columnSpan);}
}
