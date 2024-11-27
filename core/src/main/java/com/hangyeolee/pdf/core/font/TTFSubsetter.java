package com.hangyeolee.pdf.core.font;

import android.util.Log;

import com.hangyeolee.pdf.core.FontExtractor;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

public class TTFSubsetter {
    private static final String TAG = "TTFSubsetter";
    private final FontExtractor.FontInfo fontInfo;
    private final List<TableEntry> tables = new ArrayList<>();
    private ByteBuffer input;
    private int numGlyphs;

    // 글리프 플래그 상수
    private static final int ARG_1_AND_2_ARE_WORDS = 0x0001;
    private static final int ARGS_ARE_XY_VALUES = 0x0002;
    private static final int ROUND_XY_TO_GRID = 0x0004;
    private static final int WE_HAVE_A_SCALE = 0x0008;
    private static final int MORE_COMPONENTS = 0x0020;
    private static final int WE_HAVE_AN_X_AND_Y_SCALE = 0x0040;
    private static final int WE_HAVE_A_TWO_BY_TWO = 0x0080;
    private static final int WE_HAVE_INSTRUCTIONS = 0x0100;
    private static final int USE_MY_METRICS = 0x0200;
    private static final int OVERLAP_COMPOUND = 0x0400;

    public static class TableEntry {
        String tag;
        int checkSum;
        int offset;
        int length;
        byte[] data;

        // 글리프 오프셋을 저장하기 위한 맵 추가
        Map<Integer, Integer> glyphOffsets = new HashMap<>();
    }
    public static class HeadTableEntry extends TableEntry{
        private int version;                    // Fixed: 0x00010000 for version 1.0
        private int fontRevision;               // Fixed: font version
        private long checkSumAdjustment;        // uint32: checksum adjustment
        private long magicNumber;               // uint32: magic number (0x5F0F3CF5)
        private int flags;                      // uint16: flags
        private int unitsPerEm;                 // uint16: units per em
        private long created;                   // int64: created date in seconds since 1904
        private long modified;                  // int64: modified date in seconds since 1904
        private short xMin;                     // FWord: minimum x value
        private short yMin;                     // FWord: minimum y value
        private short xMax;                     // FWord: maximum x value
        private short yMax;                     // FWord: maximum y value
        private int macStyle;                   // uint16: mac style bits
        private int lowestRecPPEM;             // uint16: smallest readable size in pixels
        private short fontDirectionHint;        // int16: font direction hint
        private short indexToLocFormat;         // int16: format of 'loca' table
        private short glyphDataFormat;          // int16: glyph data format

        /**
         * glyf 테이블의 오프셋을 계산하는데 필요한 indexToLocFormat을 반환합니다.
         * @return 0: short format, 1: long format
         */
        public boolean isLongLocaFormat() {
            return indexToLocFormat == 1;
        }

        /**
         * checkSumAdjustment를 업데이트합니다.
         */
        public void updateCheckSumAdjustment(long newCheckSum) {
            ByteBuffer buffer = ByteBuffer.wrap(data);
            buffer.order(ByteOrder.BIG_ENDIAN);
            buffer.position(8);
            buffer.putInt((int)(newCheckSum & 0xFFFFFFFFL));
            checkSumAdjustment = newCheckSum;
        }

        /**
         * FontDescriptor에 필요한 값들을 가져옵니다.
         */
        public int[] getFontDescriptorMetrics() {
            return new int[] {
                    xMin, yMin, xMax, yMax,
                    unitsPerEm,
                    (macStyle & 0x01) != 0 ? 1 : 0,  // Bold
                    (macStyle & 0x02) != 0 ? 1 : 0   // Italic
            };
        }

        /**
         * TTFSubsetter에서 loca 테이블을 처리할 때 필요한 파라미터들을 반환합니다.
         */
        public int getGlyphOffsetMultiplier() {
            return indexToLocFormat == 0 ? 2 : 1;
        }

        /**
         * 글리프 오프셋을 계산합니다.
         */
        public int calculateGlyphOffset(int locaValue) {
            return indexToLocFormat == 0 ? locaValue * 2 : locaValue;
        }

        public int getUnitsPerEm() {
            return unitsPerEm;
        }

        public int getMacStyle() {
            return macStyle;
        }

        public short[] getBoundingBox() {
            return new short[] { xMin, yMin, xMax, yMax };
        }
    }

    // 글리프 매핑 관리를 위한 클래스
    private static class GlyphMapping {
        private final Map<Integer, Integer> oldToNewId = new HashMap<>();
        private int nextNewId = 1;

        public GlyphMapping() {
            // 0번 글리프는 항상 0번으로 유지
            mapGlyph(0, 0);
        }

