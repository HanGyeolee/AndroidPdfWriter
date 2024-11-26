package com.hangyeolee.pdf.chart.data;

import java.util.Collection;

public class BarDataSet extends DataSet implements IBarDataSet{
    private float barWidth = 0.85f; // 막대 너비 (0-1)

    public BarDataSet(String label) {
        super(label);
    }
    public BarDataSet(String label, Iterable<? extends Number> Ys) {
        super(label, Ys);
    }
    public BarDataSet(String label, Collection<Entry> entries) {
        super(label, entries);
    }

    public void setBarWidth(float barWidth) {
        this.barWidth = Math.min(1f, Math.max(0f, barWidth));
    }

    public float getBarWidth() {
        return barWidth;
    }
}
