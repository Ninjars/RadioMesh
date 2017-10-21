package com.ninjarific.radiomesh.ui.clusters;

import com.ninjarific.radiomesh.forcedirectedgraph.QuadTree;
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
        applySimpleRecursiveNBodyForces(node, quadTree);
    }

    private void applySimpleRecursiveNBodyForces(ForceConnectedNode node, QuadTree<ForceConnectedNode> tree) {
        // repel
        for (ForceConnectedNode other : tree.getContainedItems()) {
            if (other != node) {
                double dx = other.getX() - node.getX();
                double dy = other.getY() - node.getY();
                applyRepulsionForce(node, dx, dy, 1);
            }
        }

        for (QuadTree<ForceConnectedNode> subtree : tree.getSubTrees()) {
            applySimpleRecursiveNBodyForces(node, subtree);
        }
    }

    private void applyRepulsionForce(ForceConnectedNode node, double dx, double dy, double multiplier) {
        double mag = Math.sqrt(dx * dx + dy * dy);
        if (mag == 0) {
            return;
        }
        double vx = dx / mag;
        double vy = dy / mag;

        double force = -Math.min(maxForce, multiplier * forceMagnitude / mag);
        double fx = vx * force;
        double fy = vy * force;

        node.addForce(fx, fy);
        Timber.i("applyRepulsionForce " + node.getIndex() + " force: " + force + " multiplied by: " + multiplier + "   dx,dy: " + dx + ", " + dy + "   vx,vy: " + vx + ", " + vy + "   fx,fy: " + fx + ", " + fy);
    }

    private void applyTreeForce(ForceConnectedNode node, QuadTree<ForceConnectedNode> tree) {
        if (tree.isEmpty()) return;
        double distance = quadTreeDistance(node, tree);
//        if (node.getContainingLeaf() == tree) {
        for (ForceConnectedNode other : tree.getContainedItems()) {
            if (other != node) {
                double dx = other.getX() - node.getX();
                double dy = other.getY() - node.getY();
                applyRepulsionForce(node, dx, dy, 1);
            }
        }

        for (QuadTree subtree : tree.getSubTrees()) {
            applyTreeForce(node, subtree);
        }

//        } else if (leafIsFar(distance, tree) || tree.isLeaf()) {
//            // apply repulsive force
//            int forceMultiplier = tree.getTotalContainedItemCount();
//            for (ForceConnectedNode node : leaf.getContainedItems()) {
//                double dx = getDx(node, tree);
//                double dy = getDy(node, tree);
//                applyRepulsionForce(node, dx, dy, forceMultiplier, forceMagnitude);
//            }
//        } else {
//            for (QuadTree<ForceConnectedNode> treeNode : tree.getSubTrees()) {
//                applyTreeForce(leaf, treeNode);
//            }
//        }
    }

    private static double getDx(ForceConnectedNode node, QuadTree tree) {
        Coordinate treeCenterOfGravity = tree.getCenterOfGravity();
        return node.getX() - treeCenterOfGravity.x;
    }

    private static double getDy(ForceConnectedNode node, QuadTree tree) {
        Coordinate treeCenterOfGravity = tree.getCenterOfGravity();
        return node.getX() - treeCenterOfGravity.y;
    }

    private static double quadTreeDistance(ForceConnectedNode node, QuadTree tree) {
        Coordinate treeCenterOfGravity = tree.getCenterOfGravity();
        if (treeCenterOfGravity == null) {
            return 0;
        }
        double dx = node.getX() - treeCenterOfGravity.x;
        double dy = node.getY() - treeCenterOfGravity.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private static boolean leafIsFar(double distance, QuadTree quadTree) {
        return quadTree.getBounds().getWidth() / distance <= TREE_INEQUALITY;
    }

    void attractNodes(ForceConnectedNode nodeA, ForceConnectedNode nodeB) {
        double dx = nodeB.getX() - nodeA.getX();
        double dy = nodeB.getY() - nodeA.getY();

        double mag = Math.sqrt(dx * dx + dy * dy);
        if (mag == 0) {
            return;
        }
        double vx = dx / mag;
        double vy = dy / mag;

        double force = Math.min(maxForce, mag * mag / optimalDistance); // Math.log(mag) / optimalDistance;

        double fx = vx * force;
        double fy = vy * force;

        nodeA.addForce(-fx, -fy);
        nodeB.addForce(fx, fy);
        Timber.i("attractNodes " + nodeA.getIndex() + " " + nodeB.getIndex() + " magnitude: " + force + "   dx,dy: " + dx + ", " + dy + "   vx,vy: " + vx + ", " + vy + "   fx,fy: " + fx + ", " + fy);
    }
}
