package com.ninjarific.radiomesh.ui.clusters;

import com.ninjarific.radiomesh.forcedirectedgraph.QuadTree;
import com.ninjarific.radiomesh.utils.Bounds;
import com.ninjarific.radiomesh.utils.Coordinate;

import timber.log.Timber;

public class NodeForceCalculator {
    private static final double TREE_INEQUALITY = 1.2;
    private static final double MINIMUM_NODE_DISTANCE = 0.01;

    private final double worldSize;
    private final double forceFactor;
    private final double optimalDistance;
    private final double maxStepDistance;
    private final double maxForce;

    NodeForceCalculator(double worldSize, double forceFactor, double optimalDistance) {
        this.worldSize = worldSize;
        this.forceFactor = forceFactor;
        this.optimalDistance = optimalDistance;
        maxStepDistance = optimalDistance / 100.0;
        maxForce = 100;
    }

    void repelNode(ForceConnectedNode node, QuadTree<ForceConnectedNode> quadTree) {
        QuadTree<ForceConnectedNode> leaf = node.getContainingLeaf();
        if (leaf != null) {
            applyTreeForce(leaf, quadTree);
        }
    }

    private void applyTreeForce(QuadTree<ForceConnectedNode> leaf, QuadTree<ForceConnectedNode> tree) {
        double distance = quadTreeDistance(leaf, tree);
        if (leafIsFar(distance, tree)) {
            // apply repulsive force
            int forceMultiplier = tree.getTotalContainedItemCount();
            double magnitude = forceMultiplier * forceFactor * optimalDistance * optimalDistance;

            for (ForceConnectedNode node : leaf.getContainedItems()) {
                double dx = getDx(node, tree);
                double dy = getDy(node, tree);
//                dx = dx / worldSize;
//                dy = dy / worldSize;
                double fx = dx < 0.0001 && dx > 0.0001 ? 0 : magnitude / dx;
                double fy = dy < 0.0001 && dy > 0.0001 ? 0 : magnitude / dy;

//                if (fx > maxForce) {
//                    fx = maxForce;
//                } else if (fx < -maxForce) {
//                    fx = -maxForce;
//                }
//                if (fy > maxForce) {
//                    fy = maxForce;
//                } else if (fy < -maxForce) {
//                    fy = -maxForce;
//                }
                node.applyForce(fx, fy);
                Timber.i("force " + node.getIndex() + " fx, fy " + fx + ", " + fy + " dx,dy " + dx + ", " + dy);
            }
        } else {
            for (QuadTree<ForceConnectedNode> treeNode : tree.getSubTrees()) {
                applyTreeForce(leaf, treeNode);
            }
        }
    }

    private static double getDx(ForceConnectedNode node, QuadTree tree) {
        Coordinate treeCenterOfGravity = tree.getCenterOfGravity();
        return node.getX() - treeCenterOfGravity.x;
    }

    private static double getDy(ForceConnectedNode node, QuadTree tree) {
        Coordinate treeCenterOfGravity = tree.getCenterOfGravity();
        return node.getX() - treeCenterOfGravity.y;
    }

    private static double quadTreeDistance(QuadTree leaf, QuadTree tree) {
        Bounds leafBounds = leaf.getBounds();
        Coordinate treeCenterOfGravity = tree.getCenterOfGravity();
        double dx = leafBounds.centerX() - treeCenterOfGravity.x;
        double dy = leafBounds.centerY() - treeCenterOfGravity.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private static boolean leafIsFar(double distance, QuadTree quadTree) {
        return quadTree.getBounds().getWidth() / distance <= TREE_INEQUALITY;
    }

    void attractNodes(ForceConnectedNode nodeA, ForceConnectedNode nodeB) {
        double dx = nodeB.getX() - nodeA.getX();
        double dy = nodeB.getY() - nodeA.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
//        double dx = nodeA.getX() - nodeB.getX();
//        dx *= dx;
//
//        double dy = nodeA.getY() - nodeB.getY();
//        dy *= dy;
//
//        double fx = Math.min(maxForce, dx / optimalDistance);
//        double fy = Math.min(maxForce, dy / optimalDistance);
//
//        nodeA.applyForce(fx, fy);
//        nodeB.applyForce(-fx, -fy);
    }
}
