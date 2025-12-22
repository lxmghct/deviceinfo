package com.example.deviceinfo.fragment.config_editor.ui;

import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deviceinfo.R;

import org.json.JSONObject;

import java.util.Date;
import java.util.List;

public class ConfigListAdapter
        extends RecyclerView.Adapter<ConfigListAdapter.VH> {

    private final List<JSONObject> list;
    private final ConfigSelectDialog.OnConfigSelected listener;
    private final Dialog dialog;

    public ConfigListAdapter(List<JSONObject> list,
                             ConfigSelectDialog.OnConfigSelected l,
                             Dialog d) {
        this.list = list;
        this.listener = l;
        this.dialog = d;
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        JSONObject obj = list.get(pos);
        h.itemView.setOnClickListener(v -> {
            listener.onSelected(obj);
            dialog.dismiss();
        });

        try {
            h.tvName.setText(obj.getString("configName"));
            String timeString = "创建时间：" + new Date(obj.getLong("createdAt"));
            h.tvTime.setText(timeString);
        } catch (Exception ignored) {
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        View view = LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_config_select, p, false);
        return new VH(view);
    }

    public static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvTime;

        VH(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvConfigName);
            tvTime = v.findViewById(R.id.tvCreatedAt);
        }
    }
}
