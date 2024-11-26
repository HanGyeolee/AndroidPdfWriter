package com.hangyeolee.pdf.chart.data;

public interface IChartData<T extends IDataSet> {
    float getMinY();
    float getMaxY();
    int getEntryCount();
    String[] getLabels();
    void addDataSet(T dataSet);
}
