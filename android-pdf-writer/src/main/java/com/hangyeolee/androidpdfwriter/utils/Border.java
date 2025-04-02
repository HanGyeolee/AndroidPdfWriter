package com.hangyeolee.androidpdfwriter.utils;

import android.graphics.Color;
import android.graphics.RectF;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.hangyeolee.androidpdfwriter.binary.BinaryConverter;

import java.util.Locale;

public class Border {
    private static final float half = 257f/512f;
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
            size = new RectF(b.size);
            color = new ColorRect(b.color);
        }
    }

    public Border setBorder(float size, @ColorInt int color){
        this.size.set(size, size, size, size);
        this.color.set(color, color, color, color);
        return this;
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

    int currentColor = Color.TRANSPARENT;
    String currentWidth;
    public void draw(PageLayout pageLayout, StringBuilder content, float measureX, float measureY, float measureWidth, float measureHeight){
        if(canDraw()) {
            // 그래픽스 상태 저장
            content.append("q\r\n"); // Save graphics state
            currentColor = Color.TRANSPARENT;
            currentWidth = "";

            float gap;
            if(allSame()){
                gap = size.left * half;
                setColorInPDF(content, color.left);
                setLineWidthInPDF(content, BinaryConverter.formatNumber(size.left, 2));

                // PDF 좌표계로 변환 (좌하단 기준)
                float pdfX = pageLayout.transform2PDFWidth(measureX + gap);
                float pdfY = pageLayout.transform2PDFHeight(measureY + measureHeight - gap);

                // PDF 컨텐츠 스트림에 사각형 그리기
                content.append(String.format(Locale.getDefault(), "%s %s %s %s re\r\n",
                        BinaryConverter.formatNumber(pdfX),
                        BinaryConverter.formatNumber(pdfY),
                        BinaryConverter.formatNumber(measureWidth - size.left),
                        BinaryConverter.formatNumber(measureHeight - size.left)
                ));

                content.append("S\r\n");  // 선 그리기 실행
            }else
            {
                // 왼쪽 테두리
                if (size.left > 0) {
                    gap = size.left * half;
                    setColorInPDF(content, color.left);
                    setLineWidthInPDF(content, BinaryConverter.formatNumber(size.left, 2));

                    String pdfx = BinaryConverter.formatNumber(pageLayout.transform2PDFWidth(measureX + gap));
                    // 시작점 이동
                    content.append(String.format(Locale.getDefault(), "%s %s m\r\n",
                            pdfx,
                            BinaryConverter.formatNumber(pageLayout.transform2PDFHeight(measureY))));  // y좌표 변환
                    // 선 그리기
                    content.append(String.format(Locale.getDefault(), "%s %s l\r\n",
                            pdfx,
                            BinaryConverter.formatNumber(pageLayout.transform2PDFHeight(measureY + measureHeight))));
                    content.append("S\r\n");  // 선 그리기 실행
                }

                // 위쪽 테두리
                if (size.top > 0) {
                    gap = size.top * half;
                    setColorInPDF(content, color.top);
                    setLineWidthInPDF(content, BinaryConverter.formatNumber(size.top, 2));

                    String pdfy = BinaryConverter.formatNumber(pageLayout.transform2PDFHeight(measureY + gap));
                    content.append(String.format(Locale.getDefault(), "%s %s m\r\n",
                            BinaryConverter.formatNumber(pageLayout.transform2PDFWidth(measureX)),
                            pdfy));
                    content.append(String.format(Locale.getDefault(), "%s %s l\r\n",
                            BinaryConverter.formatNumber(pageLayout.transform2PDFWidth(measureX + measureWidth)),
                            pdfy));
                    content.append("S\r\n");
                }

                // 오른쪽 테두리
                if (size.right > 0) {
                    gap = size.right * half;
                    setColorInPDF(content, color.right);
                    setLineWidthInPDF(content, BinaryConverter.formatNumber(size.right, 2));

                    String pdfx = BinaryConverter.formatNumber(pageLayout.transform2PDFWidth(measureX + measureWidth - gap));
                    content.append(String.format(Locale.getDefault(), "%s %s m\r\n",
                            pdfx,
                            BinaryConverter.formatNumber(pageLayout.transform2PDFHeight(measureY))));
                    content.append(String.format(Locale.getDefault(), "%s %s l\r\n",
                            pdfx,
                            BinaryConverter.formatNumber(pageLayout.transform2PDFHeight(measureY + measureHeight))));
                    content.append("S\r\n");
                }

                // 아래쪽 테두리
                if (size.bottom > 0) {
                    gap = size.bottom * half;
                    setColorInPDF(content, color.bottom);
                    setLineWidthInPDF(content, BinaryConverter.formatNumber(size.bottom, 2));

                    String pdfy = BinaryConverter.formatNumber(pageLayout.transform2PDFHeight(measureY + measureHeight - gap));
                    content.append(String.format(Locale.getDefault(), "%s %s m\r\n",
                            BinaryConverter.formatNumber(pageLayout.transform2PDFWidth(measureX)),
                            pdfy));
                    content.append(String.format(Locale.getDefault(), "%s %s l\r\n",
                            BinaryConverter.formatNumber(pageLayout.transform2PDFWidth(measureX + measureWidth)),
                            pdfy));
                    content.append("S\r\n");
                }
            }

            // 그래픽스 상태 복원
            content.append("Q\r\n"); // Restore graphics state
        }
    }

    public boolean canDraw(){
        return size.left > 0 || size.top > 0 || size.right > 0 || size.bottom > 0;
    }

    private boolean allSame(){
        return (size.left == size.top && size.top == size.right && size.right == size.bottom) &&
                (color.left == color.top && color.top == color.right && color.right == color.bottom);
    }

    /**
     * PDF 컨텐츠 스트림에 색상 설정
     */
    private void setColorInPDF(StringBuilder content, @ColorInt int color) {
        if(currentColor != color) {
            float red = Color.red(color) / 255f;
            float green = Color.green(color) / 255f;
            float blue = Color.blue(color) / 255f;
            float alpha = Color.alpha(color) / 255f;

            if (alpha == 1.0f) {
                content.append(String.format(Locale.getDefault(), "%s %s %s RG\r\n",
                        BinaryConverter.formatNumber(red, 3),
                        BinaryConverter.formatNumber(green, 3),
                        BinaryConverter.formatNumber(blue, 3))
                );
            } else {
                // 알파값이 있는 경우 ExtGState 사용
                content.append(String.format(Locale.getDefault(), "/GS%s gs\r\n",
                        BinaryConverter.formatNumber(alpha, 2)
                )); // 알파값에 해당하는 ExtGState 사용
                content.append(String.format(Locale.getDefault(), "%s %s %s RG\r\n",
                        BinaryConverter.formatNumber(red, 3),
                        BinaryConverter.formatNumber(green, 3),
                        BinaryConverter.formatNumber(blue, 3))
                );
            }
            currentColor = color;
        }
    }

    /**
     * PDF 컨텐츠 스트림에 선 두께 설정
     */
    private void setLineWidthInPDF(StringBuilder content, String width) {
        if(!currentWidth.equals(width)) {
            content.append(String.format(Locale.getDefault(), "%s w\r\n", width));
            currentWidth = width;
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
