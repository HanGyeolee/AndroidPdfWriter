package com.hangyeolee.androidpdfwriter.components;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.RawRes;

import com.hangyeolee.androidpdfwriter.PDFBuilder;
import com.hangyeolee.androidpdfwriter.binary.BinaryConverter;
import com.hangyeolee.androidpdfwriter.binary.BinarySerializer;
import com.hangyeolee.androidpdfwriter.exceptions.ImageNotFoundException;
import com.hangyeolee.androidpdfwriter.utils.Anchor;
import com.hangyeolee.androidpdfwriter.utils.Border;
import com.hangyeolee.androidpdfwriter.utils.Fit;

import com.hangyeolee.androidpdfwriter.listener.Action;
import com.hangyeolee.androidpdfwriter.utils.Zoomable;

import java.util.Locale;

/**
 * 이미지 컴포넌트.<br>
 * 모든 이미지는 {@link Bitmap}에서 {@link android.graphics.Bitmap.CompressFormat#JPEG}로 압축되며
 * 컴포넌트의 크기가 이미지의 크기보다 5배 이상 작다면 압축을 시도 합니다.<br>
 * Image components.<br>
 * All images are compressed from {@link Bitmap} to {@link android.graphics.Bitmap.CompressFormat#JPEG}
 * If the component is more than 5 times smaller than the size of the image, try compressing.<br>
 * @see PDFText
 */
public class  PDFImage extends PDFResourceComponent{
    BitmapExtractor.BitmapInfo info = null;


    // Fit 적용 후의 크기 (기존 resizeW, resizeH 대체)
    float resizedWidth;
    float resizedHeight;

    // Anchor에 의한 여백 (기존 gapX, gapY 유지)
    float gapX;
    float gapY;

    // 이미지 압축 활성화 플래그 (기존 유지)
    boolean compressEnable = false;

    protected PDFImage(BitmapExtractor.BitmapInfo info){
        if(info == null) throw new ImageNotFoundException("Font not found.");
        this.info = info;
        bufferPaint = new Paint();
        bufferPaint.setFlags(TextPaint.FILTER_BITMAP_FLAG | TextPaint.LINEAR_TEXT_FLAG | TextPaint.ANTI_ALIAS_FLAG);
        anchor.vertical = Anchor.Center;
        anchor.horizontal = Anchor.Center;
    }
    protected PDFImage(BitmapExtractor.BitmapInfo info, @Fit.FitInt int fit){
        if(info == null) throw new ImageNotFoundException("Font not found.");
        this.info = info;
        bufferPaint = new Paint();
        bufferPaint.setFlags(TextPaint.FILTER_BITMAP_FLAG | TextPaint.LINEAR_TEXT_FLAG | TextPaint.ANTI_ALIAS_FLAG);
        anchor.vertical = Anchor.Center;
        anchor.horizontal = Anchor.Center;
        setFit(fit);
    }

    @Override
    public void measure(float x, float y) {
        super.measure(x, y);

        // 측정 가능한 실제 너비/높이 계산
        float availableWidth = measureWidth - border.size.left - padding.left
                - border.size.right - padding.right;
        float availableHeight = measureHeight - border.size.top - padding.top
                - border.size.bottom - padding.bottom;

        // 높이가 명시적으로 지정된 경우, 부모 크기 업데이트
        if (height > 0) {
            float _height = getTotalHeight();
            while (height > _height) {
                updateHeight(height - _height);
                _height = getTotalHeight();

                // 업데이트된 부모 크기로 사용 가능한 높이 재계산
                availableHeight = measureHeight - border.size.top - padding.top
                        - border.size.bottom - padding.bottom;
            }
        }

        // 항상 원본 비트맵으로 크기 계산
        measureSize(info.origin, availableWidth, availableHeight);

        // 계산된 크기가 가용 공간보다 큰 경우 부모 크기 업데이트
        while (resizedHeight > availableHeight) {
            updateHeight(resizedHeight - availableHeight);
            availableHeight = measureHeight - border.size.top - padding.top
                    - border.size.bottom - padding.bottom;

            // 새로운 가용 공간으로 다시 크기 계산
            measureSize(info.origin, availableWidth, availableHeight);
        }

        // 최종 높이 설정
        float updatedHeight = resizedHeight;
        height = updatedHeight + border.size.top + padding.top
                + border.size.bottom + padding.bottom + margin.top + margin.bottom;

        // Anchor에 따른 여백 계산
        float gapWidth = availableWidth - resizedWidth;
        float gapHeight = availableHeight - resizedHeight;
        gapX = Anchor.getDeltaPixel(anchor.horizontal, gapWidth);
        gapY = Anchor.getDeltaPixel(anchor.vertical, gapHeight);

        // 압축을 위한 maxWidth/maxHeight 업데이트
        if(compressEnable) {
            if(fit == Fit.NONE) {
                info.maxHeight = info.origin.getHeight();
                info.maxWidth = info.origin.getWidth();
            }else if (resizedWidth * 5f < info.origin.getWidth() && resizedHeight * 5f < info.origin.getHeight()) {
                if(info.maxHeight < resizedHeight) info.maxHeight = resizedHeight;
                if(info.maxWidth < resizedWidth) info.maxWidth = resizedWidth;
            }
        }
    }

