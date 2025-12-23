package com.example.deviceinfo.fragment.config_editor.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class BaseConfig implements Serializable {
    public String configId;
    public String configName;
    public long createdAt;
    public long updatedAt;

    public Map<String, Object> data = new HashMap<>();

    public abstract List<ConfigItem> getConfigItems();

    public abstract String getKeyOfDefaultName();

    public static final Set<Class<?>> NumberGroups = Set.of(
            Integer.class, int.class,
            Long.class, long.class,
            Float.class, float.class,
            Double.class, double.class,
            Short.class, short.class,
            Byte.class, byte.class
    );

    public static final Set<Class<?>> BooleanGroups = Set.of(
            Boolean.class, boolean.class
    );

    public Class<?> getKeyType(String key) {
        for (ConfigItem item : getConfigItems()) {
            if (item.key.equals(key)) {
                return item.type;
            }
        }
        return null;
    }

    public String getDefaultConfigName() {
        String key = getKeyOfDefaultName();
        Object value = data.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    public BaseConfig copy() {
        try {
            Class<? extends BaseConfig> cls = this.getClass();
            BaseConfig newObj = cls.newInstance();
            newObj.configId = this.configId;
            newObj.configName = this.configName;
            newObj.createdAt = this.createdAt;
            newObj.updatedAt = this.updatedAt;
            newObj.data.putAll(this.data);
            return newObj;
        } catch (Exception e) {
            Log.e("BaseConfig", "Failed to copy object", e);
            return null;
        }
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject root = new JSONObject();
        root.put("configId", configId);
        root.put("configName", configName);
        root.put("createdAt", createdAt);
        root.put("updatedAt", updatedAt);

        JSONObject data = new JSONObject();
        for (Map.Entry<String, Object> entry : this.data.entrySet()) {
            String key = entry.getKey();
            Class<?> type = getKeyType(key);
            if (type == null) {
                Log.w("BaseConfig", "Unknown config key: " + key);
                continue;
            }
            if (isTypeDifferent(type, entry.getValue().getClass())) {
                Log.w("BaseConfig", "Type mismatch for key: " + key + ", expected: " + type.getName() + ", got: " + entry.getValue().getClass().getName());
                continue;
            }
            data.put(entry.getKey(), entry.getValue());
        }
        root.put("data", data);
        return root;
    }

    public static <T extends BaseConfig> T fromJsonObject(JSONObject obj, Class<T> cls) throws Exception {
        T config = cls.newInstance();
        config.configId = obj.getString("configId");
        config.configName = obj.getString("configName");
        config.createdAt = obj.getLong("createdAt");
        config.updatedAt = obj.getLong("updatedAt");

        JSONObject data = obj.getJSONObject("data");
        for (Iterator<String> it = data.keys(); it.hasNext(); ) {
            String key = it.next();
            Class<?> type = config.getKeyType(key);
            if (type == null) {
                Log.w("BaseConfig", "Unknown config key: " + key);
                continue;
            }
            Object value = data.get(key);
            if (isTypeDifferent(type, value.getClass())) {
                Log.w("BaseConfig", "Type mismatch for key: " + key + ", expected: " + type.getName() + ", got: " + value.getClass().getName());
                continue;
            }
            config.data.put(key, value);
        }
        return config;
    }

    private static boolean isTypeDifferent(Class<?> c1, Class<?> c2) {
        if (c1.equals(c2)) {
            return false;
        }
        if (NumberGroups.contains(c1) && NumberGroups.contains(c2)) {
            return false;
        }
        return !BooleanGroups.contains(c1) || !BooleanGroups.contains(c2);
    }

    public static class ConfigItem implements Serializable {
        public String key;
        public Class<?> type;
        public String description;

        public ConfigItem(String key, Class<?> type, String description) {
            this.key = key;
            this.type = type;
            this.description = description;
        }
    }
}
