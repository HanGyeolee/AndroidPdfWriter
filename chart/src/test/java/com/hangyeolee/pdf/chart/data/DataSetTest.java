package com.hangyeolee.pdf.chart.data;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class DataSetTest {
    IDataSet set;

    @Test
    public void addEntry() {
        List<Double> ys = new ArrayList<>();
        ys.add(0.0);ys.add(1.0);ys.add(2.0);ys.add(3.0);ys.add(4.0);
        set = new DataSet("label", ys);

        assertSame(ys.size(), set.getEntryCount());
    }
}