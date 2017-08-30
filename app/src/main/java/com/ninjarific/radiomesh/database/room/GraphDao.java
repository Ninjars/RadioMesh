package com.ninjarific.radiomesh.database.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.ninjarific.radiomesh.database.room.entities.Graph;
import com.ninjarific.radiomesh.database.room.queries.PopulatedGraph;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface GraphDao {
    @Query("SELECT * FROM graphs")
    List<Graph> getAll();

    @Query("SELECT * FROM graphs WHERE id = :id LIMIT 1")
    PopulatedGraph loadGraph(long id);

    @Query("SELECT * FROM graphs")
    List<PopulatedGraph> loadGraphs();

    @Query("SELECT * FROM graphs")
    Flowable<List<Graph>> getGraphsObservable();

    @Insert
    void insertAll(Graph... entities);

    @Insert
    long insert(Graph entity);

    @Delete
    void delete(Graph entity);
}
