package com.hangyeolee.pdf.core.binary;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

/**
 * Image 객체 (XObject의 하위 타입)
 */
class BinaryImage extends BinaryXObject {
    private final byte[] imageData;

    public BinaryImage(int objectNumber, Bitmap bitmap, int quality) {
        super(objectNumber);
        dictionary.put("/Subtype", "/Image");
        dictionary.put("/Width", bitmap.getWidth());
        dictionary.put("/Height", bitmap.getHeight());
        dictionary.put("/ColorSpace", "/DeviceRGB");
        dictionary.put("/BitsPerComponent", 8);
        dictionary.put("/Filter", "/DCTDecode");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);
        this.imageData = bos.toByteArray();
        dictionary.put("/Length", imageData.length);
    }

    @Override
    public byte[] getStreamData() {
        return imageData;
    }
}
