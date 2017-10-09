package com.ninjarific.radiomesh.forcedirectedgraph;

public interface PositionedItem {
    float getX();
    float getY();
    void setContainingLeaf(QuadTree quadTree);
}
