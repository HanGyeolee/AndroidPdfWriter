package com.hangyeolee.androidpdfwriter.binary;

import java.util.ArrayList;
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

    /**
     * 객체의 딕셔너리 문자열 생성
     */
    public String toDictionaryString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<<\n");
        for (Map.Entry<String, Object> entry : dictionary.entrySet()) {
            sb.append(entry.getKey()).append(" ");
            Object value = entry.getValue();
            if (value instanceof BinaryObject) {
                sb.append(((BinaryObject)value).getObjectNumber()).append(" 0 R");
            } else {
                sb.append(value.toString());
            }
            sb.append("\n");
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
