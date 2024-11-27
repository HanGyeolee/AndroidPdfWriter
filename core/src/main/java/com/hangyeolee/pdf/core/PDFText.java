package com.hangyeolee.pdf.core;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.RawRes;

import com.hangyeolee.pdf.core.binary.BinaryConverter;
import com.hangyeolee.pdf.core.binary.BinarySerializer;
import com.hangyeolee.pdf.core.exceptions.FontNotFoundException;
import com.hangyeolee.pdf.core.font.FontMetrics;
import com.hangyeolee.pdf.core.font.PDFFont;
import com.hangyeolee.pdf.core.utils.Fit;
import com.hangyeolee.pdf.core.utils.TextAlign;
import com.hangyeolee.pdf.core.utils.Border;

import com.hangyeolee.pdf.core.listener.Action;
import com.hangyeolee.pdf.core.utils.FontType;
import com.hangyeolee.pdf.core.utils.Zoomable;

import java.util.Locale;

public class PDFText extends PDFResourceComponent {
    public final static String Lorem =
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor " +
            "incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud " +
            "exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute " +
            "irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla " +
            "pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui " +
            "officia deserunt mollit anim id est laborum. ";
    private final static String FONTBBOX_TEXT = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
            "[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~" +
            "\u007f\u0080\u0081\u0082\u0083\u0084\u0085\u0086\u0087\u0088\u0089\u008a\u008b\u008c" +
            "\u008d\u008e\u008f\u0090\u0091\u0092\u0093\u0094\u0095\u0096\u0097\u0098\u0099\u009a" +
            "\u009b\u009c\u009d\u009e\u009f\u00a0¡¢£¤¥¦§¨©ª«¬\u00ad®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄ" +
            "\u00c5\u00c6\u00c7\u00c8\u00c9\u00ca\u00cb\u00cc\u00cd\u00ce\u00cf\u00d0\u00d1\u00d2" +
            "\u00d3\u00d4\u00d5\u00d6\u00d7\u00d8\u00d9\u00da\u00db\u00dc\u00dd\u00de\u00df\u00e0" +
            "\u00e1\u00e2\u00e3\u00e4\u00e5\u00e6\u00e7\u00e8\u00e9\u00ea\u00eb\u00ec\u00ed\u00ee" +
            "\u00ef\u00f0\u00f1\u00f2\u00f3\u00f4\u00f5\u00f6\u00f7\u00f8\u00f9\u00fa\u00fb\u00fc" +
            "\u00fd\u00fe\u00ff";
    FontExtractor.FontInfo info = null;
    private int italicAngle;
    private int ascent;
    private int descent;
    private int capHeight;
    private int stemV;
    private int xHeight;
    private int stemH;
    private Rect fontBBox;
    private int[] charWidths = null;  // 문자별 폭 저장

    String text = null;
    Layout.Alignment align = Layout.Alignment.ALIGN_NORMAL;
    StaticLayout layout = null;

    String lastText = null;
    TextPaint lastPaint = null;
    Layout.Alignment lastAlign = null;
    float lastWidth = 0;

    protected PDFText(String text, @NonNull @PDFFont.ID String fontName){
        this.setText(text).setTextPaint(null).setFont(fontName);
    }
    protected PDFText(String text, TextPaint paint, @NonNull @PDFFont.ID String fontName){
        this.setText(text).setTextPaint(paint).setFont(fontName);
    }