        public void mapGlyph(int oldId, int newId) {
            oldToNewId.put(oldId, newId);
            nextNewId = Math.max(nextNewId, newId + 1);
        }

        public int getNewId(int oldId) {
            Integer newId = oldToNewId.get(oldId);
            return newId != null ? newId : -1;
        }

        public int createNewId(int oldId) {
            int newId = nextNewId++;
            mapGlyph(oldId, newId);
            return newId;
        }

        public boolean containsOldId(int oldId) {
            return oldToNewId.containsKey(oldId);
        }

        public int size() {
            return oldToNewId.size();
        }

        public Set<Integer> getAllOldIds() {
            return oldToNewId.keySet();
        }
    }

    public TTFSubsetter(FontExtractor.FontInfo fontInfo) {
        this.fontInfo = fontInfo;
    }

    public byte[] subset() {
        byte[] originalFont = fontInfo.getFontData();
        if (originalFont == null) {
            Log.e(TAG, "Failed to get font data");
            return null;
        }

        try {
            input = ByteBuffer.wrap(originalFont);
            input.order(ByteOrder.BIG_ENDIAN);

            // 1. 기본 테이블 읽기
            readMaxpTable();
            readTableDirectory();

            // 3. 테이블 처리
            GlyphMapping glyphMapping = processRequiredTables();

            // 4. FontInfo의 글리프 매핑 업데이트
            updateFontInfoGlyphMap(glyphMapping);

            // 5. 새로운 폰트 파일 생성
            return buildSubsetFont();
        } catch (Exception e) {
            Log.e(TAG, "Font subsetting failed", e);
            return null;
        }
    }

    private GlyphMapping collectRequiredGlyphs() {
        GlyphMapping mapping = new GlyphMapping();

        // 1. 사용된 글리프 수집
        Set<Integer> requiredGlyphs = new HashSet<>(fontInfo.usedGlyph.values());

        // 2. 복합 글리프 의존성 분석
        Set<Integer> additionalGlyphs = analyzeCompositeGlyphs(requiredGlyphs);
        requiredGlyphs.addAll(additionalGlyphs);

        // 3. 글리프 ID 재매핑
        List<Integer> sortedGlyphIds = new ArrayList<>(requiredGlyphs);
        Collections.sort(sortedGlyphIds);

        // 0번은 이미 매핑되어 있음
        for (int oldId : sortedGlyphIds) {
            if (oldId != 0 && !mapping.containsOldId(oldId)) {
                mapping.createNewId(oldId);
            }
        }

        return mapping;
    }

    private Set<Integer> analyzeCompositeGlyphs(Set<Integer> initialGlyphs) {
        TableEntry glyfTable = findTable("glyf");
        TableEntry locaTable = findTable("loca");

        if (glyfTable == null || locaTable == null) {
            Log.e(TAG, "Required tables missing for composite glyph analysis");
            return new HashSet<>();
        }

        Set<Integer> additionalGlyphs = new HashSet<>();
        Set<Integer> pendingGlyphs = new HashSet<>(initialGlyphs);
        Set<Integer> processedGlyphs = new HashSet<>(initialGlyphs.size());

        while (!pendingGlyphs.isEmpty()) {
            Iterator<Integer> it = pendingGlyphs.iterator();
            int glyphId = it.next();
            it.remove();

            if (processedGlyphs.contains(glyphId)) {
                continue;
            }

            processedGlyphs.add(glyphId);

            // 글리프 오프셋 찾기
            int offset = locaTable.glyphOffsets.get(glyphId);
            int length;

            if (glyphId < numGlyphs - 1) {
                length = locaTable.glyphOffsets.get(glyphId + 1) - offset;
            } else {
                length = glyfTable.data.length - offset;
            }

            if (length > 0) {
                Set<Integer> referencedGlyphs = getReferencedGlyphs(glyfTable.data, offset);
                for (int referencedGlyph : referencedGlyphs) {
                    if (!processedGlyphs.contains(referencedGlyph)) {
                        pendingGlyphs.add(referencedGlyph);
                        additionalGlyphs.add(referencedGlyph);
                    }
                }
            }
        }

        return additionalGlyphs;
    }

