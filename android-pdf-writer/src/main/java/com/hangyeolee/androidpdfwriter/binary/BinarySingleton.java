package com.hangyeolee.androidpdfwriter.binary;

import java.nio.charset.Charset;
import java.util.ArrayList;

class BinarySingleton {
    public static BinarySingleton instance = null;
    public static final Charset US_ASCII = Charset.forName("US-ASCII");

    public static BinarySingleton getInstance() {
        if(instance == null)
            instance = new BinarySingleton();
        return instance;
    }


    public int quality;
    public long byteLength = 0;
    public final ArrayList<XRef> XRefs;

    public void addXRef(int value, byte b){
        BinarySingleton.getInstance().XRefs.add(new XRef(byteLength,value, b));
    }
    public void addXRef(byte b){
        BinarySingleton.getInstance().XRefs.add(new XRef(byteLength, b));
    }
    public int getLengthXref(){return XRefs.size();}

    private BinarySingleton(){
        XRefs = new ArrayList<>(6);
    }
}
