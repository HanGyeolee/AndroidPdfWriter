package com.hangyeolee.pdf.core.binary;

import com.hangyeolee.pdf.core.BuildConfig;

class BinaryInfo extends BinaryDictionary {
    public BinaryInfo(int objectNumber) {
        super(objectNumber);
        dictionary.put("/Producer", "(Gyeolee/APW v" + BuildConfig.PUBLISH_VERSION + ")");
    }
}
