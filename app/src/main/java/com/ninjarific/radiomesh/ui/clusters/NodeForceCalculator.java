package com.ninjarific.radiomesh.ui.clusters;

import com.ninjarific.radiomesh.forcedirectedgraph.QuadTree;
import com.ninjarific.radiomesh.utils.Bounds;
import com.ninjarific.radiomesh.utils.Coordinate;

import timber.log.Timber;

public class NodeForceCalculator {
    private static final double TREE_INEQUALITY = 1.2;
    private static final double MINIMUM_NODE_DISTANCE = 0.00001;

    private final double worldSize;
    private final double forceFactor;
    private final double optimalDistance;
    private final double maxStepDistance;
    private final double maxForce;
    private final double forceMagnitude;

    NodeForceCalculator(double worldSize, double forceFactor, double optimalDistance) {
        this.worldSize = worldSize;
        this.forceFactor = forceFactor;
        this.optimalDistance = optimalDistance;
        forceMagnitude = forceFactor * optimalDistance * optimalDistance;
        maxStepDistance = optimalDistance / 100.0;
        maxForce = 100;
    }

    void repelNode(ForceConnectedNode node, QuadTree<ForceConnectedNode> quadTree) {
        QuadTree<ForceConnectedNode> leaf = node.getContainingLeaf();
        if (leaf != null) {
            applyTreeForce(leaf, quadTree);
        }
    }

    private void applyRepulsionForce(ForceConnectedNode node, double dx, double dy, double multiplier, double scalingFactor) {
        double mag = Math.sqrt(dx * dx + dy * dy);
        if (mag == 0) {
            return;
        }
        double vx = dx / mag;
        double vy = dy / mag;

        //                double vx = Math.min(1, Math.max(-1, dx / mag));
        //                double vy = Math.min(1, Math.max(-1, dy / mag));
        //                dx = dx / worldSize;
        //                dy = dy / worldSize;
        double magnitude = -multiplier * scalingFactor;
        double fx = dx < MINIMUM_NODE_DISTANCE && dx > -MINIMUM_NODE_DISTANCE ? 0 : magnitude / dx;
        double fy = dy < MINIMUM_NODE_DISTANCE && dy > -MINIMUM_NODE_DISTANCE ? 0 : magnitude / dy;

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
        node.applyForce(vx, vy);
        Timber.i("applyRepulsionForce " + node.getIndex() + " multiplied by: " + multiplier + "   dx,dy: " + dx + ", " + dy + "   vx,vy: " + vx + ", " + vy + "   fx,fy: " + fx + ", " + fy);
    }

    private void applyTreeForce(QuadTree<ForceConnectedNode> leaf, QuadTree<ForceConnectedNode> tree) {
        if (tree.isEmpty()) return;
        double distance = quadTreeDistance(leaf, tree);
        if (leaf == tree) {
            for (ForceConnectedNode nodeA : leaf.getContainedItems()) {
                for (ForceConnectedNode nodeB : tree.getContainedItems()) {
                    if (nodeA != nodeB) {
                        double dx = nodeB.getX() - nodeA.getX();
                        double dy = nodeB.getY() - nodeA.getY();
                        applyRepulsionForce(nodeA, dx, dy, 1, forceMagnitude);
                    }
                }
            }

        } else if (leafIsFar(distance, tree) || tree.isLeaf()) {
            // apply repulsive force
            int forceMultiplier = tree.getTotalContainedItemCount();
            for (ForceConnectedNode node : leaf.getContainedItems()) {
                double dx = getDx(node, tree);
                double dy = getDy(node, tree);
                applyRepulsionForce(node, dx, dy, forceMultiplier, forceMagnitude);
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
