package com.example.deviceinfo.fragment.config_editor;


import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deviceinfo.R;
import com.example.deviceinfo.fragment.config_editor.model.BaseConfig;
import com.example.deviceinfo.fragment.config_editor.ui.ConfigSelectDialog;

import java.util.List;
import java.util.Set;

public class ConfigEditorController {

    private final Context context;
    private BaseConfig current;
    private final ReflectAdapter adapter;
    private boolean fromFile = false;
    private final String defaultNameKey;

    private final Set<Character> illegalChars = Set.of('\\', '/', ':', '*', '?', '\"', '<', '>', '|', '\'');

    public ConfigEditorController(
            Context c,
            View rootView,
            BaseConfig config,
            List<String> keys,
            String defaultKey) {

        this.context = c;
        this.current = config;
        this.defaultNameKey = defaultKey;

        RecyclerView rv = rootView.findViewById(R.id.recyclerView);
        rv.setLayoutManager(new LinearLayoutManager(c));

        adapter = new ReflectAdapter(config, keys);
        rv.setAdapter(adapter);

        rootView.findViewById(R.id.btnSaveOriginal)
                .setOnClickListener(v -> showSaveOriginalDialog(current));

        rootView.findViewById(R.id.btnSaveModified)
                .setOnClickListener(v -> onSaveModifiedClicked());

        rootView.findViewById(R.id.btnImport)
                .setOnClickListener(v -> showImportDialog());

        rootView.findViewById(R.id.btnClear)
                .setOnClickListener(v -> clearModifiedValues());
    }

    public void setTargetConfig(BaseConfig config) {
        adapter.updateTargetObject(config);
        this.current = config;
    }

    private void showImportDialog() {
        ConfigSelectDialog dialog =
                new ConfigSelectDialog(context, current.getClass(), config -> {
                    try {
                        BaseConfig obj = ConfigStorage.loadConfig(config, current.getClass());
                        adapter.updateModifiedValues(obj);
                        fromFile = true;
                    } catch (Exception e) {
                        Log.e("MainActivity", "showImportDialog: ", e);
                    }
                });
        dialog.show();
    }

    private void showSaveOriginalDialog(BaseConfig obj) {

        EditText et = new EditText(context);
        et.setSingleLine(true);

        String defaultName = ConfigStorage.getDefaultConfigName(obj, defaultNameKey);
        et.setText(defaultName);

        new AlertDialog.Builder(context)
                .setTitle("保存配置")
                .setView(et)
                .setPositiveButton("保存", (d, w) -> {
                    String name = et.getText().toString().trim();
                    String err = checkFileName(name);
                    if (err != null) {
                        toast(err);
                        return;
                    }
                    if (ConfigStorage.configNameExists(context, obj.getClass(), name)) {
                        showOverwriteConfirm(() -> saveOriginalInternal(obj, name));
                    } else {
                        saveOriginalInternal(obj, name);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void saveOriginalInternal(BaseConfig obj, String name) {
        try {
            obj.configId = null; // 强制新建
            obj.configName = name;
            ConfigStorage.saveConfig(context, obj, false);
            if (obj == current) {
                fromFile = true;
            }
            toast("配置已保存");
        } catch (Exception e) {
            toast("保存失败");
            Log.e("ConfigEditorController", "saveOriginalInternal: ", e);
        }
    }

    private void showOverwriteConfirm(Runnable onConfirm) {
        new AlertDialog.Builder(context)
                .setTitle("文件已存在")
                .setMessage("是否覆盖已有配置？")
                .setPositiveButton("覆盖", (d, w) -> onConfirm.run())
                .setNegativeButton("取消", null)
                .show();
    }

    private void onSaveModifiedClicked() {
        BaseConfig obj = adapter.applyModifiedValues();
        if (obj == null) {
            toast("无法应用修改");
            return;
        }
        if (!fromFile) {
            showSaveOriginalDialog(obj);
            return;
        }
        // 从文件加载的 → 先确认是否覆盖
        new AlertDialog.Builder(context)
                .setTitle("保存修改")
                .setMessage("是否覆盖当前配置？")
                .setPositiveButton("覆盖", (d, w) -> {
                    try {
                        ConfigStorage.saveConfig(context, obj, true);
                        fromFile = true;
                        toast("配置已覆盖");
                    } catch (Exception e) {
                        toast("保存失败");
                    }
                })
                .setNegativeButton("另存为", (d, w) -> showSaveOriginalDialog(obj))
                .show();
    }

    private void clearModifiedValues() {
        adapter.clearModifiedValues();
        fromFile = false;
    }

    private void toast(String s) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }

    private String checkFileName(String name) {
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
}
