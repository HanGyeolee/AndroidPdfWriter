package com.hangyeolee.androidpdfwriter.utils;

import android.graphics.Color;
import android.graphics.RectF;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import java.util.Locale;

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
        this.size = new RectF(size, size, size, size);
        this.color = new ColorRect(color, color, color, color);
    }

    /**
     * 테두리 굵기 및 색상 지정 <br>
     * Specify border thickness and color
     * @param size 테두리 굵기, thickness
     * @param color 테두리 색상, color
     */
    public Border(RectF size, ColorRect color){
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
        this.size.left = size;
        this.color.left = color;
        return this;
    }
    public Border setTop(float size,@ColorInt int color){
        this.size.top = size;
        this.color.top = color;
        return this;
    }
    public Border setRight(float size,@ColorInt int color){
        this.size.right = size;
        this.color.right = color;
        return this;
    }
    public Border setBottom(float size,@ColorInt int color){
        this.size.bottom = size;
        this.color.bottom = color;
        return this;
    }

    public void draw(StringBuilder content, float measureX, float measureY, float measureWidth, float measureHeight){
        if(canDraw()) {
            // 그래픽스 상태 저장
            content.append("q\n"); // Save graphics state

            float gap;
            // 왼쪽 테두리
            if (size.left > 0) {
                gap = size.left * 0.5f;
                setColorInPDF(content, color.left);
                setLineWidthInPDF(content, size.left);

                // 시작점 이동
                content.append(String.format(Locale.getDefault(), "%.2f %.2f m\n",
                        Zoomable.getInstance().transform2PDFWidth(measureX + gap),
                        Zoomable.getInstance().transform2PDFHeight(measureY)));  // y좌표 변환
                // 선 그리기
                content.append(String.format(Locale.getDefault(), "%.2f %.2f l\n",
                        Zoomable.getInstance().transform2PDFWidth(measureX + gap),
                        Zoomable.getInstance().transform2PDFHeight(measureY + measureHeight)));
                content.append("S\n");  // 선 그리기 실행
            }

            // 위쪽 테두리
            if (size.top > 0) {
                gap = size.top * 0.5f;
                setColorInPDF(content, color.top);
                setLineWidthInPDF(content, size.top);
                content.append(String.format(Locale.getDefault(), "%.2f %.2f m\n",
                        Zoomable.getInstance().transform2PDFWidth(measureX),
                        Zoomable.getInstance().transform2PDFHeight(measureY + gap)));
                content.append(String.format(Locale.getDefault(), "%.2f %.2f l\n",
                        Zoomable.getInstance().transform2PDFWidth(measureX + measureWidth),
                        Zoomable.getInstance().transform2PDFHeight(measureY + gap)));
                content.append("S\n");
            }

            // 오른쪽 테두리
            if (size.right > 0) {
                gap = size.right * 0.5f;
                setColorInPDF(content, color.right);
                setLineWidthInPDF(content, size.right);
                content.append(String.format(Locale.getDefault(), "%.2f %.2f m\n",
                        Zoomable.getInstance().transform2PDFWidth(measureX + measureWidth - gap),
                        Zoomable.getInstance().transform2PDFHeight(measureY)));
                content.append(String.format(Locale.getDefault(), "%.2f %.2f l\n",
                        Zoomable.getInstance().transform2PDFWidth(measureX + measureWidth - gap),
                        Zoomable.getInstance().transform2PDFHeight(measureY + measureHeight)));
                content.append("S\n");
            }

            // 아래쪽 테두리
            if (size.bottom > 0) {
                gap = size.bottom * 0.5f;
                setColorInPDF(content, color.bottom);
                setLineWidthInPDF(content, size.bottom);
                content.append(String.format(Locale.getDefault(), "%.2f %.2f m\n",
                        Zoomable.getInstance().transform2PDFWidth(measureX),
                        Zoomable.getInstance().transform2PDFHeight(measureY + measureHeight - gap)));
                content.append(String.format(Locale.getDefault(), "%.2f %.2f l\n",
                        Zoomable.getInstance().transform2PDFWidth(measureX + measureWidth),
                        Zoomable.getInstance().transform2PDFHeight(measureY + measureHeight - gap)));
                content.append("S\n");
            }

            // 그래픽스 상태 복원
            content.append("Q\n"); // Restore graphics state
        }
    }

    public boolean canDraw(){
        return size.left > 0 || size.top > 0 || size.right > 0 || size.bottom > 0;
    }

    /**
     * PDF 컨텐츠 스트림에 색상 설정
     */
    private void setColorInPDF(StringBuilder content, @ColorInt int color) {
        float red = Color.red(color) / 255f;
        float green = Color.green(color) / 255f;
        float blue = Color.blue(color) / 255f;
        float alpha = Color.alpha(color) / 255f;

        if (alpha == 1.0f) {
            content.append(String.format(Locale.getDefault(),"%.3f %.3f %.3f RG\n", red, green, blue));
        } else {
            // 알파값이 있는 경우 ExtGState 사용
            content.append(String.format(Locale.getDefault(),"/GS%.2f gs\n", alpha)); // 알파값에 해당하는 ExtGState 사용
            content.append(String.format(Locale.getDefault(),"%.3f %.3f %.3f RG\n", red, green, blue));
        }
    }

    /**
     * PDF 컨텐츠 스트림에 선 두께 설정
     */
    private void setLineWidthInPDF(StringBuilder content, float width) {
        content.append(String.format(Locale.getDefault(),"%.2f w\n", width));
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
        return new Border(new RectF(size,0,0, 0), new ColorRect(color, Color.WHITE, Color.WHITE, Color.WHITE));
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
        return new Border(new RectF(0,size,0, 0), new ColorRect(Color.WHITE, color, Color.WHITE, Color.WHITE));
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
        return new Border(new RectF(0,0,size, 0), new ColorRect(Color.WHITE, Color.WHITE, color, Color.WHITE));
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
        return new Border(new RectF(0,0,0, size), new ColorRect(Color.WHITE, Color.WHITE, Color.WHITE, color));
    }
}