    private Set<Integer> getReferencedGlyphs(byte[] glyfData, int offset) {
        Set<Integer> referencedGlyphs = new HashSet<>();
        ByteBuffer bb = ByteBuffer.wrap(glyfData);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.position(offset);

        // 윤곽선 개수 읽기
        short numberOfContours = bb.getShort();

        if (numberOfContours >= 0) {
            // 단순 글리프는 참조하는 글리프가 없음
            return referencedGlyphs;
        }

        // 복합 글리프 처리
        bb.position(offset + 10); // 바운딩 박스 스킵

        boolean hasMoreComponents;
        do {
            int flags = bb.getShort() & 0xFFFF;
            int glyphIndex = bb.getShort() & 0xFFFF;

            referencedGlyphs.add(glyphIndex);

            // 변환 행렬 파라미터 스킵
            if ((flags & ARG_1_AND_2_ARE_WORDS) != 0) {
                bb.position(bb.position() + 4);
            } else {
                bb.position(bb.position() + 2);
            }

            // 추가 변환 파라미터 스킵
            if ((flags & WE_HAVE_A_SCALE) != 0) {
                bb.position(bb.position() + 2);
            } else if ((flags & WE_HAVE_AN_X_AND_Y_SCALE) != 0) {
                bb.position(bb.position() + 4);
            } else if ((flags & WE_HAVE_A_TWO_BY_TWO) != 0) {
                bb.position(bb.position() + 8);
            }

            // 명령어 데이터 스킵
            if ((flags & WE_HAVE_INSTRUCTIONS) != 0) {
                int instructionLength = bb.getShort() & 0xFFFF;
                bb.position(bb.position() + instructionLength);
            }

            hasMoreComponents = (flags & MORE_COMPONENTS) != 0;
        } while (hasMoreComponents);

        return referencedGlyphs;
    }

    private void readMaxpTable() {
        // maxp 테이블 위치 찾기
        input.position(4);
        int numTables = input.getShort() & 0xFFFF;
        input.position(12);

        for (int i = 0; i < numTables; i++) {
            byte[] tagBytes = new byte[4];
            input.get(tagBytes);
            String tag = new String(tagBytes);

            int checkSum = input.getInt();
            int offset = input.getInt();
            int length = input.getInt();

            if (tag.equals("maxp")) {
                int savedPos = input.position();
                input.position(offset + 4); // maxp 버전 스킵
                numGlyphs = input.getShort() & 0xFFFF;
                input.position(savedPos);
                break;
            }
        }
    }

    private void readTableDirectory() {
        input.position(0);
        // sfnt 버전 확인
        int sfntVersion = input.getInt();
        if (sfntVersion != 0x00010000 && sfntVersion != 0x4F54544F) { // TTF or OTF
            throw new IllegalStateException("Invalid font format");
        }

        int numTables = input.getShort() & 0xFFFF;
        input.position(12);

        for (int i = 0; i < numTables; i++) {
            String tag = new String(new byte[]{
                    input.get(), input.get(), input.get(), input.get()
            });
            TableEntry entry;
            if(tag.equals("head")){
                entry = new HeadTableEntry();
            } else {
                entry = new TableEntry();
            }
            entry.tag = tag;
            entry.checkSum = input.getInt();
            entry.offset = input.getInt();
            entry.length = input.getInt();

            if (isRequiredTable(entry.tag)) {
                // 테이블 데이터 복사
                entry.data = new byte[entry.length];
                int savedPosition = input.position();
                input.position(entry.offset);
                input.get(entry.data);
                input.position(savedPosition);
                tables.add(entry);
                if(tag.equals("head")){
                    processHeadTable((HeadTableEntry)entry);
                }
            }
        }
    }

    private GlyphMapping processRequiredTables() {
        // 테이블 처리 순서가 중요: loca -> glyf -> hmtx -> maxp -> cmap
        int loca_idx = -1;
        int glyf_idx = -1;
        int hmtx_idx = -1;
        int maxp_idx = -1;
        int cmap_idx = -1;

        for (int i = 0; i < tables.size(); i++) {
            TableEntry entry = tables.get(i);
            switch (entry.tag) {
                case "loca": loca_idx = i; break;
                case "glyf": glyf_idx = i; break;
                case "hmtx": hmtx_idx = i; break;
                case "maxp": maxp_idx = i; break;
                case "cmap": cmap_idx = i; break;
            }
        }

        // 순서대로 처리
        GlyphMapping glyphMapping = processLocaTable(tables.get(loca_idx));
        processGlyfTable(tables.get(glyf_idx), tables.get(loca_idx), glyphMapping);
        processHmtxTable(tables.get(hmtx_idx), glyphMapping);
        updateMaxpTable(tables.get(maxp_idx), glyphMapping);
        processCmapTable(tables.get(cmap_idx), glyphMapping);
        return glyphMapping;
    }

