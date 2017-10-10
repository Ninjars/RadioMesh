package com.ninjarific.radiomesh.ui.clusters;

import android.graphics.PointF;
import android.graphics.RectF;

import com.ninjarific.radiomesh.forcedirectedgraph.QuadTree;

import timber.log.Timber;

class ForceHelper {
    private static final double MAX_FORCE_DISTANCE = 100;
    private static final double MAX_STEP_DISTANCE = 10;
    private static final double SPRING_FACTOR = 1;
    private static final double SPRING_DIVISOR = 1;
    private static final double REPEL_FACTOR = 1;
    private static final double TREE_INEQUALITY = 1.2;

    static void applyAttractionBetweenNodes(ForceConnectedNode nodeA, ForceConnectedNode nodeB) {
        // normalise dx and dy to a fixed value
        double dx = nodeB.getX() - nodeA.getX();
        double dy = nodeB.getY() - nodeA.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance < 1) {
            return;
        }
        double force = Math.log(distance);// / DISTANCE_FACTOR;
        if (nodeA.getIndex() == 5) {
            Timber.e("attraction force: " + force + " at " + distance);
        }
        double fx = force * dx;
        double fy = force * dy;
        nodeA.addForce(fx, fy);
        nodeB.addForce(-fx, -fy);

//        double dx = Math.min(1, (nodeB.getX() - nodeA.getX()) / MAX_FORCE_DISTANCE);
//        double dy = Math.min(1, (nodeB.getY() - nodeA.getY()) / MAX_FORCE_DISTANCE);
//        double distance = Math.sqrt(dx * dx + dy * dy);
//        if (distance == 0) {
//            return;
//        }
//        double force = SPRING_FACTOR * Math.log(distance / SPRING_DIVISOR);
//        double scaleFactor = Math.min(MAX_STEP_DISTANCE, force / distance);
//        double fx = scaleFactor * dx;
//        double fy = scaleFactor * dy;
//        nodeA.addForce(fx, fy);
//        nodeB.addForce(-fx, -fy);

//        double dx = nodeB.getX() - nodeA.getX();
//        double dy = nodeB.getY() - nodeA.getY();
//        double distance = Math.sqrt(dx * dx + dy * dy);
//        if (distance == 0) {
//            return;
//        }
//        double force = SPRING_FACTOR * Math.log(distance / SPRING_DIVISOR);
//        double scaleFactor = force / distance;
//        double fx = scaleFactor * dx;
//        double fy = scaleFactor * dy;
//        nodeA.addForce(fx, fy);
//        nodeB.addForce(-fx, -fy);
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
        if (leaf != null) {
            applyTreeForce(leaf, quadTree);
        }
    }

    private static double FORCE_FACTOR = 1;
    private static double DISTANCE_FACTOR = 10;
    private static void applyTreeForce(QuadTree<ForceConnectedNode> leaf, QuadTree<ForceConnectedNode> tree) {
        double distance = quadTreeDistance(leaf, tree);
        if (leafIsFar(distance, tree)) {
            if (distance < 1) {
                return;
            }
            // apply repulsive force
            int forceMultiplier = tree.getTotalContainedItemCount();
            double magnitude = forceMultiplier * FORCE_FACTOR * DISTANCE_FACTOR * DISTANCE_FACTOR / distance;
            double fx = getDx(leaf, tree) * magnitude;
            double fy = getDy(leaf, tree) * magnitude;
            for (ForceConnectedNode node : leaf.getContainedItems()) {
                node.addForce(fx, fy);
                if (node.getIndex() == 5) {
                    Timber.e("repulsion force: " + magnitude + " at " + distance);
                }
            }
        } else {
            for (QuadTree<ForceConnectedNode> treeNode : tree.getSubTrees()) {
                applyTreeForce(leaf, treeNode);
            }
        }
    }

    private static float getDx(QuadTree leaf, QuadTree tree) {
        RectF leafBounds = leaf.getBounds();
        PointF treeCenterOfGravity = tree.getCenterOfGravity();
        return leafBounds.centerX() - treeCenterOfGravity.x;
    }

    private static float getDy(QuadTree leaf, QuadTree tree) {
        RectF leafBounds = leaf.getBounds();
        PointF treeCenterOfGravity = tree.getCenterOfGravity();
        return leafBounds.centerY() - treeCenterOfGravity.y;
    }

    private static double quadTreeDistance(QuadTree leaf, QuadTree tree) {
        RectF leafBounds = leaf.getBounds();
        PointF treeCenterOfGravity = tree.getCenterOfGravity();
        float dx = leafBounds.centerX() - treeCenterOfGravity.x;
        float dy = leafBounds.centerY() - treeCenterOfGravity.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private static boolean leafIsFar(double distance, QuadTree quadTree) {
        return quadTree.getBounds().width() / distance <= TREE_INEQUALITY;
    }
}
