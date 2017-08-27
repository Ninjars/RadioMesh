package com.ninjarific.radiomesh.database.room;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.ninjarific.radiomesh.database.room.entities.Connection;
import com.ninjarific.radiomesh.database.room.entities.Graph;
import com.ninjarific.radiomesh.database.room.entities.Node;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class RoomDatabaseTest {

    private RoomDatabase database;
    private GraphDao graphDao;
    private NodeDao nodeDao;
    private ConnectionDao connectionDao;

    @Before
    public void setUp() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();
        database = Room.inMemoryDatabaseBuilder(context, RoomDatabase.class).build();
        graphDao = database.getGraphDao();
        nodeDao = database.getNodeDao();
        connectionDao = database.getConnectionDao();
    }

    @After
    public void tearDown() throws Exception {
        database.close();
    }

    @Test
    public void writeGraphIn() throws Exception {
        Graph graph = new Graph();
        graphDao.insert(graph);

        assertEquals(1, graphDao.getAll().size());
    }

    @Test
    public void deleteGraph() throws Exception {
        Graph graph = new Graph();
        graphDao.insert(graph);
        graphDao.delete(graphDao.getAll().get(0));

        assertEquals(0, graphDao.getAll().size());
    }

    @Test
    public void writeNodeIn() throws Exception {
        Graph graph = new Graph();
        graphDao.insert(graph);
        int graphId = graphDao.getAll().get(0).getId();
        Node node = new Node("bssid", "ssid", graphId);
        nodeDao.insertAll(node);

        node = nodeDao.getAll().get(0);
        assertEquals("bssid", node.getBssid());
        assertEquals("ssid", node.getSsid());
        assertEquals(graphId, node.getGraphId());
    }

    @Test
    public void deleteNode() throws Exception {
        Graph graph = new Graph();
        graphDao.insert(graph);
        int graphId = graphDao.getAll().get(0).getId();
        Node node = new Node("bssid", "ssid", graphId);
        nodeDao.insertAll(node);
        nodeDao.delete(nodeDao.getAll().get(0));

        assertEquals(0, nodeDao.getAll().size());
    }

    @Test
    public void addNodeToGraph() throws Exception {
        Graph graph = new Graph();
        graphDao.insert(graph);

        graph = graphDao.getAll().get(0);

        final String bssid1 = "bssid1";
        final String ssid1 = "ssid1";
        final String bssid2 = "bssid2";
        final String ssid2 = "ssid2";
        Node node1 = new Node(bssid1, ssid1, graph.getId());
        Node node2 = new Node(bssid2, ssid2, graph.getId());

        nodeDao.insertAll(node1, node2);
        List<Node> graphNodes = nodeDao.getAllForGraph(graph.getId());
        assertEquals(2, graphNodes.size());

        graphNodes.sort((a, b) -> a.getBssid().compareTo(b.getBssid()));

        assertEquals(bssid1, graphNodes.get(0).getBssid());
        assertEquals(ssid1, graphNodes.get(0).getSsid());

        assertEquals(bssid2, graphNodes.get(1).getBssid());
        assertEquals(ssid2, graphNodes.get(1).getSsid());
    }

    @Test
    public void connectNodes() throws Exception {
        Graph graph = new Graph();
        graphDao.insert(graph);
        int graphId = graphDao.getAll().get(0).getId();

        final String bssid1 = "bssid1";
        final String ssid1 = "ssid1";
        final String bssid2 = "bssid2";
        final String ssid2 = "ssid2";
        Node node1 = new Node(bssid1, ssid1, graphId);
        Node node2 = new Node(bssid2, ssid2, graphId);
        nodeDao.insertAll(node1, node2);

        Connection connection = new Connection(node1.getBssid(), node2.getBssid());
        connectionDao.insertAll(connection);

        connection = connectionDao.getConnectionsForRadioPoint(bssid1).get(0);

        assertEquals(bssid1, connection.getFromNodeId());
        assertEquals(bssid2, connection.getToNodeId());

        List<Connection> oneWayConnections = connectionDao.getConnectionsForRadioPoint(bssid2);
        assertEquals(0, oneWayConnections.size());
    }
}
