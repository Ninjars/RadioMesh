package com.ninjarific.radiomesh.database.room.queries;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import com.ninjarific.radiomesh.database.room.entities.Connection;
import com.ninjarific.radiomesh.database.room.entities.Node;

import java.util.List;

public class ConnectedRadioPoint {
    @Embedded
    private Node node;
    @Relation(parentColumn = "bssid", entityColumn = "fromNodeId", entity = Connection.class)
    private List<Connection> connections;
}
