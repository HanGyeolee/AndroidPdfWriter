package com.hangyeolee.androidpdfwriter.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class BitmapExtractor {
    private static final String TAG = "BitmapExtractor";

    // 캐시: 경로/리소스ID -> {비트맵이름, 이미지} 매핑
    private static final Map<String, Bitmap> bitmapCache = new HashMap<>();

    public static Bitmap loadFromAsset(@NonNull Context context, @NonNull String assetPath){
        if (bitmapCache.containsKey(assetPath)) {
            return bitmapCache.get(assetPath);
        }

        try {
            // 1. Bitmap 생성
            InputStream is = context.getAssets().open(assetPath);
            Bitmap origin = BitmapFactory.decodeStream(is);

            // 2. 결과 캐싱
            bitmapCache.put(assetPath, origin);
            return origin;
        } catch (IOException e) {
            Log.e(TAG, "Error loading bitmap from asset: " + assetPath, e);
            return null;
        }
    }

    public static Bitmap loadFromFile(@NonNull String path) {
        if (bitmapCache.containsKey(path)) {
            return bitmapCache.get(path);
        }

        // 1. Bitmap 생성
        Bitmap origin = BitmapFactory.decodeFile(path);

        // 2. 결과 캐싱
        bitmapCache.put(path, origin);
        return origin;
    }
    @SuppressLint("ResourceType")
    public static Bitmap loadFromResource(@NonNull Context context, @RawRes int resourceId){
        String key = String.valueOf(resourceId);
        if (bitmapCache.containsKey(key)) {
            return bitmapCache.get(key);
        }

        // 1. Bitmap 생성
        Bitmap origin = BitmapFactory.decodeResource(context.getResources(), resourceId);

        // 2. 결과 캐싱
        bitmapCache.put(key, origin);
        return origin;
    }
}
