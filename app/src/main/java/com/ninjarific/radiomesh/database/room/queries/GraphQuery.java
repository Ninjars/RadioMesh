package com.ninjarific.radiomesh.database.room.queries;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import com.ninjarific.radiomesh.database.room.entities.Graph;
import com.ninjarific.radiomesh.database.room.entities.RadioPoint;

import java.util.List;

public class GraphQuery {
    @Embedded
    private Graph graph;
    @Relation(parentColumn = "id", entityColumn = "graphId", entity = RadioPoint.class)
    private List<RadioPoint> radioPoints;

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public List<RadioPoint> getRadioPoints() {
        return radioPoints;
    }

    public void setRadioPoints(List<RadioPoint> radioPoints) {
        this.radioPoints = radioPoints;
    }
}
