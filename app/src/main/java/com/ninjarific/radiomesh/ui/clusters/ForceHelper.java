package com.ninjarific.radiomesh.ui.clusters;

import android.graphics.PointF;
import android.graphics.RectF;

import com.ninjarific.radiomesh.forcedirectedgraph.QuadTree;

class ForceHelper {
    private static final double MAX_FORCE_DISTANCE = 100;
    private static final double MAX_STEP_DISTANCE = 10;
    private static final double SPRING_FACTOR = 0.5;
    private static final double SPRING_DIVISOR = 0.1;
    private static final double REPEL_FACTOR = 0.25;
    private static final double TREE_INEQUALITY = 1.2;

    static void applyAttractionBetweenNodes(ForceConnectedNode nodeA, ForceConnectedNode nodeB) {
        // normalise dx and dy to a fixed value
        double dx = Math.min(1, (nodeB.getX() - nodeA.getX()) / MAX_FORCE_DISTANCE);
        double dy = Math.min(1, (nodeB.getY() - nodeA.getY()) / MAX_FORCE_DISTANCE);
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance == 0) {
            return;
        }
        double force = SPRING_FACTOR * Math.log(distance / SPRING_DIVISOR);
        double scaleFactor = Math.min(MAX_STEP_DISTANCE, force / distance);
        double fx = scaleFactor * dx;
        double fy = scaleFactor * dy;
        nodeA.addForce(fx, fy);
        nodeB.addForce(-fx, -fy);
    }

    static void applyRepulsionBetweenNodes(ForceConnectedNode nodeA, ForceConnectedNode nodeB) {
        // normalise dx and dy to a fixed value
        double dx = Math.min(1, (nodeB.getX() - nodeA.getX()) / MAX_FORCE_DISTANCE);
        double dy = Math.min(1, (nodeB.getY() - nodeA.getY()) / MAX_FORCE_DISTANCE);
        double distanceSquared = dx * dx + dy * dy;
        if (distanceSquared == 0) {
            return;
        }
        double force = REPEL_FACTOR / distanceSquared;
        double scaleFactor = Math.min(5, force / Math.sqrt(distanceSquared));
        double fx = -scaleFactor * dx;
        double fy = -scaleFactor * dy;
        nodeA.addForce(fx, fy);
        nodeB.addForce(-fx, -fy);
    }

    public static void applyForceForNode(ForceConnectedNode node, QuadTree<ForceConnectedNode> quadTree) {
        QuadTree<ForceConnectedNode> leaf = node.getContainingLeaf();
        applyTreeForce(leaf, quadTree);
    }

    private static void applyTreeForce(QuadTree<ForceConnectedNode> leaf, QuadTree<ForceConnectedNode> tree) {
        double distance = quadTreeDistance(leaf, tree);
        if (leafIsFar(distance, tree)) {
            // apply repulsive force
            int forceMultiplier = tree.getTotalContainedItemCount();
            // TODO: apply force
            return;
        } else {
            for (QuadTree<ForceConnectedNode> treeNode : tree.getSubTrees()) {
                applyTreeForce(leaf, treeNode);
            }
        }
    }

    private static double quadTreeDistance(QuadTree leaf, QuadTree tree) {
        RectF leafBounds = leaf.getBounds();
        PointF treeCenterOfGravity = tree.getCenterOfGravity();
        float dx = (leafBounds.centerX() - treeCenterOfGravity.x);
        float dy = (leafBounds.centerY() - treeCenterOfGravity.y);
        return Math.sqrt(dx * dx + dy * dy);
    }

    private static boolean leafIsFar(double distance, QuadTree quadTree) {
        return quadTree.getBounds().width() / distance <= TREE_INEQUALITY;
    }
}
