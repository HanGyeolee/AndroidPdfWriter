package com.hangyeolee.pdf.chart.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class DataSet implements IDataSet{
    protected List<Entry> entries;
    protected String label;
    protected int color;

    public DataSet(String label) {
        this.label = label;
        this.entries = new ArrayList<>();
    }
    public DataSet(String label, Iterable<? extends Number> Ys) {
        this.label = label;
        this.entries = new ArrayList<>();
        Iterator<? extends Number> iterator = Ys.iterator();
        while(iterator.hasNext()) {
            entries.add(new Entry(entries.size(), iterator.next().floatValue()));
        }
    }
    public DataSet(String label, Collection<Entry> entries) {
        this.label = label;
        this.entries = new ArrayList<>();
        this.entries.addAll(entries);
    }

    public void addEntry(Entry entry) {
        if (entries == null) {
            entries = new ArrayList<>();
        }
        entries.add(entry);
    }

    public void clear() {
        if (entries != null) {
            entries.clear();
        }
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getEntryCount() {
        return entries == null ? 0 : entries.size();
    }
}
