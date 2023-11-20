package com.hangyeolee.androidpdfwriter;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.hangyeolee.androidpdfwriter.binary.BinaryPage;
import com.hangyeolee.androidpdfwriter.components.PDFLayout;
import com.hangyeolee.androidpdfwriter.utils.DPI;
import com.hangyeolee.androidpdfwriter.utils.Zoomable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PDFBuilder<T extends PDFLayout> {
    BinaryPage page;
    float pageWidth, pageHeight;
    RectF contentRect;
    public T root = null;

    public PDFBuilder(float pageWidth, float pageHeight){
        this.pageWidth = pageWidth;
        this.pageHeight = pageHeight;
        contentRect = new RectF(0, 0, pageWidth, pageHeight);
        setDPI(DPI.Standard);
    }

    public PDFBuilder<T> setPagePadding(float vertical, float horizontal){
        this.contentRect.set(horizontal, vertical,
                this.pageWidth - horizontal, this.pageHeight - vertical);
        return this;
    }

    public PDFBuilder<T> setDPI(int dpi){
        Zoomable.getInstance().density = (dpi / 72.0f);
        return this;
    }

    /**
     * PDF 그리기<br>
     * draw PDF
     */
    public PDFBuilder<T> draw(){
        if(root != null) {
            root.setSize(contentRect.width(),0.0f).measure();

            int contentWidth = Math.round(contentRect.width() * Zoomable.getInstance().density);
            int contentHeight = Math.round(contentRect.height() * Zoomable.getInstance().density);

            // 필요한 페이지 개수
            int pageCount = (int) Math.ceil(root.getTotalHeight() / (float)contentHeight);

            // 하나의 비트맵으로 그려내기
            Bitmap longBitmap = Bitmap.createBitmap(contentWidth, contentHeight * pageCount, Bitmap.Config.ARGB_8888);
            Canvas longCanvas = new Canvas(longBitmap);
            longCanvas.drawColor(Color.WHITE);
            root.draw(longCanvas);

            page = new BinaryPage(pageCount);
            page.setContentRect(contentRect);
            Canvas canvas = new Canvas();
            // 비트맵을 쪼개서 페이지로 표현
            for (int i = 0; i < pageCount; i++) {
                Bitmap pageBitmap = Bitmap.createBitmap(contentWidth, contentHeight, Bitmap.Config.ARGB_8888);
                canvas.setBitmap(pageBitmap);
                canvas.drawBitmap(longBitmap, 0, -(contentHeight * i), null);
                page.addBitmap(pageBitmap);
            }

            longBitmap.recycle();
        }
        return this;
    }

    /**
     * PDF 저장하기<br>
     * save PDF file
     * @param context 컨텍스트
     * @param filename 저장할 위치
     */
    public void save(Context context, String filename) {
        File file;
        try {
            file = new File("Download/"+filename);
            OutputStream fos;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                String path = file.getPath().replace("/"+filename,"");
                ContentValues values = new ContentValues();
                values.put("title", filename);
                values.put("_display_name", filename);
                values.put("mime_type", "application/pdf");
                values.put("bucket_id", filename);
                values.put("datetaken", System.currentTimeMillis());
                values.put("relative_path", path);

                Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                fos = context.getContentResolver().openOutputStream(uri, "w");
            } else {
                file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
                fos = new FileOutputStream(file, false);
            }

            page.saveTo(fos);
            fos.flush();
            fos.close();
        } catch(IOException ignored){}
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
}