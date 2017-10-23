package com.ninjarific.radiomesh.ui.clusters;

import com.ninjarific.radiomesh.forcedirectedgraph.QuadTree;
import com.ninjarific.radiomesh.utils.Coordinate;

import timber.log.Timber;

public class NodeForceCalculator {
    private static final double TREE_INEQUALITY = 1.2;
    private static final double MIN_FORCE_THRESHOLD = 0.00001;

    private final double optimalDistance;
    private final double maxForce;
    private final double forceMagnitude;

    NodeForceCalculator(double forceFactor, double optimalDistance) {
        this.optimalDistance = optimalDistance;
        forceMagnitude = forceFactor * optimalDistance * optimalDistance;
        maxForce = 10;
    }

    void repelNode(ForceConnectedNode node, QuadTree<ForceConnectedNode> quadTree) {
//        applySimpleRecursiveNBodyForces(node, quadTree);
        applyTreeForce(node, quadTree);
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
        if (mag < MIN_FORCE_THRESHOLD) {
            return;
        }
        applyRepulsionForce(node, dx, dy, multiplier, mag);
    }

    private void applyRepulsionForce(ForceConnectedNode node, double dx, double dy, double multiplier, double magnitude) {
        double vx = dx / magnitude;
        double vy = dy / magnitude;

        double force = -Math.min(maxForce, Math.min(0, multiplier * forceMagnitude / magnitude));
        double fx = vx * force;
        double fy = vy * force;

        node.addForce(fx, fy);
//        Timber.i("applyRepulsionForce " + node.getIndex() + " force: " + force + " multiplied by: " + multiplier + "   dx,dy: " + dx + ", " + dy + "   vx,vy: " + vx + ", " + vy + "   fx,fy: " + fx + ", " + fy);
        Timber.w("repel," + node.getIndex() + "," + -force + "," + dx + "," + dy + "," + fx + "," + fy);
    }

    private void applyTreeForce(ForceConnectedNode node, QuadTree<ForceConnectedNode> tree) {
        if (tree.isEmpty()) return;
        if (tree.isLeaf()) {
//            Timber.i("applyTreeForce - leaf tree " + node + " " + tree.hashCode() + " other nodes " + tree.getTotalContainedItemCount());
            for (ForceConnectedNode other : tree.getContainedItems()) {
                if (other != node) {
                    double dx = other.getX() - node.getX();
                    double dy = other.getY() - node.getY();
                    applyRepulsionForce(node, dx, dy, 1);
                }
            }
        } else {
            double distance = quadTreeDistance(node, tree);
            if (leafIsFar(distance, tree)) {
//                Timber.i("applyTreeForce - distant tree " + node + " " + tree);
                applyRepulsionForce(node, getDx(node, tree), getDy(node, tree), tree.getTotalContainedItemCount(), distance);
            } else {
                for (QuadTree<ForceConnectedNode> subtree : tree.getSubTrees()) {
                    applyTreeForce(node, subtree);
                }
            }
        }
    }

    private static double getDx(ForceConnectedNode node, QuadTree tree) {
        Coordinate treeCenterOfGravity = tree.getCenterOfGravity();
        return treeCenterOfGravity.x - node.getX();
    }

    private static double getDy(ForceConnectedNode node, QuadTree tree) {
        Coordinate treeCenterOfGravity = tree.getCenterOfGravity();
        return treeCenterOfGravity.y - node.getY();
    }

    private static double quadTreeDistance(ForceConnectedNode node, QuadTree tree) {
        Coordinate treeCenterOfGravity = tree.getCenterOfGravity();
        if (treeCenterOfGravity == null) {
            return 0;
        }
        double dx = treeCenterOfGravity.x - node.getX();
        double dy = treeCenterOfGravity.y - node.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    private static boolean leafIsFar(double distance, QuadTree quadTree) {
        return quadTree.getBounds().getWidth() / distance <= TREE_INEQUALITY;
    }

    void attractNodes(ForceConnectedNode nodeA, ForceConnectedNode nodeB) {
        double dx = nodeB.getX() - nodeA.getX();
        double dy = nodeB.getY() - nodeA.getY();

        double mag = Math.sqrt(dx * dx + dy * dy);
        if (mag <= MIN_FORCE_THRESHOLD) {
            return;
        }
        double vx = dx / mag;
        double vy = dy / mag;

        double force = Math.min(maxForce, Math.max(0, Math.log(mag) / optimalDistance));

        double fx = vx * force;
        double fy = vy * force;

        nodeA.addForce(fx, fy);
        nodeB.addForce(-fx, -fy);
//        Timber.i("attractNodes " + nodeA.getIndex() + " " + nodeB.getIndex() + " magnitude: " + force + "   dx,dy: " + dx + ", " + dy + "   vx,vy: " + vx + ", " + vy + "   fx,fy: " + fx + ", " + fy);
        Timber.w("attract," + nodeA.getIndex() + "," + force + "," + dx + "," + dy + "," + fx + "," + fy);
        Timber.w("attract," + nodeB.getIndex() + "," + -force + "," + dx + "," + dy + "," + fx + "," + fy);
    }
}
