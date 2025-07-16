package com.meferi.mssql;

import android.app.Application;
import android.util.Log;

import com.meferi.mssql.db.AppDatabase;
import com.meferi.mssql.db.ConfigDao;
import com.meferi.mssql.db.ConfigEntity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MyApp extends Application {
    private static final String TAG = "MyApp";
    private static MyApp instance;

    public static MyApp getInstance() {
        return instance;
    }

    // Global config version
    public static String configVersion = "1.0";

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        new Thread(() -> {
            try {
                ConfigDao configDao = AppDatabase.getInstance(this).configDao();
                List<ConfigEntity> existingConfigs = configDao.getAll();

                // Always read version from JSON
                List<ConfigEntity> defaultConfigs = loadDefaultConfigFromJson();

                if (existingConfigs.isEmpty()) {
                    Log.d(TAG, "No existing config, initializing from JSON.");
                    configDao.insertAll(defaultConfigs);
                    Log.d(TAG, "Inserted default configs into database.");
                } else {
                    Log.d(TAG, "Existing config found, skipping initialization.");
                    for (ConfigEntity entity : existingConfigs) {
                        Log.d(TAG, "Existing config: " + entity);
                    }
                }

                Log.d(TAG, "Current config version: " + configVersion);
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize Room config", e);
            }
        }).start();
    }

    private List<ConfigEntity> loadDefaultConfigFromJson() {
        List<ConfigEntity> list = new ArrayList<>();

        try (InputStream is = getAssets().open("default_config2.json");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            JSONObject jsonObject = new JSONObject(builder.toString());

            // Update global version
            configVersion = jsonObject.optString("version", "1.0");

            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if ("version".equals(key)) continue;

                JSONObject item = jsonObject.getJSONObject(key);
                int id = item.getInt("id");
                String value = item.getString("value");

                list.add(new ConfigEntity(id, key, value));
            }

        } catch (Exception e) {
            Log.e(TAG, "Failed to load default_config.json", e);
        }

        return list;
    }
}
