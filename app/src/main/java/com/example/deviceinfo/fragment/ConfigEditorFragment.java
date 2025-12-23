package com.example.deviceinfo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.deviceinfo.R;

import com.example.deviceinfo.fragment.config_editor.ConfigEditorController;
import com.example.deviceinfo.fragment.config_editor.model.BaseConfig;
import com.example.deviceinfo.util.UiUtils;


public class ConfigEditorFragment extends Fragment {

    private static final String ARG_CONFIG = "config";

    private ConfigEditorController controller;

    private static Runnable refreshCallback = null;

    public static ConfigEditorFragment newInstance(BaseConfig config, Runnable refreshCallback) {
        Bundle b = new Bundle();
        b.putSerializable(ARG_CONFIG, config);

        ConfigEditorFragment f = new ConfigEditorFragment();
        f.setArguments(b);

        ConfigEditorFragment.refreshCallback = refreshCallback;
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater i, ViewGroup c, Bundle b) {

        View v = i.inflate(R.layout.fragment_config_editor, c, false);

        // 添加全局点击事件：点击后关闭键盘并移除焦点
        v.setOnClickListener(view -> {
            UiUtils.hideKeyboardAndClearFocus(requireContext(), view);
        });

        Bundle args = getArguments();
        if (args == null) {
            throw new IllegalStateException("Arguments cannot be null");
        }
        BaseConfig config = (BaseConfig) args.getSerializable(ARG_CONFIG);
        if (config == null) {
            throw new IllegalStateException("Config cannot be null");
        }
        controller = new ConfigEditorController(requireContext(), v, config, refreshCallback);

        return v;
    }

    public void setTargetConfig(BaseConfig config) {
        if (controller != null) {
            controller.setTargetConfig(config);
        }
    }

    public void getCurrentConfig() {
        if (controller != null) {
            controller.getCurrentConfig();
        }
    }
}
