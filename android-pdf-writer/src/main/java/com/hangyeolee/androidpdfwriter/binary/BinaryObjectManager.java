package com.hangyeolee.androidpdfwriter.binary;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.hangyeolee.androidpdfwriter.listener.Action;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * PDF 객체들을 관리하는 클래스
 */
class BinaryObjectManager {
    public static final Charset US_ASCII = Charset.forName("US-ASCII");

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

    public void writeAllObjects(BufferedOutputStream bufos) throws IOException {
        // 객체들의 의존성을 고려한 순서로 쓰기
        Set<BinaryObject> written = new HashSet<>();
        for (BinaryObject obj : objects.values()) {
            writeObjectWithDependencies(obj, written, bufos);
        }
    }

    private void writeObjectWithDependencies(BinaryObject obj, Set<BinaryObject> written,
                                             BufferedOutputStream bufos) throws IOException {
        // 이미 쓴 객체는 건너뛰기
        if (written.contains(obj)) return;

        // 먼저 의존성 있는 객체들 쓰기
        for (BinaryObject dep : obj.getDependencies()) {
            writeObjectWithDependencies(dep, written, bufos);
        }

        // 객체 데이터 쓰기
        writeObject(obj, bufos);
        written.add(obj);
    }

    private void writeObject(BinaryObject obj, BufferedOutputStream bufos) throws IOException {
        byte[] data = addObject(obj);
        bufos.write(data);
        // xref 정보 추가
        addXRef((byte) 'n');
        byteLength += data.length;
    }

    // 유틸리티 메서드들...
    private byte[] ASCII(String s) {
        return s.getBytes(US_ASCII);
    }

    private byte[] addObject(BinaryObject obj){
        // 딕셔너리
        byte[] dictionary = ASCII(obj.toDictionaryString());
        // 스트림 데이터가 있다면 쓰기
        byte[] streamData = obj.getStreamData();
        byte[] data;
        int sum = 0;
        if (streamData != null) {
            byte[] streamHeader = ASCII("\nstream\n");
            byte[] streamFooter = ASCII("\nendstream");
            data = new byte[
                    dictionary.length +
                    streamHeader.length +
                    streamData.length +
                    streamFooter.length
            ];
            System.arraycopy(dictionary, 0, data, sum, dictionary.length);
            sum += dictionary.length;
            System.arraycopy(streamHeader, 0, data, sum, streamHeader.length);
            sum += streamHeader.length;
            System.arraycopy(streamData, 0, data, sum, streamData.length);
            sum += streamData.length;
            System.arraycopy(streamFooter, 0, data, sum, streamFooter.length);
        }
        else {
            data = new byte[
                dictionary.length
            ];
            System.arraycopy(dictionary, 0, data, 0, dictionary.length);
        }

        // {객체 번호} {세대 번호} obj
        // 객체 헤더
        byte[] header = ASCII(obj.getObjectNumber() + " 0 obj\n");
        // 객체 끝
        byte[] footer = ASCII("\nendobj\n");
        byte[] result = new byte[header.length + data.length + footer.length];

        sum = 0;
        System.arraycopy(header, 0, result, sum, header.length);
        sum += header.length;
        System.arraycopy(data, 0, result, sum, data.length);
        sum += data.length;
        System.arraycopy(footer, 0, result, sum, footer.length);

        return result;
    }

    public void addXRef(int value, byte b){
        XRefs.add(new XRef(byteLength,value, b));
    }
    private void addXRef(byte b){
        XRefs.add(new XRef(byteLength, b));
    }
    public void writeXref(BufferedOutputStream bufos) throws IOException {
        bufos.write(ASCII("xref\n" + "0 " + XRefs.size() + "\n"));
        for(XRef b: XRefs){
            bufos.write(b.write());
        }
    }
    public void wrtieTrailer(BufferedOutputStream bufos) throws IOException {
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

        bufos.write(ASCII("trailer\n" +
                "<< /Size " + XRefs.size() + "\n" +
                "/Info "+infoObjNum+" 0 R\n" +
                "/Root "+catalogObjNum+" 0 R\n>>\n" +
                "startxref\n" + byteLength
                ));
        bufos.write(ASCII("\n%%EOF"));
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