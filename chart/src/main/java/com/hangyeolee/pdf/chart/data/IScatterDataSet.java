package com.hangyeolee.pdf.chart.data;

public interface IScatterDataSet extends IDataSet{
    float getShapeSize();
    void setShapeSize(float shapeSize);
    int getShapeColor();
    void setShapeColor(int shapeColor);
    int getShapeType();
    void setShapeType(int shapeType);
}
