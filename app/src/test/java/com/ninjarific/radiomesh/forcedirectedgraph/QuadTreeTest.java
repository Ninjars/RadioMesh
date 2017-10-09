package com.ninjarific.radiomesh.forcedirectedgraph;

import android.graphics.RectF;

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

    private final RectF rootBounds = new RectF(0, 0, 16f, 16f);

    @Test
    public void addItem_empty_isLeaf() throws Exception {
        QuadTree<PositionedItem> node = new QuadTree<>(0, rootBounds);
        node.insert(new PositionedItem() {
            @Override
            public float getX() {
                return 3f;
            }

            @Override
            public float getY() {
                return 3f;
            }
        });
        assertTrue(node.isLeaf());
    }

    @Test
    public void addItem_twoFar_singleDepth() throws Exception {
        QuadTree<PositionedItem> node = new QuadTree<>(0, rootBounds);
        node.insert(new PositionedItem() {
            @Override
            public float getX() {
                return 3f;
            }

            @Override
            public float getY() {
                return 3f;
            }
        });
        node.insert(new PositionedItem() {
            @Override
            public float getX() {
                return 15f;
            }

            @Override
            public float getY() {
                return 15f;
            }
        });
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
        node.insert(new PositionedItem() {
            @Override
            public float getX() {
                return 1f;
            }

            @Override
            public float getY() {
                return 1f;
            }
        });
        node.insert(new PositionedItem() {
            @Override
            public float getX() {
                return 5f;
            }

            @Override
            public float getY() {
                return 5f;
            }
        });
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

}