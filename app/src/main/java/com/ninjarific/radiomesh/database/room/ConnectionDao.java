package com.ninjarific.radiomesh.database.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.ninjarific.radiomesh.database.room.entities.Connection;

import java.util.List;

@Dao
public interface ConnectionDao {

    @Query("SELECT * FROM connections WHERE fromNodeId == :id")
    List<Connection> getConnectionsForNode(long id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<Connection> entities);

    @Delete
    void delete(Connection entity);
}
