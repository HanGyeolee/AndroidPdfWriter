package com.hangyeolee.androidpdfwriter;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.Nullable;

import com.hangyeolee.androidpdfwriter.binary.BinarySerializer;
import com.hangyeolee.androidpdfwriter.components.PDFLayout;
import com.hangyeolee.androidpdfwriter.utils.Paper;
import com.hangyeolee.androidpdfwriter.utils.PaperUnit;
import com.hangyeolee.androidpdfwriter.utils.StandardDirectory;
import com.hangyeolee.androidpdfwriter.utils.Zoomable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PDFBuilder {
    int quality = 85;
    BinarySerializer page;
    Paper pageSize = Paper.A4;
    public PDFLayout root = null;

    public PDFBuilder(Paper pageSize){
        this.pageSize = pageSize;
        Zoomable.getInstance().setContentRect(
                new RectF(0, 0, pageSize.getWidth(), pageSize.getHeight())
        );
    }

    public PDFBuilder(
            @FloatRange(from = 1.0f) float width,
            @FloatRange(from = 1.0f) float height,
            PaperUnit unit){
        if(width < 1) width = 1;
        if(height < 1) height = 1;
        this.pageSize.setCustom(width, height, unit);
        Zoomable.getInstance().setContentRect(
                new RectF(0, 0, pageSize.getWidth(), pageSize.getHeight())
        );
    }

    @Deprecated
    public PDFBuilder setPagePadding(
            @FloatRange(from = 0.0f) float vertical,
            @FloatRange(from = 0.0f) float horizontal){
        if(vertical < 0) vertical = 0;
        if(horizontal < 0) horizontal = 0;
        root.setPadding(horizontal, vertical);
        return this;
    }

    @Deprecated
    public PDFBuilder setPagePadding(@Nullable Float left,@Nullable Float top,@Nullable Float right,@Nullable Float bottom){
        float n_left = 0;
        float n_top = 0;
        float n_right = 0;
        float n_bottom = 0;

        if(left != null && left > 0.0f) n_left = left;
        if(top != null && top > 0.0f) n_top = top;
        if(right != null && right > 0.0f) n_right = right;
        if(bottom != null && bottom > 0.0f) n_bottom = bottom;

        root.setPadding(n_left, n_top, n_right, n_bottom);
        return this;
    }

    /**
     * PDF 내에 들어가는 이미지에 대한 품질 값을 설정합니다. <br>
     * JPEG 압축 품질을 변경할 수 있으며, 품질 기본 설정값은 85 입니다.<br>
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
     */
    public PDFBuilder draw(){
        if(root != null) {
            root.setSize(Zoomable.getInstance().getContentRect().width(), 0.0f).measure();
            page = new BinarySerializer(root);
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
                fos = context.getContentResolver().openOutputStream(uri, "w");
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

            if(fos != null) {
                page.saveTo(fos);
                fos.flush();
                fos.close();
            }
        } catch(IOException ignored){}

        return uri;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
}