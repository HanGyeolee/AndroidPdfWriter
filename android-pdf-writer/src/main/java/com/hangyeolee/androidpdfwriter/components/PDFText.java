package com.hangyeolee.androidpdfwriter.components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.RawRes;

import com.hangyeolee.androidpdfwriter.binary.BinarySerializer;
import com.hangyeolee.androidpdfwriter.exceptions.FontNotFoundException;
import com.hangyeolee.androidpdfwriter.font.FontExtractor;
import com.hangyeolee.androidpdfwriter.font.FontMetrics;
import com.hangyeolee.androidpdfwriter.font.PDFFont;
import com.hangyeolee.androidpdfwriter.utils.TextAlign;
import com.hangyeolee.androidpdfwriter.utils.Border;

import com.hangyeolee.androidpdfwriter.listener.Action;
import com.hangyeolee.androidpdfwriter.utils.FontType;
import com.hangyeolee.androidpdfwriter.utils.Zoomable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;

public class PDFText extends PDFResourceComponent {
    private static String FONTBBOX_TEXT = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789" +
            " !@#$%^&*()_+-=,./<>?;':\"[]{}`~\\|";
    FontExtractor.FontInfo info = null;
    private int flags;
    private int italicAngle;
    private int ascent;
    private int descent;
    private int capHeight;
    private int stemV;
    private int xHeight;
    private int stemH;
    private Rect fontBBox;
    private int[] charWidths;  // 문자별 폭 저장

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

