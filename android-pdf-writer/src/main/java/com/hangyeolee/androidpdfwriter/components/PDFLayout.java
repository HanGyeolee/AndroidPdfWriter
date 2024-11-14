package com.hangyeolee.androidpdfwriter.components;

import java.util.ArrayList;

public abstract class PDFLayout extends PDFComponent{
    ArrayList<PDFComponent> child;

    public PDFLayout(){super();}

    public ArrayList<PDFComponent> getChild(){return child;}

    @Override
    public void measure(float x, float y) {
        super.measure(x, y);

        // 레이아웃은 Anchor를 적용하지 않는 다.
        float d = 0;
        if (parent != null)
            d += parent.measureX + parent.border.size.left + parent.padding.left;
        measureX = relativeX + margin.left + d;
        d = 0;
        if (parent != null)
            d += parent.measureY + parent.border.size.top + parent.padding.top;
        measureY = relativeY + margin.top + d;
    }

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

