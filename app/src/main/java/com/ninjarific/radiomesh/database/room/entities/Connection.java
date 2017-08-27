package com.ninjarific.radiomesh.database.room.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "connections",
        foreignKeys = {
                @ForeignKey(parentColumns = "bssid", childColumns = "fromNodeId", entity = Node.class),
                @ForeignKey(parentColumns = "bssid", childColumns = "toNodeId", entity = Node.class)
        },
        indices = {
                @Index(value = "fromNodeId"),
                @Index(value = "toNodeId")
        })
public class Connection {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String fromNodeId;
    private String toNodeId;

    public Connection() {}

    @Ignore
    public Connection(String fromNodeId, String toNodeId) {
        this.fromNodeId = fromNodeId;
        this.toNodeId = toNodeId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFromNodeId() {
        return fromNodeId;
    }

    public void setFromNodeId(String fromNodeId) {
        this.fromNodeId = fromNodeId;
    }

    public String getToNodeId() {
        return toNodeId;
    }

    public void setToNodeId(String toNodeId) {
        this.toNodeId = toNodeId;
    }
}
