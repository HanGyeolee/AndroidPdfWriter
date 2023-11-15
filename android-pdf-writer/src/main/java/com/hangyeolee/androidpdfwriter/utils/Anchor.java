package com.hangyeolee.androidpdfwriter.utils;

public class Anchor{
    public byte vertical = -1;
    public byte horizontal = -1;

    public Anchor(){}
    public Anchor(byte vertical, byte horizontal){
        this.vertical = vertical;
        this.horizontal = horizontal;

        if(this.vertical > 1) this.vertical = 1;
        else if(this.vertical < -1) this.vertical = -1;
        if(this.horizontal > 1) this.horizontal = 1;
        else if(this.horizontal < -1) this.horizontal = -1;
    }

    public static int getDeltaPixel(byte axis,int gap){
        switch (axis){
            case -1:
                return 0;
            case 0:
                return (int)(gap * 0.5);
            case 1:
                return gap;
        }
        return 0;
    }
}