    private void processHeadTable(HeadTableEntry entry) {
        ByteBuffer bb = ByteBuffer.wrap(entry.data);
        bb.order(ByteOrder.BIG_ENDIAN);

        entry.version = bb.getInt();                  // 0-3
        entry.fontRevision = bb.getInt();             // 4-7
        entry.checkSumAdjustment = bb.getInt() & 0xFFFFFFFFL;  // 8-11
        entry.magicNumber = bb.getInt() & 0xFFFFFFFFL;         // 12-15
        entry.flags = bb.getShort() & 0xFFFF;         // 16-17
        entry.unitsPerEm = bb.getShort() & 0xFFFF;    // 18-19

        // 날짜 처리 (각각 8바이트)
        entry.created = bb.getLong();                 // 20-27
        entry.modified = bb.getLong();                // 28-35

        // 바운딩 박스 좌표
        entry.xMin = bb.getShort();                   // 36-37
        entry.yMin = bb.getShort();                   // 38-39
        entry.xMax = bb.getShort();                   // 40-41
        entry.yMax = bb.getShort();                   // 42-43

        entry.macStyle = bb.getShort() & 0xFFFF;      // 44-45
        entry.lowestRecPPEM = bb.getShort() & 0xFFFF; // 46-47
        entry.fontDirectionHint = bb.getShort();      // 48-49
        entry.indexToLocFormat = bb.getShort();       // 50-51
        entry.glyphDataFormat = bb.getShort();        // 52-53
    }

    private GlyphMapping processLocaTable(TableEntry entry) throws NullPointerException {
        HeadTableEntry headTable = (HeadTableEntry)findTable("head");
        if(headTable == null) throw new NullPointerException();
        boolean isLongFormat = headTable.isLongLocaFormat();

        ByteBuffer bb = ByteBuffer.wrap(entry.data);
        bb.order(ByteOrder.BIG_ENDIAN);

        // 원본 글리프 오프셋 읽기
        for (int oldGlyphId = 0; oldGlyphId < numGlyphs; oldGlyphId++) {
            int offset;
            if (isLongFormat) {
                offset = bb.getInt();
            } else {
                offset = (bb.getShort() & 0xFFFF) * 2; // short format에서는 2를 곱해야 함
            }
            entry.glyphOffsets.put(oldGlyphId, offset);
        }

        // 복합 글리프 분석 및 글리프 매핑 생성
        GlyphMapping glyphMapping = collectRequiredGlyphs();

        // 새로운 loca 테이블 생성
        ByteArrayOutputStream newLoca = new ByteArrayOutputStream();

        // 새로운 글리프 ID 순서대로 오프셋 저장
        for (int newId = 0; newId < glyphMapping.size(); newId++) {
            int oldId = -1;
            for (int id : glyphMapping.getAllOldIds()) {
                if (glyphMapping.getNewId(id) == newId) {
                    oldId = id;
                    break;
                }
            }
            if (oldId != -1) {
                if (isLongFormat) {
                    writeInt(newLoca, entry.glyphOffsets.get(oldId));
                } else {
                    writeShort(newLoca, entry.glyphOffsets.get(oldId) >> 1); // short format에서는 2로 나눠야 함
                }
            } else {
                // 마지막 엔트리이거나 누락된 글리프의 경우
                if (isLongFormat) {
                    writeInt(newLoca, newId > 0 ? entry.glyphOffsets.get(oldId - 1) : 0);
                } else {
                    writeShort(newLoca, newId > 0 ? entry.glyphOffsets.get(oldId - 1) >> 1 : 0);
                }
            }
        }

        entry.data = newLoca.toByteArray();

        return glyphMapping;
    }

