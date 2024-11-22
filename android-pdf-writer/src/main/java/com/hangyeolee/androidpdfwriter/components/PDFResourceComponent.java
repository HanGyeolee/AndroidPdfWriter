package com.hangyeolee.androidpdfwriter.components;

import com.hangyeolee.androidpdfwriter.binary.BinarySerializer;
import com.hangyeolee.androidpdfwriter.utils.Fit;

public abstract class PDFResourceComponent extends PDFComponent {
    protected String resourceId;  // 등록된 리소스 ID
    @Fit.FitInt
    int fit = Fit.NONE;

    @Override
    public void draw(BinarySerializer page) {
        registerResources(page);
        super.draw(page);
    }

    public PDFResourceComponent setFit(@Fit.FitInt int fit){
        this.fit = fit;
        return this;
    }

    /**
     * 컴포넌트에서 사용하는 리소스를 등록
     * @param page BinaryPage 인스턴스
     */
    public abstract void registerResources(BinarySerializer page);
}
