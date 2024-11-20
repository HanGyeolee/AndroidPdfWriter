package com.hangyeolee.androidpdfwriter.binary;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FontSubSetter {
    public static String TAG = "FONTSUBSET";
    public static byte[] fontSubsetting(byte[] fontStream, Set<Character> fontBytes){
        // Unicode to Glyph ID 매핑 추출 (cmap 테이블)
        Map<Integer, Integer> unicodeToGID = extractCmapTable(fontStream);
        Set<Integer> glyphIDs = getRequiredGlyphIDs(unicodeToGID, fontBytes);
        Map<Integer, byte[]> glyfDataMap = extractGlyfData(fontStream, glyphIDs);
        try {
            return createSubsetFont(fontStream, glyfDataMap);
        } catch (Exception e){
            Log.e(TAG,"Out Of Memory : createSubsetFont Method", e);
            return null;
        }
    }

    /**
     * Extracts Unicode to Glyph ID mappings from the cmap table in a TTF file.
     *
     * @param fontStream The byte array of the TTF font file.
     * @return A map of Unicode characters to Glyph IDs.
     */
    protected static Map<Integer, Integer> extractCmapTable(byte[] fontStream) {
        Map<Integer, Integer> unicodeToGID = new HashMap<>();

        ByteBuffer buffer = ByteBuffer.wrap(fontStream).order(ByteOrder.BIG_ENDIAN);

        // Step 1: Locate cmap table
        int cmapOffset = findTableOffset(fontStream, "cmap");
        if (cmapOffset == -1) {
            throw new IllegalArgumentException("cmap table not found in font.");
        }

        buffer.position(cmapOffset);

        // Step 2: Read cmap header
        int version = buffer.getShort(); // Version (0 or 1)
        int numTables = buffer.getShort(); // Number of subtables

        // Step 3: Locate the desired subtable (Platform ID = 3, Encoding ID = 1)
        int subtableOffset = -1;
        for (int i = 0; i < numTables; i++) {
            int platformID = buffer.getShort();
            int encodingID = buffer.getShort();
            int offset = buffer.getInt();

            if (platformID == 3 && encodingID == 1) { // Windows Unicode
                subtableOffset = cmapOffset + offset;
                break;
            }
        }

        if (subtableOffset == -1) {
            throw new IllegalArgumentException("Windows Unicode cmap subtable not found.");
        }

        // Step 4: Read the subtable (Format 4 assumed)
        buffer.position(subtableOffset);
        int format = buffer.getShort(); // Format type
        if (format != 4) {
            throw new UnsupportedOperationException("Only Format 4 cmap tables are supported.");
        }

        // Step 5: Parse Format 4 subtable
        buffer.getShort(); // Length (skip)
        buffer.getShort(); // Language (skip)
        int segCountX2 = buffer.getShort();
        int segCount = segCountX2 / 2;

        buffer.getShort(); // SearchRange (skip)
        buffer.getShort(); // EntrySelector (skip)
        buffer.getShort(); // RangeShift (skip)

        // Read arrays in Format 4
        int[] endCodes = new int[segCount];
        int[] startCodes = new int[segCount];
        int[] idDeltas = new int[segCount];
        int[] idRangeOffsets = new int[segCount];

        for (int i = 0; i < segCount; i++) {
            endCodes[i] = buffer.getShort() & 0xFFFF; // Unsigned short
        }

        buffer.getShort(); // ReservedPad (skip)

        for (int i = 0; i < segCount; i++) {
            startCodes[i] = buffer.getShort() & 0xFFFF;
        }

        for (int i = 0; i < segCount; i++) {
            idDeltas[i] = buffer.getShort();
        }

        int rangeOffsetStart = buffer.position();
        for (int i = 0; i < segCount; i++) {
            idRangeOffsets[i] = buffer.getShort() & 0xFFFF;
        }

        // Step 6: Map Unicode to Glyph ID
        for (int i = 0; i < segCount; i++) {
            for (int code = startCodes[i]; code <= endCodes[i]; code++) {
                if (idRangeOffsets[i] == 0) {
                    // Direct mapping
                    int gid = (code + idDeltas[i]) & 0xFFFF;
                    unicodeToGID.put(code, gid);
                } else {
                    // Indirect mapping via glyph array
                    int glyphIndexOffset = rangeOffsetStart + (idRangeOffsets[i] / 2 + (code - startCodes[i]));
                    int gid = buffer.getShort(glyphIndexOffset * 2) & 0xFFFF;
                    unicodeToGID.put(code, gid);
                }
            }
        }

        return unicodeToGID;
    }

    /**
     * Finds the offset of a specific table in the TTF file.
     *
     * @param fontStream The byte array of the TTF font file.
     * @param tableName  The 4-character table name (e.g., "cmap").
     * @return The offset of the table, or -1 if not found.
     */
    private static int findTableOffset(byte[] fontStream, String tableName) {
        ByteBuffer buffer = ByteBuffer.wrap(fontStream).order(ByteOrder.BIG_ENDIAN);
        buffer.position(4); // Skip sfnt version

        int numTables = buffer.getShort(); // Number of tables
        buffer.getShort(); // Skip searchRange
        buffer.getShort(); // Skip entrySelector
        buffer.getShort(); // Skip rangeShift

        for (int i = 0; i < numTables; i++) {
            byte[] nameBytes = new byte[4];
            buffer.get(nameBytes); // Table name
            String name = new String(nameBytes);

            int checksum = buffer.getInt(); // Skip checksum
            int offset = buffer.getInt(); // Table offset
            int length = buffer.getInt(); // Table length

            if (name.equals(tableName)) {
                return offset;
            }
        }

        return -1;
    }

    protected static Set<Integer> getRequiredGlyphIDs(Map<Integer, Integer> unicodeToGID, Set<Character> fontBytes) {
        Set<Integer> requiredGlyphIDs = new HashSet<>();

        // Iterate over the fontBytes (actual used Unicode characters)
        for (Character unicode : fontBytes) {
            int unicodeValue = unicode; // Convert Character to int (Unicode value)
            if (unicodeToGID.containsKey(unicodeValue)) {
                requiredGlyphIDs.add(unicodeToGID.get(unicodeValue));
            }
        }

        return requiredGlyphIDs;
    }

    /**
     * Extracts the glyf table for specific glyph IDs.
     *
     * @param fontStream  The byte array of the TTF font file.
     * @param glyphIDs    The set of glyph IDs to extract.
     * @return A map of glyph IDs to their binary data.
     */
    protected static Map<Integer, byte[]> extractGlyfData(byte[] fontStream, Set<Integer> glyphIDs) {
        Map<Integer, byte[]> glyfDataMap = new HashMap<>();

        ByteBuffer buffer = ByteBuffer.wrap(fontStream).order(ByteOrder.BIG_ENDIAN);

        // Step 1: Locate required tables
        int glyfOffset = findTableOffset(fontStream, "glyf");
        int locaOffset = findTableOffset(fontStream, "loca");
        int maxpOffset = findTableOffset(fontStream, "maxp");
        int headOffset = findTableOffset(fontStream, "head");

        if (glyfOffset == -1 || locaOffset == -1 || maxpOffset == -1 || headOffset == -1) {
            throw new IllegalArgumentException("Required tables (glyf, loca, maxp, head) not found in font.");
        }

        // Step 2: Determine loca table format (short or long)
        buffer.position(headOffset + 51); // loca format flag is at offset 51 in head table
        boolean isLocaLongFormat = buffer.getShort() == 1; // 1 = long, 0 = short

        // Step 3: Get the number of glyphs from maxp table
        buffer.position(maxpOffset + 4); // Number of glyphs is at offset 4 in maxp table
        int numGlyphs = buffer.getShort() & 0xFFFF;

        // Step 4: Parse loca table to get glyph offsets
        List<Integer> locaOffsets = parseLocaTable(buffer, locaOffset, numGlyphs, isLocaLongFormat);

        // Step 5: Extract glyf data for required glyph IDs
        for (int glyphID : glyphIDs) {
            if (glyphID < 0 || glyphID >= numGlyphs) {
                continue; // Invalid glyph ID
            }

            int startOffset = glyfOffset + locaOffsets.get(glyphID);
            int endOffset = glyfOffset + locaOffsets.get(glyphID + 1);

            if (startOffset != endOffset) {
                byte[] glyphData = Arrays.copyOfRange(fontStream, startOffset, endOffset);
                glyfDataMap.put(glyphID, glyphData);
            }
        }

        return glyfDataMap;
    }

    /**
     * Parses the loca table to get a list of glyph offsets.
     *
     * @param buffer          The ByteBuffer wrapping the font data.
     * @param locaOffset      The offset of the loca table.
     * @param numGlyphs       The number of glyphs in the font.
     * @param isLongFormat    Whether the loca table uses long format (32-bit offsets).
     * @return A list of glyph offsets.
     */
    private static List<Integer> parseLocaTable(ByteBuffer buffer, int locaOffset, int numGlyphs, boolean isLongFormat) {
        List<Integer> offsets = new ArrayList<>(numGlyphs + 1);

        buffer.position(locaOffset);

        for (int i = 0; i <= numGlyphs; i++) {
            if (isLongFormat) {
                offsets.add(buffer.getInt());
            } else {
                offsets.add(buffer.getShort() & 0xFFFF); // Unsigned short
            }
        }

        return offsets;
    }

    /**
     * Create a subset font using the provided glyf data and other necessary font tables.
     *
     * @param originalFont   Original TTF font as a byte array.
     * @param glyfDataMap    Map of glyph IDs to their binary glyf data.
     * @throws Exception     If an error occurs during font creation.
     */
    public static byte[] createSubsetFont(byte[] originalFont, Map<Integer, byte[]> glyfDataMap) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(originalFont).order(ByteOrder.BIG_ENDIAN);

        // Locate required tables in the original font
        int locaOffset = findTableOffset(originalFont, "loca");
        int headOffset = findTableOffset(originalFont, "head");
        int maxpOffset = findTableOffset(originalFont, "maxp");
        int hmtxOffset = findTableOffset(originalFont, "hmtx");

        if (locaOffset == -1 || headOffset == -1 || maxpOffset == -1 || hmtxOffset == -1) {
            throw new IllegalArgumentException("Required tables (loca, head, maxp, hmtx) not found in font.");
        }

        // Read number of glyphs and loca format from maxp and head tables
        buffer.position(maxpOffset + 4);
        int numGlyphs = buffer.getShort() & 0xFFFF;

        buffer.position(headOffset + 51);
        boolean isLocaLongFormat = buffer.getShort() == 1;

        // Build loca and glyf tables based on glyfDataMap
        List<Integer> newLocaOffsets = new ArrayList<>();
        ByteArrayOutputStream glyfTableStream = new ByteArrayOutputStream();

        int currentOffset = 0;
        for (int i = 0; i < numGlyphs; i++) {
            if (glyfDataMap.containsKey(i)) {
                byte[] glyphData = glyfDataMap.get(i);
                glyfTableStream.write(glyphData);
                newLocaOffsets.add(currentOffset);
                currentOffset += glyphData.length;
            } else {
                newLocaOffsets.add(currentOffset);
            }
        }
        newLocaOffsets.add(currentOffset); // Final offset for loca table
        byte[] glyfTable = glyfTableStream.toByteArray();

        // Write the loca table
        ByteArrayOutputStream locaTableStream = new ByteArrayOutputStream();
        for (int offset : newLocaOffsets) {
            if (isLocaLongFormat) {
                locaTableStream.write(ByteBuffer.allocate(4).putInt(offset).array());
            } else {
                locaTableStream.write(ByteBuffer.allocate(2).putShort((short) (offset / 2)).array());
            }
        }
        byte[] locaTable = locaTableStream.toByteArray();

        // Copy other required tables
        Map<String, byte[]> tables = new HashMap<>();
        tables.put("glyf", glyfTable);
        tables.put("loca", locaTable);
        tables.put("head", copyTable(originalFont, "head"));
        tables.put("maxp", updateMaxpTable(originalFont, glyfDataMap.size()));
        tables.put("hmtx", copyTable(originalFont, "hmtx"));

        // Generate the new font binary
        return buildFont(tables);
    }

    /**
     * Copy a table from the original font.
     */
    private static byte[] copyTable(byte[] fontData, String tableName) {
        int offset = findTableOffset(fontData, tableName);
        if (offset == -1) {
            return null; // Table not found
        }

        ByteBuffer buffer = ByteBuffer.wrap(fontData).order(ByteOrder.BIG_ENDIAN);
        buffer.position(offset);
        int length = buffer.getInt(); // Length from table record
        byte[] tableData = new byte[length];
        buffer.get(tableData);
        return tableData;
    }

    /**
     * Update the maxp table for the subset font.
     */
    private static byte[] updateMaxpTable(byte[] originalFont, int newNumGlyphs) {
        byte[] maxpTable = copyTable(originalFont, "maxp");
        if (maxpTable == null) {
            throw new IllegalArgumentException("maxp table not found in font.");
        }

        ByteBuffer buffer = ByteBuffer.wrap(maxpTable).order(ByteOrder.BIG_ENDIAN);
        buffer.position(4); // Number of glyphs field
        buffer.putShort((short) newNumGlyphs); // Update number of glyphs
        return maxpTable;
    }

    /**
     * Build the new font binary using the provided tables.
     */
    private static byte[] buildFont(Map<String, byte[]> tables) throws Exception {
        ByteArrayOutputStream fontStream = new ByteArrayOutputStream();

        // Write SFNT header
        fontStream.write(ByteBuffer.allocate(12)
                .putInt(0x00010000) // Version 1.0
                .putShort((short) tables.size())
                .putShort((short) (16 * tables.size())) // Search range
                .putShort((short) (int) (Math.log(tables.size()) / Math.log(2)))
                .putShort((short) (tables.size() * 16 - (int) Math.pow(2, (int) Math.log(tables.size()) / Math.log(2))))
                .array());

        // Write table records
        int offset = 12 + tables.size() * 16; // Start after table records
        Map<String, Integer> tableOffsets = new HashMap<>();
        for (String tag : tables.keySet()) {
            byte[] tableData = tables.get(tag);
            fontStream.write(tag.getBytes()); // Table tag
            fontStream.write(ByteBuffer.allocate(4).putInt(calculateChecksum(tableData)).array());
            fontStream.write(ByteBuffer.allocate(4).putInt(offset).array());
            fontStream.write(ByteBuffer.allocate(4).putInt(tableData.length).array());
            tableOffsets.put(tag, offset);
            offset += tableData.length;
            if (offset % 4 != 0) {
                offset += 4 - (offset % 4); // Align to 4-byte boundary
            }
        }

        // Write table data
        for (String tag : tables.keySet()) {
            byte[] tableData = tables.get(tag);
            fontStream.write(tableData);
            if (tableData.length % 4 != 0) {
                fontStream.write(new byte[4 - (tableData.length % 4)]); // Align to 4-byte boundary
            }
        }

        return fontStream.toByteArray();
    }

    /**
     * Calculate checksum for a table.
     */
    private static int calculateChecksum(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);
        int sum = 0;
        while (buffer.remaining() >= 4) {
            sum += buffer.getInt();
        }
        return sum;
    }
}
