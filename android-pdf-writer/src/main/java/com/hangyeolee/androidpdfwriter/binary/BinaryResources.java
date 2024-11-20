package com.hangyeolee.androidpdfwriter.binary;


import java.util.HashMap;
import java.util.Map;

/**
 * Resources 객체
 */
class BinaryResources extends BinaryDictionary {
    private final Map<String, BinaryFont> fonts = new HashMap<>();
    private final Map<String, BinaryXObject> xObjects = new HashMap<>();

    public BinaryResources(int objectNumber) {
        super(objectNumber);
    }

    public void addFont(String name, BinaryFont font) {
        fonts.put(name, font);
    }

    public void addXObject(String name, BinaryXObject xObject) {
        xObjects.put(name, xObject);
    }

    @Override
    public String toDictionaryString() {
        StringBuilder procSet = new StringBuilder("[/PDF ");
        if(!fonts.isEmpty()) {
            procSet.append("/Text ");
            StringBuilder fontDict = new StringBuilder("<< ");
            for (Map.Entry<String, BinaryFont> entry : fonts.entrySet()) {
                fontDict.append("/").append(entry.getKey()).append(" ")
                        .append(entry.getValue().getObjectNumber()).append(" 0 R ");
            }
            fontDict.append(">>");
            dictionary.put("/Font", fontDict.toString());
        }

        if (!xObjects.isEmpty()) {
            procSet.append("/ImageB /ImageC /ImageI ");
            StringBuilder xObjectDict = new StringBuilder("<< ");
            for (Map.Entry<String, BinaryXObject> entry : xObjects.entrySet()) {
                xObjectDict.append("/").append(entry.getKey()).append(" ")
                        .append(entry.getValue().getObjectNumber()).append(" 0 R ");
            }
            xObjectDict.append(">>");
            dictionary.put("/XObject", xObjectDict.toString());
        }

        procSet.append("]");
        dictionary.put("/ProcSet", procSet.toString());
        return super.toDictionaryString();
    }
}
