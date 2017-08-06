package com.ninjarific.radiomesh.database.room.queries;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import com.ninjarific.radiomesh.database.room.entities.Connection;
import com.ninjarific.radiomesh.database.room.entities.RadioPoint;

import java.util.List;

public class ConnectedRadioPoint {
    @Embedded
    private RadioPoint radioPoint;
    @Relation(parentColumn = "bssid", entityColumn = "fromNodeId", entity = Connection.class)
    private List<Connection> connections;
}
