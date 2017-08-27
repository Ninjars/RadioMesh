package com.ninjarific.radiomesh.database.room;

import android.arch.persistence.room.Database;

import com.ninjarific.radiomesh.database.room.entities.Connection;
import com.ninjarific.radiomesh.database.room.entities.Graph;
import com.ninjarific.radiomesh.database.room.entities.Node;

@Database(entities =  {Graph.class, Node.class, Connection.class}, version = 1)
public abstract class RoomDatabase extends android.arch.persistence.room.RoomDatabase {
    public abstract GraphDao getGraphDao();
    public abstract NodeDao getNodeDao();
    public abstract ConnectionDao getConnectionDao();
}