    private void measureSize(Bitmap bitmap, float availableWidth, float availableHeight){
        // 기본 크기 설정
        resizedWidth = bitmap.getWidth();
        resizedHeight = bitmap.getHeight();

        // 비트맵의 가로세로 비율 계산
        float aspectRatio = (float)bitmap.getHeight() / bitmap.getWidth();

        // 부모 크기가 지정되지 않은 경우
        if (availableWidth < 1 && availableHeight > 0) {
            resizedWidth = availableHeight / aspectRatio;
            resizedHeight = availableHeight;
        } else if (availableHeight < 1 && availableWidth > 0) {
            resizedWidth = availableWidth;
            resizedHeight = availableWidth * aspectRatio;
        }

        if (fit == Fit.SCALE_DOWN){
            fit = Fit.CONTAIN;
            if(resizedWidth > bitmap.getWidth() &&
                resizedHeight > bitmap.getHeight()){
                fit = Fit.NONE;
            }
        }

        switch (fit) {
            case Fit.FILL:
                resizedWidth = availableWidth;
                resizedHeight = availableHeight;
                break;
            case Fit.CONTAIN:
                if (bitmap.getWidth() > bitmap.getHeight()) {
                    // 이미지가 가로로 더 긴 경우
                    resizedWidth = availableHeight / aspectRatio;
                    resizedHeight = availableHeight;

                    if (resizedWidth > availableWidth) {
                        // 계산된 가로가 가용 공간을 넘으면 가로 기준으로 다시 계산
                        resizedWidth = availableWidth;
                        resizedHeight = availableWidth * aspectRatio;
                    }
                }
                else {
                    // 이미지가 세로로 더 길거나 정사각형인 경우
                    resizedWidth = availableWidth;
                    resizedHeight = availableWidth * aspectRatio;

                    if (resizedHeight > availableHeight) {
                        // 계산된 세로가 가용 공간을 넘으면 세로 기준으로 다시 계산
                        resizedHeight = availableHeight;
                        resizedWidth = availableHeight / aspectRatio;
                    }
                }
                break;
            case Fit.COVER:
                if (bitmap.getWidth() > bitmap.getHeight()) {
                    // 이미지가 가로로 더 긴 경우
                    resizedWidth = availableWidth;
                    resizedHeight = availableWidth * aspectRatio;

                    if (resizedHeight < availableHeight) {
                        // 계산된 세로가 가용 공간보다 작으면 세로 기준으로 다시 계산
                        resizedHeight = availableHeight;
                        resizedWidth = availableHeight / aspectRatio;
                    }
                }
                else {
                    // 이미지가 세로로 더 길거나 정사각형인 경우
                    resizedHeight = availableHeight;
                    resizedWidth = availableHeight / aspectRatio;

                    if (resizedWidth < availableWidth) {
                        // 계산된 가로가 가용 공간보다 작으면 가로 기준으로 다시 계산
                        resizedWidth = availableWidth;
                        resizedHeight = availableWidth * aspectRatio;
                    }
                }
                break;
            case Fit.NONE:
            default:
                resizedWidth = bitmap.getWidth();
                resizedHeight = bitmap.getHeight();
                break;
        }
    }

    @Override
    public void registerResources(BinarySerializer page) {
        resourceId = page.registerImage(info);
    }

