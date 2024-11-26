package com.hangyeolee.pdf.chart.data;

import java.util.Collection;

public class LineDataSet extends DataSet implements ILineDataSet{
    private float lineWidth = 1.0f;
    private boolean drawCircles = true;
    private float circleRadius = 4f;
    private int circleColor;
    private boolean drawFilled = false;
    private int fillColor;

    public LineDataSet(String label) {
        super(label);
    }
    public LineDataSet(String label, Iterable<? extends Number> Ys) {
        super(label, Ys);
    }
    public LineDataSet(String label, Collection<Entry> entries) {
        super(label, entries);
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(float width) {
        this.lineWidth = width;
    }

    public boolean isDrawCircles() {
        return drawCircles;
    }

    public void setDrawCircles(boolean drawCircles) {
        this.drawCircles = drawCircles;
    }

    public float getCircleRadius() {
        return circleRadius;
    }

    public void setCircleRadius(float circleRadius) {
        this.circleRadius = circleRadius;
    }

    public int getCircleColor() {
        return circleColor;
    }

    public void setCircleColor(int circleColor) {
        this.circleColor = circleColor;
    }

    public boolean isDrawFilled() {
        return drawFilled;
    }

    public void setDrawFilled(boolean drawFilled) {
        this.drawFilled = drawFilled;
    }

    public int getFillColor() {
        return fillColor;
    }

    public void setFillColor(int fillColor) {
        this.fillColor = fillColor;
    }
}
