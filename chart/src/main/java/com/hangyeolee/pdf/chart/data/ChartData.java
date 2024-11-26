package com.hangyeolee.pdf.chart.data;

import java.util.ArrayList;
import java.util.List;

public class ChartData<T extends IDataSet> implements IChartData<T> {
    private final List<T> dataSets = new ArrayList<>();
    private String[] labels;
    private float minY = Float.MAX_VALUE;
    private float maxY = -Float.MAX_VALUE;

    public float getMinY() {
        return minY;
    }

    public float getMaxY() {
        return maxY;
    }

    public int getEntryCount() {
        return labels.length;
    }

    public String[] getLabels() {
        return labels;
    }

    public void addDataSet(T dataSet) {
        dataSets.add(dataSet);
        calculateMinMax();
    }

    private void calculateMinMax() {
        minY = Float.MAX_VALUE;
        maxY = -Float.MAX_VALUE;

        for (IDataSet dataSet : dataSets) {
            for (Entry entry : dataSet.getEntries()) {
                if (entry.y < minY) minY = entry.y;
                if (entry.y > maxY) maxY = entry.y;
            }
        }
    }
}
