package com.ninjarific.radiomesh.forcedirectedgraph;

import android.support.annotation.Nullable;

import com.ninjarific.radiomesh.utils.Bounds;
import com.ninjarific.radiomesh.utils.Coordinate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuadTree<T extends PositionedItem> {
    private static final int MAX_DEPTH = 7;
    final List<QuadTree<T>> subNodes = new ArrayList<>(4);
    private final Bounds bounds;
    private final int depth;
    private List<T> containedItems = new ArrayList<>();
    private Coordinate centerOfGravity;
    private int totalContainedItemCount = -1;

    public QuadTree(int depth, Bounds bounds) {
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
            item.setContainingLeaf(this);
            return true;
        }
        if (subNodes.isEmpty()) {
            subDivide();
            for (T containedItem : containedItems) {
                insert(containedItem);
            }
            containedItems = Collections.emptyList();
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
        double xMid = bounds.left + 0.5f * bounds.getWidth();
        double yMid = bounds.top + 0.5f * bounds.getHeight();
        int childLevel = depth + 1;
        subNodes.add(new QuadTree<>(childLevel, new Bounds(bounds.left, bounds.top, xMid, yMid)));
        subNodes.add(new QuadTree<>(childLevel, new Bounds(xMid, bounds.top, bounds.right, yMid)));
        subNodes.add(new QuadTree<>(childLevel, new Bounds(bounds.left, yMid, xMid, bounds.bottom)));
        subNodes.add(new QuadTree<>(childLevel, new Bounds(xMid, yMid, bounds.right, bounds.bottom)));
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

    public Bounds getBounds() {
        return bounds;
    }

    @Nullable
    public Coordinate getCenterOfGravity() {
        if (isEmpty()) return null;
        if (centerOfGravity == null) {
            List<T> allItems = new ArrayList<>(containedItems);
            for (QuadTree<T> tree : subNodes) {
                tree.getAllContainedItems(allItems);
            }
            float x = 0;
            float y = 0;
            for (T item : allItems) {
                x += item.getX();
                y += item.getY();
            }
            x /= allItems.size();
            y /= allItems.size();
            centerOfGravity = new Coordinate(x, y);
        }
        return centerOfGravity;
    }

    public int getTotalContainedItemCount() {
        if (totalContainedItemCount < 0) {
            List<T> allItems = new ArrayList<>();
            getAllContainedItems(allItems);
            totalContainedItemCount = allItems.size();
        }
        return totalContainedItemCount;
    }

    private void getAllContainedItems(List<T> outList) {
        if (isLeaf()){
            outList.addAll(containedItems);
        } else {
            for (QuadTree<T> tree : subNodes) {
                tree.getAllContainedItems(outList);
            }
        }
    }

    public List<T> getContainedItems() {
        return containedItems;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("QuadTree " + depth + " " + bounds);
        if (isLeaf()) {
            sb.append("\n\t>> leaf node: ");
            sb.append(containedItems.toString());
        } else {
            for (QuadTree subNode : subNodes) {
                sb.append("\n\t>> subNode: \n\t");
                sb.append(subNode.toString());
            }
        }
        return sb.toString();
    }
}
