package com.hangyeolee.androidpdfwriter.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;

import com.hangyeolee.androidpdfwriter.font.EncodingWindow;
import com.hangyeolee.androidpdfwriter.font.PDFFont;
import com.hangyeolee.androidpdfwriter.font.TTFPlatform;
import com.hangyeolee.androidpdfwriter.font.TTFSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

public class FontExtractor {
    private static final String TAG = "FontExtractor";

    // 캐시: 경로/리소스ID -> {폰트이름, 폰트 정보} 매핑
    private static final Map<String, FontInfo> fontCache = new HashMap<>();

    public static class FontInfo {
        public final String postScriptName;
        public final Typeface typeface;
        public final String encoding;
        public final byte[] stream;
        public final Map<Character, Integer> W = new HashMap<>();

        public FontInfo(
                String postScriptName, String encoding,
                Typeface typeface, byte[] stream) {
            this.postScriptName = postScriptName;
            this.encoding = encoding;
            this.typeface = typeface;
            this.stream = stream;
        }
    }

    private static class NameTableRecord {
        public String name;
        public String encoding;
    }

    protected static FontInfo loadFromDefault(@NonNull @PDFFont.ID String fontName){
        if (fontCache.containsKey(fontName)) {
            return fontCache.get(fontName);
        }
        FontInfo fontInfo = new FontInfo(fontName, null, PDFFont.getTypeface(fontName), null);
        fontCache.put(fontName, fontInfo);
        return fontInfo;
    }

    // Assets 폴더에서 폰트 로드
    protected static FontInfo loadFromAsset(@NonNull Context context, @NonNull String assetPath) {
        if (fontCache.containsKey(assetPath)) {
            return fontCache.get(assetPath);
        }

        try {
            // 1. Typeface 생성 시도
            Typeface typeface = Typeface.createFromAsset(context.getAssets(), assetPath);
            if (typeface == null) {
                Log.e(TAG, "Failed to create typeface from asset: " + assetPath);
                return null;
            }

            // 2. 폰트 이름 추출
            InputStream is = context.getAssets().open(assetPath);
            NameTableRecord record = extractPostScriptName(is);

            if(record == null) throw new IOException();
            is = context.getAssets().open(assetPath);
            byte[] stream;
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    stream = is.readAllBytes();
                } else {
                    stream = new byte[is.available()];
                    is.read(stream);
                }
            } finally {
                is.close();
            }

