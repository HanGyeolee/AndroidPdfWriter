package com.hangyeolee.pdf.chart.data;


public class Entry {
    protected float x;
    protected float y;

    public Entry(float x, float y) {
        this.x = x;
        this.y = y;
    }
    public float getX(){return x;}
    public float getY(){return y;}
    public void setX(float x){this.x = x;}
    public void setY(float y){this.y = y;}
}