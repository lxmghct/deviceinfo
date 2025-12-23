package com.example.deviceinfo.fragment.config_editor.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deviceinfo.R;
import com.example.deviceinfo.fragment.config_editor.ConfigStorage;
import com.example.deviceinfo.util.UiUtils;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConfigListAdapter
        extends RecyclerView.Adapter<ConfigListAdapter.ViewHolder> {

    private final Context context;
    private final List<JSONObject> list;
    private final ConfigSelectDialog.OnConfigSelected listener;
    private final Dialog dialog;
    private final Class<?> cls;

    private int editingPosition = -1;

    public ConfigListAdapter(List<JSONObject> list, Class<?> cls,
                             ConfigSelectDialog.OnConfigSelected l, Dialog d) {
        this.list = list;
        this.context = d.getContext();
        this.cls = cls;
        this.listener = l;
        this.dialog = d;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int _pos) {
        int pos = h.getAdapterPosition();
        JSONObject obj = list.get(pos);
        boolean editing = pos == editingPosition;

        try {
            String name = obj.getString("configName");
            h.tvName.setText(name);
            h.etName.setText(name);


            h.tvName.setVisibility(editing ? View.GONE : View.VISIBLE);
            h.etName.setVisibility(editing ? View.VISIBLE : View.GONE);

            h.btnEdit.setVisibility(editing ? View.GONE : View.VISIBLE);
            h.btnDelete.setVisibility(editing ? View.GONE : View.VISIBLE);

            h.btnConfirm.setVisibility(editing ? View.VISIBLE : View.GONE);
            h.btnCancel.setVisibility(editing ? View.VISIBLE : View.GONE);

            // 格式: 创建时间：YYYY-MM-DD HH:MM:SS
            Date date = new Date(obj.getLong("createdAt"));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String timeString = "创建时间：" + sdf.format(date);
            h.tvTime.setText(timeString);

            // 点击整行：选中配置
            h.textLayout.setOnClickListener(v -> {
                if (!editing) {
                    listener.onSelected(obj);
                    dialog.dismiss();
                }
            });

            h.btnDelete.setOnClickListener(v ->
                    confirmDelete(obj, pos));

            h.btnEdit.setOnClickListener(v -> {
                editingPosition = pos;
                notifyItemChanged(pos);
            });

            h.btnCancel.setOnClickListener(v -> {
                editingPosition = -1;
                notifyItemChanged(pos);
            });

            h.btnConfirm.setOnClickListener(v ->
                    confirmRename(obj, h.etName.getText().toString().trim(), pos)
            );
        } catch (Exception ignored) {
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int v) {
        View view = LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_config_select, p, false);
        return new ViewHolder(view);
    }

    private void confirmDelete(JSONObject obj, int pos) {
        String configId;
        try {
            configId = obj.getString("configId");
        } catch (Exception e) {
            return;
        }
        new AlertDialog.Builder(context)
                .setTitle("删除配置")
                .setMessage("确定要删除该配置吗？")
                .setPositiveButton("删除", (d, w) -> {
                    ConfigStorage.deleteConfig(context, cls, configId);
                    list.remove(pos);
                    notifyItemRemoved(pos);
                    UiUtils.toast(context, "删除成功");
                })
                .setNegativeButton("取消", null)
                .show();
    }


    private void confirmRename(JSONObject obj, String newName, int pos) {
        if (newName.isEmpty()) {
            UiUtils.toast(context, "名称不能为空");
            return;
        }

        if (ConfigStorage.configNameExists(context, cls, newName)) {
            UiUtils.toast(context, "已存在同名配置");
            return;
        }

        try {
            obj.put("configName", newName);
            obj.put("updatedAt", System.currentTimeMillis());

            ConfigStorage.saveConfig(context, cls, obj, true);

            editingPosition = -1;
            notifyItemChanged(pos);
            UiUtils.toast(context, "重命名成功");
        } catch (Exception ignored) {}
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTime;
        EditText etName;
        LinearLayout textLayout;
        ImageButton btnEdit, btnDelete, btnConfirm, btnCancel;

        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvConfigName);
            tvTime = v.findViewById(R.id.tvCreatedAt);
            etName = v.findViewById(R.id.etConfigName);
            textLayout = v.findViewById(R.id.layoutText);
            btnEdit = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelete);
            btnConfirm = v.findViewById(R.id.btnConfirm);
            btnCancel = v.findViewById(R.id.btnCancel);
        }
    }
}
