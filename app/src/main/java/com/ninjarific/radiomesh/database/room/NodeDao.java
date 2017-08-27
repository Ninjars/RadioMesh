package com.ninjarific.radiomesh.database.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.ninjarific.radiomesh.database.room.entities.Node;

import java.util.List;

@Dao
public interface NodeDao {
    @Query("SELECT * FROM radiopoints")
    List<Node> getAll();

    @Query("SELECT * FROM radiopoints WHERE graph_id LIKE :graphId")
    List<Node> getAllForGraph(int graphId);

    @Insert
    void insertAll(Node... entities);

    @Delete
    void delete(Node entity);
}