    private void processGlyfTable(TableEntry entry, TableEntry locaEntry, GlyphMapping glyphMapping)
            throws NullPointerException {
        ByteArrayOutputStream newGlyf = new ByteArrayOutputStream();
        HeadTableEntry headTable = (HeadTableEntry)findTable("head");
        if(headTable == null) throw new NullPointerException();
        boolean isLongFormat = headTable.isLongLocaFormat();

        // 새로운 오프셋 맵
        Map<Integer, Integer> newOffsets = new HashMap<>();
        int currentOffset = 0;

        // 새 글리프 ID 순서대로 처리
        for (int newId = 0; newId < glyphMapping.size(); newId++) {
            int oldId = -1;
            for (int id : glyphMapping.getAllOldIds()) {
                if (glyphMapping.getNewId(id) == newId) {
                    oldId = id;
                    break;
                }
            }
            if (oldId == -1) continue;

            int oldOffset = locaEntry.glyphOffsets.get(oldId);
            int length;

            if (oldId < numGlyphs - 1) {
                length = locaEntry.glyphOffsets.get(oldId + 1) - oldOffset;
            } else {
                length = entry.data.length - oldOffset;
            }

            newOffsets.put(newId, currentOffset);

            if (length > 0) {
                // 글리프 데이터 읽기
                byte[] glyphData = new byte[length];
                System.arraycopy(entry.data, oldOffset, glyphData, 0, length);

                // 복합 글리프인지 확인
                ByteBuffer bb = ByteBuffer.wrap(glyphData);
                bb.order(ByteOrder.BIG_ENDIAN);
                short numberOfContours = bb.getShort();

                if(numberOfContours == -1){
                    // 복합 글리프 갱신
                    ByteArrayOutputStream updatedGlyph = new ByteArrayOutputStream();
                    writeShort(updatedGlyph, numberOfContours);

                    // 바운딩 박스 복사 (8 bytes)
                    updatedGlyph.write(glyphData, 2, 8);
                    bb.position(10);  // numberOfContours(2) + bbox(8)

                    // 컴포넌트 처리
                    boolean hasMoreComponents;
                    do {
                        int flags = bb.getShort() & 0xFFFF;
                        writeShort(updatedGlyph, flags);
                        // 복합 글리프 처리 부분에서 수정
                        int oldComponentId = bb.getShort() & 0xFFFF;
                        int newComponentId = glyphMapping.getNewId(oldComponentId);
                        writeShort(updatedGlyph, newComponentId);

                        // 변환 행렬 데이터 처리
                        int componentDataLength = 0;
                        if ((flags & ARG_1_AND_2_ARE_WORDS) != 0) {
                            componentDataLength += 4;  // 2 words
                        } else {
                            componentDataLength += 2;  // 2 bytes
                        }

                        if ((flags & WE_HAVE_A_SCALE) != 0) {
                            componentDataLength += 2;
                        } else if ((flags & WE_HAVE_AN_X_AND_Y_SCALE) != 0) {
                            componentDataLength += 4;
                        } else if ((flags & WE_HAVE_A_TWO_BY_TWO) != 0) {
                            componentDataLength += 8;
                        }

                        // 변환 행렬 데이터 복사
                        updatedGlyph.write(glyphData, bb.position(), componentDataLength);
                        bb.position(bb.position() + componentDataLength);

                        // instructions 처리
                        if ((flags & WE_HAVE_INSTRUCTIONS) != 0) {
                            int instructionLength = bb.getShort() & 0xFFFF;
                            writeShort(updatedGlyph, instructionLength);
                            updatedGlyph.write(glyphData, bb.position(), instructionLength);
                            bb.position(bb.position() + instructionLength);
                        }

                        hasMoreComponents = (flags & MORE_COMPONENTS) != 0;
                    } while (hasMoreComponents);

                    glyphData = updatedGlyph.toByteArray();
                }
                // 4바이트 정렬
                int alignedLength = (length + 3) & ~3;
                byte[] alignedData = new byte[alignedLength];
                System.arraycopy(glyphData, 0, alignedData, 0, length);

                newGlyf.write(alignedData, 0, alignedLength);
                currentOffset += alignedLength;
            }
        }

        entry.data = newGlyf.toByteArray();

        // loca 테이블 업데이트
        updateLocaOffsets(locaEntry, newOffsets, entry.data.length, isLongFormat);
    }

    private void updateLocaOffsets(TableEntry locaEntry, Map<Integer, Integer> newOffsets,
                                  int finalGlyfSize, boolean isLongFormat) {
        ByteArrayOutputStream newLoca = new ByteArrayOutputStream();


        // 모든 글리프 + 마지막 엔트리에 대한 오프셋 작성
        for (int i = 0; i <= newOffsets.size(); i++) {
            int offset;
            if (i < newOffsets.size()) {
                offset = newOffsets.get(i);
            } else {
                // 마지막 엔트리는 glyf 테이블의 전체 크기
                offset = finalGlyfSize;
            }

            if (isLongFormat) {
                writeInt(newLoca, offset);
            } else {
                // short format에서는 2로 나눠서 저장
                writeShort(newLoca, offset >> 1);
            }
        }

        locaEntry.data = newLoca.toByteArray();
    }

    private void processHmtxTable(TableEntry entry, GlyphMapping glyphMapping) {
        ByteBuffer oldHmtx = ByteBuffer.wrap(entry.data);
        oldHmtx.order(ByteOrder.BIG_ENDIAN);
        ByteArrayOutputStream newHmtx = new ByteArrayOutputStream();

        // 새 글리프 ID 순서대로 처리
        for (int newId = 0; newId < glyphMapping.size(); newId++) {
            int oldId = -1;
            for (int id : glyphMapping.getAllOldIds()) {
                if (glyphMapping.getNewId(id) == newId) {
                    oldId = id;
                    break;
                }
            }
            if (oldId != -1) {
                oldHmtx.position(oldId * 4);
                writeInt(newHmtx, oldHmtx.getInt());
            }
        }

        entry.data = newHmtx.toByteArray();
    }