    @Override
    public void measure(float x, float y) {
        super.measure(x, y);
        if(text != null) {
            boolean isBase14 = BinaryConverter.isBase14Font(info.postScriptName);
            if(!isBase14){
                // 1000 유닛 기준으로 변환할 스케일 팩터 계산
                float scale = 1000f / bufferPaint.getTextSize();
                for(char c : text.toCharArray()) {
                    float width = bufferPaint.measureText(String.valueOf(c), 0, 1) - 0.5f;
                    // 글리프
                    Integer glyphIndex = info.glyphIndexMap.get(c);
                    if (glyphIndex != null) {
                        info.W.put(c, (int) Math.ceil(width * scale));
                        info.usedGlyph.put(c, glyphIndex);
                    }
                }
            }

            float maxWidth = 0;
            float _width = (measureWidth - border.size.left - padding.left
                    - border.size.right - padding.right);

            for(;;) {
                if (bufferPaint != lastPaint ||
                        !text.equals(lastText) || lastWidth != _width || lastAlign != align) {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                        layout = StaticLayout.Builder.obtain(text,
                                        0,
                                        text.length(),
                                        (TextPaint) bufferPaint,
                                        (int) Math.ceil(_width))
                                .setAlignment(align)
                                .build();
                    }
                    else {
                        layout = new StaticLayout(
                                text, 0, text.length(), (TextPaint) bufferPaint, (int) Math.ceil(_width), align,
                                1.0f, 0.0f,
                                true, null, (int) Math.ceil(_width));
                    }
                }

                if (width > 0) {
                    break;
                }
                for (int i = 0; i < layout.getLineCount(); i++) {
                    float w = layout.getLineWidth(i);
                    if (w > maxWidth) maxWidth = w;
                }
                if (maxWidth == _width) {
                    maxWidth += margin.right + margin.left + border.size.right + border.size.left
                            + padding.right + padding.left;
                    break;
                } else {
                    _width = maxWidth;
                }
            }

            if(fit == Fit.NONE || fit == Fit.CONTAIN) {
                boolean check = false;
                if (width <= 0) {
                    width = maxWidth;
                    check = true;
                }
                if(check){
                    super.measure(x, y);
                }
            }

            lastPaint = (TextPaint) bufferPaint;
            lastText = text;
            lastWidth = _width;
            lastAlign = align;

            float updatedHeight = measureTextHeight();
            height = (updatedHeight + border.size.top + padding.top
                    + border.size.bottom + padding.bottom + margin.top + margin.bottom);
            float _height = getTotalHeight();
            /*
            updatedHeight 가 measureHeight 보다 크다면?
            상위 컴포넌트의 Height를 업데이트 한다.
            */
            while (updatedHeight > _height) {
                updateHeight(updatedHeight - _height);
                _height = getTotalHeight();
            }
        }
    }

    protected float measureTextHeight(){
        float pageHeight = Zoomable.getInstance().getContentHeight();
        float maxGap = 0;
        float currentY = measureY;
        int i = 0;
        float lastBaseLine = layout.getLineBaseline(i);

        float lineHeight;
        for (i = 0; i < layout.getLineCount(); i++) {
            if(i > 0) {
                lineHeight = layout.getLineBaseline(i) - lastBaseLine;
                lastBaseLine = layout.getLineBaseline(i);
            }else{
                lineHeight = lastBaseLine - layout.getLineTop(i);
            }
            // 시작 페이지와 끝 페이지 계산
            int startPage = calculatePageIndex(currentY);
            int endPage = calculatePageIndex(currentY, lineHeight);
            float gap = 0;
            if(startPage != endPage){
                gap = endPage * pageHeight - currentY;
                maxGap += gap;
                currentY += gap;
            }
            currentY += lineHeight;
        }
        return layout.getHeight() + maxGap;
    }

    private void setFontMetrics(){
        TextPaint paint = (TextPaint) this.bufferPaint;
        // 1000 유닛 기준으로 변환할 스케일 팩터 계산
        float scale = 1000f / bufferPaint.getTextSize();
        Rect bounds = new Rect();

        italicAngle = paint.getTypeface().isItalic() ? -12 : 0;
        // 폰트 메트릭 정보 저장
        ascent = (int)Math.ceil(-paint.ascent() * scale);
        descent = (int)Math.ceil(-paint.descent() * scale);

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


        boolean isBase14 = BinaryConverter.isBase14Font(info.postScriptName);
        if(isBase14){
            if(charWidths == null) {
                // charWidths 계산
                charWidths = new int[0xE0];
                for (int i = 0; i < 0xE0; i++) {
                    float charWidth = paint.measureText(FONTBBOX_TEXT, i, i + 1) - 0.5f;
                    charWidths[i] = Math.round(charWidth * scale);
                }
            }
        }

        // FontBBox 계산
        // 모든 가능한 문자에 대한 경계 상자 계산
        Paint.FontMetrics metrics = paint.getFontMetrics();

        // 가장 왼쪽으로 확장되는 문자들 체크 (예: 'j', 'f' 등)
        float minLeft = 0;
        float maxRight = 0;
        for (char c : FONTBBOX_TEXT.toCharArray()) {
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

    @Override
    public void registerResources(BinarySerializer page) {
        // 폰트 리소스 등록
        if (bufferPaint != null && bufferPaint.getTypeface() != null) {
            // Paint 메트릭 계산
            setFontMetrics();

            int flag = info.flags;
            Typeface typeface = bufferPaint.getTypeface();

            // 스타일 수정 시
            if (typeface.isBold()) {
                flag |= 1 << 18; // ForceBold
            }
            if (typeface.isItalic()) {
                flag |= 1 << 6; // Italic
            }

            FontMetrics fontMetrics = new FontMetrics(
                    flag,
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

    @Override
    public void draw(BinarySerializer serializer) {
        super.draw(serializer);

        if (text == null || layout == null) return;
        float pageHeight = Zoomable.getInstance().getContentHeight();


        // 페이지별로 분할하여 그리기
        int currentPage = 0;
        float lastBaseLine = layout.getLineBaseline(0);
        float lineHeight = lastBaseLine - layout.getLineTop(0);

        // 시작 페이지와 끝 페이지 계산
        int startPage = calculatePageIndex(measureY, lineHeight);

        // 첫 페이지의 Y 좌표 조정
        float currentY = measureY - startPage * pageHeight;
        if(currentY < 0) currentY = 0;
        currentY += lineHeight;

        StringBuilder content = serializer.getPage(startPage + currentPage);
        drawBaseline(content, currentY);

        float lastAlignX = 0;
        // 여러 줄의 텍스트 처리
        for (int i = 0; i < layout.getLineCount(); i++) {
            // 현재 줄의 시작과 끝 인덱스
            int lineStart = layout.getLineStart(i);
            int lineEnd = layout.getLineEnd(i);
            String lineText = text.substring(lineStart, lineEnd).trim();

            if (lineText.isEmpty()) continue;

            // 줄 정렬 처리
            float lineWidth = layout.getLineWidth(i);
            float alignX = 0;

            switch (align) {
                case ALIGN_CENTER:
                    alignX = (measureWidth - border.size.left - border.size.right
                            - padding.left - padding.right - lineWidth) / 2 - lastAlignX;
                    break;
                case ALIGN_OPPOSITE:
                    alignX = measureWidth - border.size.left - border.size.right
                            - padding.left - padding.right - lineWidth - lastAlignX;
                    break;
            }

            // 첫 줄이 아닌 경우 새로운 줄로 이동
            if (i > 0) {
                lineHeight = layout.getLineBaseline(i) - lastBaseLine;
                lastBaseLine = layout.getLineBaseline(i);
                currentY += lineHeight;
                if(currentY > pageHeight){
                    // 다음 페이지로 이동 전 텍스트 객체 종료
                    PDFTextsState.restore(content);

                    currentY = 0; // 다음 페이지에서는 최상단부터 시작
                    currentPage++;

                    // 다음 페이지
                    content = serializer.getPage(startPage + currentPage);
                    currentY += lineHeight;
                    drawBaseline(content, currentY);
                    content.append(String.format(Locale.getDefault(),"%s 0 Td\r\n",
                            BinaryConverter.formatNumber(lastAlignX+alignX)
                    ));
                } else {
                    content.append(String.format(Locale.getDefault(),"%s %s Td\r\n",
                            BinaryConverter.formatNumber(alignX),
                            BinaryConverter.formatNumber(-lineHeight)
                    ));
                }
            } else if (alignX > 0) {
                content.append(String.format(Locale.getDefault(),"%s 0 Td\r\n",
                        BinaryConverter.formatNumber(alignX)
                ));
            }

            // 텍스트 이스케이프 처리
            escapePDFString(content, lineText);
            lastAlignX += alignX;
        }

        // 텍스트 객체 종료
        PDFTextsState.restore(content);
    }

    private void drawBaseline(StringBuilder content, float currentY){
        // 텍스트 객체 시작
        PDFTextsState.save(content);

        // 텍스트 렌더링 모드 설정
        content.append("0 Tr\n");
        // 폰트 및 크기 설정
        content.append("/").append(resourceId).append(" ")
                .append(String.format(Locale.getDefault(),"%s",
                        BinaryConverter.formatNumber(bufferPaint.getTextSize(), 2)
                ))
                .append(" Tf\r\n");

        // 텍스트 색상 설정
        PDFGraphicsState.addFillColor(content, bufferPaint.getColor());

        // 베이스라인 위치 계산
        float x = Zoomable.getInstance().transform2PDFWidth(measureX + border.size.left + padding.left);
        float y = Zoomable.getInstance().transform2PDFHeight(currentY + border.size.top + padding.top);

        // 텍스트 위치로 이동
        content.append(String.format(Locale.getDefault(),"%s %s Td\r\n",
                BinaryConverter.formatNumber(x),
                BinaryConverter.formatNumber(y))
        );
    }

    /**
     * PDF 문자열 이스케이프 처리
     */
    private void escapePDFString(StringBuilder content, String text) {
        String s = "(" + text.replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)") + ")";
        if(BinaryConverter.isBase14Font(info.postScriptName)){
            // ASCII only - 괄호 문자열로 처리
            content.append(s).append(" Tj\r\n");
        } else {
            StringBuilder result = new StringBuilder();
            try {
                // JVM은 기본적으로 UTF-16BE로 인코딩
                result.append("[");
                for (int i = 0; i < text.length(); i++) {
                    // 폰트에서의 글리프 인덱스 가 들어가야함.
                    Integer glyphIndex = info.usedGlyph.get(text.charAt(i));
                    if(glyphIndex != null)
                        result.append(String.format(Locale.getDefault(),"<%04X>", glyphIndex & 0xFFFF));
                }
                result.append("]");
            } catch (Exception ignored) {
                content.append(s).append(" Tj\r\n"); // fallback - 일반 문자열로 처리
            }
            content.append(result).append(" TJ\r\n");
        }
    }

    @Override
    public PDFText setSize(Number width, Number height) {
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
    public PDFText setBorder(float size, @ColorInt int color) {
        super.setBorder(size, color);
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
    /**
     * 컴포넌트의 크기를 기준으로 텍스트 확대, 축소 조건 설정<br>
     * Set image enlargement and reduction conditions based on component size
     * @param fit 조건
     * @return 자기자신
     */
    public PDFText setFit(@Fit.FitInt int fit){
        this.fit = fit;
        return this;
    }

    /**
     * {@link PDFFont}로 {@link PDFFont#HELVETICA}를 사용.<br/>
     * Use {@link PDFFont#HELVETICA} as the default {@link PDFFont}
     * @param text 글자
     */
    public static PDFText build(String text){return new PDFText(text, PDFFont.HELVETICA);}
    /**
     * {@link PDFFont}를 지정하여 문장 요소를 만듭니다..<br/>
     * Create a text component by specifying the {@link PDFFont}.
     * @param text 글자
     * @param fontName 기본 폰트
     */
    public static PDFText build(String text, @NonNull @PDFFont.ID String fontName){return new PDFText(text, fontName);}
    /**
     * {@link PDFFont}와 {@link TextPaint}를 지정하여 문장 요소를 만듭니다..<br/>
     * Create a text component by {@link TextPaint} and specifying the {@link PDFFont}.
     * @param text 글자
     * @param paint 글자 스타일
     * @param fontName 기본 폰트
     */
    public static PDFText build(String text, TextPaint paint, @NonNull @PDFFont.ID String fontName){return new PDFText(text, paint, fontName);}
}
