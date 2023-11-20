package com.hangyeolee.androidpdfwriter.binary;

import android.graphics.Bitmap;
import android.graphics.RectF;

import com.hangyeolee.androidpdfwriter.listener.Action;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.zip.Deflater;

class BinaryImage {
    int objNumber;
    byte[] stream;
    float widthPage, heightPage;
    int width, height;

    public int pageNumber;
    public int currentNumber;

    protected BinaryImage(int n, Bitmap bitmap, int widthPage, int heightPage){
        currentNumber = objNumber = n;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress( Bitmap.CompressFormat.JPEG, BinarySingleton.getInstance().quality, stream);
        this.stream = stream.toByteArray();

        this.width = bitmap.getWidth();
        this.height = bitmap.getHeight();

        this.widthPage = widthPage;
        this.heightPage = heightPage;
    }
    protected byte[] getContentOfPage(RectF contentRect, int ParentNumber){
        StringBuilder tmp;
        byte[] tmp_b;
        byte[] stream_b = addObject(addStream());
        BinarySingleton.getInstance().addXRef((byte) 'n');
        BinarySingleton.getInstance().byteLength += stream_b.length;

        tmp = new StringBuilder();
        tmp.append("q\n")
            .append(contentRect.width()).append(" 0 0 ").append(contentRect.height())
                .append(" ").append(contentRect.left).append(" ").append(contentRect.top).append(" cm\n")
            .append("/X").append(objNumber).append(" Do\n")
            .append("Q");
        tmp_b = tmp.toString().getBytes(BinarySingleton.US_ASCII);

        tmp = new StringBuilder();
        tmp.append("<< /Length ").append(tmp_b.length).append(" >>");
        // contentNumber = objNumber+1
        byte[] content_b = addObject(addStream(tmp.toString(), tmp_b));
        BinarySingleton.getInstance().addXRef((byte) 'n');
        BinarySingleton.getInstance().byteLength += content_b.length;

        tmp = new StringBuilder();
        tmp.append("<<\n/Type /Page\n")
                .append("/Resources ")
                    .append("<<\n/ProcSet [/PDF /Text /ImageB /ImageC /ImageI]")
                    .append("\n/XObject << /X").append(objNumber).append(" ")
                    .append(objNumber).append(" 0 R >>>>\n")
                .append("/MediaBox [0 0 ").append(Math.round(widthPage)).append(" ").append(Math.round(heightPage)).append("]\n")
                .append("/Contents ").append(objNumber+1).append(" 0 R\n")
                .append("/StructParents 0\n")
                .append("/Parent ").append(ParentNumber).append(" 0 R\n>>");
        pageNumber = objNumber+2;
        byte[] page_b = addObject(addDictionary(tmp.toString()));
        BinarySingleton.getInstance().addXRef((byte) 'n');
        BinarySingleton.getInstance().byteLength += page_b.length;

        tmp_b = new byte[stream_b.length + content_b.length + page_b.length];
        int i = 0;
        for(int j = 0; j < stream_b.length; j++, i++){
            tmp_b[i] = stream_b[j];
        }
        for(int j = 0; j < content_b.length; j++, i++){
            tmp_b[i] = content_b[j];
        }
        for(int j = 0; j < page_b.length; j++, i++){
            tmp_b[i] = page_b[j];
        }
        return tmp_b;
    }

    private byte[] addDictionary(String s){
        return s.getBytes(BinarySingleton.US_ASCII);
    }
    private byte[] addStream(String s, byte[] data){
        StringBuilder start = new StringBuilder();
        start.append(s).append("stream\n");
        byte[] start_b = start.toString().getBytes(BinarySingleton.US_ASCII);
        StringBuilder end = new StringBuilder();
        end.append("\nendstream");
        byte[] end_b  = end.toString().getBytes(BinarySingleton.US_ASCII);
        byte[] re = new byte[start_b.length + data.length + end_b.length];

        int i = 0;
        for(int j = 0; j < start_b.length; j++, i++){
            re[i] = start_b[j];
        }
        for(int j = 0; j < data.length; j++, i++){
            re[i] = data[j];
        }
        for(int j = 0; j < end_b.length; j++, i++){
            re[i] = end_b[j];
        }
        return re;
    }
    private byte[] addStream() {
        StringBuilder start = new StringBuilder();
        // {객체 번호} {세대 번호} obj
        start.append("<< /Type /XObject\n")
                .append("/Subtype /Image\n")
                .append("/Width ").append(Math.round(this.width)).append("\n")
                .append("/Height ").append(Math.round(this.height)).append("\n")
                .append("/ColorSpace /DeviceRGB\n")
                .append("/BitsPerComponent 8\n")
                .append("/Filter /DCTDecode\n")
                .append("/Length ").append(stream.length).append(">>\nstream\n");
        byte[] start_b = start.toString().getBytes(BinarySingleton.US_ASCII);

        StringBuilder end = new StringBuilder();
        end.append("\nendstream");
        byte[] end_b  = end.toString().getBytes(BinarySingleton.US_ASCII);

        byte[] re = new byte[start_b.length + stream.length + end_b.length];

        int i = 0;
        for(int j = 0; j < start_b.length; j++, i++){
            re[i] = start_b[j];
        }
        for(int j = 0; j < stream.length; j++, i++){
            re[i] = stream[j];
        }
        for(int j = 0; j < end_b.length; j++, i++){
            re[i] = end_b[j];
        }
        return re;
    }
    private byte[] addObject(byte[] data){
        StringBuilder start = new StringBuilder();
        // {객체 번호} {세대 번호} obj
        start.append(currentNumber).append(" 0 obj\n");
        byte[] start_b = start.toString().getBytes(BinarySingleton.US_ASCII);

        StringBuilder end = new StringBuilder();
        end.append("\nendobj\n");
        byte[] end_b  = end.toString().getBytes(BinarySingleton.US_ASCII);

        byte[] re = new byte[start_b.length + data.length + end_b.length];

        int i = 0;
        for(int j = 0; j < start_b.length; j++, i++){
            re[i] = start_b[j];
        }
        for(int j = 0; j < data.length; j++, i++){
            re[i] = data[j];
        }
        for(int j = 0; j < end_b.length; j++, i++){
            re[i] = end_b[j];
        }
        currentNumber += 1;
        return re;
    }
}
