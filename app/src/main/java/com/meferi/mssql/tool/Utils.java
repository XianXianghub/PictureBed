package com.meferi.mssql.tool;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.meferi.mssql.db.ConfigEntity;
import com.meferi.mssql.db.ConfigManager;

import java.io.ByteArrayOutputStream;
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
        ConfigManager configManager = new ConfigManager(context); // 如果在 Activity 中

        configString = extractConfigPayload(configString);
        try {
            configString = Utils.unzipString(configString);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "unzipString exception");
            return false    ;
        }
        List<ConfigEntity> configs = configManager.getAllConfigs();

        if (configs == null || configs.isEmpty()) return false;

        String[] values = configString.split("\u001c"); // 分隔符为 ASCII 0x1C
        // 按id排序（假设数据库已经按id顺序，否则这里再排序）
        configs.sort((a, b) -> Integer.compare(a.id, b.id));
        if(values != null && values.length <= configs.size()) {
            for (int i = 0; i < configs.size(); i++) {
                configManager.putConfig(configs.get(i).key, values[i]);
                Log.d(TAG, "putConfig: " + configs.get(i).key + " " + values[i] );
            }
        }
        Log.d(TAG, "unzipString: " + configString);
        return true;
    }
    public static String extractConfigPayload(String configString) {
        // 截取中间内容
        return configString.substring(CONFIG_PREFIX.length(), configString.length() - CONFIG_SUBFIX.length());
    }


}