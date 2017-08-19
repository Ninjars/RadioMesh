package com.ninjarific.radiomesh.database.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.ninjarific.radiomesh.database.room.entities.Connection;

import java.util.List;

@Dao
public interface ConnectionDao {

    @Query("SELECT * FROM connections WHERE fromNodeId LIKE :bssid")
    List<Connection> getConnectionsForRadioPoint(String bssid);

    @Insert
    void insertAll(Connection... entities);

    @Delete
    void delete(Connection entity);
}
