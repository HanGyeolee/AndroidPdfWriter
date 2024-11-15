package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import androidx.annotation.ColorInt;

import com.hangyeolee.androidpdfwriter.binary.BinarySerializer;
import com.hangyeolee.androidpdfwriter.utils.FontMetrics;
import com.hangyeolee.androidpdfwriter.utils.TextAlign;
import com.hangyeolee.androidpdfwriter.utils.Border;

import com.hangyeolee.androidpdfwriter.listener.Action;
import com.hangyeolee.androidpdfwriter.utils.FontType;

import java.util.Locale;
import java.util.Objects;

public class PDFText extends PDFResourceComponent {
    private int flags;
    private int italicAngle;
    private float ascent;
    private float descent;
    private float capHeight;
    private float stemV;
    private RectF fontBBox;
    private float[] charWidths;  // 문자별 폭 저장

    /*
     * Line spacing multiplier for default line spacing.
     */
    public static final float DEFAULT_LINESPACING_MULTIPLIER = 1.0f;

    /*
     * Line spacing addition for default line spacing.
     */
    public static final float DEFAULT_LINESPACING_ADDITION = 0.0f;

    String text = null;
    Layout.Alignment align = Layout.Alignment.ALIGN_NORMAL;
    StaticLayout layout = null;

    String lastText = null;
    TextPaint lastPaint = null;
    Layout.Alignment lastAlign = null;
    float lastWidth = 0;

    float updatedHeight;
    Typeface plaintype = null;

    @Override
    public void measure(float x, float y) {
        super.measure(x, y);
        float _width =  (measureWidth - border.size.left - padding.left
                - border.size.right - padding.right);

        if(text != null) {
            // Paint 메트릭 계산
            Paint.FontMetrics metrics = bufferPaint.getFontMetrics();

            flags = getFontFlags(bufferPaint.getTypeface());
            italicAngle = bufferPaint.getTypeface().isItalic() ? -12 : 0;
            // 폰트 메트릭 정보 저장
            ascent = -metrics.ascent;
            descent = metrics.descent;
            // capHeight는 대문자 'H'의 높이로 추정
            capHeight = bufferPaint.measureText("H");

            // stemV는 대문자 'I'의 폭으로 추정
            stemV = bufferPaint.measureText("I");

            // FontBBox 계산
            float maxWidth = 0;
            charWidths = new float[text.length()];
            for (int i = 0; i < text.length(); i++) {
                float charWidth = bufferPaint.measureText(text, i, i + 1);
                charWidths[i] = charWidth;
                maxWidth = Math.max(maxWidth, charWidth);
            }

            fontBBox = new RectF(
                    0,                  // left
                    metrics.bottom,     // bottom
                    maxWidth,           // right
                    -metrics.top        // top
            );

            if(layout == null || bufferPaint != lastPaint ||
                    !Objects.equals(text, lastText) || lastWidth != _width || lastAlign != align) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                    layout = StaticLayout.Builder.obtain(text,
                                    0,
                                    text.length(),
                                    (TextPaint) bufferPaint,
                                    (int) _width)
                            .setAlignment(align)
                            .build();
                }
                else {
                    layout = new StaticLayout(
                            text, 0, text.length(), (TextPaint) bufferPaint, (int)_width, align,
                            DEFAULT_LINESPACING_MULTIPLIER, DEFAULT_LINESPACING_ADDITION,
                            true, null, (int)_width);
                }

                lastPaint = (TextPaint) bufferPaint;
                lastText = text;
                lastWidth = _width;
                lastAlign = align;
            }

