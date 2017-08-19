package com.ninjarific.radiomesh.database.room;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.ninjarific.radiomesh.database.room.entities.Graph;

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
    private RadioPointDao radioDao;
    private ConnectionDao connectionDao;

    @Before
    public void setUp() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();
        database = Room.inMemoryDatabaseBuilder(context, RoomDatabase.class).build();
        graphDao = database.getGraphDao();
        radioDao = database.getRadioPointDao();
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

        List<Graph> graphList = graphDao.getAll();
        assertEquals(1, graphList.size());
    }
}
