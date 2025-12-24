package com.example.deviceinfo.content_provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.deviceinfo.fragment.config_editor.model.BaseConfig;
import com.example.deviceinfo.pojo.LocationData;
import com.example.deviceinfo.pojo.WifiData;
import com.example.deviceinfo.fragment.config_editor.ConfigStorage;

import java.util.Map;

public class ConfigProvider extends ContentProvider {

    private static final Map<String, Class<? extends BaseConfig>> configClassMap = Map.of(
            WifiData.class.getSimpleName(), WifiData.class,
            LocationData.class.getSimpleName(), LocationData.class
    );

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Bundle call(@NonNull String method, String arg, Bundle extras) {
        if (!ProviderConstants.METHOD_GET_CURRENT_CONFIG.equals(method)) {
            return null;
        }
        if (extras == null) return null;

        String className = extras.getString(ProviderConstants.EXTRA_CLASS_NAME);
        if (className == null) return null;

        Class<? extends BaseConfig> cls = configClassMap.get(className);
        if (cls == null) {
            return null;
        }

        Context context = getContext();
        if (context == null) return null;

        try {
            BaseConfig config = ConfigStorage.getCurrentConfig(context, cls);
            if (config == null) return null;

            Bundle bundle = new Bundle();

            for (Map.Entry<String, Object> e : config.data.entrySet()) {
                Object v = e.getValue();
                String k = e.getKey();

                if (v instanceof String) {
                    bundle.putString(k, (String) v);
                } else if (v instanceof Integer) {
                    bundle.putInt(k, (Integer) v);
                } else if (v instanceof Boolean) {
                    bundle.putBoolean(k, (Boolean) v);
                } else if (v instanceof Long) {
                    bundle.putLong(k, (Long) v);
                } else if (v instanceof Float) {
                    bundle.putFloat(k, (Float) v);
                } else if (v instanceof Double) {
                    bundle.putDouble(k, (Double) v);
                }
            }
            return bundle;

        } catch (Exception e) {
            Log.e("ConfigProvider", "getCurrentConfig failed", e);
            return null;
        }
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String s, String[] as) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues v, String s, String[] as) {
        return 0;
    }
}