    @Override
    public void draw(BinarySerializer serializer) {
        // 리소스 등록 전 비트맵 압축 수행
        compressBitmap(serializer);

        // 부모 클래스의 draw 호출 (리소스 등록 포함)
        super.draw(serializer);
        float pageHeight = Zoomable.getInstance().getContentHeight();

        // 시작 페이지
        int currentPage = calculatePageIndex(measureY, measureHeight);

        // 첫 페이지의 Y 좌표 조정
        float currentY = measureY - currentPage * pageHeight;
        if(currentY < 0) currentY = 0;

        // PDF 좌표계로 변환하여 실제 그리기 위치 계산
        float x = Zoomable.getInstance().transform2PDFWidth(measureX + border.size.left + padding.left + gapX);
        float y = Zoomable.getInstance().transform2PDFHeight(currentY + border.size.top + padding.top + gapY + resizedHeight);

        StringBuilder content = serializer.getPage(currentPage);
        // 지정된 크기에 맞게 늘리기
        content.append(String.format(Locale.getDefault(),
                "%s 0 0 %s %s %s cm\r\n",
                BinaryConverter.formatNumber(resizedWidth),
                BinaryConverter.formatNumber(resizedHeight),
                BinaryConverter.formatNumber(x),
                BinaryConverter.formatNumber(y))
        );
        content.append("/").append(resourceId).append(" Do\r\n");
    }

    private void compressBitmap(BinarySerializer serializer){
        if(compressEnable) {
            // 이미 resize 이미지가 생성된 경우 연산을 무시한다.
            if(info.resize == null) {
                // 압축 불필요
                if ((info.maxWidth < 0 || info.maxHeight < 0) ||
                        (info.maxWidth == info.origin.getWidth() && info.maxHeight == info.origin.getHeight())) {
                    // info 내의 max 크기가 0 이하면 origin 을 resize 에 넣는 다.
                    info.resize = info.origin;
                } else {
                    // resize 생성해보기
                    float scaleX = info.maxWidth * 5f / info.origin.getWidth();
                    float scaleY = info.maxHeight * 5f / info.origin.getHeight();
                    Matrix matrix = new Matrix();
                    matrix.setScale(scaleX, scaleY);

                    Bitmap resize = null;
                    try {
                        resize = Bitmap.createBitmap(
                                info.origin,
                                0, 0,
                                info.origin.getWidth(),
                                info.origin.getHeight(),
                                matrix,
                                true
                        );
                    } catch (OutOfMemoryError e) {
                        // 메모리 부족 시 리사이징 실패 처리
                        Log.e(PDFBuilder.TAG, "OutOfMemoryError: PDFImage. " +
                                "The info.original image is used because there is not enough memory required for image reduction resizing.");
                        info.resize = info.origin;
                    }

                    // resize 비트맵을 생성해보고, 해당 크기가 origin 보다 무거우면 origin을 resize 에 넣는 다.
                    if (resize != null) {
                        int resizeSize = BitmapExtractor.getCompressedSize(resize, serializer.getQuality());
                        int originSize = BitmapExtractor.getCompressedSize(info.origin, serializer.getQuality());
                        if (resizeSize < originSize) {
                            info.resize = resize;
                        } else {
                            resize.recycle();
                            info.resize = info.origin;
                        }
                    }
                }
            }
        }
    }

    /**
     * GridLayout 안에 있는 이미지의 크기는 같은 열에 이미지 하나만 있는 경우에 적용된다.<br>
     * 같은 열에 더 큰 크기의 컴포넌트가 존재한다면, 해당 컴포넌트의 크기에 따라 Fit 된다.<br><br>
     * The size of the image in GridLayout is applied when there is only one image in the same column.<br>
     * If a larger component exists in the same column, it will fit according to the size of that component.
     * @deprecated 이 메소드는 더 이상 사용되지 않습니다.<br/>This method is no longer used.
     * @see PDFImage#setHeight(Number)
     * @param width 가로 크기
     * @param height 세로 크기
     * @return 자기 자신
     */
    @Override
    @Deprecated
    public PDFImage setSize(Number width, Number height) {
        super.setSize(width, height);
        return this;
    }
    /**
     * Layout 안에 있는 이미지의 가로 길이는 부모의 가로 길이로 무조건 적용된다.<br><br>
     * The width of the image in the layout is unconditionally applied as the horizontal length of the parent.
     * @param height 세로 크기
     * @return 자기 자신
     */
    public PDFImage setHeight(Number height) {
        super.setSize(null, height);
        return this;
    }

