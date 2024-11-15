package com.hangyeolee.androidpdfwriter.components;

import com.hangyeolee.androidpdfwriter.binary.BinarySerializer;

public abstract class PDFResourceComponent extends PDFComponent {
    protected String resourceId;  // 등록된 리소스 ID

    @Override
    public StringBuilder draw(BinarySerializer page) {
        registerResources(page);
        return super.draw(page);
    }

    /**
     * 컴포넌트에서 사용하는 리소스를 등록
     * @param page BinaryPage 인스턴스
     */
    public abstract void registerResources(BinarySerializer page);
}