            // 3. 결과 캐싱
            FontInfo fontInfo = new FontInfo(record.name, record.encoding, typeface, stream);
            fontCache.put(assetPath, fontInfo);
            return fontInfo;

        } catch (IOException e) {
            Log.e(TAG, "Error loading font from asset: " + assetPath, e);
            return null;
        }
    }

    // 파일 시스템에서 폰트 로드
    protected static FontInfo loadFromFile(@NonNull String path) {
        if (fontCache.containsKey(path)) {
            return fontCache.get(path);
        }

        try {
            // 1. Typeface 생성 시도
            Typeface typeface = Typeface.createFromFile(path);
            if (typeface == null) {
                Log.e(TAG, "Failed to create typeface from file: " + path);
                return null;
            }

            // 2. 폰트 이름 추출
            FileInputStream fis = new FileInputStream(path);
            NameTableRecord record = extractPostScriptName(fis);

            if(record == null) throw new IOException();
            fis = new FileInputStream(path);
            byte[] stream;
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    stream = fis.readAllBytes();
                } else {
                    stream = new byte[fis.available()];
                    fis.read(stream);
                }
            } finally {
                fis.close();
            }
            // 3. 결과 캐싱
            FontInfo fontInfo = new FontInfo(record.name, record.encoding, typeface, stream);
            fontCache.put(path, fontInfo);
            return fontInfo;

        } catch (IOException e) {
            Log.e(TAG, "Error loading font from file: " + path, e);
            return null;
        }
    }

    // Raw 리소스에서 폰트 로드
    @SuppressLint("ResourceType")
    protected static FontInfo loadFromResource(@NonNull Context context, @RawRes int resourceId) {
        String key = String.valueOf(resourceId);
        if (fontCache.containsKey(key)) {
            return fontCache.get(key);
        }

        try {
            InputStream is = context.getResources().openRawResource(resourceId);
            Typeface typeface = null;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                typeface = context.getResources().getFont(resourceId);
            } else {
                // 1. Typeface 생성 시도
                try{
                    File tempFile = createTempFontFile(context, is);
                    if(tempFile != null) {
                        typeface = Typeface.createFromFile(tempFile);
                        // 4. 임시 파일 삭제
                        tempFile.delete();
                    }
                } catch (IOException ignored) {
                    return null;
                }
            }
            if (typeface == null) {
                Log.e(TAG, "Failed to create typeface from resource: " + resourceId);
                return null;
            }

            // 2. 폰트 이름 추출
            is = context.getResources().openRawResource(resourceId);
            NameTableRecord record = extractPostScriptName(is);

            if(record == null) throw new IOException();
            is = context.getResources().openRawResource(resourceId);
            byte[] stream;
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    stream = is.readAllBytes();
                } else {
                    stream = new byte[is.available()];
                    is.read(stream);
                }
            } finally {
                is.close();
            }

            // 3. 결과 캐싱
            FontInfo fontInfo = new FontInfo(record.name, record.encoding, typeface, stream);
            fontCache.put(key, fontInfo);

            return fontInfo;

        } catch (IOException e) {
            Log.e(TAG, "Error loading font from resource: " + resourceId, e);
            return null;
        }
    }



    private static NameTableRecord  extractPostScriptName(InputStream is) throws IOException {
        try {
            // name 테이블 태그 ('name' in hex)
            final int NAME_TABLE_TAG = 0x6E616D65;
            // PostScript 이름의 nameID
            final int POST_SCRIPT_NAME_ID = 6;

            // TTF 파일 읽기 준비
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            ByteBuffer bb = ByteBuffer.wrap(buffer);
            bb.order(ByteOrder.BIG_ENDIAN);

            // 테이블 수 읽기
            bb.position(4);
            int numTables = bb.getShort() & 0xFFFF;

            // name 테이블 찾기
            int tableOffset = 12;
            int nameTableOffset = -1;

            for (int i = 0; i < numTables; i++) {
                bb.position(tableOffset + i * 16);
                int tag = bb.getInt();
                if (tag == NAME_TABLE_TAG) {
                    bb.position(tableOffset + i * 16 + 8);
                    nameTableOffset = bb.getInt();
                    break;
                }
            }

            if (nameTableOffset == -1) {
                return null;
            }

            // name 테이블 읽기
            bb.position(nameTableOffset);
            int format = bb.getShort() & 0xFFFF;
            int count = bb.getShort() & 0xFFFF;
            int stringOffset = (bb.getShort() & 0xFFFF) + nameTableOffset;

            NameTableRecord record = null;
            int bestScore = -1;

            // 레코드 순회
            for (int i = 0; i < count; i++) {
                int platformID = bb.getShort() & 0xFFFF;
                int encodingID = bb.getShort() & 0xFFFF;
                int languageID = bb.getShort() & 0xFFFF;
                int nameID = bb.getShort() & 0xFFFF;
                int length = bb.getShort() & 0xFFFF;
                int offset = bb.getShort() & 0xFFFF;

                if (nameID == POST_SCRIPT_NAME_ID) {
                    int score = calculatePriority(platformID, encodingID);
                    if (score > bestScore) {
                        int savedPosition = bb.position();
                        bb.position(stringOffset + offset);
                        byte[] data = new byte[length];
                        bb.get(data);
                        bb.position(savedPosition);

                        String encoding = TTFSystem.getCharset(platformID, encodingID);
                        String name = new String(data, encoding);
                        if (!name.trim().isEmpty()) {
                            bestScore = score;
                            record = new NameTableRecord();
                            record.name = name;
                            record.encoding = encoding;
                        }
                    }
                }
            }
            return record;

        } finally {
            is.close();
        }
    }

    private static int calculatePriority(int platformId, int encodingId) {
        int score = 0;

        // Unicode 플랫폼 우선
        if (platformId == TTFPlatform.PLATFORM_UNICODE) {
            score += 1000;
            // 더 최신 유니코드 버전 선호
            score += encodingId * 10;
        }
        // Windows 플랫폼 다음 선호
        else if (platformId == TTFPlatform.PLATFORM_WINDOWS) {
            score += 500;
            // Unicode 인코딩 선호
            if (encodingId == EncodingWindow.ENCODING_WINDOWS_UNICODE_BMP ||
                    encodingId == EncodingWindow.ENCODING_WINDOWS_UNICODE_FULL) {
                score += 100;
            }
        }
        // Mac 플랫폼은 마지막 선호
        else if (platformId == TTFPlatform.PLATFORM_MAC) {
            score += 100;
        }

        return score;
    }

    private static File createTempFontFile(Context context, InputStream is) throws IOException {
        File tempFile = File.createTempFile("font", ".tmp", context.getCacheDir());
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (IOException ignored){
            return null;
        }
        return tempFile;
    }
}