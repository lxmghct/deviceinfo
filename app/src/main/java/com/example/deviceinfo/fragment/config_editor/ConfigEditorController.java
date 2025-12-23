package com.example.deviceinfo.fragment.config_editor;


import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deviceinfo.R;
import com.example.deviceinfo.fragment.config_editor.model.BaseConfig;
import com.example.deviceinfo.fragment.config_editor.ui.ConfigSelectDialog;
import com.example.deviceinfo.util.UiUtils;

public class ConfigEditorController {

    private final Context context;
    private final ConfigTableAdapter adapter;
    private final View rootView;

    public ConfigEditorController(Context c, View rootView, BaseConfig config, Runnable refreshCallback) {

        this.context = c;
        this.rootView = rootView;

        RecyclerView rv = rootView.findViewById(R.id.recyclerView);
        rv.setLayoutManager(new LinearLayoutManager(c));

        adapter = new ConfigTableAdapter(config);
        rv.setAdapter(adapter);

        rootView.findViewById(R.id.btnSaveOriginal)
                .setOnClickListener(v -> showSaveOriginalDialog());

        rootView.findViewById(R.id.btnSaveModified)
                .setOnClickListener(v -> onSaveModifiedClicked());

        rootView.findViewById(R.id.btnImport)
                .setOnClickListener(v -> showImportDialog());

        rootView.findViewById(R.id.btnClear)
                .setOnClickListener(v -> clearModifiedValues());

        rootView.findViewById(R.id.btnRefresh)
                .setOnClickListener(v -> {
                    if (refreshCallback != null) {
                        refreshCallback.run();
                    }
                });

        rootView.findViewById(R.id.btnApplyChange)
                .setOnClickListener(v -> applyCurrentConfig());

        rootView.findViewById(R.id.btnGetCurrentConfig)
                .setOnClickListener(v -> getCurrentConfig());
    }

    public void setTargetConfig(BaseConfig config) {
        adapter.updateTargetObject(config);
    }

    private void showImportDialog() {
        Class<? extends BaseConfig> cls = adapter.getOriginalObject().getClass();
        ConfigSelectDialog dialog =
                new ConfigSelectDialog(context, cls, config -> {
                    try {
                        BaseConfig obj = BaseConfig.fromJsonObject(config, cls);
                        adapter.updateModifiedValues(obj);
                    } catch (Exception e) {
                        Log.e("MainActivity", "showImportDialog: ", e);
                    }
                }, this::refreshModifiedValues);
        dialog.show();
    }

    private void showSaveOriginalDialog() {
        showSaveConfigDialog(adapter.getOriginalObject(), null, null);
    }

    private void showSaveConfigDialog(BaseConfig obj, Runnable onSaved, Runnable onCanceled) {
        EditText et = new EditText(context);
        et.setSingleLine(true);
        et.setText(obj.getDefaultConfigName());

        new AlertDialog.Builder(context)
                .setTitle("保存配置")
                .setView(et)
                .setPositiveButton("保存", (d, w) -> {
                    String name = et.getText().toString().trim();
                    String err = ConfigStorage.checkFileName(name);
                    if (err != null) {
                        UiUtils.toast(context, err);
                        return;
                    }
                    if (ConfigStorage.configNameExists(context, obj.getClass(), name)) {
                        showOverwriteConfirm(() -> saveConfigWithCallback(obj, name, onSaved, onCanceled), onCanceled);
                    } else {
                        saveConfigWithCallback(obj, name, onSaved, onCanceled);
                    }
                })
                .setNegativeButton("取消", (d, w) -> {
                    if (onCanceled != null) {
                        onCanceled.run();
                    }
                })
                .show();
    }

    private void saveConfigWithCallback(BaseConfig obj, String name, Runnable onSaved, Runnable onCanceled) {
        if (saveConfigInternal(obj, name)) {
            if (onSaved != null) {
                onSaved.run();
            }
        } else {
            if (onCanceled != null) {
                onCanceled.run();
            }
        }
    }

