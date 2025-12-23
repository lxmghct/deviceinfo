package com.example.deviceinfo.fragment.config_editor;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deviceinfo.R;
import com.example.deviceinfo.fragment.config_editor.model.BaseConfig;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReflectAdapter extends RecyclerView.Adapter<ReflectAdapter.ViewHolder> {

    private BaseConfig targetObject;
    private final List<String> keys;
    private final Map<String, Object> modifiedValues = new HashMap<>();

    // 保存 ViewHolder，便于批量 UI 更新
    private final Map<String, ViewHolder> holderMap = new HashMap<>();

    public ReflectAdapter(BaseConfig targetObject, List<String> keys) {
        this.targetObject = targetObject;
        this.keys = keys;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String key = keys.get(position);
        holder.tvKey.setText(key);
        holderMap.put(key, holder);

        try {
            Field field = targetObject.getClass().getDeclaredField(key);
            field.setAccessible(true);
            Object value = field.get(targetObject);

            holder.tvOriginalValue.setText(value == null ? "" : String.valueOf(value));

            Object modified = modifiedValues.get(key);
            holder.etModifiedValue.setText(modified == null ? "" : String.valueOf(modified));

            holder.etModifiedValue.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {
                    String newValueStr = s.toString();
                    if (newValueStr.isEmpty()) {
                        modifiedValues.remove(key);
                    } else {
                        Object convertedValue = convertType(field.getType(), newValueStr);
                        modifiedValues.put(key, convertedValue);
                    }
                }
            });
        } catch (Exception e) {
            holder.tvOriginalValue.setText("N/A");
        }

        // 修改值输入框暂不处理事件
        holder.etModifiedValue.setText("");
    }

    @Override
    public int getItemCount() {
        return keys == null ? 0 : keys.size();
    }

    public BaseConfig applyModifiedValues() {
        if (modifiedValues.isEmpty()) {
            return null;
        }
        BaseConfig obj;
        try {
            obj = targetObject.getClass().newInstance();
        } catch (Exception e) {
            Log.e("ReflectAdapter", "无法创建配置对象", e);
            return null;
        }
        for (Map.Entry<String, Object> e : modifiedValues.entrySet()) {
            try {
                Field field = obj.getClass().getDeclaredField(e.getKey());
                field.setAccessible(true);
                field.set(obj, e.getValue());
            } catch (Exception ignored) {
            }
        }
        obj.configId = targetObject.configId;
        obj.configName = targetObject.configName;
        obj.createdAt = targetObject.createdAt;
        return obj;
    }

    /**
     * 更新目标对象（原始值列）
     */
    public void updateTargetObject(BaseConfig newTarget) {
        this.targetObject = newTarget;

        for (String key : keys) {
            ViewHolder holder = holderMap.get(key);
            if (holder == null) continue;

            try {
                Field field = newTarget.getClass().getDeclaredField(key);
                field.setAccessible(true);

                Object value = field.get(newTarget);
                holder.tvOriginalValue.setText(value == null ? "" : String.valueOf(value));

            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 更新修改值（不影响原始值）
     */
    public void updateModifiedValues(BaseConfig config) {
        modifiedValues.clear();

        for (String key : keys) {
            try {
                Field field =
                        config.getClass().getDeclaredField(key);
                field.setAccessible(true);

                Object value = field.get(config);
                modifiedValues.put(key, value);

                ViewHolder holder = holderMap.get(key);
                if (holder != null) {
                    holder.etModifiedValue.setText(value == null ? "" : String.valueOf(value));
                }
            } catch (Exception ignored) {}
        }
    }

    public void clearModifiedValues() {
        modifiedValues.clear();
        for (String key : keys) {
            ViewHolder holder = holderMap.get(key);
            if (holder != null) {
                holder.etModifiedValue.setText("");
            }
        }
    }

    private Object convertType(Class<?> type, String value) {
        if (type == int.class) return Integer.parseInt(value);
        if (type == long.class) return Long.parseLong(value);
        if (type == boolean.class) return Boolean.parseBoolean(value);
        return value;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvKey;
        TextView tvOriginalValue;
        EditText etModifiedValue;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvKey = itemView.findViewById(R.id.tvKey);
            tvOriginalValue = itemView.findViewById(R.id.tvOriginalValue);
            etModifiedValue = itemView.findViewById(R.id.etModifiedValue);
        }
    }
}
