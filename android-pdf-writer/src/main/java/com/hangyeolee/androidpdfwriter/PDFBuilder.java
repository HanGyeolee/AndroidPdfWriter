package com.hangyeolee.androidpdfwriter;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.hangyeolee.androidpdfwriter.components.PDFLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PDFBuilder<T extends PDFLayout> {
    PdfDocument document = new PdfDocument();
    int pageWidth, pageHeight;
    public T root = null;

    public PDFBuilder(int pageWidth, int pageHeight){
        this.pageWidth = pageWidth;
        this.pageHeight = pageHeight;
    }

    /**
     * PDF 그리기<br>
     * draw PDF
     */
    public void draw(){
        if(root != null) {
            root.setSize(pageWidth, pageHeight)
                    .measure();

            int pageCount = (int) Math.ceil(root.getTotalHeight() / (float)pageHeight);

            PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(root.getTotalWidth(), pageHeight, pageCount).create();
            PdfDocument.Page page = document.startPage(info);

            root.draw(page.getCanvas());

            document.finishPage(page);
        }
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