    /**
     * 이미지 압축 여부 설정<br>
     * 지정한 크기의 5배 보다 원본이 크다면 압축한다.
     * <p>압축 비율 = '지정한 크기 * 5 / 원본 크기'</p>
     * compressEnable이 false 면 이미지 리소스에 원본으로 저장한다.<br>
     * true 면, 지정한 크기로 원본 이미지를 축소한 뒤 이미지 리소스에 축소한 이미지를 저장한다.<br>
     * 단, 원본 이미지보다 크게 확대하면 무조건 원본으로 저장한다.<br><br>
     * Sets whether or not the image is compressed.<br>
     * If the info.original is larger than 5 times the specified size, compress it.
     * <p>compression ratio = specified size * 5 / info.original size</p>
     * If compressEnable is false, save it as the source in the image resource.<br>
     * If true, shrink the info.original image to the specified size and store the reduced image in the image resource.<br>
     * However, if it is enlarged larger than the info.original image, it will be saved as the info.original unconditionally.
     * @see Fit#NONE
     * @param compressEnable 압축 허용 여부.<br>
     * @return 자기자신
     */
    public PDFImage setCompress(boolean compressEnable) {
        this.compressEnable = compressEnable;
        return this;
    }

    @Override
    public PDFImage setBackgroundColor(int color) {
        super.setBackgroundColor(color);
        return this;
    }

    @Override
    public PDFImage setMargin(RectF margin) {
        super.setMargin(margin);
        return this;
    }

    @Override
    public PDFImage setMargin(float left, float top, float right, float bottom) {
        super.setMargin(left, top, right, bottom);
        return this;
    }

    @Override
    public PDFImage setMargin(float all) {
        super.setMargin(all);
        return this;
    }

    @Override
    public PDFImage setMargin(float horizontal, float vertical) {
        super.setMargin(horizontal, vertical);
        return this;
    }

    @Override
    public PDFImage setPadding(float all) {
        super.setPadding(all);
        return this;
    }

    @Override
    public PDFImage setPadding(float horizontal, float vertical) {
        super.setPadding(horizontal, vertical);
        return this;
    }

    @Override
    public PDFImage setPadding(RectF padding) {
        super.setPadding(padding);
        return this;
    }

    @Override
    public PDFImage setPadding(float left, float top, float right, float bottom) {
        super.setPadding(left, top, right, bottom);
        return this;
    }

    @Override
    public PDFImage setBorder(Action<Border, Border> action) {
        super.setBorder(action);
        return this;
    }

    @Override
    public PDFImage setBorder(float size, @ColorInt int color) {
        super.setBorder(size, color);
        return this;
    }

    @Override
    public PDFImage setAnchor(Integer horizontal, Integer vertical) {
        super.setAnchor(horizontal, vertical);
        return this;
    }

    @Override
    protected PDFImage setParent(PDFComponent parent) {
        super.setParent(parent);
        return this;
    }

    /**
     * 컴포넌트의 크기를 기준으로 이미지 확대, 축소 조건 설정<br><br>
     * Set image enlargement and reduction conditions based on component size
     * @param fit 조건
     * @return 자기자신
     */
    public PDFImage setFit(@Fit.FitInt int fit){
        this.fit = fit;
        return this;
    }

    @Override
    protected void finalize() throws Throwable {
        if(info.origin != null && !info.origin.isRecycled())
            this.info.origin.recycle();
        if(info.resize != null && !info.resize.isRecycled())
            this.info.resize.recycle();
        super.finalize();
    }

