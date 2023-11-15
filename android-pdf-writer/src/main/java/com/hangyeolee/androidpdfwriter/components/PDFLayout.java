package com.hangyeolee.androidpdfwriter.components;

import java.util.ArrayList;

public abstract class PDFLayout extends PDFComponent{
    ArrayList<PDFComponent> child;

    public PDFLayout(){super();}
    public PDFLayout addChild(PDFComponent component){
        component.setParent(this);
        child.add(component);
        return this;
    }
}
