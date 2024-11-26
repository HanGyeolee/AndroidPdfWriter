package com.hangyeolee.pdf.chart.utils;

import android.graphics.Color;

public class ChartStyle {
    // 레이아웃 관련
    public float leftMargin = 60f;    // y축 라벨을 위한 여백
    public float rightMargin = 30f;   // 여유 공간
    public float topMargin = 30f;     // 제목을 위한 여백
    public float bottomMargin = 40f;  // x축 라벨을 위한 여백

    // 그리드 관련
    public boolean showGrid = true;
    public float gridLineWidth = 0.5f;
    public int gridLineColor = Color.LTGRAY;
    public float[] gridLineDash = {5, 5}; // 점선 패턴

    // 축 관련
    public float axisLineWidth = 1f;
    public int axisLineColor = Color.BLACK;
    public float axisLabelSize = 12f;
    public int axisLabelColor = Color.BLACK;

    // 범례 관련
    @LegendPosition.ID
    public int legendPosition = LegendPosition.NONE;
    public float legendTextSize = 12f;

    // 데이터 표현 관련
    public float dataLineWidth = 2f;
    public int[] dataColors = {
            Color.rgb(70, 130, 180),   // Steel Blue
            Color.rgb(255, 99, 71),    // Tomato
            Color.rgb(50, 205, 50),    // Lime Green
            Color.rgb(255, 215, 0)     // Gold
    };
}