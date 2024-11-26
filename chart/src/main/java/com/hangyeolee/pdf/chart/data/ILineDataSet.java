package com.hangyeolee.pdf.chart.data;

public interface ILineDataSet extends IDataSet {
    float getLineWidth();
    void setLineWidth(float width);
    boolean isDrawCircles();
    void setDrawCircles(boolean drawCircles);
    float getCircleRadius();
    void setCircleRadius(float circleRadius);
    int getCircleColor();
    void setCircleColor(int circleColor);
    boolean isDrawFilled();
    void setDrawFilled(boolean drawFilled);
    int getFillColor();
    void setFillColor(int fillColor);
}
