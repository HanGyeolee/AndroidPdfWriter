package com.hangyeolee.pdf.core.binary;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Image 객체 (XObject의 하위 타입)
 */
class BinaryImage extends BinaryXObject {
    private static final String TAG = "BinaryImage";

    public BinaryImage(int objectNumber, Bitmap bitmap, int quality) {
        super(objectNumber);
        dictionary.put("/Subtype", "/Image");
        dictionary.put("/Width", bitmap.getWidth());
        dictionary.put("/Height", bitmap.getHeight());
        dictionary.put("/ColorSpace", "/DeviceRGB");
        dictionary.put("/BitsPerComponent", 8);
        dictionary.put("/Filter", "/" + DCT_DECODE);

        compressDirectly(bitmap, quality);
    }
    public BinaryImage(int objectNumber, byte[] binary, int width, int height) {
        super(objectNumber);
        dictionary.put("/Subtype", "/Image");
        dictionary.put("/Width", width);
        dictionary.put("/Height", height);
        dictionary.put("/BitsPerComponent", 8);
        writeContent(binary, 1);
        dictionary.put("/Length", binary != null ? binary.length : 0);
    }

    public void setFilter(String filter){
        if(filter != null && !filter.isBlank() && !filter.isEmpty()) {
            try {
                if (filter.equals(FLATE_DECODE)) {
                    byte[] compressed;
                    try {
                        {
                            byte[] imageData = readTemporaryStream(1);
                            // 컨텐츠 압축
                            compressContent(imageData);
                            dictionary.put("/Length1", imageData.length);
                        }

                        long length = getTemporaryLength();
                        // 딕셔너리에 압축 관련 항목 추가
                        dictionary.put("/Filter", "/" + FLATE_DECODE);
                        dictionary.put("/Length", length);
                    } catch (Throwable e) {
                        // 압축 실패
                        compressed = readTemporaryStream(1);
                        dictionary.put("/Length", compressed.length);
                        writeContent(compressed);
                    }
                    deleteTemp(1);
                }
            } catch (IOException e){
                Log.e(TAG, "Can not Read Temporary File", e);
            }
        }
    }

    @Override
    public byte[] getStreamData() {
        try {
            byte[] imageData = readTemporaryStream();
            deleteTemp();
            if (!dictionary.containsKey("/Length")) {
                dictionary.put("/Length", imageData != null ? imageData.length : 0);
            }
            return imageData;
        } catch (IOException e){
            Log.e(TAG, "Can not Read Temporary File", e);
        }
        return null;
    }

    public void setColorSpace(String colorSpace){
        if(colorSpace != null && !colorSpace.isEmpty() && !colorSpace.isBlank()) {
            dictionary.put("/ColorSpace", "/"+colorSpace);
        }
    }
    public void setSMask(BinaryObject object){
        if(object != null) {
            dictionary.put("/SMask", object);
        }
    }

    private void compressDirectly(Bitmap bitmap, int quality) throws OutOfMemoryError {
        try {
            OutputStream stream = createTemporaryStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
            stream.close();
        } catch (IOException e){
            Log.e(TAG, "Can not Create Temporary File", e);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        deleteTemp(0 ,1);
        super.finalize();
    }
}
