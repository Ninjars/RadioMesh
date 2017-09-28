package com.ninjarific.radiomesh.ui.clusters;

class ForceHelper {
    private static final double MAX_FORCE_DISTANCE = 100;
    private static final double MAX_STEP_DISTANCE = 10;
    private static final double SPRING_FACTOR = 0.5;
    private static final double SPRING_DIVISOR = 0.1;
    private static final double REPEL_FACTOR = 0.25;

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
        double weightFactor = nodeB.getWeight() / nodeA.getWeight();
        nodeA.addForce(fx * weightFactor, fy * weightFactor);
        nodeB.addForce(-fx / weightFactor, -fy / weightFactor);
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
        double weightFactor = nodeB.getWeight() / nodeA.getWeight();
        nodeA.addForce(fx * weightFactor, fy * weightFactor);
        nodeB.addForce(-fx / weightFactor, -fy / weightFactor);
    }
}
