package com.ninjarific.radiomesh.database.room;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import com.ninjarific.radiomesh.database.IDatabase;
import com.ninjarific.radiomesh.database.room.entities.Connection;
import com.ninjarific.radiomesh.database.room.entities.Graph;
import com.ninjarific.radiomesh.database.room.entities.Node;
import com.ninjarific.radiomesh.database.room.queries.PopulatedGraph;
import com.ninjarific.radiomesh.utils.listutils.ListUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

public class DatabaseHelper implements IDatabase {

    private final RoomDatabase database;

    public static DatabaseHelper init(Context context) {
        return new DatabaseHelper(Room.databaseBuilder(context, RoomDatabase.class, "database").build());
    }

    private DatabaseHelper(RoomDatabase database) {
        this.database = database;
    }

    public void getAllGraphs(GraphLoadedCallback callback) {
        new LoadGraphs(callback).execute();
    }

    @Override
    public void registerScanResults(List<ScanResult> scanResults, @Nullable Runnable scanFinishedCallback) {
        new ProcessScanResults(scanResults, scanFinishedCallback).execute();
    }

    private static List<Connection> connectNodes(List<Long> nodeIds) {
        List<Connection> connections = new ArrayList<>(nodeIds.size() * 2);
        for (Long a : nodeIds) {
            for (Long b : nodeIds) {
                if (a.equals(b)) {
                    continue;
                }
                connections.add(new Connection(a, b));
            }
        }
        return connections;
    }

    public interface GraphLoadedCallback {
        void onGraphsLoaded(List<PopulatedGraph> graphs);
    }

    private class LoadGraphs extends AsyncTask<Void, Void, List<PopulatedGraph>> {
        private final GraphLoadedCallback callback;

        public LoadGraphs(GraphLoadedCallback callback) {
            this.callback = callback;
        }

        @Override
        protected List<PopulatedGraph> doInBackground(Void... voids) {
            return database.getGraphDao().loadGraphs();
        }

        @Override
        protected void onPostExecute(List<PopulatedGraph> populatedGraphs) {
            super.onPostExecute(populatedGraphs);
            callback.onGraphsLoaded(populatedGraphs);
        }
    }

    private class ProcessScanResults extends AsyncTask<Void, Void, Void> {

        private final List<ScanResult> scanResults;
        @Nullable
        private final Runnable scanFinishedCallback;

        ProcessScanResults(List<ScanResult> scanResults, @Nullable Runnable scanFinishedCallback) {
            this.scanResults = scanResults;
            this.scanFinishedCallback = scanFinishedCallback;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Timber.d("ProcessScanResults: async transaction begun");
            List<Long> existingNodes = new ArrayList<>(scanResults.size());
            List<ScanResult> newResults = new ArrayList<>(scanResults.size());
            Set<Long> foundGraphsSet = new HashSet<>();
            final NodeDao nodeDao = database.getNodeDao();
            for (ScanResult result : scanResults) {
                Node existingNode = nodeDao.get(result.BSSID);
                if (existingNode == null) {
                    Timber.v(">>>> new point found " + result.BSSID);
                    newResults.add(result);
                } else {
                    existingNodes.add(existingNode.getId());
                    foundGraphsSet.add(existingNode.getGraphId());
                }
            }
            List<Long> foundGraphsList = new ArrayList<>(foundGraphsSet);
            final GraphDao graphDao = database.getGraphDao();
            final ConnectionDao connectionDao = database.getConnectionDao();
            if (foundGraphsSet.isEmpty()) {
                // create new graph for all results
                Graph graph = new Graph();
                long graphId = graphDao.insert(graph);
                List<Node> nodes = ListUtils.map(newResults, scanResult -> new Node(scanResult.BSSID, scanResult.SSID, graphId));
                List<Long> newNodes = nodeDao.insertAll(nodes);

                // create connections
                List<Connection> connections = connectNodes(newNodes);
                connectionDao.insertAll(connections);

            } else if (foundGraphsSet.size() == 1) {
                // single existing graph; append new results
                long graphId = foundGraphsList.get(0);
                List<Node> nodes = ListUtils.map(newResults, scanResult -> new Node(scanResult.BSSID, scanResult.SSID, graphId));
                List<Long> nodeIds = nodeDao.insertAll(nodes);
                nodeIds.addAll(existingNodes);

                // create connections
                List<Connection> connections = connectNodes(nodeIds);
                connectionDao.insertAll(connections);

            } else {
                // merge graphs, add all results to first graph
                long graphId = foundGraphsList.get(0);
                for (int i = 1; i < foundGraphsSet.size(); i++) {
                    PopulatedGraph populatedGraph = graphDao.loadGraph(i);
                    List<Node> graphNodes = populatedGraph.getNodes();
                    for (Node node : graphNodes) {
                        node.setGraphId(graphId);
                    }
                    nodeDao.updateNodes(graphNodes);
                    graphDao.delete(populatedGraph.getGraph());
                }
                List<Node> nodes = ListUtils.map(newResults, scanResult -> new Node(scanResult.BSSID, scanResult.SSID, graphId));
                List<Long> nodeIds = nodeDao.insertAll(nodes);
                nodeIds.addAll(existingNodes);

                // create connections
                List<Connection> connections = connectNodes(nodeIds);
                connectionDao.insertAll(connections);
            }
            Timber.d("> registerScanResults: async transaction completed");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Timber.d("ProcessScanResults: post execute; has callback? " + (scanFinishedCallback != null));
            if (scanFinishedCallback != null) {
                scanFinishedCallback.run();
            }
        }
    }
}
