package com.hangyeolee.pdf.chart;

import com.hangyeolee.pdf.chart.data.ChartData;
import com.hangyeolee.pdf.chart.data.DataSet;
import com.hangyeolee.pdf.core.PDFComponent;

public class PDFChart<T extends ChartData<? extends DataSet>> extends PDFComponent {
    protected T data;
}
