package com.meferi.mssql;

import android.content.Context;
import android.content.SharedPreferences;

import kotlin.jvm.JvmStatic;
import kotlin.jvm.internal.Intrinsics;

public final class Utils {
    public static final Utils INSTANCE = new Utils();
    private static SharedPreferences sharedPreferences;

    private Utils() {
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
}