package com.ninjarific.radiomesh.ui.clusters;

import java.util.List;

public class ForceConnectedNode {
    private List<Integer> neighbours;
    private float x;
    private float y;
    private double dx;
    private double dy;

    public ForceConnectedNode(List<Integer> neighbours, float x, float y) {
        this.neighbours = neighbours;
        this.x = x;
        this.y = y;
    }

    public List<Integer> getNeighbours() {
        return neighbours;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void clearForce() {
        dx = 0;
        dy = 0;
    }

    public void addForce(double fx, double fy) {
        dx += fx;
        dy += fy;
    }

    public void updatePosition(double forceFactor) {
        x = (float) Math.max(0, Math.min(1, x + dx * forceFactor));
        y = (float) Math.max(0, Math.min(1, y + dy * forceFactor));
    }
}
