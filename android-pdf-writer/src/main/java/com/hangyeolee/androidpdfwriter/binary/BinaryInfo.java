package com.hangyeolee.androidpdfwriter.binary;

import com.hangyeolee.androidpdfwriter.BuildConfig;

class BinaryInfo extends BinaryObject {
    public BinaryInfo(int objectNumber) {
        super(objectNumber);
        dictionary.put("/Producer", "(Gyeolee/APW v" + BuildConfig.PUBLISH_VERSION + ")");
    }
}
