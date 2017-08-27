package com.ninjarific.radiomesh.database.room.queries;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import com.ninjarific.radiomesh.database.room.entities.Graph;
import com.ninjarific.radiomesh.database.room.entities.Node;

import java.util.List;

public class PopulatedGraph {
    @Embedded
    private Graph graph;
    @Relation(parentColumn = "id", entityColumn = "graph_id", entity = Node.class)
    private List<Node> nodes;

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }
}
