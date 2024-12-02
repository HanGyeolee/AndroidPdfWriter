package com.hangyeolee.pdf.core.binary;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.hangyeolee.pdf.core.listener.Action;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * PDF 객체들을 관리하는 클래스
 */
class BinaryObjectManager {
    private int nextObjectNumber = 1;
    private final Map<Integer, BinaryObject> objects = new HashMap<>();

    public long byteLength = 0;
    public final ArrayList<XRef> XRefs = new ArrayList<>(6);

    /**
     * 새로운 PDF 객체 생성
     */
    public <T extends BinaryObject> T createObject(Action<Integer, T> creator) {
        T object = creator.invoke(nextObjectNumber++);
        objects.put(object.getObjectNumber(), object);
        return object;
    }

    public void writeAllObjects(OutputStream bufos) throws IOException {
        // 1. Map의 엔트리들을 List로 변환
        List<Map.Entry<Integer, BinaryObject>> entries = new ArrayList<>(objects.entrySet());

        // 2. objectNumber를 기준으로 정렬
        Collections.sort(entries, (o1, o2) -> o1.getKey().compareTo(o2.getKey()));

        // 3. 정렬된 BinaryObject들 저장
        for (Map.Entry<Integer, BinaryObject> entry : entries) {
            writeObject(entry.getValue(), bufos);
        }
    }

    private void writeObject(BinaryObject obj, OutputStream bufos) throws IOException {
        int length = addObject(obj, bufos);
        // xref 정보 추가
        addXRef((byte) 'n');
        byteLength += length;
    }

    private int addObject(BinaryObject obj, OutputStream bufos) throws IOException {
        int length = 0;
        byte[] tmp;

        // {객체 번호} {세대 번호} obj
        // 객체 헤더
        tmp = BinaryConverter.toBytes(obj.getObjectNumber() + " 0 obj\r\n");
        bufos.write(tmp);
        length += tmp.length;
        if(obj instanceof BinaryDictionary){
            // 객체 딕셔너리
            tmp = BinaryConverter.toBytes(((BinaryDictionary)obj).toDictionaryString());
            bufos.write(tmp);
            length += tmp.length;
        }
        // 스트림 데이터가 있다면 쓰기
        {
            byte[] streamData = obj.getStreamData();
            if (streamData != null) {
                tmp = BinaryConverter.toBytes("stream\r\n");
                bufos.write(tmp);
                length += tmp.length;
                bufos.write(streamData);
                length += streamData.length;

                tmp = BinaryConverter.toBytes("\r\nendstream");
                bufos.write(tmp);
                length += tmp.length;
            }
        }

        // 객체 끝
        tmp = BinaryConverter.toBytes("\r\nendobj\r\n");
        bufos.write(tmp);
        length += tmp.length;

        return length;
    }

    public void addXRef(int value, byte b){
        XRefs.add(new XRef(byteLength, value, b));
    }
    private void addXRef(byte b){
        XRefs.add(new XRef(byteLength, b));
    }
    public void writeXref(OutputStream bufos) throws IOException {
        bufos.write(BinaryConverter.toBytes("xref\r\n" + "0 " + XRefs.size() + "\r\n"));
        for(XRef b: XRefs){
            bufos.write(b.write());
        }
    }
    public void wrtieTrailer(OutputStream bufos) throws IOException {
        int infoObjNum;
        BinaryObject foundObject = findFirstObject(objects, BinaryInfo.class);
        if (foundObject != null) {
            infoObjNum = foundObject.getObjectNumber();
        } else {
            throw new NullPointerException("Can not find Info Type");
        }

        int catalogObjNum;
        foundObject = findFirstObject(objects, BinaryCatalog.class);
        if (foundObject != null) {
            catalogObjNum = foundObject.getObjectNumber();
        } else {
            throw new NullPointerException("Can not find Catalog Type");
        }

        bufos.write(BinaryConverter.toBytes("trailer\r\n" +
                "<<\r\n"+
                "/Root "+catalogObjNum+" 0 R\r\n" +
                "/Info "+infoObjNum+" 0 R\r\n" +
                "/Size " + XRefs.size() + ">>\r\n" +
                "startxref\r\n" + byteLength
                ));
        bufos.write(BinaryConverter.toBytes("\r\n%%EOF\r\n"));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public BinaryObject findFirstObjectModern(Map<Integer, BinaryObject> objects, Class<? extends BinaryObject> targetClass) {
        return objects.values()
                .stream()
                .filter(obj -> obj.getClass().equals(targetClass))
                .findFirst()
                .orElse(null);
    }

    public BinaryObject findFirstObjectLegacy(Map<Integer, BinaryObject> objects, Class<? extends BinaryObject> targetClass) {
        for (BinaryObject obj : objects.values()) {
            if (obj.getClass().equals(targetClass)) {
                return obj;
            }
        }
        return null;
    }

    public BinaryObject findFirstObject(Map<Integer, BinaryObject> objects, Class<? extends BinaryObject> targetClass) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return findFirstObjectModern(objects, targetClass);
        } else {
            return findFirstObjectLegacy(objects, targetClass);
        }
    }

}