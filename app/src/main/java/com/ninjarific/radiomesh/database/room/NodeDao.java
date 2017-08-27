package com.ninjarific.radiomesh.database.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.ninjarific.radiomesh.database.room.entities.Node;

import java.util.List;

@Dao
public interface NodeDao {
    @Query("SELECT * FROM radiopoints")
    List<Node> getAll();

    @Query("SELECT * FROM radiopoints WHERE bssid LIKE :bssid LIMIT 1")
    Node get(String bssid);

    @Query("SELECT * FROM radiopoints WHERE id == :id LIMIT 1")
    Node get(long id);

    @Query("SELECT * FROM radiopoints WHERE graph_id LIKE :graphId")
    List<Node> getAllForGraph(long graphId);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    Long insert(Node entity);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    List<Long> insertAll(Node... entities);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    List<Long> insertAll(List<Node> entities);

    @Delete
    void delete(Node entity);
}
