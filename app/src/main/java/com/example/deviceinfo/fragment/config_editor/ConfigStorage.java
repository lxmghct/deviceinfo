package com.example.deviceinfo.fragment.config_editor;

import android.content.Context;
import android.util.Log;

import com.example.deviceinfo.fragment.config_editor.model.BaseConfig;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ConfigStorage {

    private static final String ROOT_DIR = "configs";
    private static final String CURRENT_CONFIG_FILE = "current_config.json";

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

    public static List<JSONObject> loadConfigList(Context c, Class<?> cls) {
        List<JSONObject> list = new ArrayList<>();
        File dir = getConfigDir(c, cls);
        for (File f : dir.listFiles()) {
            JSONObject obj = readJSONObject(f);
            if (obj != null) {
                list.add(obj);
            }
        }
        return list;
    }

    public static BaseConfig loadConfig(Context c, Class<? extends BaseConfig> cls, String id) {
        File dir = getConfigDir(c, cls);
        File file = new File(dir, id + ".json");
        JSONObject obj = readJSONObject(file);
        if (obj != null) {
            try {
                return BaseConfig.fromJsonObject(obj, cls);
            } catch (Exception e) {
                Log.e("ConfigStorage", "loadConfig: ", e);
            }
        }
        return null;
    }

    public static boolean configNameExists(Context c, Class<?> cls, String configName) {
        try {
            List<JSONObject> list = ConfigStorage.loadConfigList(c, cls);
            for (JSONObject obj : list) {
                if (configName.equals(obj.getString("configName"))) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }

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

    public static void applyCurrentConfig(Context c, Class<?> cls, String configId) throws Exception {
        File rootDir = new File(c.getFilesDir(), ROOT_DIR);
        if (!rootDir.exists()) rootDir.mkdirs();
        File file = new File(rootDir, CURRENT_CONFIG_FILE);
        JSONObject root = readJSONObject(file);
        if (root == null) {
            root = new JSONObject();
        }

        String key = cls.getSimpleName();

        root.put(key, Objects.requireNonNullElse(configId, JSONObject.NULL));

        FileWriter fw = new FileWriter(file);
        fw.write(root.toString(2));
        fw.close();
    }

    public static <T extends BaseConfig> T getCurrentConfig(Context c, Class<T> cls) throws Exception {
        File file = new File(c.getFilesDir(), ROOT_DIR + "/" + CURRENT_CONFIG_FILE);
        JSONObject root = readJSONObject(file);
        if (root == null) {
            return null;
        }
        String key = cls.getSimpleName();
        if (!root.has(key) || root.isNull(key)) {
            return null;
        }

        String configId = root.getString(key);
        File configFile = new File(getConfigDir(c, cls), configId + ".json");
        JSONObject obj = readJSONObject(configFile);
        if (obj == null) {
            return null;
        }

        return BaseConfig.fromJsonObject(obj, cls);
    }



    private static void createConfigFile(Context c, Class<?> cls, String id, JSONObject obj) throws Exception {
        File file = new File(getConfigDir(c, cls), id + ".json");
        FileWriter fw = new FileWriter(file);
        fw.write(obj.toString(2));
        fw.close();
    }

    private static String generateId() {
        // 使用时间戳 + 随机数生成唯一ID
        int randomPart = (int) (Math.random() * 100000);
        return System.currentTimeMillis() + "-" + randomPart;
    }


    private static JSONObject readJSONObject(File f) {
        if (!f.exists()) {
            return null;
        }
        try {
            String json = new String(Files.readAllBytes(f.toPath()));
            return new JSONObject(json);
        } catch (Exception e) {
            Log.e("ConfigStorage", "readJSONObject: ", e);
            return null;
        }
    }
}