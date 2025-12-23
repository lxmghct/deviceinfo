package com.example.deviceinfo.fragment.config_editor.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

public abstract class BaseConfig implements Serializable {
    public String configId;
    public String configName;
    public long createdAt;
    public long updatedAt;

    public abstract List<ConfigItem> getConfigItems();

    public abstract String getKeyOfDefaultName();

    public JSONObject toJsonObject() throws JSONException, IllegalAccessException {
        JSONObject root = new JSONObject();
        root.put("configId",configId);
        root.put("configName", configName);
        root.put("createdAt", createdAt);
        root.put("updatedAt", updatedAt);

        JSONObject data = new JSONObject();
        for (Field f :getClass().getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers())) continue;
            f.setAccessible(true);
            Object val = f.get(this);
            if (val != null) {
                data.put(f.getName(), val);
            }
        }
        root.put("data", data);
        return root;
    }

    public static  <T extends BaseConfig> T fromJsonObject(JSONObject obj, Class<T> cls) throws Exception {
        T config = cls.newInstance();
        config.configId = obj.getString("configId");
        config.configName = obj.getString("configName");
        config.createdAt = obj.getLong("createdAt");
        config.updatedAt = obj.getLong("updatedAt");

        JSONObject data = obj.getJSONObject("data");
        for (Field f : cls.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers())) continue;
            f.setAccessible(true);
            if (data.has(f.getName())) {
                Object val = data.get(f.getName());
                f.set(config, val);
            }
        }
        return config;
    }

    public static class ConfigItem implements Serializable {
        public String key;
        public String description;

        public ConfigItem(String key, String description) {
            this.key = key;
            this.description = description;
        }
    }
}
