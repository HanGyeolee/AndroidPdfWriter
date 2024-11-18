package com.hangyeolee.androidpdfwriter.binary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * PDF 객체를 표현하는 클래스
 */
class BinaryObject {
    private final int objectNumber;
    protected Map<String, Object> dictionary;  // PDF 딕셔너리 데이터

    public BinaryObject(int objectNumber) {
        this.objectNumber = objectNumber;
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

        // 나머지 키를 알파벳 순서로 정렬하되, /Widths와 /Length는 제외
        List<String> keys = new ArrayList<>(dictionary.keySet());
        Collections.sort(keys);

        for (String key : keys) {
            Object value = dictionary.get(key);
            sb.append(key).append(instance(value));
        }

        sb.append(">>");
        return sb.toString();
    }

    private String instance(Object object){
        if (object instanceof BinaryObject) {
            return (" " +((BinaryObject)object).getObjectNumber()+" 0 R ");
        } else if (object instanceof Number) {
            return " " + object;
        } else {
            return object.toString();
        }
    }
    /**
     * 스트림이 있는 경우 오버라이드
     */
    public byte[] getStreamData() { return null; }

    public int getObjectNumber() { return objectNumber; }

    /**
     * 단일 숫자에 대한 포맷팅
     * - 0인 경우: "0"
     * - 0이 아닌 경우: 소수점 아래 유효숫자만 표시
     */
    protected static String formatNumber(float number) {
        if (number == 0f) {
            return "0";
        }

        // 숫자를 문자열로 변환 (지수 표기법 없이)
        String str = String.format(Locale.US, "%.5f", number);

        // 후행 0 제거
        str = str.replaceAll("0*$", "");

        // 소수점이 마지막 문자인 경우 제거
        if (str.endsWith(".")) {
            str = str.substring(0, str.length() - 1);
        }

        return str;
    }
}
