package com.ninjarific.radiomesh.forcedirectedgraph;

import android.graphics.RectF;

import java.util.ArrayList;
import java.util.List;

public class QuadTree<T extends PositionedItem> {
    private static final int MAX_DEPTH = 7;
    final List<QuadTree<T>> subNodes = new ArrayList<>(4);
    private final RectF bounds;
    private final int depth;
    private List<T> containedItems = new ArrayList<>();

    public QuadTree(int depth, RectF bounds) {
        this.depth = depth;
        this.bounds = bounds;
    }

    public void insertAll(List<T> items) {
        for (T item : items) {
            insert(item);
        }
    }

    boolean insert(T item) {
        if (!bounds.contains(item.getX(), item.getY())) {
            return false;
        }
        if (subNodes.isEmpty() && (containedItems.isEmpty() || depth >= MAX_DEPTH)) {
            containedItems.add(item);
            return true;
        }
        if (subNodes.isEmpty()) {
            subDivide();
            for (T containedItem : containedItems) {
                insert(containedItem);
            }
            containedItems.clear();
        }
        boolean added = false;
        for (QuadTree<T> node : subNodes) {
            added = node.insert(item);
            if (added) {
                break;
            }
        }
        return added;
    }

    private void subDivide() {
        float xMid = bounds.left + 0.5f * bounds.width();
        float yMid = bounds.top + 0.5f * bounds.height();
        int childLevel = depth + 1;
        subNodes.add(new QuadTree<>(childLevel, new RectF(bounds.left, bounds.top, xMid, yMid)));
        subNodes.add(new QuadTree<>(childLevel, new RectF(xMid, bounds.top, bounds.right, yMid)));
        subNodes.add(new QuadTree<>(childLevel, new RectF(bounds.left, yMid, xMid, bounds.bottom)));
        subNodes.add(new QuadTree<>(childLevel, new RectF(xMid, yMid, bounds.right, bounds.bottom)));
    }

    public boolean isLeaf() {
        return subNodes.isEmpty();
    }

    public boolean isEmpty() {
        return subNodes.isEmpty() && containedItems.isEmpty();
    }

    public int depth() {
        return depth;
    }

    public List<QuadTree<T>> getSubTrees() {
        return subNodes;
    }

    public RectF getBounds() {
        return bounds;
    }
}
