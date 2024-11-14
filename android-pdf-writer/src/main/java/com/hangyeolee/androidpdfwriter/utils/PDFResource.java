package com.hangyeolee.androidpdfwriter.utils;

public class PDFResource {
    private final int objectNumber;
    private final String resourceId;

    public PDFResource(int objectNumber, String resourceId) {
        this.objectNumber = objectNumber;
        this.resourceId = resourceId;
    }

    public int getObjectNumber() { return objectNumber; }
    public String getResourceId() { return resourceId; }
}
