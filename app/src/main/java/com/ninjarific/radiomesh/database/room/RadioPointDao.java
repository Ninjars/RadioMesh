package com.ninjarific.radiomesh.database.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.ninjarific.radiomesh.database.room.entities.RadioPoint;

import java.util.List;

@Dao
public interface RadioPointDao {
    @Query("SELECT * FROM radiopoints")
    List<RadioPoint> getAll();

    @Insert
    void insertAll(RadioPoint... entities);

    @Delete
    void delete(RadioPoint entity);
}
