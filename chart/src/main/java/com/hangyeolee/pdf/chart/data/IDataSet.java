package com.hangyeolee.pdf.chart.data;

import java.util.ArrayList;
import java.util.List;

public interface IDataSet {
    void addEntry(Entry entry);

    void clear();

    List<Entry> getEntries();

    String getLabel();

    void setLabel(String label);

    int getColor();

    void setColor(int color);

    int getEntryCount();
}
