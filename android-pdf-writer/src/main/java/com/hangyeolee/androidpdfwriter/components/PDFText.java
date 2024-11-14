package com.hangyeolee.androidpdfwriter.components;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import androidx.annotation.ColorInt;

import com.hangyeolee.androidpdfwriter.binary.BinaryPage;
import com.hangyeolee.androidpdfwriter.utils.FontMetrics;
import com.hangyeolee.androidpdfwriter.utils.TextAlign;
import com.hangyeolee.androidpdfwriter.utils.Border;

import com.hangyeolee.androidpdfwriter.listener.Action;
import com.hangyeolee.androidpdfwriter.utils.FontType;
import com.hangyeolee.androidpdfwriter.utils.Zoomable;

import java.io.BufferedOutputStream;
import java.util.Objects;

public class PDFText extends PDFComponent {
    private String fontResourceId;  // 등록된 폰트의 리소스 ID
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
    int lastWidth = 0;

    int updatedHeight;
    Typeface plaintype = null;

    @Override
    public void measure(float x, float y) {
        super.measure(x, y);
        int _width = Math.round (measureWidth - border.size.left - padding.left
                - border.size.right - padding.right);

        if(text != null) {
            // Paint 메트릭 계산
            Paint.FontMetrics metrics = bufferPaint.getFontMetrics();

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
                                    _width)
                            .setAlignment(align)
                            .build();
                }
                else {
                    layout = new StaticLayout(
                            text, 0, text.length(), (TextPaint) bufferPaint, _width, align,
                            DEFAULT_LINESPACING_MULTIPLIER, DEFAULT_LINESPACING_ADDITION,
                            true, null, _width);
                }

                lastPaint = (TextPaint) bufferPaint;
                lastText = text;
                lastWidth = _width;
                lastAlign = align;
            }

            updatedHeight = layout.getHeight();
            height = Math.round (updatedHeight + border.size.top + padding.top
                    + border.size.bottom + padding.bottom);
            int _height = Math.round (measureHeight - border.size.top - padding.top
                    - border.size.bottom - padding.bottom);
            /*
            updatedHeight 가 measureHeight 보다 크다면?
            상위 컴포넌트의 Height를 업데이트 한다.
            */
            while (updatedHeight > _height) {
                updateHeight(updatedHeight - _height);
                _height = Math.round (measureHeight - border.size.top - padding.top
                        - border.size.bottom - padding.bottom);
            }
        }
    }

    @Override
    protected void createPDFObject(BinaryPage page, StringBuilder content) {
        // 등록된 폰트 리소스 ID 사용
        content.append("/").append(fontResourceId).append(" ").append(this.bufferPaint.getTextSize()).append(" Tf\n");
    }

    @Override
    public void registerResources(BinaryPage page, BufferedOutputStream bufos) {
        // 폰트 리소스 등록
        if (bufferPaint != null && bufferPaint.getTypeface() != null) {
            FontMetrics fontMetrics = new FontMetrics(
                    ascent,
                    descent,
                    capHeight,
                    stemV,
                    fontBBox,
                    charWidths
            );
            fontResourceId = page.registerFont(bufferPaint.getTypeface(), fontMetrics, bufos);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        canvas.drawBitmap(buffer,
                measureX + border.size.left + padding.left,
                measureY + border.size.top + padding.top,
                bufferPaint);
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
    public PDFText setMargin(Rect margin) {
        super.setMargin(margin);
        return this;
    }

    @Override
    public PDFText setMargin(int left, int top, int right, int bottom) {
        super.setMargin(left, top, right, bottom);
        return this;
    }

    @Override
    public PDFText setMargin(int all) {
        super.setMargin(all);
        return this;
    }

    @Override
    public PDFText setMargin(int horizontal, int vertical) {
        super.setMargin(horizontal, vertical);
        return this;
    }

    @Override
    public PDFText setPadding(int all) {
        super.setPadding(all);
        return this;
    }

    @Override
    public PDFText setPadding(int horizontal, int vertical) {
        super.setPadding(horizontal, vertical);
        return this;
    }

    @Override
    public PDFText setPadding(Rect padding) {
        super.setPadding(padding);
        return this;
    }

    @Override
    public PDFText setPadding(int left, int top, int right, int bottom) {
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
            this.bufferPaint.setTextSize(16f * Zoomable.getInstance().density);
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
        this.bufferPaint.setTextSize(fontsize * Zoomable.getInstance().density);
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