            updatedHeight = layout.getHeight();
            height =  (updatedHeight + border.size.top + padding.top
                    + border.size.bottom + padding.bottom);
            float _height =  (measureHeight - border.size.top - padding.top
                    - border.size.bottom - padding.bottom);
            /*
            updatedHeight 가 measureHeight 보다 크다면?
            상위 컴포넌트의 Height를 업데이트 한다.
            */
            while (updatedHeight > _height) {
                updateHeight(updatedHeight - _height);
                _height =  (measureHeight - border.size.top - padding.top
                        - border.size.bottom - padding.bottom);
            }
        }
    }

    @Override
    public void registerResources(BinarySerializer page) {
        // 폰트 리소스 등록
        if (bufferPaint != null && bufferPaint.getTypeface() != null) {
            FontMetrics fontMetrics = new FontMetrics(
                    flags,
                    italicAngle,
                    ascent,
                    descent,
                    capHeight,
                    stemV,
                    fontBBox,
                    charWidths
            );
            resourceId = page.registerFont(bufferPaint.getTypeface(), fontMetrics);
        }
    }

    /**
     * 폰트 플래그 계산
     */
    private int getFontFlags(Typeface typeface) {
        int flags = 0;

        // Symbolic 폰트 플래그
        flags |= 1 << 2;

        // 기타 플래그들
        if (typeface.isBold()) {
            flags |= 1 << 18; // ForceBold
        }
        if (typeface.isItalic()) {
            flags |= 1 << 6; // Italic
        }

        return flags;
    }

    @Override
    public StringBuilder draw(BinarySerializer serializer) {
        StringBuilder content = super.draw(serializer);

        if (text == null || layout == null) return null;

        // 그래픽스 상태 저장
        PDFGraphicsState.save(content);
        // 텍스트 객체 시작
        PDFTextsState.save(content);

        // 폰트 및 크기 설정
        content.append("/").append(resourceId).append(" ")
                .append(String.format(Locale.getDefault(),"%.2f", bufferPaint.getTextSize())).append(" Tf\n");

        // 텍스트 색상 설정
        float red = Color.red(bufferPaint.getColor()) / 255f;
        float green = Color.green(bufferPaint.getColor()) / 255f;
        float blue = Color.blue(bufferPaint.getColor()) / 255f;
        content.append(String.format(Locale.getDefault(),"%.3f %.3f %.3f rg\n", red, green, blue));

        // 베이스라인 위치 계산
        float x = measureX + border.size.left + padding.left;
        float y = serializer.getPageHeight() - (measureY + border.size.top + padding.top + bufferPaint.getFontMetrics().ascent);

        // 텍스트 위치로 이동
        content.append(String.format(Locale.getDefault(),"%.2f %.2f Td\n", x, y));

        // 여러 줄의 텍스트 처리
        for (int i = 0; i < layout.getLineCount(); i++) {
            // 현재 줄의 시작과 끝 인덱스
            int lineStart = layout.getLineStart(i);
            int lineEnd = layout.getLineEnd(i);
            String lineText = text.substring(lineStart, lineEnd).trim();

            if (lineText.isEmpty()) continue;

            // 줄 정렬 처리
            float lineWidth = layout.getLineWidth(i);
            float alignmentOffset = 0;

            switch (align) {
                case ALIGN_CENTER:
                    alignmentOffset = (measureWidth - border.size.left - border.size.right
                            - padding.left - padding.right - lineWidth) / 2;
                    break;
                case ALIGN_OPPOSITE:
                    alignmentOffset = measureWidth - border.size.left - border.size.right
                            - padding.left - padding.right - lineWidth;
                    break;
            }

            // 첫 줄이 아닌 경우 새로운 줄로 이동
            if (i > 0) {
                float lineHeight = layout.getLineBottom(i) - layout.getLineTop(i);
                content.append(String.format(Locale.getDefault(),"%.2f %.2f Td\n", alignmentOffset, -lineHeight));
            } else if (alignmentOffset > 0) {
                content.append(String.format(Locale.getDefault(),"%.2f 0 Td\n", alignmentOffset));
            }

            // 텍스트 이스케이프 처리
            String escapedText = escapePDFString(lineText);
            content.append("(").append(escapedText).append(") Tj\n");
        }

        // 텍스트 객체 종료
        PDFTextsState.restore(content);
        // 그래픽스 상태 복원
        PDFGraphicsState.restore(content);

        return null;
    }

    /**
     * PDF 문자열 이스케이프 처리
     */
    private String escapePDFString(String text) {
        return text.replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    @Override
    public PDFText setSize(Float width, Float height) {
        super.setSize(width, height);
        return this;
    }

    @Override
    public PDFText setBackgroundColor(int color) {
        super.setBackgroundColor(color);
        return this;
    }

    @Override
    public PDFText setMargin(RectF margin) {
        super.setMargin(margin);
        return this;
    }

    @Override
    public PDFText setMargin(float left, float top, float right, float bottom) {
        super.setMargin(left, top, right, bottom);
        return this;
    }

    @Override
    public PDFText setMargin(float all) {
        super.setMargin(all);
        return this;
    }

    @Override
    public PDFText setMargin(float horizontal, float vertical) {
        super.setMargin(horizontal, vertical);
        return this;
    }

    @Override
    public PDFText setPadding(float all) {
        super.setPadding(all);
        return this;
    }

    @Override
    public PDFText setPadding(float horizontal, float vertical) {
        super.setPadding(horizontal, vertical);
        return this;
    }

    @Override
    public PDFText setPadding(RectF padding) {
        super.setPadding(padding);
        return this;
    }

    @Override
    public PDFText setPadding(float left, float top, float right, float bottom) {
        super.setPadding(left, top, right, bottom);
        return this;
    }

    @Override
    public PDFText setBorder(Action<Border, Border> action) {
        super.setBorder(action);
        return this;
    }

    @Override
    public PDFText setAnchor(Integer horizontal, Integer vertical) {
        super.setAnchor(horizontal, vertical);
        return this;
    }

    @Override
    protected PDFText setParent(PDFComponent parent) {
        super.setParent(parent);
        return this;
    }

    public PDFText(String text){
        this.setText(text).setTextPaint(null);
    }
    public PDFText(String text, TextPaint paint){
        this.setText(text).setTextPaint(paint);
    }
    public PDFText setText(String text){
        lastText = this.text;
        this.text = text;
        return this;
    }
    public PDFText setTextPaint(TextPaint paint){
        if(paint == null){
            this.bufferPaint = new TextPaint();
            this.align = Layout.Alignment.ALIGN_NORMAL;
            this.bufferPaint.setTextSize(16f);
        }else{
            lastPaint = (TextPaint) this.bufferPaint;
            this.bufferPaint = paint;
        }
        this.bufferPaint.setFlags(TextPaint.FILTER_BITMAP_FLAG|TextPaint.LINEAR_TEXT_FLAG|TextPaint.ANTI_ALIAS_FLAG);
        return this;
    }
    public PDFText setTextColor(@ColorInt int color){
        setTextPaint((TextPaint) bufferPaint);
        this.bufferPaint.setColor(color);
        return this;
    }
    public PDFText setFontFamily(Typeface plaintype){
        return this.setFontFamily(plaintype, FontType.NORMAL);
    }
    public PDFText setFontFamily(Typeface plaintype, @FontType.Style int style){
        this.plaintype = plaintype;
        return setFontStyle(style);
    }
    public PDFText setFontStyle(@FontType.Style int style){
        if(this.plaintype == null) throw new NullPointerException("Please, call setFont first.");
        setTextPaint((TextPaint) bufferPaint);
        this.bufferPaint.setTypeface(Typeface.create(plaintype, style));
        return this;
    }

    public PDFText setFontsize(float fontsize){
        setTextPaint((TextPaint) bufferPaint);
        this.bufferPaint.setTextSize(fontsize);
        return this;
    }
    public PDFText setTextAlign(@TextAlign.TextAlignInt int align){
        lastAlign = this.align;
        switch (align){
            case TextAlign.Start:
                this.align = Layout.Alignment.ALIGN_NORMAL;
                break;
            case TextAlign.Center:
                this.align = Layout.Alignment.ALIGN_CENTER;
                break;
            case TextAlign.End:
                this.align = Layout.Alignment.ALIGN_OPPOSITE;
                break;
        }
        return this;
    }

    public static PDFText build(String text){return new PDFText(text);}

}
