package com.hangyeolee.androidpdfwriter.components;

import java.util.ArrayList;

public abstract class PDFLayout extends PDFComponent{
    ArrayList<PDFComponent> child;

    public PDFLayout(){super();}

    /**
     * 레이아웃에 자식 추가<br>
     * Add children to layout
     * @param component 자식 컴포넌트
     * @return 자기자신
     */
    public PDFLayout addChild(PDFComponent component){
        component.setParent(this);
        child.add(component);
        return this;
    }
}

