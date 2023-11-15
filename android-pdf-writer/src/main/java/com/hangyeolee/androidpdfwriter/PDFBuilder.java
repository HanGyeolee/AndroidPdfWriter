package com.hangyeolee.androidpdfwriter;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import com.hangyeolee.androidpdfwriter.components.PDFLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PDFBuilder<T extends PDFLayout> {
    PdfDocument document = new PdfDocument();
    int pageWidth, pageHeight;
    Rect contentRect;
    public T root = null;

    public PDFBuilder(int pageWidth, int pageHeight){
        this.pageWidth = pageWidth;
        this.pageHeight = pageHeight;
        contentRect = new Rect(0, 0, pageWidth, pageHeight);
    }

    public PDFBuilder<T> setPagePadding(int vertical, int horizontal){
        this.contentRect.set(horizontal, vertical,
                this.pageWidth - horizontal, this.pageHeight - vertical);
        return this;
    }

    /**
     * PDF 그리기<br>
     * draw PDF
     */
    public PDFBuilder<T> draw(){
        if(root != null) {
            int contentWidth = contentRect.width();
            int contentHeight = contentRect.height();
            root.setSize(contentWidth,0).measure();

            // 필요한 페이지 개수
            int pageCount = (int) Math.ceil(root.getTotalHeight() / (float)contentHeight);

            // 하나의 비트맵으로 그려내기
            Bitmap longBitmap = Bitmap.createBitmap(contentWidth, contentHeight * pageCount, Bitmap.Config.ARGB_8888);
            Canvas longCanvas = new Canvas(longBitmap);
            root.draw(longCanvas);

            // 비트맵을 쪼개서 페이지로 표현
            for(int i = 0; i < pageCount; i++) {
                PdfDocument.PageInfo info = new PdfDocument.PageInfo
                        .Builder(pageWidth, pageHeight, i)
                        .setContentRect(contentRect)
                        .create();
                PdfDocument.Page page = document.startPage(info);
                Canvas pageCanvas = page.getCanvas();
                pageCanvas.drawBitmap(longBitmap, 0, -(contentHeight * i), null);
                document.finishPage(page);
            }

            longBitmap.recycle();
        }
        return this;
    }

    /**
     * PDF 저장하기<br>
     * save PDF file
     * @param context 컨텍스트
     * @param pathname 저장할 위치
     */
    public void save(Context context, String pathname) {
        File file = new File(pathname);
        try {
            OutputStream fos;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                String fileName = file.getName();
                String path = pathname.replace("/"+file.getName(),"");
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.TITLE, fileName);
                values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
                values.put(MediaStore.Downloads.BUCKET_ID, fileName);
                values.put(MediaStore.Downloads.DATE_TAKEN, System.currentTimeMillis());
                values.put(MediaStore.Downloads.RELATIVE_PATH, path);

                Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                fos = context.getContentResolver().openOutputStream(uri, "w");
            } else
                fos = new FileOutputStream(file, false);

            document.writeTo(fos);
        } catch(IOException ignored){}
    }

    @Override
    protected void finalize() throws Throwable {
        document.close();
        super.finalize();
    }
}