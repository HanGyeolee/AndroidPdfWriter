package com.hangyeolee.androidpdfwriter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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
import com.hangyeolee.androidpdfwriter.utils.Paper;
import com.hangyeolee.androidpdfwriter.utils.StandardDirectory;
import com.hangyeolee.androidpdfwriter.utils.Zoomable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Provider;
import java.util.Locale;

public class PDFBuilder<T extends PDFLayout> {
    int quality = 60;
    BinaryPage page;
    Paper pageSize;
    RectF contentRect;
    public T root = null;

    public PDFBuilder(Paper pageSize){
        this.pageSize = pageSize;
        contentRect = new RectF(0, 0, pageSize.getWidth(), pageSize.getHeight());
        setDPI(DPI.M5);
    }
    public PDFBuilder(float width, float height){
        this.pageSize = Paper.A0;
        this.pageSize.setCustom(width, height);
        contentRect = new RectF(0, 0, pageSize.getWidth(), pageSize.getHeight());
        setDPI(DPI.M5);
    }

    public PDFBuilder<T> setPagePadding(float vertical, float horizontal){
        this.contentRect.set(horizontal, vertical,
                this.pageSize.getWidth() - horizontal, this.pageSize.getHeight() - vertical);
        return this;
    }

    public PDFBuilder<T> setQuality(int quality){
        this.quality = quality;
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

            page = new BinaryPage(pageCount, quality);
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
    public void save(Context context,@StandardDirectory.DirectoryString String directory, String filename) {

        try {
            OutputStream fos;
            Uri uri;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Files.FileColumns.TITLE, filename);
                values.put(MediaStore.Files.FileColumns.DISPLAY_NAME, filename);
                values.put(MediaStore.Files.FileColumns.MIME_TYPE, "application/pdf");
                values.put(MediaStore.Files.FileColumns.BUCKET_ID, filename);
                values.put(MediaStore.Files.FileColumns.DATE_TAKEN, System.currentTimeMillis());
                values.put(MediaStore.Files.FileColumns.RELATIVE_PATH, directory);
;
                uri = context.getContentResolver().insert(
                        MediaStore.Files.getContentUri("external")
                        ,values);
                fos = context.getContentResolver().openOutputStream(uri, "w");
            } else {

                File dir;
                File file;

                if(!StandardDirectory.isStandardDirectory(directory)){
                    dir = new File(directory);
                    dir.mkdirs();
                }else{
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        dir = context.getExternalFilesDir(directory);
                    } else {
                        dir = Environment.getExternalStoragePublicDirectory(directory);
                    }
                }

                file = new File(dir, filename);
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