package com.ninjarific.radiomesh.ui.clusters;

import java.util.List;

import timber.log.Timber;

public class CompoundNode {
    private final Long id;
    private final List<Long> containedNodes;
    private final List<Long> neighbourIds;

    private CompoundNode(Long id, List<Long> containedNodes, List<Long> neighbourIds) {
        this.id = id;
        this.containedNodes = containedNodes;
        this.neighbourIds = neighbourIds;
    }

    public static CompoundNode create(Long id, List<Long> containedNodes, List<Long> neighbourIds) {
        Timber.d("create: " + id + " : " + containedNodes + " neighbouring " + neighbourIds);
        return new CompoundNode(id, containedNodes, neighbourIds);
    }

    public Long getId() {
        return id;
    }

    public List<Long> getContainedNodes() {
        return containedNodes;
    }

    public List<Long> getNeighbourIds() {
        return neighbourIds;
    }
}