    private boolean saveConfigInternal(BaseConfig obj, String name) {
        try {
            obj.configId = null; // 强制新建
            obj.configName = name;
            ConfigStorage.saveConfig(context, obj, false);
            UiUtils.toast(context, "配置已保存");
            return true;
        } catch (Exception e) {
            UiUtils.toast(context, "保存失败");
            Log.e("ConfigEditorController", "saveOriginalInternal: ", e);
            return false;
        }
    }

    private void showOverwriteConfirm(Runnable onConfirm, Runnable onCanceled) {
        new AlertDialog.Builder(context)
                .setTitle("文件已存在")
                .setMessage("是否覆盖已有配置？")
                .setPositiveButton("覆盖", (d, w) -> {
                    if (onConfirm != null) {
                        onConfirm.run();
                    }
                })
                .setNegativeButton("取消", (d, w) -> {
                    if (onCanceled != null) {
                        onCanceled.run();
                    }
                })
                .show();
    }

    private void saveModifiedConfigCallback(Runnable afterSavedCallback) {
        adapter.applyTempNewConfig();
        UiUtils.hideKeyboardAndClearFocus(context, rootView);
        if (afterSavedCallback != null) {
            afterSavedCallback.run();
        }
    }

    private void saveModified(Runnable afterSavedCallback, Runnable onCanceled) {
        BaseConfig obj = adapter.generateTempNewConfig();
        if (obj == null) {
            UiUtils.toast(context, "无法应用修改");
            return;
        }
        if (!adapter.isModifiedConfigFromFile()) {
            showSaveConfigDialog(obj, () -> saveModifiedConfigCallback(afterSavedCallback), onCanceled);
            return;
        }
        // 从文件加载的 → 先确认是否覆盖
        new AlertDialog.Builder(context)
                .setTitle("保存修改")
                .setMessage("是否覆盖当前配置？")
                .setPositiveButton("覆盖", (d, w) -> {
                    try {
                        ConfigStorage.saveConfig(context, obj, true);
                        saveModifiedConfigCallback(afterSavedCallback);
                        UiUtils.toast(context, "配置已覆盖");
                    } catch (Exception e) {
                        UiUtils.toast(context, "保存失败");
                    }
                })
                .setNegativeButton("另存为", (d, w) -> showSaveConfigDialog(obj, () -> saveModifiedConfigCallback(afterSavedCallback), onCanceled))
                .show();
    }

    private void onSaveModifiedClicked() {
        if (!adapter.isModified()) {
            UiUtils.toast(context, "暂无修改内容");
            return;
        }
        saveModified(null, null);
    }

    private void clearModifiedValues() {
        adapter.clearModifiedValues();
    }

    private void applyCurrentConfigCallback() {
        BaseConfig temp = adapter.getModifiedConfig();
        if (temp == null) {
            UiUtils.toast(context, "暂无可应用的配置");
            return;
        }
        try {
            ConfigStorage.applyCurrentConfig(context, temp.getClass(), temp.configId);
            UiUtils.toast(context, "应用成功");
        } catch (Exception e) {
            UiUtils.toast(context, "应用失败");
            Log.e("ConfigEditorController", "applyCurrentConfigCallback: ", e);
        }
    }

    private void applyCurrentConfig() {
        if (!adapter.isModified()) {
            applyCurrentConfigCallback();
            return;
        }
        UiUtils.enableToast(false);
        saveModified(() -> {
            UiUtils.enableToast(true);
            applyCurrentConfigCallback();
        }, () -> UiUtils.enableToast(true));
    }

    public void getCurrentConfig() {
        try {
            BaseConfig config = ConfigStorage.getCurrentConfig(context, adapter.getOriginalObject().getClass());
            adapter.updateModifiedValues(config);
        } catch (Exception e) {
            UiUtils.toast(context, "获取失败");
            Log.e("ConfigEditorController", "getCurrentConfig: ", e);
        }
    }

    private void refreshModifiedValues() {
        // 从配置选择对话框退出后，需要检查当前配置是否被修改
        BaseConfig temp = adapter.getModifiedConfig();
        if (temp == null) {
            return;
        }
        BaseConfig obj = ConfigStorage.loadConfig(context, temp.getClass(), temp.configId);
        if (obj == null) {
            adapter.resetUpdatedObject(null);
        } else {
            adapter.resetUpdatedObject(obj.configName);
        }
    }

}
