package com.example.deviceinfo.fragment.config_editor;

import android.content.Context;

import com.example.deviceinfo.fragment.config_editor.model.BaseConfig;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ConfigStorage {

    private static final String ROOT_DIR = "configs";


    private static final Set<Character> illegalChars = Set.of('\\', '/', ':', '*', '?', '\"', '<', '>', '|', '\'');

    public static File getConfigDir(Context c, Class<?> cls) {
        File dir = new File(c.getFilesDir(), ROOT_DIR + "/" + cls.getSimpleName());
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public static void saveConfig(Context c, BaseConfig obj, boolean overwrite) throws Exception {
        if (obj.configName == null || obj.configName.isEmpty()) {
            throw new IllegalArgumentException("Config name cannot be empty");
        }
        if (!overwrite || obj.configId == null) {
            obj.configId = generateId();
            obj.createdAt = System.currentTimeMillis();
        }
        obj.updatedAt = System.currentTimeMillis();

        JSONObject root = obj.toJsonObject();
        createConfigFile(c, obj.getClass(), obj.configId, root);
    }

    public static void saveConfig(Context c, Class<?> cls, JSONObject obj, boolean overwrite) throws Exception {
        String configId = obj.getString("configId");
        String configName = obj.getString("configName");
        long now = System.currentTimeMillis();

        if (configName.isEmpty()) {
            throw new IllegalArgumentException("Config name cannot be empty");
        }
        if (!overwrite || configId.isEmpty()) {
            configId = generateId();
            obj.put("configId", configId);
            obj.put("createdAt", now);
        }
        obj.put("updatedAt", System.currentTimeMillis());
        createConfigFile(c, cls, configId, obj);
    }

    private static void createConfigFile(Context c, Class<?> cls, String id, JSONObject obj) throws Exception {
        File file = new File(getConfigDir(c, cls), id + ".json");
        FileWriter fw = new FileWriter(file);
        fw.write(obj.toString(2));
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

    public static String getDefaultConfigName(BaseConfig obj, String key) {
        try {
            Field f = obj.getClass().getDeclaredField(key);
            f.setAccessible(true);
            Object v = f.get(obj);
            return v == null ? "config" : String.valueOf(v);
        } catch (Exception e) {
            return "config";
        }
    }

    public static boolean configNameExists(Context c, Class<?> cls, String configName) {

        try {
            List<JSONObject> list = ConfigStorage.loadConfigList(c, cls);
            for (JSONObject obj : list) {
                if (configName.equals(obj.getString("configName"))) {
                    return true;
                }
            }
        } catch (Exception ignored) {}

        return false;
    }

    public static void deleteConfig(Context c, Class<?> cls, String id) {
        File dir = getConfigDir(c, cls);
        File file = new File(dir, id + ".json");
        if (file.exists()) {
            file.delete();
        }
    }

    public static String checkFileName(String name) {
        if (name == null || name.isEmpty()) {
            return "配置名称不能为空";
        }
        for (char c : name.toCharArray()) {
            if (illegalChars.contains(c)) {
                return "配置名称包含非法字符";
            }
        }
        if (name.length() > 100) {
            return "配置名称过长";
        }
        return null;
    }

    private static String generateId() {
        // 使用时间戳 + 随机数生成唯一ID
        int randomPart = (int)(Math.random() * 100000);
        return System.currentTimeMillis() + "-" + randomPart;
    }
}