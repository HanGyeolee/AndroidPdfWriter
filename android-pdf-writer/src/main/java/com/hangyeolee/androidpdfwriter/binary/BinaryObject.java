package com.hangyeolee.androidpdfwriter.binary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PDF 객체를 표현하는 클래스
 */
class BinaryObject {
    private final int objectNumber;
    private final List<BinaryObject> dependencies;  // 이 객체가 참조하는 다른 객체들
    protected Map<String, Object> dictionary;  // PDF 딕셔너리 데이터

    public BinaryObject(int objectNumber) {
        this.objectNumber = objectNumber;
        this.dependencies = new ArrayList<>();
        this.dictionary = new HashMap<>();
    }

    public void setSubtype(String type){
        dictionary.put("/Subtype", "/" + type);
    }

    /**
     * 객체의 딕셔너리 문자열 생성
     */
    public String toDictionaryString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<<");

        // /Type과 /Subtype을 먼저 추가
        if (dictionary.containsKey("/Type")) {
            sb.append("/Type ").append(dictionary.get("/Type")).append("\r\n");
        }
        if (dictionary.containsKey("/Subtype")) {
            sb.append("/Subtype ").append(dictionary.get("/Subtype")).append("\r\n");
        }

        // 나머지 키를 알파벳 순서로 정렬하되, /Widths와 /Length는 제외
        List<String> keys = new ArrayList<>(dictionary.keySet());
        keys.remove("/Type");
        keys.remove("/Subtype");
        keys.remove("/Widths");
        keys.remove("/Length");
        keys.remove("/FontFile3를");
        Collections.sort(keys);

        for (String key : keys) {
            Object value = dictionary.get(key);
            sb.append(key).append(" ");
            if (value instanceof BinaryObject) {
                sb.append(((BinaryObject)value).getObjectNumber()).append(" 0 R");
            } else {
                sb.append(value.toString());
            }
            sb.append("\r\n");
        }

        // /Widths와 /Length, /FontFile3를 마지막에 추가
        if (dictionary.containsKey("/FontFile3")) {
            sb.append("/FontFile3 ").append(dictionary.get("/Length")).append("\r\n");
        }
        if (dictionary.containsKey("/Widths")) {
            sb.append("/Widths ").append(dictionary.get("/Widths")).append("\r\n");
        }
        if (dictionary.containsKey("/Length")) {
            sb.append("/Length ").append(dictionary.get("/Length")).append("\r\n");
        }

        sb.append(">>");
        return sb.toString();
    }

    /**
     * 스트림이 있는 경우 오버라이드
     */
    public byte[] getStreamData() { return null; }

    public int getObjectNumber() { return objectNumber; }
    public void addDependency(BinaryObject obj) { dependencies.add(obj); }
    public List<BinaryObject> getDependencies() { return dependencies; }
}
