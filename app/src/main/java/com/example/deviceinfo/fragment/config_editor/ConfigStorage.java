package com.example.deviceinfo.fragment.config_editor;

import android.content.Context;

import com.example.deviceinfo.fragment.config_editor.model.BaseConfig;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class ConfigStorage {

    private static final String ROOT_DIR = "configs";

    public static File getConfigDir(Context c, Class<?> cls) {
        File dir = new File(c.getFilesDir(), ROOT_DIR + "/" + cls.getSimpleName());
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public static void saveConfig(Context c, BaseConfig obj, boolean overwrite) throws Exception {
        if (!overwrite || obj.configId == null) {
            obj.configId = UUID.randomUUID().toString();
            obj.createdAt = System.currentTimeMillis();
        }
        obj.updatedAt = System.currentTimeMillis();

        JSONObject root = new JSONObject();
        root.put("configId", obj.configId);
        root.put("configName", obj.configName);
        root.put("createdAt", obj.createdAt);
        root.put("updatedAt", obj.updatedAt);

        JSONObject data = new JSONObject();
        for (Field f : obj.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers())) continue;
            f.setAccessible(true);
            data.put(f.getName(), f.get(obj));
        }
        root.put("data", data);

        File file = new File(getConfigDir(c, obj.getClass()), obj.configId + ".json");
        FileWriter fw = new FileWriter(file);
        fw.write(root.toString(2));
        fw.close();
    }

    public static List<JSONObject> loadConfigList(Context c, Class<?> cls) throws Exception {
        List<JSONObject> list = new ArrayList<>();
        File dir = getConfigDir(c, cls);
        for (File f : dir.listFiles()) {
            String json = new String(Files.readAllBytes(f.toPath()));
            list.add(new JSONObject(json));
        }
        return list;
    }

    public static <T extends BaseConfig> T loadConfig(
            JSONObject json, Class<T> cls) throws Exception {

        T obj = cls.newInstance();
        obj.configId = json.getString("configId");
        obj.configName = json.getString("configName");
        obj.createdAt = json.getLong("createdAt");
        obj.updatedAt = json.getLong("updatedAt");

        JSONObject data = json.getJSONObject("data");
        for (Iterator<String> it = data.keys(); it.hasNext(); ) {
            String key = it.next();
            Field f = cls.getDeclaredField(key);
            f.setAccessible(true);
            f.set(obj, data.get(key));
        }
        return obj;
    }
}