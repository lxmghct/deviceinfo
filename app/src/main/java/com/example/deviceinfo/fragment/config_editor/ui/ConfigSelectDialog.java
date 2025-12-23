package com.example.deviceinfo.fragment.config_editor.ui;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deviceinfo.R;
import com.example.deviceinfo.fragment.config_editor.ConfigStorage;

import org.json.JSONObject;

import java.util.List;

public class ConfigSelectDialog extends Dialog {

    public interface OnConfigSelected {
        void onSelected(JSONObject config);
    }

    public ConfigSelectDialog(@NonNull Context ctx, Class<?> cls, OnConfigSelected listener) {

        super(ctx);
        setContentView(R.layout.dialog_config_select);

        RecyclerView rv = findViewById(R.id.rvConfigs);
        rv.setLayoutManager(new LinearLayoutManager(ctx));

        try {
            List<JSONObject> list = ConfigStorage.loadConfigList(ctx, cls);
            Log.d("ConfigSelectDialog",
                    "Loaded " + list.size() + " configs of type " + cls.getSimpleName());
            rv.setAdapter(new ConfigListAdapter(list, cls, listener, this));
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        setDialogWidth();
    }

    private void setDialogWidth() {
        Window window = getWindow();
        if (window == null) return;

        int screenWidth = getContext()
                .getResources()
                .getDisplayMetrics()
                .widthPixels;

        int marginDp = 32;
        int marginPx = (int) (marginDp *
                getContext().getResources().getDisplayMetrics().density);

        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = screenWidth - marginPx;
        window.setAttributes(lp);
    }
}
