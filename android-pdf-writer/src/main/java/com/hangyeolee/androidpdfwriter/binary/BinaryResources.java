package com.hangyeolee.androidpdfwriter.binary;


import java.util.HashMap;
import java.util.Map;

/**
 * Resources 객체
 */
class BinaryResources extends BinaryObject {
    private final Map<String, BinaryFont> fonts = new HashMap<>();
    private final Map<String, BinaryXObject> xObjects = new HashMap<>();

    public BinaryResources(int objectNumber) {
        super(objectNumber);
    }

    public void addFont(String name, BinaryFont font) {
        fonts.put(name, font);
        addDependency(font);
    }

    public void addXObject(String name, BinaryXObject xObject) {
        xObjects.put(name, xObject);
        addDependency(xObject);
    }

    @Override
    public String toDictionaryString() {
        StringBuilder fontDict = new StringBuilder("<<\n");
        for (Map.Entry<String, BinaryFont> entry : fonts.entrySet()) {
            fontDict.append("/").append(entry.getKey()).append(" ")
                    .append(entry.getValue().getObjectNumber()).append(" 0 R\n");
        }
        fontDict.append(">>");
        dictionary.put("/Font", fontDict.toString());

        if (!xObjects.isEmpty()) {
            StringBuilder xObjectDict = new StringBuilder("<<\n");
            for (Map.Entry<String, BinaryXObject> entry : xObjects.entrySet()) {
                xObjectDict.append("/").append(entry.getKey()).append(" ")
                        .append(entry.getValue().getObjectNumber()).append(" 0 R\n");
            }
            xObjectDict.append(">>");
            dictionary.put("/XObject", xObjectDict.toString());
        }

        dictionary.put("ProcSet", "[/PDF /Text /ImageB /ImageC /ImageI]");
        return super.toDictionaryString();
    }
}
