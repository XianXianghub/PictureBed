package com.meferi.mssql.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ConfigDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ConfigEntity> configs);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ConfigEntity config);

    @Update
    void update(ConfigEntity config);

    @Query("SELECT * FROM config ORDER BY id ASC")
    List<ConfigEntity> getAll();

    @Query("SELECT * FROM config WHERE `key` = :key LIMIT 1")
    ConfigEntity getByKey(String key);

    @Query("DELETE FROM config")
    void clearAll();
}
