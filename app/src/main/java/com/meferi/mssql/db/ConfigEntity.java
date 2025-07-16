package com.meferi.mssql.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "config")
public class ConfigEntity {

    @PrimaryKey
    public int id;

    public String key;
    public String value;

    public ConfigEntity(int id, String key, String value) {
        this.id = id;
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return "ConfigEntity{" +
                "id=" + id +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
