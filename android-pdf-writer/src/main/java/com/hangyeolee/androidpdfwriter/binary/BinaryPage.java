package com.hangyeolee.androidpdfwriter.binary;

import android.graphics.Bitmap;
import android.graphics.RectF;

import com.hangyeolee.androidpdfwriter.BuildConfig;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class BinaryPage {
    int currentNumber = 1;

    ArrayList<Bitmap> lists = new ArrayList<>();

    int pageCount;
    int pageWidth, pageHeight;
    RectF contentRect;

    public BinaryPage(int pageCount, int quality){
        this.pageCount = pageCount;
        BinarySingleton.getInstance().quality = quality;
    }

    public void setContentRect(RectF contentRect){
        this.contentRect = contentRect;
        this.pageWidth = Math.round(contentRect.right+contentRect.left);
        this.pageHeight = Math.round(contentRect.bottom+contentRect.top);
    }

    public void addBitmap(Bitmap bitmap){
        lists.add(bitmap);
    }

    public void saveTo(OutputStream fos){
        StringBuilder tmp;
        byte[] tmp_b;

        try {
            BufferedOutputStream bufos = new BufferedOutputStream(fos);

            tmp = new StringBuilder();
            tmp.append("%PDF-1.4\n");
            tmp_b = tmp.toString().getBytes(BinarySingleton.US_ASCII);
            BinarySingleton.getInstance().addXRef(65535, (byte) 'f');
            BinarySingleton.getInstance().byteLength += tmp_b.length + 1;
            bufos.write(tmp_b);

            tmp = new StringBuilder();
            tmp.append("<</Producer (Gyeolee/APW v").append(BuildConfig.PUBLISH_VERSION).append(")>>");
            tmp_b = addObject(addDictionary(tmp.toString()));
            BinarySingleton.getInstance().addXRef((byte) 'n');
            BinarySingleton.getInstance().byteLength += tmp_b.length;
            bufos.write(tmp_b);

            int PagesNumber = currentNumber+1;
            tmp = new StringBuilder();
            tmp.append("<< /Type /Catalog\n")
                .append("/Pages ").append(PagesNumber).append(" 0 R>>");
            tmp_b = addObject(addDictionary(tmp.toString()));
            BinarySingleton.getInstance().addXRef((byte) 'n');
            BinarySingleton.getInstance().byteLength += tmp_b.length;
            // 1 0 obj Catalog
            bufos.write(tmp_b);

            tmp = new StringBuilder();
            tmp.append("<< /Type /Pages\n")
                .append("/Count ").append(pageCount).append('\n')
                .append("/Kids [");

            int objNumber = PagesNumber+1;
            for(int i = 0; i < lists.size(); i ++){
                objNumber = measurePageNumber(objNumber);
                tmp.append(objNumber).append(" 0 R\n");
                objNumber += 1;
            }

            tmp.append("]>>");
            tmp_b = addObject(addDictionary(tmp.toString()));
            BinarySingleton.getInstance().addXRef((byte) 'n');
            BinarySingleton.getInstance().byteLength += tmp_b.length;
            // 2 0 obj Pages
            bufos.write(tmp_b);

            objNumber = PagesNumber+1;
            for(int i = 0; i < lists.size(); i ++){
                BinaryImage image = new BinaryImage(objNumber, lists.get(i), pageWidth, pageHeight);
                bufos.write(image.getContentOfPage(contentRect, PagesNumber));
                objNumber = image.currentNumber;
            }
            currentNumber = objNumber;

            tmp = new StringBuilder();
            tmp.append("xref\n")
                .append("0 ").append(BinarySingleton.getInstance().getLengthXref()).append("\n");
            tmp_b = tmp.toString().getBytes(BinarySingleton.US_ASCII);
            bufos.write(tmp_b);

            for(XRef b: BinarySingleton.getInstance().XRefs){
                bufos.write(b.write());
            }

            tmp = new StringBuilder();
            tmp.append("trailer\n")
                .append("<< /Size ").append(BinarySingleton.getInstance().getLengthXref()).append("\n")
                .append("/Info 1 0 R\n")
                .append("/Root 2 0 R\n>>\n")
                .append("startxref\n")
                .append(BinarySingleton.getInstance().byteLength);
            tmp_b = tmp.toString().getBytes(BinarySingleton.US_ASCII);
            bufos.write(tmp_b);

            bufos.write("\n%%EOF".getBytes(BinarySingleton.US_ASCII));

            bufos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < lists.size(); i++){
            if(lists.get(i) != null) {
                lists.get(i).recycle();
            }
        }
        lists.clear();
    }

    private int measurePageNumber(int objNumber){
        return objNumber+2;
    }
    private byte[] addDictionary(String s){
        return s.getBytes(BinarySingleton.US_ASCII);
    }
    private byte[] addObject(byte[] data){
        StringBuilder start = new StringBuilder();
        // {객체 번호} {세대 번호} obj
        start.append(currentNumber).append(" ").append(0).append(" obj\n");
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

    @Override
    protected void finalize() throws Throwable {
        for(int i = 0; i < lists.size(); i++){
            if(lists.get(i) != null) {
                lists.get(i).recycle();
            }
        }
        lists.clear();
        super.finalize();
    }
}
