package com.hangyeolee.pdf.core.binary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BinaryDictionary extends BinaryObject{
    protected Map<String, Object> dictionary;  // PDF 딕셔너리 데이터

    public BinaryDictionary(int objectNumber) {
        super(objectNumber);
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
}
