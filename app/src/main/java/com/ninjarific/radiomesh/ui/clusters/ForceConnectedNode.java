package com.ninjarific.radiomesh.ui.clusters;

import java.util.List;

public class ForceConnectedNode {
    private final int index;
    private final float weight;
    private final List<Integer> neighbours;
    private float x;
    private float y;
    private double dx;
    private double dy;

    public ForceConnectedNode(int i, List<Integer> neighbours, float x, float y) {
        this(i, neighbours, 1, x, y);
    }

    public ForceConnectedNode(int i, List<Integer> neighbours, int weight, float x, float y) {
        this.index = i;
        this.neighbours = neighbours;
        this.x = x;
        this.y = y;
        this.weight = weight;
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
        x = (float) (x + dx * forceFactor);
        y = (float) (y + dy * forceFactor);
    }

    public int getIndex() {
        return index;
    }

    public float getWeight() {
        return weight;
    }
}
