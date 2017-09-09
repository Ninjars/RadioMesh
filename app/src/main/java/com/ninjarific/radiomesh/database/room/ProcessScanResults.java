package com.ninjarific.radiomesh.database.room;

import android.net.wifi.ScanResult;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

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

/**
 * Data persistence task to store scan results as nodes, connect them to each other, and group them in graphs
 */
class ProcessScanResults extends AsyncTask<Void, Void, Void> {

    private final RoomDatabase database;
    private final List<ScanResult> scanResults;
    @Nullable
    private final Runnable scanFinishedCallback;

    ProcessScanResults(RoomDatabase database, List<ScanResult> scanResults, @Nullable Runnable scanFinishedCallback) {
        this.database = database;
        this.scanResults = scanResults;
        this.scanFinishedCallback = scanFinishedCallback;
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

    private static void processScanResults(NodeDao nodeDao, List<ScanResult> scanResults,
                                           List<ScanResult> outNewResults,
                                           List<Long> outExistingNodeIds,
                                           Set<Long> outFoundGraphIds) {
        for (ScanResult result : scanResults) {
            Node existingNode = nodeDao.get(result.BSSID);
            if (existingNode == null) {
                Timber.v(">>>> new point found " + result.BSSID);
                outNewResults.add(result);
            } else {
                outExistingNodeIds.add(existingNode.getId());
                outFoundGraphIds.add(existingNode.getGraphId());
            }
        }
    }

    /**
     * create new graph for all results
     */
    private static void createNewGraph(RoomDatabase database, List<ScanResult> scanResults) {
        Graph graph = new Graph();
        long graphId = database.getGraphDao().insert(graph);

        List<Node> nodes = ListUtils.map(scanResults, scanResult -> new Node(scanResult.BSSID, scanResult.SSID, graphId));
        List<Long> newNodes = database.getNodeDao().insertAll(nodes);

        List<Connection> connections = connectNodes(newNodes);
        database.getConnectionDao().insertAll(connections);
    }

    /**
     * Look up existing graph by ID and add all new nodes to it
     *
     * @param database      to access DAOs
     * @param graphId       of graph to append results to
     * @param scanResults   list of scan results to turn into Nodes
     * @param existingNodes IDs of already persisted nodes that should all share the same graphId, to be used for new connections
     */
    private static void appendToExistingGraph(RoomDatabase database, long graphId, List<ScanResult> scanResults, List<Long> existingNodes) {
        List<Node> nodes = ListUtils.map(scanResults, scanResult -> new Node(scanResult.BSSID, scanResult.SSID, graphId));
        List<Long> nodeIds = database.getNodeDao().insertAll(nodes);
        nodeIds.addAll(existingNodes);

        // create connections
        List<Connection> connections = connectNodes(nodeIds);
        database.getConnectionDao().insertAll(connections);
    }

    /**
     * Merge multiple graphs that are connected by the new ScanResults into a single one.
     * The first graph in the graphIds list will be retained as the graph containing all the nodes;
     * the other graphs will be deleted.
     *
     * @param database      to access DAOs
     * @param graphIds      list of ids that this scan connects
     * @param scanResults   list of scan results to turn into nodes
     * @param existingNodes IDs of already persisted node IDs to move to same graph and connect to new nodes
     */
    private static void mergeGraphs(RoomDatabase database, List<Long> graphIds, List<ScanResult> scanResults, List<Long> existingNodes) {
        long graphId = graphIds.get(0);
        GraphDao graphDao = database.getGraphDao();
        NodeDao nodeDao = database.getNodeDao();
        for (int i = 1; i < graphIds.size(); i++) {
            PopulatedGraph populatedGraph = graphDao.loadGraph(graphIds.get(i));
            List<Node> graphNodes = populatedGraph.getNodes();
            for (Node node : graphNodes) {
                node.setGraphId(graphId);
            }
            int updatedNodeCount = nodeDao.updateNodes(graphNodes);
            Timber.d("Moved " + updatedNodeCount + " nodes from graph " + graphIds.get(i) + " to " + graphId);
            graphDao.delete(populatedGraph.getGraph());
        }
        List<Node> nodes = ListUtils.map(scanResults, scanResult -> new Node(scanResult.BSSID, scanResult.SSID, graphId));
        List<Long> nodeIds = nodeDao.insertAll(nodes);
        nodeIds.addAll(existingNodes);

        // create connections
        List<Connection> connections = connectNodes(nodeIds);
        database.getConnectionDao().insertAll(connections);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Timber.d("ProcessScanResults: async transaction begun");
        List<Long> existingNodes = new ArrayList<>(scanResults.size());
        List<ScanResult> newResults = new ArrayList<>(scanResults.size());
        Set<Long> foundGraphsSet = new HashSet<>();
        final NodeDao nodeDao = database.getNodeDao();
        processScanResults(nodeDao, scanResults, newResults, existingNodes, foundGraphsSet);

        List<Long> foundGraphsList = new ArrayList<>(foundGraphsSet);
        if (foundGraphsSet.isEmpty()) {
            createNewGraph(database, newResults);

        } else if (foundGraphsSet.size() == 1) {
            long graphId = foundGraphsList.get(0);
            appendToExistingGraph(database, graphId, newResults, existingNodes);

        } else {
            mergeGraphs(database, foundGraphsList, newResults, existingNodes);

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