    private void updateMaxpTable(TableEntry entry, GlyphMapping glyphMapping) {
        ByteBuffer bb = ByteBuffer.wrap(entry.data);
        bb.order(ByteOrder.BIG_ENDIAN);

        // maxp 버전은 유지
        bb.position(4);

        // 새로운 글리프 수 기록
        bb.putShort((short) glyphMapping.size());
    }

    private void processCmapTable(TableEntry entry, GlyphMapping glyphMapping) {
        ByteBuffer bb = ByteBuffer.wrap(entry.data);
        bb.order(ByteOrder.BIG_ENDIAN);

        // Version과 numTables 읽기
        int version = bb.getShort() & 0xFFFF;
        int numTables = bb.getShort() & 0xFFFF;

        // 최적의 서브테이블 찾기
        int format4Offset = -1;
        int format12Offset = -1;
        int bestPlatformID = -1;
        int bestEncodingID = -1;

        for (int i = 0; i < numTables; i++) {
            int platformID = bb.getShort() & 0xFFFF;
            int encodingID = bb.getShort() & 0xFFFF;
            int offset = bb.getInt();

            int savedPosition = bb.position();
            bb.position(offset);
            int format = bb.getShort() & 0xFFFF;

            boolean isPreferred = isPlatformPreferred(platformID, encodingID);

            if (format == 4 && (format4Offset == -1 || isPreferred)) {
                format4Offset = offset;
                if (isPreferred) {
                    bestPlatformID = platformID;
                    bestEncodingID = encodingID;
                }
            } else if (format == 12 && (format12Offset == -1 || isPreferred)) {
                format12Offset = offset;
                if (isPreferred) {
                    bestPlatformID = platformID;
                    bestEncodingID = encodingID;
                }
            }

            bb.position(savedPosition);
        }

        // 새로운 cmap 테이블 생성
        ByteArrayOutputStream newCmap = new ByteArrayOutputStream();

        // 헤더 작성
        writeShort(newCmap, version);
        writeShort(newCmap, 1); // 하나의 서브테이블만 포함

        // 유니코드 범위 분석
        Map<Integer, List<Integer>> unicodeRanges = analyzeUnicodeRanges(glyphMapping);

        // BMP 범위만 있는지 확인
        boolean onlyBMP = isOnlyBMPRange(unicodeRanges);

        // 인코딩 레코드 작성
        writeShort(newCmap, bestPlatformID != -1 ? bestPlatformID : 3); // Windows
        writeShort(newCmap, bestEncodingID != -1 ? bestEncodingID : 10); // Unicode UCS-4
        writeInt(newCmap, 12); // 첫 번째 서브테이블까지의 오프셋

        if (onlyBMP && format4Offset != -1) {
            byte[] format4Data = createFormat4Subtable(unicodeRanges);
            newCmap.write(format4Data, 0, format4Data.length);
        } else {
            byte[] format12Data = createFormat12Subtable(unicodeRanges);
            newCmap.write(format12Data, 0, format12Data.length);
        }

        entry.data = newCmap.toByteArray();
    }


    private boolean isPlatformPreferred(int platformID, int encodingID) {
        if (platformID == 0) return true;  // Unicode
        if (platformID == 3 && (encodingID == 1 || encodingID == 10)) return true; // Windows Unicode
        return false;
    }

    private Map<Integer, List<Integer>> analyzeUnicodeRanges(GlyphMapping glyphMapping) {
        Map<Integer, List<Integer>> ranges = new TreeMap<>();

        // 유니코드 값으로 정렬된 엔트리 생성
        List<Map.Entry<Character, Integer>> sortedEntries = new ArrayList<>(fontInfo.usedGlyph.entrySet());
        Collections.sort(sortedEntries, (o1, o2) -> o1.getValue().compareTo(o2.getValue()));

        Integer currentStartGlyphId = null;
        List<Integer> currentRange = null;

        for (Map.Entry<Character, Integer> entry : sortedEntries) {
            char unicode = entry.getKey();
            int oldGlyphId = entry.getValue();
            int newGlyphId = glyphMapping.getNewId(oldGlyphId);

            if (currentStartGlyphId == null) {
                currentStartGlyphId = newGlyphId;
                currentRange = new ArrayList<>();
            } else if (isSequential(currentRange, unicode) &&
                    isSequentialGlyphId(currentStartGlyphId, currentRange.size(), newGlyphId)) {
            } else {
                if (!currentRange.isEmpty()) {
                    ranges.put(currentStartGlyphId, currentRange);
                }
                currentStartGlyphId = newGlyphId;
                currentRange = new ArrayList<>();
            }

            currentRange.add((int)unicode);
        }

        if (currentRange != null) {
            ranges.put(currentStartGlyphId, currentRange);
        }

        return ranges;
    }

