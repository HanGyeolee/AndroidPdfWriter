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

import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.Nullable;

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
    Paper pageSize = Paper.A4;
    public T root = null;

    public PDFBuilder(Paper pageSize){
        this.pageSize = pageSize;
        Zoomable.getInstance().setContentRect(
                new RectF(0, 0, pageSize.getWidth(), pageSize.getHeight())
        );
        setDPI(DPI.M5);
    }
    public PDFBuilder(
            @FloatRange(from = 1.0f) float width,
            @FloatRange(from = 1.0f) float height){
        if(width < 1) width = 1;
        if(height < 1) height = 1;
        this.pageSize.setCustom(width, height);
        Zoomable.getInstance().setContentRect(
                new RectF(0, 0, pageSize.getWidth(), pageSize.getHeight())
        );
        setDPI(DPI.M5);
    }

    public PDFBuilder<T> setPagePadding(
            @FloatRange(from = 0.0f) float vertical,
            @FloatRange(from = 0.0f) float horizontal){
        if(vertical < 0) vertical = 0;
        if(horizontal < 0) horizontal = 0;

        Zoomable.getInstance().getContentRect().set(horizontal, vertical,
                this.pageSize.getWidth() - horizontal, this.pageSize.getHeight() - vertical);
        return this;
    }

    public PDFBuilder<T> setPagePadding(@Nullable Float left,@Nullable Float top,@Nullable Float right,@Nullable Float bottom){
        float n_left = 0;
        float n_top = 0;
        float n_right = 0;
        float n_bottom = 0;

        if(left != null && left > 0.0f) n_left = left;
        if(top != null && top > 0.0f) n_top = top;
        if(right != null && right > 0.0f) n_right = right;
        if(bottom != null && bottom > 0.0f) n_bottom = bottom;

        Zoomable.getInstance().getContentRect().set(n_left, n_top,
                this.pageSize.getWidth() - n_right, this.pageSize.getHeight() - n_bottom);
        return this;
    }

    /**
     * 이 라이브러리는 사용자가 설정한 화면을 비트맵으로 그려 JPEG 형식으로 압축한 후, PDF 파일로 출력합니다. <br>
     * 페이지 당 1장의 이미지만 포함합니다. JPEG 압축 품질을 변경할 수 있으며, 품질 기본 설정값은 60 입니다.<br>
     * The library draws the screen set by the user in Bitmap, compresses it in JPEG format, and outputs it as a PDF file. <br>
     * One image is entered per page. Can change compress quality. Default quality is 60.
     * @param quality 0 ~ 100, default = 60
     */
    public PDFBuilder<T> setQuality(@IntRange(from = 0, to = 100) int quality){
        if(quality < 0) quality = 0;
        else if (quality > 100) quality = 100;
        this.quality = quality;
        return this;
    }

    /**
     * DPI는 pdf로 생성할 이미지의 해상도입니다. 해상도가 높을수록 이미지의 용량이 증가할 것 입니다.<br>
     * 디바이스 dpi와 상관없이 오직 pdf의 72dpi를 기준으로 해상도가 증가합니다. DPI 기본 설정값은 DPI.M5로 360dpi 입니다.<br>
     * DPI is the resolution of the image to be created in pdf. The higher the resolution, the higher the capacity of the image will be. <br>
     * Regardless of the device dpi, the resolution is increased based solely on the dpi of the pdf. Default Dpi is DPI.M5, as 360dpi.
     * @param dpi 7 ~ , default = 360
     */
    public PDFBuilder<T> setDPI(@DPI.DPIInt @IntRange(from = 7) int dpi){
        Zoomable.getInstance().density = (dpi / 72.0f);
        return this;
    }

    /**
     * int형의 DPI값을 입력하는 대신, 72DPI에 대한 비율로 설정하고 싶을 때 호출합니다.<br>
     * Call when you want to set the DPI value as a ratio to 72 DPI, rather than putting the int-type DPI value in.
     * @param ratio 0.1f ~ 100.0f, default = 5.0f
     */
    public PDFBuilder<T> setDPI(@FloatRange(from = 0.1f, to = 100.0f) float ratio){
        Zoomable.getInstance().density = ratio;
        return this;
    }

    /**
     * PDF 그리기<br>
     * draw PDF
     */
    public PDFBuilder<T> draw(){
        if(root != null) {
            root.setSize(Zoomable.getInstance().getContentRect().width(),0.0f).measure();

            int contentWidth = Math.round(Zoomable.getInstance().getZoomWidth());
            int contentHeight = Math.round(Zoomable.getInstance().getZoomHeight());

            // 필요한 페이지 개수
            int pageCount = (int) Math.ceil(root.getTotalHeight() / (float)contentHeight);

            // 하나의 비트맵으로 그려내기
            Bitmap longBitmap = Bitmap.createBitmap(contentWidth, contentHeight * pageCount, Bitmap.Config.ARGB_8888);
            Canvas longCanvas = new Canvas(longBitmap);
            longCanvas.drawColor(Color.WHITE);
            root.draw(longCanvas);

            page = new BinaryPage(pageCount, quality);
            page.setContentRect(Zoomable.getInstance().getContentRect());
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

            page.saveTo(fos);
            fos.flush();
            fos.close();
        } catch(IOException ignored){}

        return uri;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
}