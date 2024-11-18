package com.hangyeolee.androidpdfwriter.binary;

public class BinaryConverter {
    public static byte[] toBytes(String input) {
        byte[] bytes = new byte[input.length()];
        for (int i = 0; i < input.length(); i++) {
            bytes[i] = (byte)(input.charAt(i) & 0xFF);
        }
        return bytes;
    }
}
