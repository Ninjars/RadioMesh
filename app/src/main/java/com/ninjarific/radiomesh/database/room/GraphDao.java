package com.ninjarific.radiomesh.database.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.ninjarific.radiomesh.database.room.entities.Graph;
import com.ninjarific.radiomesh.database.room.queries.PopulatedGraph;

import java.util.List;

@Dao
public interface GraphDao {
    @Query("SELECT * FROM graphs")
    List<Graph> getAll();

    @Query("SELECT * FROM graphs WHERE id = :id")
    PopulatedGraph loadGraph(int id);

    @Insert
    void insertAll(Graph... entities);

    @Insert
    void insert(Graph entity);

    @Delete
    void delete(Graph entity);
}
