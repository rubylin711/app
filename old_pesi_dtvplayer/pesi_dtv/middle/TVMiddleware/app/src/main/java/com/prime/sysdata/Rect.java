package com.prime.sysdata;

/**
 * Created by gary_hsu on 2018/1/4.
 */

public class Rect {
    private static final String TAG="Rect";
    private int X;
    private int Y;
    private int DX;
    private int DY;

    public Rect(int x,int y,int dx,int dy) {
        this.X = x;
        this.Y = y;
        this.DX = dx;
        this.DY = dy;
    }

    public int getX() {
        return X;
    }

    public void setX(int x) {
        X = x;
    }

    public int getY() {
        return Y;
    }

    public void setY(int y) {
        Y = y;
    }

    public int getDX() {
        return DX;
    }

    public void setDX(int DX) {
        this.DX = DX;
    }

    public int getDY() {
        return DY;
    }

    public void setDY(int DY) {
        this.DY = DY;
    }
}
