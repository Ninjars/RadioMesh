package com.ninjarific.radiomesh.database.room.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "connections",
        foreignKeys = {
                @ForeignKey(parentColumns = "id", childColumns = "fromNodeId", entity = Node.class),
                @ForeignKey(parentColumns = "id", childColumns = "toNodeId", entity = Node.class)
        },
        indices = {
                @Index(value = {"fromNodeId", "toNodeId"}, unique = true),
                @Index(value = {"toNodeId"})
        })
public class Connection {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private long fromNodeId;
    private long toNodeId;

    public Connection() {}

    @Ignore
    public Connection(long fromNodeId, long toNodeId) {
        this.fromNodeId = fromNodeId;
        this.toNodeId = toNodeId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getFromNodeId() {
        return fromNodeId;
    }

    public void setFromNodeId(long fromNodeId) {
        this.fromNodeId = fromNodeId;
    }

    public long getToNodeId() {
        return toNodeId;
    }

    public void setToNodeId(long toNodeId) {
        this.toNodeId = toNodeId;
    }
}