    /**
     * {@link Context#getAssets()}을 통해 {@link PDFImage}를 만듭니다.<br/>
     * Create an {@link PDFImage} via {@link Context#getAssets()}.
     * @see PDFImage#fromFile(String)
     * @see PDFImage#fromResource(Context, int)
     * @param context AppContext
     * @param assetPath 에셋 주소
     */
    public static PDFImage fromAsset(@NonNull Context context, @NonNull String assetPath){
        return new PDFImage(BitmapExtractor.loadFromAsset(context, assetPath));
    }
    /**
     * {@link BitmapFactory#decodeFile(String)}을 통해 {@link PDFImage}를 만듭니다.<br/>
     * Create an {@link PDFImage} via {@link BitmapFactory#decodeFile(String)}.
     * @see PDFImage#fromAsset(Context, String)
     * @see PDFImage#fromResource(Context, int)
     * @param path 파일 주소
     */
    public static PDFImage fromFile(@NonNull String path){
        return new PDFImage(BitmapExtractor.loadFromFile(path));
    }
    /**
     * {@link BitmapFactory#decodeResource(Resources, int)}을 통해 {@link PDFImage}를 만듭니다.<br/>
     * Create an {@link PDFImage} via {@link BitmapFactory#decodeResource(Resources, int)}.
     * @see PDFImage#fromAsset(Context, String)
     * @see PDFImage#fromFile(String)
     * @param context AppContext
     * @param resourceId 리소스 아이디
     */
    public static PDFImage fromResource(@NonNull Context context, @RawRes int resourceId){
        return new PDFImage(BitmapExtractor.loadFromResource(context, resourceId));
    }
    /**
     * {@link Context#getAssets()}을 통해 {@link PDFImage}를 만듭니다.<br/>
     * Create an {@link PDFImage} via {@link Context#getAssets()}.
     * @see PDFImage#fromFile(String, int) 
     * @see PDFImage#fromResource(Context, int, int)
     * @param context AppContext
     * @param assetPath 에셋 주소
     * @param fit 이미지 맞춤
     */
    public static PDFImage fromAsset(@NonNull Context context, @NonNull String assetPath, @Fit.FitInt int fit){
        return new PDFImage(BitmapExtractor.loadFromAsset(context, assetPath), fit);
    }
    /**
     * {@link BitmapFactory#decodeFile(String)}을 통해 {@link PDFImage}를 만듭니다.<br/>
     * Create an {@link PDFImage} via {@link BitmapFactory#decodeFile(String)}.
     * @see PDFImage#fromAsset(Context, String, int)
     * @see PDFImage#fromResource(Context, int, int)
     * @param path 파일 주소
     * @param fit 이미지 맞춤
     */
    public static PDFImage fromFile(@NonNull String path, @Fit.FitInt int fit){
        return new PDFImage(BitmapExtractor.loadFromFile(path), fit);
    }
    /**
     * {@link BitmapFactory#decodeResource(Resources, int)}을 통해 {@link PDFImage}를 만듭니다.<br/>
     * Create an {@link PDFImage} via {@link BitmapFactory#decodeResource(Resources, int)}.
     * @see PDFImage#fromAsset(Context, String, int)
     * @see PDFImage#fromFile(String, int)
     * @param context AppContext
     * @param resourceId 리소스 아이디
     * @param fit 이미지 맞춤
     */
    public static PDFImage fromResource(@NonNull Context context, @RawRes int resourceId, @Fit.FitInt int fit){
        return new PDFImage(BitmapExtractor.loadFromResource(context, resourceId), fit);
    }

    /**
     * {@link Bitmap}을 통해서 {@link PDFImage} 를 생성합니다.<br>
     * {@link Bitmap}으로 키값을 생성하는 데, 시간이 오래 걸릴 수 있습니다.<br>
     * Create {@link PDFImage} via {@link Bitmap}.<br>
     * Generating a key value with {@link Bitmap} can take a long time.<br>
     * @deprecated 추후 업데이트에서 삭제될 예정입니다.<br>It will be removed from future updates.
     * @see PDFImage#fromAsset(Context, String)
     * @see PDFImage#fromFile(String)
     * @see PDFImage#fromResource(Context, int)
     * @param bitmap 이미지
     * @return 자기 자신
     */
    @Deprecated
    public static PDFImage build(Bitmap bitmap){
        return new PDFImage(BitmapExtractor.loadFromBitmap(bitmap));
    }

    /**
     * {@link Bitmap}을 통해서 {@link PDFImage} 를 생성합니다.<br>
     * {@link Bitmap}으로 키값을 생성하는 데, 시간이 오래 걸릴 수 있습니다.<br>
     * Create {@link PDFImage} via {@link Bitmap}.<br>
     * Generating a key value with {@link Bitmap} can take a long time.<br>
     * @deprecated 추후 업데이트에서 삭제될 예정입니다.<br>It will be removed from future updates.
     * @see PDFImage#fromAsset(Context, String, int)
     * @see PDFImage#fromFile(String, int)
     * @see PDFImage#fromResource(Context, int, int)
     * @param bitmap 이미지
     * @param fit 정합 조건
     * @return 자기 자신
     */
    @Deprecated
    public static PDFImage build(Bitmap bitmap, @Fit.FitInt int fit){
        return new PDFImage(BitmapExtractor.loadFromBitmap(bitmap), fit);
    }

}
