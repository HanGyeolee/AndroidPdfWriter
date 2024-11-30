package com.hangyeolee.androidpdfwriter;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import androidx.annotation.IntRange;

import com.hangyeolee.androidpdfwriter.binary.BinarySerializer;
import com.hangyeolee.androidpdfwriter.components.PDFLayout;
import com.hangyeolee.androidpdfwriter.utils.PageLayout;
import com.hangyeolee.androidpdfwriter.utils.PageLayoutFactory;
import com.hangyeolee.androidpdfwriter.utils.StandardDirectory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PDFBuilder {
    public static String TAG = "PDFBuilder";
    /**
     * 만약 DEBUG 가 true 라면 모든 stream 의 압축을 진행하지 않는 다.<br/>
     * 즉, Filter FlateDecode 항목을 해제한다.<br/>
     * If DEBUG is true, it does not compress all streams.<br/>
     * That is, release the Filter FlatDecode entry.
     */
    public static boolean DEBUG = false;
    private final PageLayout pageLayout;

    int quality = 85;
    BinarySerializer page;

    public PDFBuilder() {
        this(PageLayoutFactory.createDefaultLayout());
    }
    public PDFBuilder(PageLayout pageLayout){
        this.pageLayout = pageLayout;
    }

    /**
     * PDF 내에 들어 가는 이미지에 대한 품질 값을 설정 합니다. <br>
     * JPEG 압축 품질을 변경할 수 있으며, 품질 기본 설정 값은 85 입니다.<br>
     * Sets the quality value for the image within the PDF. <br>
     * Can change compress quality. Default quality is 85.
     * @param quality 0 ~ 100, default = 85
     */
    public PDFBuilder setQuality(@IntRange(from = 0, to = 100) int quality){
        if(quality < 0) quality = 0;
        else if (quality > 100) quality = 100;
        this.quality = quality;
        return this;
    }

    /**
     * PDF 그리기<br>
     * draw PDF
     * @param root Root Layout Component
     */
    public PDFBuilder draw(PDFLayout root){
        if(root != null) {
            float width = pageLayout.getContentWidth();

            root.setPageLayout(pageLayout);
            root.setSize(width, null).measure();
            page = new BinarySerializer(root, pageLayout);
            page.setQuality(quality);
            page.draw();
        }
        return this;
    }

    /**
     * PDF 저장하기<br>
     * save PDF file
     * @param context 컨텍스트
     * @param filename 저장할 위치
     * @return 저장된 파일의 Uri
     */
    public Uri save(Context context,@StandardDirectory.DirectoryString String relativePath, String filename) {
        Uri uri = null;

        try {
            ParcelFileDescriptor pfd = null;
            OutputStream fos;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Files.FileColumns.TITLE, filename);
                values.put(MediaStore.Files.FileColumns.DISPLAY_NAME, filename);
                values.put(MediaStore.Files.FileColumns.MIME_TYPE, "application/pdf");
                values.put(MediaStore.Files.FileColumns.BUCKET_ID, filename);
                values.put(MediaStore.Files.FileColumns.DATE_TAKEN, System.currentTimeMillis());
                values.put(MediaStore.Files.FileColumns.RELATIVE_PATH, relativePath);

                uri = context.getContentResolver().insert(
                        MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
                        , values);
                if(uri == null) throw new NullPointerException("Can not Create PDF file at " + relativePath);
                pfd = context.getContentResolver().openFileDescriptor(uri, "w");
                if(pfd != null)
                    fos = new FileOutputStream(pfd.getFileDescriptor());
                else
                    throw new NullPointerException("Can not Call FileDescriptor");
            } else {

                File dir;
                File file;

                if(!StandardDirectory.isStandardDirectory(relativePath)){
                    dir = new File(relativePath);
                    if(!dir.mkdirs())
                        throw new NullPointerException("Can not Make Directory : " + relativePath);
                }else{
                    dir = Environment.getExternalStoragePublicDirectory(relativePath);
                }

                file = new File(dir, filename);
                uri = Uri.fromFile(file);
                if(uri == null) throw new NullPointerException("Can not Create PDF file at " + relativePath);
                fos = new FileOutputStream(file, false);
            }

            page.save(fos);
            fos.flush();
            fos.close();
            if(pfd != null){
                pfd.close();
            }
        } catch(IOException ignored){}

        return uri;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
}