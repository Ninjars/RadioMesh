package com.ninjarific.radiomesh.ui.clusters;

import com.ninjarific.radiomesh.forcedirectedgraph.PositionedItem;
import com.ninjarific.radiomesh.forcedirectedgraph.QuadTree;

import java.util.List;

public class ForceConnectedNode implements PositionedItem {
    private final int index;
    private List<Integer> neighbours;
    private float x;
    private float y;
    private double dx;
    private double dy;
    private QuadTree<ForceConnectedNode> containingLeaf;

    public ForceConnectedNode(int i, List<Integer> neighbours, float x, float y) {
        this.index = i;
        this.neighbours = neighbours;
        this.x = x;
        this.y = y;
    }

    public List<Integer> getNeighbours() {
        return neighbours;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public void setContainingLeaf(QuadTree quadTree) {
        this.containingLeaf = quadTree;
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

    public QuadTree<ForceConnectedNode> getContainingLeaf() {
        return containingLeaf;
    }
}
