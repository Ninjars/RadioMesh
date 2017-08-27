package com.ninjarific.radiomesh.database.room.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.util.List;


@Entity(tableName = "radiopoints",
        foreignKeys = @ForeignKey(entity = Graph.class, parentColumns = "id", childColumns = "graph_id"),
        indices = {
                @Index(value = "bssid", unique = true),
                @Index(value = "graph_id")
        })
public class Node {
    @PrimaryKey
    private String bssid;
    private String ssid;
    @ColumnInfo(name = "graph_id")
    private int graphId;
    @Ignore
    private List<Connection> connections;

    public Node() {}

    @Ignore
    public Node(String bssid, String ssid, int graphId) {
        this.bssid = bssid;
        this.ssid = ssid;
        this.graphId = graphId;
    }

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
