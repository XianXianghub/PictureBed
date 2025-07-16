package com.meferi.mssql.db;

import android.content.Context;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ConfigManager {

    private final ConfigDao configDao;

    public ConfigManager(Context context) {
        configDao = AppDatabase.getInstance(context).configDao();
    }

    // 异步写入或更新配置（推荐在后台线程中调用）
    public void putConfig(String key, String value) {
        Executors.newSingleThreadExecutor().execute(() -> {
            ConfigEntity entity = configDao.getByKey(key);
            if (entity != null) {
                entity.value = value;
                configDao.update(entity);
            } else {
                int nextId = configDao.getAll().size();
                configDao.insert(new ConfigEntity(nextId, key, value));
            }
        });
    }
    public String getConfig(String key) {
        Callable<String> task = () -> {
            ConfigEntity entity = configDao.getByKey(key);
            return entity != null ? entity.value : "";
        };
        Future<String> future = Executors.newSingleThreadExecutor().submit(task);
        try {
            return future.get(); // 阻塞等待结果
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return "";
        }
    }
    // 同步读取配置（注意：不要在主线程调用）
    public String getConfig(String key, String defaultValue) {
        Callable<String> task = () -> {
            ConfigEntity entity = configDao.getByKey(key);
            return entity != null ? entity.value : defaultValue;
        };
        Future<String> future = Executors.newSingleThreadExecutor().submit(task);
        try {
            return future.get(); // 阻塞等待结果
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    // 同步获取所有配置（不推荐主线程调用）
    public List<ConfigEntity> getAllConfigs() {
        Callable<List<ConfigEntity>> task = configDao::getAll;
        Future<List<ConfigEntity>> future = Executors.newSingleThreadExecutor().submit(task);
        try {
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
