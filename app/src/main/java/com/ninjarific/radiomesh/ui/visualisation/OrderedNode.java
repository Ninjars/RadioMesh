package com.ninjarific.radiomesh.ui.visualisation;

import java.util.List;

public class OrderedNode<T> {
    private final T nodeContent;
    private final int index;
    private final List<Integer> neighbours;
    private int position;
    private float average;

    public OrderedNode(T nodeContent, int index, List<Integer> neighbours) {
        this.nodeContent = nodeContent;
        this.index = index;
        this.neighbours = neighbours;
    }

    public T getNodeContent() {
        return nodeContent;
    }

    public int getIndex() {
        return index;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public List<Integer> getNeighbours() {
        return neighbours;
    }

    public void setAverage(float average) {
        this.average = average;
    }

    public float getAverage() {
        return average;
    }
}