    private boolean isSequential(List<Integer> range, int unicode) {
        return range.isEmpty() || unicode == range.get(range.size() - 1) + 1;
    }

    private boolean isSequentialGlyphId(int startGlyphId, int rangeSize, int newGlyphId) {
        return newGlyphId == startGlyphId + rangeSize;
    }

    private boolean isOnlyBMPRange(Map<Integer, List<Integer>> ranges) {
        for (List<Integer> range : ranges.values()) {
            for (int unicode : range) {
                if (unicode > 0xFFFF) return false;
            }
        }
        return true;
    }

    private byte[] createFormat4Subtable(Map<Integer, List<Integer>> ranges) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Format 4 헤더
        writeShort(out, 4);  // format
        writeShort(out, 0);  // length (나중에 업데이트)
        writeShort(out, 0);  // language

        int segCount = ranges.size() + 1; // +1 for terminating segment
        writeShort(out, segCount * 2); // segCountX2

        // 검색 관련 값 계산
        int entrySelector = (int)(Math.log(segCount) / Math.log(2));
        int searchRange = (1 << entrySelector) * 2;

        writeShort(out, searchRange);
        writeShort(out, entrySelector);
        writeShort(out, (segCount * 2) - searchRange);

        // endCode 배열
        for (List<Integer> range : ranges.values()) {
            writeShort(out, range.get(range.size() - 1));
        }
        writeShort(out, 0xFFFF); // 종료 세그먼트

        writeShort(out, 0); // reservedPad

        // startCode 배열
        for (List<Integer> range : ranges.values()) {
            writeShort(out, range.get(0));
        }
        writeShort(out, 0xFFFF); // 종료 세그먼트

        // idDelta 배열
        int index = 0;
        for (Map.Entry<Integer, List<Integer>> entry : ranges.entrySet()) {
            int glyphId = entry.getKey();
            int startCode = entry.getValue().get(0);
            writeShort(out, glyphId - startCode);
        }
        writeShort(out, 1); // 종료 세그먼트

        // idRangeOffset 배열
        for (int i = 0; i < segCount; i++) {
            writeShort(out, 0); // 직접 매핑 사용
        }

        // 길이 업데이트
        byte[] data = out.toByteArray();
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putShort(2, (short)data.length);

