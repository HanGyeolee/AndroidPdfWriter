package com.hangyeolee.pdf.core.binary;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Image 객체 (XObject의 하위 타입)
 */
class BinaryImage extends BinaryXObject {
    private static final String TAG = "BinaryImage";
    private static final int CHUNK_SIZE = 4096;
    private final byte[] imageData;

    public BinaryImage(int objectNumber, Bitmap bitmap, int quality) {
        super(objectNumber);
        dictionary.put("/Subtype", "/Image");
        dictionary.put("/Width", bitmap.getWidth());
        dictionary.put("/Height", bitmap.getHeight());
        dictionary.put("/ColorSpace", "/DeviceRGB");
        dictionary.put("/BitsPerComponent", 8);
        dictionary.put("/Filter", "/DCTDecode");

        this.imageData = compressInChunks(bitmap, quality);
        dictionary.put("/Length", this.imageData != null ? imageData.length : 0);
    }

    @Override
    public byte[] getStreamData() {
        return imageData;
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
                    Log.e(TAG, "Out of Memory: too large image, nothing returned", e1);
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
