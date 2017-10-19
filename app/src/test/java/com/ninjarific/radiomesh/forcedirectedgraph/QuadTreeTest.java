package com.ninjarific.radiomesh.forcedirectedgraph;

import com.ninjarific.radiomesh.utils.Bounds;
import com.ninjarific.radiomesh.utils.Coordinate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class QuadTreeTest {

    private final Bounds rootBounds = new Bounds(0, 0, 16f, 16f);

    private PositionedItem createItem(float x, float y) {
        return new PositionedItem() {
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

            }
        };
    }

    @Test
    public void addItem_empty_isLeaf() throws Exception {
        QuadTree<PositionedItem> node = new QuadTree<>(0, rootBounds);
        node.insert(createItem(3f, 3f));
        assertTrue(node.isLeaf());
    }

    @Test
    public void addItem_twoFar_singleDepth() throws Exception {
        QuadTree<PositionedItem> node = new QuadTree<>(0, rootBounds);
        node.insert(createItem(3f, 3f));
        node.insert(createItem(15f, 15f));
        assertFalse(node.isLeaf());

        List<QuadTree<PositionedItem>> subnodes = node.subNodes;
        assertTrue(subnodes.get(0).isLeaf());
        assertFalse(subnodes.get(0).isEmpty());
        assertEquals(1, subnodes.get(0).depth());

        assertTrue(subnodes.get(1).isEmpty());
        assertTrue(subnodes.get(2).isEmpty());

        assertTrue(subnodes.get(3).isLeaf());
        assertFalse(subnodes.get(3).isEmpty());
    }

    @Test
    public void addItem_twoClose_doubleDepth() throws Exception {
        QuadTree<PositionedItem> node = new QuadTree<>(0, rootBounds);
        node.insert(createItem(1f, 1f));
        node.insert(createItem(5f, 5f));
        assertFalse(node.isLeaf());

        List<QuadTree<PositionedItem>> subnodes = node.subNodes;
        assertFalse(subnodes.get(0).isLeaf());
        assertFalse(subnodes.get(0).isEmpty());
        assertEquals(1, subnodes.get(0).depth());

        assertTrue(subnodes.get(1).isEmpty());
        assertTrue(subnodes.get(2).isEmpty());
        assertTrue(subnodes.get(3).isEmpty());

        List<QuadTree<PositionedItem>> childSubnodes = subnodes.get(0).subNodes;
        assertTrue(childSubnodes.get(0).isLeaf());
        assertFalse(childSubnodes.get(0).isEmpty());
        assertEquals(2, childSubnodes.get(0).depth());

        assertTrue(childSubnodes.get(1).isEmpty());
        assertTrue(childSubnodes.get(2).isEmpty());

        assertTrue(childSubnodes.get(3).isLeaf());
        assertFalse(childSubnodes.get(3).isEmpty());
    }

    @Test
    public void getCenterOfGravity_singleItem() throws Exception {
        QuadTree<PositionedItem> node = new QuadTree<>(0, rootBounds);
        node.insert(createItem(5f, 7f));
        Coordinate centre = node.getCenterOfGravity();
        assertEquals(5f, centre.x, 0.001f);
        assertEquals(7f, centre.y, 0.001f);
    }

    @Test
    public void getCenterOfGravity_multiItem() throws Exception {
        QuadTree<PositionedItem> node = new QuadTree<>(0, rootBounds);
        node.insert(createItem(1f, 1f));
        node.insert(createItem(3f, 3f));
        node.insert(createItem(2f, 2f));
        Coordinate centre = node.getCenterOfGravity();
        assertEquals(2f, centre.x, 0.001f);
        assertEquals(2f, centre.y, 0.001f);
    }
}