        return data;
    }

    private byte[] createFormat12Subtable(Map<Integer, List<Integer>> ranges) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Format 12 헤더
        writeShort(out, 12); // format
        writeShort(out, 0);  // reserved
        writeInt(out, 0);    // length (나중에 업데이트)
        writeInt(out, 0);    // language
        writeInt(out, ranges.size()); // nGroups

        // 그룹 데이터 작성
        for (Map.Entry<Integer, List<Integer>> entry : ranges.entrySet()) {
            List<Integer> range = entry.getValue();
            int startCharCode = range.get(0);
            int endCharCode = range.get(range.size() - 1);
            int startGlyphID = entry.getKey();

            writeInt(out, startCharCode);
            writeInt(out, endCharCode);
            writeInt(out, startGlyphID);
        }

        // 길이 업데이트
        byte[] data = out.toByteArray();
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putInt(4, data.length);

        return data;
    }

    private void updateFontInfoGlyphMap(GlyphMapping glyphMapping) {
        // 기존 매핑을 새로운 글리프 ID로 업데이트
        for (Map.Entry<Character, Integer> entry : fontInfo.usedGlyph.entrySet()) {
            char unicode = entry.getKey();
            int oldGlyphId = entry.getValue();
            int newGlyphId = glyphMapping.getNewId(oldGlyphId);

            if (newGlyphId != -1) {
                fontInfo.usedGlyph.put(unicode, newGlyphId);
            }
        }
    }

    private byte[] buildSubsetFont() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            // 1. sfnt 버전 (0x00010000 for TrueType)
            writeInt(output, 0x00010000);

            // 2. 테이블 수
            writeShort(output, tables.size());

            // 3. 이진 검색 테이블
            int entrySelector = (int)(Math.log(tables.size()) / Math.log(2));
            int searchRange = (1 << entrySelector) * 16;
            int rangeShift = tables.size() * 16 - searchRange;

            writeShort(output, searchRange);
            writeShort(output, entrySelector);
            writeShort(output, rangeShift);

            // 4. 테이블 디렉토리
            // 테이블 오프셋 계산을 위해 헤더 크기 계산
            int tableOffset = 12 + tables.size() * 16;

            // 태그로 테이블 정렬
            Collections.sort(tables, (a, b) -> a.tag.compareTo(b.tag));

            // 디렉토리 엔트리 작성
            for (TableEntry entry : tables) {
                // 태그 작성
                output.write(entry.tag.getBytes(), 0, 4);

                // 체크섬 계산 및 작성
                entry.checkSum = calculateChecksum(entry.data);
                writeInt(output, entry.checkSum);

                // 새 오프셋 및 길이 작성
                writeInt(output, tableOffset);
                writeInt(output, entry.data.length);

                // 다음 테이블의 오프셋 계산 (4바이트 정렬)
                tableOffset += (entry.data.length + 3) & ~3;
            }

            // 5. 테이블 데이터 작성
            for (TableEntry entry : tables) {
                // 테이블 데이터 쓰기
                output.write(entry.data);

                // 4바이트 정렬을 위한 패딩 추가
                int padding = (-entry.data.length) & 3;
                for (int i = 0; i < padding; i++) {
                    output.write(0);
                }
            }

            // 6. head 테이블의 checkSumAdjustment 업데이트
            updateCheckSumAdjustment(output.toByteArray());

            return output.toByteArray();

        } catch (Exception e) {
            Log.e(TAG, "Error building subset font", e);
            return null;
        }
    }

    private void updateCheckSumAdjustment(byte[] fontData) {
        // head 테이블 찾기
        TableEntry headTable = findTable("head");
        if (headTable == null) return;

        // 전체 폰트 체크섬 계산
        long checkSum = 0;
        ByteBuffer bb = ByteBuffer.wrap(fontData);
        bb.order(ByteOrder.BIG_ENDIAN);

        int nLongs = (fontData.length + 3) / 4;
        for (int i = 0; i < nLongs; i++) {
            if (bb.remaining() >= 4) {
                checkSum += bb.getInt() & 0xFFFFFFFFL;
            } else {
                byte[] padding = new byte[4];
                bb.get(padding, 0, bb.remaining());
                ByteBuffer paddedBB = ByteBuffer.wrap(padding);
                paddedBB.order(ByteOrder.BIG_ENDIAN);
                checkSum += paddedBB.getInt() & 0xFFFFFFFFL;
            }
        }
        checkSum = checkSum & 0xFFFFFFFFL;

        // checkSumAdjustment 계산 및 업데이트
        long checkSumAdjustment = (0xB1B0AFBAL - checkSum) & 0xFFFFFFFFL;

        // head 테이블에서 checkSumAdjustment 필드 업데이트 (오프셋 8)
        ByteBuffer headBuffer = ByteBuffer.wrap(headTable.data);
        headBuffer.order(ByteOrder.BIG_ENDIAN);
        headBuffer.position(8);
        headBuffer.putInt((int)checkSumAdjustment);
    }

    private int calculateChecksum(byte[] data) {
        int sum = 0;
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.BIG_ENDIAN);

        int nlongs = (data.length + 3) / 4;
        for (int i = 0; i < nlongs; i++) {
            if (bb.remaining() >= 4) {
                sum += bb.getInt();
            } else {
                byte[] padding = new byte[4];
                bb.get(padding, 0, bb.remaining());
                ByteBuffer paddedBB = ByteBuffer.wrap(padding);
                paddedBB.order(ByteOrder.BIG_ENDIAN);
                sum += paddedBB.getInt();
            }
        }
        return sum;
    }

    private void writeInt(ByteArrayOutputStream out, int value) {
        out.write((value >> 24) & 0xFF);
        out.write((value >> 16) & 0xFF);
        out.write((value >> 8) & 0xFF);
        out.write(value & 0xFF);
    }

    private void writeShort(ByteArrayOutputStream out, int value) {
        out.write((value >> 8) & 0xFF);
        out.write(value & 0xFF);
    }

    private boolean isRequiredTable(String tag) {
        // PDF에 필수적인 테이블만 포함
        switch (tag) {
            case "head": // 폰트 헤더
            case "hhea": // 수평 헤더
            case "maxp": // 최대값 테이블
            case "loca": // 글리프 위치
            case "glyf": // 글리프 데이터
            case "cmap": // 문자 매핑
            case "hmtx": // 수평 메트릭
            case "name": // 이름 테이블
            case "post": // PostScript 정보
            case "OS/2": // OS/2 및 Windows 메트릭
                return true;
            default:
                return false;
        }
    }

    public TableEntry findTable(String tag) {
        for (TableEntry entry : tables) {
            if (entry.tag.equals(tag)) {
                return entry;
            }
        }
        return null;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (input != null) {
                input.clear();
            }
            for (TableEntry entry : tables) {
                entry.data = null;
                entry.glyphOffsets.clear();
            }
            tables.clear();
        } finally {
            super.finalize();
        }
    }
}