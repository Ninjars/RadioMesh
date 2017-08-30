package com.ninjarific.radiomesh.ui.resultslist;

import com.ninjarific.radiomesh.database.room.entities.Node;

import java.util.List;

public class GraphNodes {
    private final long graphId;
    private final List<Node> nodes;

    public GraphNodes(long graphId, List<Node> nodes) {
        this.graphId = graphId;
        this.nodes = nodes;
    }

    public long getGraphId() {
        return graphId;
    }

    public List<Node> getNodes() {
        return nodes;
    }
}
