package com.hangyeolee.androidpdfwriter.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

public class BitmapExtractor {
    private static final String TAG = "BitmapExtractor";

    // 캐시: 경로/리소스ID -> {비트맵이름, 이미지} 매핑
    private static final WeakHashMap<String, BitmapInfo> bitmapCache = new WeakHashMap<>();

    public static class BitmapInfo {
        public final Bitmap origin;
        public Bitmap resize = null;
        public float maxWidth = -1;
        public float maxHeight = -1;

        public BitmapInfo(Bitmap origin) {
            this.origin = origin;
        }
    }

    protected static BitmapInfo loadFromAsset(@NonNull Context context, @NonNull String assetPath){
        if (bitmapCache.containsKey(assetPath)) {
            return bitmapCache.get(assetPath);
        }

        try {
            // 1. Bitmap 생성
            InputStream is = context.getAssets().open(assetPath);
            Bitmap origin = BitmapFactory.decodeStream(is);

            // 2. 결과 캐싱
            BitmapInfo info = new BitmapInfo(origin);
            bitmapCache.put(assetPath, info);
            return info;
        } catch (IOException e) {
            Log.e(TAG, "Error loading bitmap from asset: " + assetPath, e);
            return null;
        }
    }

    protected static BitmapInfo loadFromFile(@NonNull String path) {
        if (bitmapCache.containsKey(path)) {
            return bitmapCache.get(path);
        }

        // 1. Bitmap 생성
        Bitmap origin = BitmapFactory.decodeFile(path);

        // 2. 결과 캐싱
        BitmapInfo info = new BitmapInfo(origin);
        bitmapCache.put(path, info);
        return info;
    }

    @SuppressLint("ResourceType")
    protected static BitmapInfo loadFromResource(@NonNull Context context, @RawRes int resourceId){
        String key = String.valueOf(resourceId);
        if (bitmapCache.containsKey(key)) {
            return bitmapCache.get(key);
        }

        // 1. Bitmap 생성
        Bitmap origin = BitmapFactory.decodeResource(context.getResources(), resourceId);

        // BitmapFactory가 실패한 경우 (XML drawable 등)
        if (origin == null) {
            Drawable drawable = context.getResources().getDrawable(resourceId);
            if (drawable != null) {
                // 이미 BitmapDrawable인 경우
                if (drawable instanceof BitmapDrawable) {
                    origin = ((BitmapDrawable) drawable).getBitmap();
                } else {
                    // 다른 종류의 Drawable인 경우 Bitmap으로 변환
                    int width = drawable.getIntrinsicWidth();
                    int height = drawable.getIntrinsicHeight();

                    // 크기가 정의되지 않은 경우 기본값 설정
                    if (width < 256) width = 256;
                    if (height < 256) height = 256;

                    origin = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(origin);
                    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                    drawable.draw(canvas);
                }
            }
        }

        // 2. 결과 캐싱
        BitmapInfo info = new BitmapInfo(origin);
        bitmapCache.put(key, info);
        return info;
    }

    /**
     * {@link PDFImage#build(Bitmap)} 를 제공하기 위함.
     * @deprecated 다음 버전에 {@link PDFImage#build(Bitmap)} 를 제거하면서, 제거됨.
     * @param bitmap 이미지
     * @return 이미지 정보
     */
    @Deprecated
    protected static BitmapInfo loadFromBitmap(Bitmap bitmap){
        String key = generateKey(bitmap);
        if (bitmapCache.containsKey(key)) {
            return bitmapCache.get(key);
        }

        // 2. 결과 캐싱
        BitmapInfo info = new BitmapInfo(bitmap);
        bitmapCache.put(key, info);
        return info;
    }

    /**
     * Bitmap을 JPEG로 압축했을 때의 예상 파일 크기를 계산
     * @param bitmap 크기를 계산할 Bitmap 객체
     * @param quality JPEG 압축 품질 (0-100)
     * @return 압축된 크기 (bytes)
     */
    protected static int getCompressedSize(Bitmap bitmap, int quality) {
        if (bitmap == null) return 0;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        int size = stream.size();
        try {
            stream.close();
        } catch (IOException ignored) {
        }

        return size;
    }

    /**
     * Bitmap의 메타데이터와 샘플링된 픽셀을 기반으로 고유 키 생성
     * @param bitmap 키를 생성할 Bitmap
     * @return 고유 키 문자열
     */
    private static String generateKey(Bitmap bitmap) {
        if (bitmap == null) return "";

        StringBuilder keyBuilder = new StringBuilder();

        // 기본 메타데이터
        keyBuilder.append("w=").append(bitmap.getWidth())
                .append(",h=").append(bitmap.getHeight())
                .append(",c=").append(bitmap.getConfig())
                .append(",d=").append(bitmap.getDensity())
                .append(",hc=").append(bitmap.hasAlpha())
                .append(",m=").append(bitmap.isMutable());

        // 코너 영역의 픽셀 샘플링 (각 코너에서 4x4 영역)
        int[][] corners = {
                {0, 0},                                    // 좌상단
                {bitmap.getWidth() - 4, 0},               // 우상단
                {0, bitmap.getHeight() - 4},              // 좌하단
                {bitmap.getWidth() - 4, bitmap.getHeight() - 4} // 우하단
        };

        for (int[] corner : corners) {
            for (int y = 0; y < 4 && y + corner[1] < bitmap.getHeight(); y++) {
                for (int x = 0; x < 4 && x + corner[0] < bitmap.getWidth(); x++) {
                    keyBuilder.append(String.format(Locale.getDefault() ,",%d",
                            bitmap.getPixel(corner[0] + x, corner[1] + y)));
                }
            }
        }

        // 해시 생성
        return String.format(Locale.getDefault(), "%016x", keyBuilder.toString().hashCode());
    }
}
