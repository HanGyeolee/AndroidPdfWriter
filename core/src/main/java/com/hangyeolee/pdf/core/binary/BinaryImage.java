package com.hangyeolee.pdf.core.binary;

import android.graphics.Bitmap;
import android.util.Log;

import com.hangyeolee.androidpdfwriter.PDFBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Image 객체 (XObject의 하위 타입)
 */
class BinaryImage extends BinaryXObject {
    private static final String TAG = "BinaryImage";
    private static final int CHUNK_SIZE = 4096;
    private byte[] imageData;

    public BinaryImage(int objectNumber, Bitmap bitmap, int quality) {
        super(objectNumber);
        dictionary.put("/Subtype", "/Image");
        dictionary.put("/Width", bitmap.getWidth());
        dictionary.put("/Height", bitmap.getHeight());
        dictionary.put("/ColorSpace", "/DeviceRGB");
        dictionary.put("/BitsPerComponent", 8);
        dictionary.put("/Filter", "/" + DCT_DECODE);

        this.imageData = compressInChunks(bitmap, quality);
        dictionary.put("/Length", this.imageData != null ? imageData.length : 0);
    }
    public BinaryImage(int objectNumber, byte[] binary, int width, int height) {
        super(objectNumber);
        dictionary.put("/Subtype", "/Image");
        dictionary.put("/Width", width);
        dictionary.put("/Height", height);
        dictionary.put("/BitsPerComponent", 8);
        this.imageData = binary;
        dictionary.put("/Length", this.imageData != null ? imageData.length : 0);
    }

    public void setFilter(String filter){
        if(filter != null && !filter.isBlank() && !filter.isEmpty()) {
            if(filter.equals(FLATE_DECODE)){
                // 컨텐츠 압축
                dictionary.put("/Length1", imageData.length);
                this.imageData = compressContent(imageData);

                // 딕셔너리에 압축 관련 항목 추가
                dictionary.put("/Filter", "/"+FLATE_DECODE);
                dictionary.put("/Length", imageData.length);
            }
        }
    }

    @Override
    public byte[] getStreamData() {
        return imageData;
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

    private byte[] compressDirectly(Bitmap bitmap, int quality) throws OutOfMemoryError {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }

    private byte[] compressInChunks(Bitmap sourceBitmap, int quality){
        int width = sourceBitmap.getWidth();
        int height = sourceBitmap.getHeight();

        // 둘 다 chunkSize보다 작으면 한 번에 처리
        if (width <= CHUNK_SIZE && height <= CHUNK_SIZE) {
            try {
                return compressDirectly(sourceBitmap, quality);
            } catch (OutOfMemoryError e){
                Log.e(TAG, "Out of Memory: return original", e);
                try {
                    ByteBuffer buffer = ByteBuffer.allocate(sourceBitmap.getByteCount());
                    sourceBitmap.copyPixelsToBuffer(buffer);
                    return buffer.array();
                } catch (Exception e1){
                    Log.e(TAG, "Out of Memory: nothing can do, return nothing", e1);
                    return null;
                }
            }
        }

        // 결과를 저장할 스트림
        ByteArrayOutputStream finalStream = new ByteArrayOutputStream();

        try {
            // 청크 단위로 처리
            for (int y = 0; y < height; y += CHUNK_SIZE) {
                for (int x = 0; x < width; x += CHUNK_SIZE) {
                    // 현재 청크의 실제 크기 계산
                    int chunkWidth = Math.min(CHUNK_SIZE, width - x);
                    int chunkHeight = Math.min(CHUNK_SIZE, height - y);

                    Bitmap chunk = null;
                    ByteArrayOutputStream chunkStream = null;

                    try {
                        // 현재 청크 추출
                        chunk = Bitmap.createBitmap(
                                sourceBitmap,
                                x, y,
                                chunkWidth, chunkHeight
                        );

                        // 청크 압축
                        chunkStream = new ByteArrayOutputStream();
                        if (!chunk.compress(Bitmap.CompressFormat.JPEG, quality, chunkStream)) {
                            Log.w(TAG, "Chunk compression failed at x=" + x + ", y=" + y);
                            return null;
                        }

                        // 결과 합치기
                        finalStream.write(chunkStream.toByteArray());
                    } finally {
                        if(chunk != null && !chunk.isRecycled()){
                            // 청크 정리
                            chunk.recycle();
                        }
                        if(chunkStream != null){
                            try {
                                chunkStream.close();
                            } catch (IOException e) {
                                Log.e(TAG, "Failed to close chunk stream", e);
                            }
                        }
                    }
                }
            }

            return finalStream.toByteArray();
        } catch (Exception e) {
            Log.e(TAG, "Error during chunk processing", e);
            return null;
        } finally {
            try {
                finalStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Can't close finalStream.", e);
            }
        }
    }
}
