package com.example.deviceinfo.fragment.config_editor;


import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deviceinfo.R;
import com.example.deviceinfo.fragment.config_editor.model.BaseConfig;
import com.example.deviceinfo.fragment.config_editor.ui.ConfigSelectDialog;

import java.util.List;

public class ConfigEditorController {

    private final Context context;
    private BaseConfig current;
    private final ReflectAdapter adapter;
    private boolean fromFile = false;
    private final String defaultNameKey;

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
                .setOnClickListener(v -> saveOriginal());

        rootView.findViewById(R.id.btnSaveModified)
                .setOnClickListener(v -> saveModified());

        rootView.findViewById(R.id.btnImport)
                .setOnClickListener(v -> showImportDialog());
    }

    public void setTargetConfig(BaseConfig config) {
        adapter.updateTargetObject(config);
        fromFile = false;
    }

    public void setModifiedConfig(BaseConfig config) {
        adapter.updateModifiedValues(config);
    }

    public BaseConfig getCurrentConfig() {
        adapter.applyModifiedValues();
        return current;
    }
    
    private void saveOriginal() {
        try {
            current.configName = "原始值-" + System.currentTimeMillis();
            ConfigStorage.saveConfig(context, current, false);
            toast("原始值已保存");
        } catch (Exception e) {
            Log.e("MainActivity", "saveOriginal: ", e);
            toast("保存失败" + e.getMessage());
        }
    }

    private void saveModified() {
        adapter.applyModifiedValues();

        boolean overwrite = fromFile;
        try {
            current.configName = overwrite
                    ? current.configName
                    : "新配置-" + System.currentTimeMillis();

            ConfigStorage.saveConfig(context, current, overwrite);
            fromFile = true;
            toast(overwrite ? "配置已覆盖" : "新配置已保存");
        } catch (Exception e) {
            toast("保存失败");
        }
    }

    private void showImportDialog() {
        ConfigSelectDialog dialog =
                new ConfigSelectDialog(context, current.getClass(), config -> {
                    try {
                        Log.d("MainActivity", "showImportDialog: selected config=" + config);
                        current = ConfigStorage.loadConfig(config, current.getClass());
                        adapter.updateModifiedValues(current);
                        fromFile = true;
                    } catch (Exception e) {
                        Log.e("MainActivity", "showImportDialog: ", e);
                    }
                });
        dialog.show();
    }

    private void toast(String s) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }
}
