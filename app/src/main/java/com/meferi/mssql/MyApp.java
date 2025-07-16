package com.meferi.mssql;

import android.app.Application;
import android.util.Log;

import com.meferi.mssql.db.AppDatabase;
import com.meferi.mssql.db.ConfigDao;
import com.meferi.mssql.db.ConfigEntity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MyApp extends Application {
    private static final String TAG = "MyApp";
    private static MyApp instance;

    public static MyApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        new Thread(() -> {
            try {
                ConfigDao configDao = AppDatabase.getInstance(this).configDao();

                // 只在数据库为空时初始化
                if (configDao.getAll().isEmpty()) {
                    List<ConfigEntity> defaultConfigs = loadDefaultConfigFromJson();
                    Log.d(TAG, "defaultConfigs="+defaultConfigs);

                    configDao.insertAll(defaultConfigs);
                    Log.d(TAG, "du，configDao.getAll()="+configDao.getAll());

                    Log.d(TAG, "默认配置写入数据库");
                } else {
                    List<ConfigEntity> list = configDao.getAll();
                    for (ConfigEntity entity : list){
                        Log.d(TAG, "entity="+entity);
                    }
                    Log.d(TAG, "已存在配置数据，configDao.getAll()="+configDao.getAll());

                    Log.d(TAG, "已存在配置数据，跳过初始化");
                }
            } catch (Exception e) {
                Log.e(TAG, "初始化 Room 配置失败", e);
            }
        }).start();
    }

    private List<ConfigEntity> loadDefaultConfigFromJson() {
        List<ConfigEntity> list = new ArrayList<>();

        try {
            InputStream is = getAssets().open("default_config.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) builder.append(line);
            reader.close();

            JSONArray jsonArray = new JSONArray(builder.toString());

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                int id = obj.getInt("id");
                String key = obj.getString("key");
                String value = obj.getString("value");
                list.add(new ConfigEntity(id, key, value));
            }

        } catch (Exception e) {
            Log.e(TAG, "读取 JSON 配置失败", e);
        }

        return list;
    }
}
