package com.ninjarific.radiomesh.database.room;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.support.annotation.Nullable;

import com.ninjarific.radiomesh.database.IDatabase;
import com.ninjarific.radiomesh.database.room.entities.Connection;
import com.ninjarific.radiomesh.database.room.entities.Graph;
import com.ninjarific.radiomesh.database.room.entities.Node;

import java.util.List;

import io.reactivex.Flowable;

public class DatabaseHelper implements IDatabase {

    private final RoomDatabase database;

    public static DatabaseHelper init(Context context) {
        return new DatabaseHelper(Room.databaseBuilder(context, RoomDatabase.class, "database").build());
    }

    private DatabaseHelper(RoomDatabase database) {
        this.database = database;
    }

    public Flowable<List<Graph>> observeGraphs() {
        return database.getGraphDao().getGraphsObservable();
    }

    public Flowable<List<Node>> getNodesForGraphObs(long graphId) {
        return database.getNodeDao().observeForGraph(graphId);
    }

    public Flowable<List<Node>> getAllNodes() {
        return database.getNodeDao().observeAll();
    }

    @Override
    public void registerScanResults(List<ScanResult> scanResults, @Nullable Runnable scanFinishedCallback) {
        new ProcessScanResults(database, scanResults, scanFinishedCallback).execute();
    }

    public List<Connection> getConnectedNodes(long nodeId) {
        return database.getConnectionDao().getConnectionsForNode(nodeId);
    }

    public Node getNode(long nodeId) {
        return database.getNodeDao().get(nodeId);
    }

}