    @Override
    public void measure(float x, float y) {
        super.measure(x, y);
        float _width =  (measureWidth - border.size.left - padding.left
                - border.size.right - padding.right);

        if(text != null) {
            // Paint 메트릭 계산
            setFontMetrics();

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
                    xHeight,
                    stemH,
                    fontBBox,
                    charWidths
            );
            resourceId = page.registerFont(info, fontMetrics);
        }
    }

    private void setFontMetrics(){
        TextPaint paint = (TextPaint) this.bufferPaint;
        // 1000 유닛 기준으로 변환할 스케일 팩터 계산
        float scale = 1000f / bufferPaint.getTextSize();
        Rect bounds = new Rect();

        flags = getFontFlags(paint.getTypeface());
        italicAngle = paint.getTypeface().isItalic() ? -12 : 0;
        // 폰트 메트릭 정보 저장
        ascent = (int)Math.ceil(-paint.ascent() * scale);
        descent = (int)Math.ceil(paint.descent() * scale);

        // 대문자 'H'의 bounds를 측정하여 높이 계산
        paint.getTextBounds("H", 0, 1, bounds);
        capHeight = (int)Math.ceil(bounds.height() * scale);

        // xHeight는 평면 비 어센딩 소문자(예: "x") 위쪽의 Y 좌표로, 기준선에서 측정됩니다.
        // 소문자 'x'의 높이로 추정
        paint.getTextBounds("x", 0, 1, bounds);
        xHeight = (int)Math.ceil(bounds.height() * scale);

        // stemV: 수직 스템 두께 (대문자 'I'의 너비로 더 정확하게 측정)
        paint.getTextBounds("I", 0, 1, bounds);
        stemV = (int)Math.ceil(bounds.width() * scale);

        // stemH: 수평 스템 두께 (소문자 'f' 또는 't'의 주 수평선 두께로 추정)
        paint.getTextBounds("f", 0, 1, bounds);
        stemH = (int)Math.ceil(Math.min(bounds.width() * 0.25f, stemV) * scale); // 일반적으로 stemV보다 작음

        // charWidths 계산
        charWidths = new int[text.length()];
        for (int i = 0; i < text.length(); i++) {
            float charWidth = bufferPaint.measureText(text, i, i + 1);
            charWidths[i] = (int)Math.ceil(charWidth * scale);
        }

        // FontBBox 계산
        // 모든 가능한 문자에 대한 경계 상자 계산
        Paint.FontMetrics metrics = paint.getFontMetrics();

        // 가장 왼쪽으로 확장되는 문자들 체크 (예: 'j', 'f' 등)
        String allChars = text + FONTBBOX_TEXT;
        float minLeft = 0;
        float maxRight = 0;
        for (char c : allChars.toCharArray()) {
            paint.getTextBounds(String.valueOf(c), 0, 1, bounds);
            minLeft = Math.min(minLeft, bounds.left);
            maxRight = Math.max(maxRight, bounds.right);
        }

        fontBBox = new Rect(
                (int)Math.ceil(minLeft * scale),                    // 왼쪽 확장
                (int)Math.ceil(-metrics.ascent * scale),               // 상단 확장(어센더)
                (int)Math.ceil(maxRight * scale),                   // 오른쪽 확장
                (int)Math.ceil(-metrics.descent * scale)            // 하단 확장(디센더)
        );
        return;
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
                .append(String.format(Locale.getDefault(),"%.2f", bufferPaint.getTextSize()))
                .append(" Tf\r\n");

        // 텍스트 색상 설정
        float red = Color.red(bufferPaint.getColor()) / 255f;
        float green = Color.green(bufferPaint.getColor()) / 255f;
        float blue = Color.blue(bufferPaint.getColor()) / 255f;
        content.append(String.format(Locale.getDefault(),"%.3f %.3f %.3f rg\r\n", red, green, blue));

        float lineHeight = layout.getLineBaseline(0) - layout.getLineTop(0);
        // 베이스라인 위치 계산
        float x = Zoomable.getInstance().transform2PDFWidth(measureX + border.size.left + padding.left);
        float y = Zoomable.getInstance().transform2PDFHeight(measureY + border.size.top + padding.top + lineHeight);

        // 텍스트 위치로 이동
        content.append(String.format(Locale.getDefault(),"%.2f %.2f Td\r\n", x, y));

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

            float alignX = x + alignmentOffset;

            // 첫 줄이 아닌 경우 새로운 줄로 이동
            if (i > 0) {
                lineHeight = layout.getLineBottom(i) - layout.getLineTop(i);
                content.append(String.format(Locale.getDefault(),"%.2f %.2f Td\r\n", alignX, -lineHeight));
            } else if (alignX > 0) {
                content.append(String.format(Locale.getDefault(),"%.2f 0 Td\r\n", alignX));
            }

            // 텍스트 이스케이프 처리
            String escapedText = escapePDFString(lineText);
            content.append(escapedText).append(" Tj\r\n");
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
        // ASCII 문자만 있는지 확인
        boolean isAsciiOnly = true;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) > 127) {
                isAsciiOnly = false;
                break;
            }
        }

        String s = "(" + text.replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)") + ")";
        if (isAsciiOnly) {
            // ASCII only - 괄호 문자열로 처리
            return s;
        } else {
            StringBuilder result = new StringBuilder();
            byte[] bytes;
            try {
                // UTF-16BE로 인코딩
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    bytes = text.getBytes(StandardCharsets.UTF_16BE);
                } else {
                    bytes = text.getBytes(Charset.forName("UTF-16BE"));
                }

                result.append("<");
                for (int i = 0; i < bytes.length; i += 2) {
                    result.append(String.format("%02X%02X", bytes[i] & 0xFF, bytes[i + 1] & 0xFF));
                }
                result.append(">");
            } catch (Exception ignored) {
                return s; // fallback - 일반 문자열로 처리
            }
            return result.toString();
        }
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

    /**
     * 기본 폰트로 helvetica 를 사용.<br/>
     * Use helvetica as the default font
     * @param text 글자
     */
    @Deprecated
    public PDFText(String text){
        this(text, PDFFont.HELVETICA);
    }
    /**
     * 기본 폰트로 helvetica 를 사용.<br/>
     * Use helvetica as the default font
     * @param text 글자
     * @param paint 스타일
     */
    @Deprecated
    public PDFText(String text, TextPaint paint){
        this(text, paint, PDFFont.HELVETICA);
    }
    public PDFText(String text, @NonNull @PDFFont.ID String fontName){
        this.setText(text).setTextPaint(null).setFont(fontName);
    }
    public PDFText(String text, TextPaint paint, @NonNull @PDFFont.ID String fontName){
        this.setText(text).setTextPaint(paint).setFont(fontName);
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
        }else if(paint != this.bufferPaint){
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
    public PDFText setFont(@NonNull @PDFFont.ID String fontName){
        FontExtractor.FontInfo info = FontExtractor.loadFromDefault(fontName);
        if(info == null) throw new FontNotFoundException("Font not found.");
        this.info = info;
        setTextPaint((TextPaint) bufferPaint);
        this.bufferPaint.setTypeface(info.typeface);
        return this;
    }
    public PDFText setFontFromAsset(@NonNull Context context, @NonNull String assetPath){
        FontExtractor.FontInfo info = FontExtractor.loadFromAsset(context, assetPath);
        if(info == null) throw new FontNotFoundException("Font not found.");
        this.info = info;
        setTextPaint((TextPaint) bufferPaint);
        this.bufferPaint.setTypeface(info.typeface);
        return this;
    }
    public PDFText setFontFromFile(@NonNull String path){
        FontExtractor.FontInfo info = FontExtractor.loadFromFile(path);
        if(info == null) throw new FontNotFoundException("Font not found.");
        this.info = info;
        setTextPaint((TextPaint) bufferPaint);
        this.bufferPaint.setTypeface(info.typeface);
        return this;
    }
    public PDFText setFontFromResource(@NonNull Context context, @RawRes int resourceId){
        FontExtractor.FontInfo info = FontExtractor.loadFromResource(context, resourceId);
        if(info == null) throw new FontNotFoundException("Font not found.");
        this.info = info;
        setTextPaint((TextPaint) bufferPaint);
        this.bufferPaint.setTypeface(info.typeface);
        return this;
    }
    public PDFText setFontStyle(@FontType.Style int style){
        if(info == null) throw new FontNotFoundException("Font of PDFText class is not initialized.");
        setTextPaint((TextPaint) bufferPaint);
        this.bufferPaint.setTypeface(Typeface.create(info.typeface, style));
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
