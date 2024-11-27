package com.hangyeolee.pdf.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;

import com.hangyeolee.pdf.core.font.EncodingWindow;
import com.hangyeolee.pdf.core.font.PDFFont;
import com.hangyeolee.pdf.core.font.TTFPlatform;
import com.hangyeolee.pdf.core.font.TTFSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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
        public final int flags;
        // 유니코드, 가로 길이
        public final Map<Character, Integer> W = new HashMap<>();
        // 유니코드, 글리프ID
        public final Map<Character, Integer> usedGlyph = new HashMap<>();
        // 유니코드, 글리프ID
        public final Map<Character, Integer> glyphIndexMap;

        private final Context context;  // Asset인 경우 필요
        private final Object key;      // 파일 경로 또는 asset 경로
        private final FontSource sourceType;  // ASSET, FILE, RESOURCE 등

        public enum FontSource {
            BASE14,
            ASSET,
            FILE,
            RESOURCE
        }

        public FontInfo(
                Context context, Object key, FontSource sourceType,
                String postScriptName, Map<Character, Integer> glyphIndexMap, int flags, Typeface typeface) {
            this.postScriptName = postScriptName;
            this.flags = flags;
            this.glyphIndexMap = glyphIndexMap;
            this.typeface = typeface;
            this.context = context;
            this.key = key;
            this.sourceType = sourceType;
        }

        // 필요할 때만 폰트 데이터를 읽어오는 메소드
        public byte[] getFontData() {
            InputStream is = null;
            try {
                switch (sourceType) {
                    case ASSET:
                        if (context == null) throw new IOException("Context is null");
                        is = context.getAssets().open((String) key);
                        break;
                    case FILE:
                        is = new FileInputStream((String) key);
                        break;
                    case RESOURCE:
                        if (context == null) throw new IOException("Context is null");
                        is = context.getResources().openRawResource((Integer) key);
                        break;
                }
                return readAllBytes(is);
            } catch (IOException ignored){} finally {
                if (is != null) {
                    try {
                        is.close();
                    }catch (IOException ignored1){}
                }
            }
            return null;
        }

        private static byte[] readAllBytes(InputStream is) throws IOException {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return is.readAllBytes();
            } else {
                byte[] buffer = new byte[is.available()];
                is.read(buffer);
                return buffer;
            }
        }
    }

    protected static class FontNameAndCmap {
        public final String postScriptName;
        public final Map<Character, Integer> glyphIndexMap;
        public final int flags;

        public FontNameAndCmap(String postScriptName, int flags, Map<Character, Integer> glyphIndexMap) {
            this.postScriptName = postScriptName;
            this.flags = flags;
            this.glyphIndexMap = glyphIndexMap;
        }
    }

    protected static FontInfo loadFromDefault(@NonNull @PDFFont.ID String fontName){
        if (fontCache.containsKey(fontName)) {
            return fontCache.get(fontName);
        }
        FontInfo fontInfo = new FontInfo(
                null, null, FontInfo.FontSource.BASE14,
                fontName, null,
                PDFFont.getFontFlags(fontName), PDFFont.getTypeface(fontName));
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
            FontNameAndCmap record = extractPostScriptName(is);

            if(record == null) throw new IOException();

            // 3. 결과 캐싱
            FontInfo fontInfo = new FontInfo(
                    context, assetPath, FontInfo.FontSource.ASSET,
                    record.postScriptName, record.glyphIndexMap,
                    record.flags, typeface);
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
            FontNameAndCmap record = extractPostScriptName(fis);

            if(record == null) throw new IOException();

            // 3. 결과 캐싱
            FontInfo fontInfo = new FontInfo(
                    null, path, FontInfo.FontSource.FILE,
                    record.postScriptName, record.glyphIndexMap,
                    record.flags, typeface);
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
            FontNameAndCmap record = extractPostScriptName(is);

            if(record == null) throw new IOException();

            // 3. 결과 캐싱
            FontInfo fontInfo = new FontInfo(
                    context, resourceId, FontInfo.FontSource.RESOURCE,
                    record.postScriptName, record.glyphIndexMap,
                    record.flags, typeface);
            fontCache.put(key, fontInfo);
            return fontInfo;

        } catch (IOException e) {
            Log.e(TAG, "Error loading font from resource: " + resourceId, e);
            return null;
        }
    }


    // name 테이블 태그 ('name' in hex)
    static final int NAME_TABLE_TAG = 0x6E616D65;
    // cmap 테이블 태그 ('cmap' in hex)
    static final int CMAP_TABLE_TAG = 0x636D6170;
    static final int HEAD_TABLE_TAG = 0x68656164; // 'head'
    static final int OS_2_TABLE_TAG = 0x4F532F32; // 'OS/2'
    // PostScript 이름의 nameID
    static final int POST_SCRIPT_NAME_ID = 6;

    private static FontNameAndCmap extractPostScriptName(InputStream is) throws IOException {
        try {
            // TTF 파일 읽기 준비
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            ByteBuffer bb = ByteBuffer.wrap(buffer);
            bb.order(ByteOrder.BIG_ENDIAN);

            // Parse offset table
            int majorVersion = bb.getShort() & 0xFFFF;
            int minorVersion = bb.getShort() & 0xFFFF;
            // 테이블 수 읽기
            int numTables = bb.getShort() & 0xFFFF;

            // 테이블 찾기
            int tableOffset = 12;
            int nameTableOffset = -1;
            int cmapTableOffset = -1;
            int headTableOffset = -1;
            int os2TableOffset = -1;

            for (int i = 0; i < numTables && (nameTableOffset == -1 || cmapTableOffset == -1); i++) {
                bb.position(tableOffset + i * 16);
                int tag = bb.getInt();
                if (tag == NAME_TABLE_TAG) {
                    bb.position(tableOffset + i * 16 + 8);
                    nameTableOffset = bb.getInt();
                } else if (tag == CMAP_TABLE_TAG) {
                    bb.position(tableOffset + i * 16 + 8);
                    cmapTableOffset = bb.getInt();
                } else if (tag == HEAD_TABLE_TAG) {
                    bb.position(tableOffset + i * 16 + 8);
                    headTableOffset = bb.getInt();
                } else if (tag == OS_2_TABLE_TAG) {
                    bb.position(tableOffset + i * 16 + 8);
                    os2TableOffset = bb.getInt();
                }
            }

            if (nameTableOffset == -1 || cmapTableOffset == -1) {
                return null;
            }

            // Extract font style and flags from 'head' and 'OS/2' tables
            int fontFlags = calculateFontFlags(bb, headTableOffset, os2TableOffset);

            // name 테이블에서 PostScript 이름 추출
            String postScriptName = extractNameFromTable(bb, nameTableOffset);

            // cmap 테이블에서 글리프 매핑 추출
            Map<Character, Integer> glyphIndexMap = extractCmapFromTable(bb, cmapTableOffset);

            return new FontNameAndCmap(postScriptName, fontFlags, glyphIndexMap);
        } finally {
            is.close();
        }
    }

    private static String extractNameFromTable(ByteBuffer bb, int nameTableOffset) throws UnsupportedEncodingException {
        bb.position(nameTableOffset);
        int format = bb.getShort() & 0xFFFF;
        int count = bb.getShort() & 0xFFFF;
        int stringOffset = (bb.getShort() & 0xFFFF) + nameTableOffset;

        String result = null;
        int bestScore = -1;

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
                        result = name;
                    }
                }
            }
        }
        return result;
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

    private static Map<Character, Integer> extractCmapFromTable(ByteBuffer bb, int cmapTableOffset) {
        Map<Character, Integer> glyphIndexMap = new HashMap<>();

        bb.position(cmapTableOffset);
        int version = bb.getShort() & 0xFFFF;
        int numSubtables = bb.getShort() & 0xFFFF;

        // 적절한 서브테이블 찾기 (format 4 또는 12 선호)
        int format4Offset = -1;
        int format12Offset = -1;

        for (int i = 0; i < numSubtables; i++) {
            int platformID = bb.getShort() & 0xFFFF;
            int encodingID = bb.getShort() & 0xFFFF;
            int offset = bb.getInt();

            if (platformID == 0 || platformID == 3) { // Unicode or Windows
                int absoluteOffset = cmapTableOffset + offset;
                bb.position(absoluteOffset);
                int format = bb.getShort() & 0xFFFF;

                if (format == 4) format4Offset = absoluteOffset;
                else if (format == 12) format12Offset = absoluteOffset;
            }
        }

        // format 12 우선 사용, 없으면 format 4 사용
        if (format12Offset != -1) {
            parseFormat12Cmap(bb, format12Offset, glyphIndexMap);
        } else if (format4Offset != -1) {
            parseFormat4Cmap(bb, format4Offset, glyphIndexMap);
        }

        return glyphIndexMap;
    }

    private static void parseFormat4Cmap(ByteBuffer bb, int offset, Map<Character, Integer> glyphIndexMap) {
        bb.position(offset + 2); // format 스킵
        int length = bb.getShort() & 0xFFFF;
        int language = bb.getShort() & 0xFFFF;
        int segCountX2 = bb.getShort() & 0xFFFF;
        int segCount = segCountX2 / 2;

        bb.position(bb.position() + 6); // searchRange, entrySelector, rangeShift 스킵

        int[] endCodes = new int[segCount];
        int[] startCodes = new int[segCount];
        int[] idDeltas = new int[segCount];
        int[] idRangeOffsets = new int[segCount];

        // 각 배열 읽기
        for (int i = 0; i < segCount; i++) endCodes[i] = bb.getShort() & 0xFFFF;
        bb.getShort(); // reservedPad 스킵
        for (int i = 0; i < segCount; i++) startCodes[i] = bb.getShort() & 0xFFFF;
        for (int i = 0; i < segCount; i++) idDeltas[i] = bb.getShort();
        int idRangeOffsetPosition = bb.position();
        for (int i = 0; i < segCount; i++) idRangeOffsets[i] = bb.getShort() & 0xFFFF;

        // 매핑 처리
        for (int i = 0; i < segCount; i++) {
            for (int charCode = startCodes[i]; charCode <= endCodes[i] && charCode != 0xFFFF; charCode++) {
                int glyphIndex;
                if (idRangeOffsets[i] == 0) {
                    glyphIndex = (charCode + idDeltas[i]) & 0xFFFF;
                } else {
                    // idRangeOffset이 현재 위치로부터의 상대적 오프셋임을 고려
                    int glyphIndexOffset = idRangeOffsetPosition + i * 2 + idRangeOffsets[i] +
                            2 * (charCode - startCodes[i]);
                    bb.position(glyphIndexOffset);
                    glyphIndex = bb.getShort() & 0xFFFF;
                    if (glyphIndex != 0) {
                        glyphIndex = (glyphIndex + idDeltas[i]) & 0xFFFF;
                    }
                }

                // glyphIndex가 0이 아닌 경우에만 매핑 추가
                if (glyphIndex != 0) {
                    glyphIndexMap.put((char)charCode, glyphIndex);
                }
            }
        }
    }

    private static void parseFormat12Cmap(ByteBuffer bb, int offset, Map<Character, Integer> glyphIndexMap) {
        bb.position(offset + 4); // format과 reserved 스킵
        int length = bb.getInt();
        int language = bb.getInt();
        int numGroups = bb.getInt();

        for (int i = 0; i < numGroups; i++) {
            int startCharCode = bb.getInt();
            int endCharCode = bb.getInt();
            int startGlyphId = bb.getInt();

            for (int charCode = startCharCode; charCode <= endCharCode; charCode++) {
                glyphIndexMap.put((char)charCode, startGlyphId + (charCode - startCharCode));
            }
        }
    }

    private static int calculateFontFlags(ByteBuffer bb, int headOffset, int os2Offset) {
        // 기본 flags 설정
        int flags = 1; // FixedPitch (모노스페이스 폰트용, 기본값으로 설정)

        if (os2Offset != -1) {
            // panose 필드 읽기 (OS/2 테이블의 offset 32에 위치)
            bb.position(os2Offset + 32);
            byte familyType = bb.get();    // 0 = Any, 1 = No Fit, 2 = Text and Display, ...
            byte serifStyle = bb.get();    // 0 = Any, 1 = No Fit, 2 = Cove, ...

            // serifStyle이 0이나 1이 아니면 serif가 있는 것으로 간주
            if (serifStyle > 1) {
                flags = 2; // Serif
            }

            // OS/2 테이블에서 fsSelection 필드 읽기
            bb.position(os2Offset + 62);
            int weightClass = bb.getShort() & 0xFFFF;

            bb.position(os2Offset + 8);
            int fsSelection = bb.getShort() & 0xFFFF;

            // Symbolic 비트 확인 (비트 0)
            if ((fsSelection & 0x0800) != 0) { // Symbol character set
                flags |= 4; // Symbolic
            } else {
                flags |= 1 << 5; // Nonsymbolic
            }

            // Italic 비트 확인 (비트 1)
            if ((fsSelection & 0x0001) != 0) {
                flags |= 1 << 6; // Italic
            }

            // Bold 확인
            if (weightClass >= 700) {
                flags |= 1 << 18; // ForceBold
            }
        }

        if (headOffset != -1) {
            // head 테이블에서 macStyle 필드 읽기
            bb.position(headOffset + 44);
            int macStyle = bb.getShort() & 0xFFFF;

            // Mac style italic 비트가 설정되어 있고 아직 italic 플래그가 없으면 설정
            if ((macStyle & 0x0002) != 0 && (flags & (1 << 6)) == 0) {
                flags |= 1 << 6; // Italic
            }

            // Mac style bold 비트가 설정되어 있고 아직 bold 플래그가 없으면 설정
            if ((macStyle & 0x0001) != 0 && (flags & (1 << 18)) == 0) {
                flags |= 1 << 18; // ForceBold
            }
        }

        return flags;
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