package com.ninjarific.radiomesh.utils;

import android.graphics.RectF;

public class Bounds {
    public final double left;
    public final double top;
    public final double right;
    public final double bottom;

    public Bounds(double left, double top, double right, double bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public double getWidth() {
        return right - left;
    }

    public double getHeight() {
        return bottom - top;
    }

    public double getLeft() {
        return left;
    }

    public double getTop() {
        return top;
    }

    public double getRight() {
        return right;
    }

    public double getBottom() {
        return bottom;
    }

    public boolean contains(float x, float y) {
        return left <= x && x <= right && top <= y && y <= bottom;
    }

    public RectF asRectF() {
        return new RectF((float) left, (float) top, (float) right, (float) bottom);
    }

    public double centerX() {
        return left + (getWidth() / 2.0);
    }

    public double centerY() {
        return top + (getHeight() / 2.0);
    }

    @Override
    public String toString() {
        return "<Bounds " + left + ", " + top + " -> " + right + ", " + bottom + ">";
    }
}
