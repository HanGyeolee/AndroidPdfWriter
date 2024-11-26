package com.hangyeolee.pdf.chart.data;

import com.hangyeolee.pdf.chart.utils.ScatterShape;

import java.util.Collection;
import java.util.Iterator;

public class ScatterDataSet extends DataSet implements IScatterDataSet{
    private float shapeSize = 15f;
    private int shapeColor;
    private int shapeType = ScatterShape.SHAPE_CIRCLE;

    public ScatterDataSet(String label) {
        super(label);
    }

    public ScatterDataSet(String label, Iterable<? extends Number> Ys) {
        super(label, Ys);
    }

    public ScatterDataSet(String label, Collection<Entry> entries) {
        super(label, entries);
    }

    public float getShapeSize() {
        return shapeSize;
    }

    public void setShapeSize(float shapeSize) {
        this.shapeSize = shapeSize;
    }

    public int getShapeColor() {
        return shapeColor;
    }

    public void setShapeColor(int shapeColor) {
        this.shapeColor = shapeColor;
    }

    public int getShapeType() {
        return shapeType;
    }

    public void setShapeType(int shapeType) {
        this.shapeType = shapeType;
    }
}
