package com.ninjarific.radiomesh.database.room.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.util.List;


@Entity(tableName = "radiopoints",
        foreignKeys = @ForeignKey(entity = Graph.class, parentColumns = "id", childColumns = "graphId"),
        indices = {
                @Index(value = "bssid", unique = true),
                @Index(value = "graphId")
        })
public class RadioPoint {
    @PrimaryKey
    private String bssid;
    private String ssid;
    private int graphId;

    @Ignore
    private List<Connection> connections;

    public List<Connection> getConnections() {
        return connections;
    }

    public void setConnections(List<Connection> connections) {
        this.connections = connections;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public int getGraphId() {
        return graphId;
    }

    public void setGraphId(int graphId) {
        this.graphId = graphId;
    }
}
