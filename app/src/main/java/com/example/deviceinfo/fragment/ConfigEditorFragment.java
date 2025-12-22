package com.example.deviceinfo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.deviceinfo.R;

import com.example.deviceinfo.fragment.config_editor.ConfigEditorController;
import com.example.deviceinfo.fragment.config_editor.model.BaseConfig;

import java.util.ArrayList;
import java.util.List;

public class ConfigEditorFragment extends Fragment {

    private static final String ARG_CONFIG = "config";
    private static final String ARG_KEYS = "keys";
    private static final String ARG_DEFAULT_KEY = "default_key";

    private ConfigEditorController controller;

    public static ConfigEditorFragment newInstance(
            BaseConfig config,
            List<String> keys,
            String defaultNameKey) {

        Bundle b = new Bundle();
        b.putSerializable(ARG_CONFIG, config);
        b.putStringArrayList(ARG_KEYS, new ArrayList<>(keys));
        b.putString(ARG_DEFAULT_KEY, defaultNameKey);

        ConfigEditorFragment f = new ConfigEditorFragment();
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater i, ViewGroup c, Bundle b) {

        View v = i.inflate(R.layout.activity_config_editor, c, false);

        Bundle args = getArguments();
        if (args == null) {
            throw new IllegalStateException("Arguments cannot be null");
        }
        controller = new ConfigEditorController(
                requireContext(),
                v,
                (BaseConfig) args.getSerializable(ARG_CONFIG),
                args.getStringArrayList(ARG_KEYS),
                args.getString(ARG_DEFAULT_KEY)
        );

        return v;
    }

    public void setTargetConfig(BaseConfig config) {
        if (controller != null) {
            controller.setTargetConfig(config);
        }
    }
}
