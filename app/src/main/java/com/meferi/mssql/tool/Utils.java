package com.meferi.mssql.tool;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.meferi.mssql.MyApp;
import com.meferi.mssql.db.ConfigEntity;
import com.meferi.mssql.db.ConfigManager;

import java.io.ByteArrayOutputStream;
import java.util.Comparator;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import kotlin.jvm.JvmStatic;
import kotlin.jvm.internal.Intrinsics;

public final class Utils {
    private static String TAG = Utils.class.getSimpleName();
    public static final Utils INSTANCE = new Utils();
    private static SharedPreferences sharedPreferences;
    public static final String CONFIG_PREFIX = "\u0012\u0013";
    public static final String CONFIG_SUBFIX = "\u0013\u0012";

    private Utils() {
    }
    public static String unzipString(String str) throws Exception {
        byte[] decode = Base64.decode(str, 1);
        Inflater inflater = new Inflater();
        inflater.setInput(decode);
        byte[] bArr = new byte[256];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(256);
        while (!inflater.finished()) {
            byteArrayOutputStream.write(bArr, 0, inflater.inflate(bArr));
        }
        inflater.end();
        return byteArrayOutputStream.toString();
    }
    public static String zipString(String str) {
        Deflater deflater = new Deflater(9);
        deflater.setInput(str.getBytes());
        deflater.finish();
        byte[] bArr = new byte[256];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(256);
        while (!deflater.finished()) {
            byteArrayOutputStream.write(bArr, 0, deflater.deflate(bArr));
        }
        deflater.end();
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), 1);
    }
    @JvmStatic
    public static final void init(Context context) {
        Intrinsics.checkNotNullParameter(context, "context");
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(context.getPackageName() + "_scanner", 4);
        }
    }

    @JvmStatic
    public static final void putString(String key, String value) {
        SharedPreferences sharedPreferences2 = sharedPreferences;
       ;
        sharedPreferences2.edit().putString(key, value).apply();
    }

    public final String getString(String key) {
        return getString(key, "");
    }

    public final String getString(String key, String defaultValue) {
        SharedPreferences sharedPreferences2 = sharedPreferences;
       ;
        return sharedPreferences2.getString(key, defaultValue);
    }

    public final void putInt(String key, int value) {
        SharedPreferences sharedPreferences2 = sharedPreferences;
       ;
        sharedPreferences2.edit().putInt(key, value).apply();
    }

    public final int getInt(String key) {
        return getInt(key, 0);
    }

    public final int getInt(String key, int defaultValue) {
        SharedPreferences sharedPreferences2 = sharedPreferences;
       ;
        return sharedPreferences2.getInt(key, defaultValue);
    }

    @JvmStatic
    public static final void putBoolean(String key, boolean value) {
        SharedPreferences sharedPreferences2 = sharedPreferences;
       ;
        sharedPreferences2.edit().putBoolean(key, value).apply();
    }

    public final boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    @JvmStatic
    public static final boolean getBoolean(String key, boolean defaultValue) {
        SharedPreferences sharedPreferences2 = sharedPreferences;
       ;
        return sharedPreferences2.getBoolean(key, defaultValue);
    }

    public final void putFloat(String key, float value) {
        SharedPreferences sharedPreferences2 = sharedPreferences;
       ;
        sharedPreferences2.edit().putFloat(key, value).apply();
    }

    public final float getFloat(String key) {
        return getFloat(key, 0.0f);
    }

    public final float getFloat(String key, float defaultValue) {
        SharedPreferences sharedPreferences2 = sharedPreferences;
       ;
        return sharedPreferences2.getFloat(key, defaultValue);
    }

    public final void putLong(String key, long value) {
        SharedPreferences sharedPreferences2 = sharedPreferences;
       ;
        sharedPreferences2.edit().putLong(key, value).apply();
    }

    public final long getLong(String key) {
        return getLong(key, 0L);
    }

    public final long getLong(String key, long defaultValue) {
        SharedPreferences sharedPreferences2 = sharedPreferences;
       ;
        return sharedPreferences2.getLong(key, defaultValue);
    }

    public final void remove(String key) {
        SharedPreferences sharedPreferences2 = sharedPreferences;
       ;
        sharedPreferences2.edit().remove(key).apply();
    }

    public final void clear() {
        SharedPreferences sharedPreferences2 = sharedPreferences;
       ;
        sharedPreferences2.edit().clear().apply();
    }
    public static boolean isValidConfig(String configString) {
        return configString != null
                && configString.startsWith(CONFIG_PREFIX)
                && configString.endsWith(CONFIG_SUBFIX)
                && configString.length() > CONFIG_PREFIX.length() + CONFIG_SUBFIX.length();
    }
    public static boolean importConfig(Context context, String configString) {
        ConfigManager configManager = new ConfigManager(context);
        Log.d(TAG, "Original configString: " + configString);

        // Extract payload
        configString = extractConfigPayload(configString);

        // Unzip config string
        try {
            configString = Utils.unzipString(configString);
        } catch (Exception e) {
            Log.e(TAG, "Failed to unzip config string", e);
            return false;
        }

        Log.d(TAG, "Unzipped configString: " + configString);

        // Check prefix
        if (!configString.startsWith("mssql")) {
            Log.w(TAG, "Invalid config format: does not start with 'mssql'");
            return false;
        }

        // Extract version
        String version = "0.0";
        if (configString.contains("\u001c")) {
            String[] array = configString.split("\u001c");
            if (array.length > 2) {
                version = array[1];
                Log.d(TAG, "Parsed config version: " + version);
            }
        }

        // Compare with current version
        if (!version.equals(MyApp.configVersion)) {
            Log.w(TAG, "Version mismatch: file=" + version + ", current=" + MyApp.configVersion);
            // You may return false here if strict version match is required
        }

        // Remove prefix: mssql + version + delimiter
        configString = configString.replaceFirst("mssql\u001c" + version + "\u001c", "");
        Log.d(TAG, "Final config payload: ===" + configString + "===");

        // Load current config keys
        List<ConfigEntity> configs = configManager.getAllConfigs();
        if (configs == null || configs.isEmpty()) {
            Log.w(TAG, "No existing configs found in database");
            return false;
        }

        // Split incoming values
        String[] values = configString.split("\u001c");
        if (values.length > configs.size()) {
            Log.e(TAG, "Too many config values; expected max " + configs.size() + ", got " + values.length);
            return false;
        }

        // Sort configs by ID
        configs.sort(Comparator.comparingInt(c -> c.id));

        // Update config entries
        for (int i = 0; i < values.length; i++) {
            String key = configs.get(i).key;
            String value = values[i];
            configManager.putConfig(key, value);
            Log.d(TAG, "Set config: " + key + " = " + value);
        }

        return true;
    }

    public static String extractConfigPayload(String configString) {
        // 截取中间内容
        return configString.substring(CONFIG_PREFIX.length(), configString.length() - CONFIG_SUBFIX.length());
    }


}