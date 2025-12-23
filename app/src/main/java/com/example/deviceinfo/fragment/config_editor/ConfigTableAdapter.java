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
import com.example.deviceinfo.fragment.config_editor.model.BaseConfig.ConfigItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigTableAdapter extends RecyclerView.Adapter<ConfigTableAdapter.ViewHolder> {

    private BaseConfig targetObject;
    private BaseConfig updatedObject;
    private final List<ConfigItem> keyDescriptions;
    private final Map<String, String> modifiedValues = new HashMap<>();

    // 保存 ViewHolder，便于批量 UI 更新
    private final Map<String, ViewHolder> holderMap = new HashMap<>();

    public ConfigTableAdapter(BaseConfig targetObject) {
        this.targetObject = targetObject;
        this.keyDescriptions = targetObject.getConfigItems();
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
        ConfigItem item = keyDescriptions.get(position);
        String key = item.key;

        // 第一列：键名和描述
        holder.tvKey.setText(key);
        holder.tvKeyDescription.setText(item.description);
        if (item.description != null && !item.description.isEmpty()) {
            holder.tvKeyDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvKeyDescription.setVisibility(View.GONE);
        }
        holderMap.put(key, holder);

        // 第二列：原始值
        Object value = targetObject.data.get(key);
        holder.tvOriginalValue.setText(value == null ? "" : String.valueOf(value));

        // 第三列：修改值
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
                    modifiedValues.put(key, newValueStr);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return keyDescriptions.size();
    }

    public BaseConfig generateTempNewConfig() {
        if (modifiedValues.isEmpty()) {
            return null;
        }
        BaseConfig obj = updatedObject.copy();
        for (ConfigItem item : keyDescriptions) {
            String key = item.key;
            String valueStr = modifiedValues.get(key);
            if (valueStr != null && !valueStr.isEmpty()) {
                try {
                    Object convertedValue = convertType(item.type, valueStr);
                    obj.data.put(key, convertedValue);
                } catch (Exception e) {
                    Log.e("ConfigTableAdapter", "generateTempNewConfig: ", e);
                }
            } else {
                obj.data.remove(key);
            }
        }
        return obj;
    }

    public BaseConfig getTargetObject() {
        return targetObject;
    }

    public BaseConfig getModifiedConfig() {
        return updatedObject;
    }

    public boolean isModifiedConfigFromFile() {
        return updatedObject != null;
    }

    /**
     * 更新目标对象（原始值列）
     */
    public void updateTargetObject(BaseConfig newTarget) {
        this.targetObject = newTarget;

        for (ConfigItem item : keyDescriptions) {
            String key = item.key;
            ViewHolder holder = holderMap.get(key);
            if (holder == null) continue;
            Object value = newTarget.data.get(key);
            holder.tvOriginalValue.setText(value == null ? "" : String.valueOf(value));
        }
    }

    public void updateModifiedValues(BaseConfig config) {
        modifiedValues.clear();
        updatedObject = config;

        for (ConfigItem item : keyDescriptions) {
            Object value = config.data.get(item.key);
            modifiedValues.put(item.key, String.valueOf(value));
            ViewHolder holder = holderMap.get(item.key);
            if (holder != null) {
                holder.etModifiedValue.setText(value == null ? "" : String.valueOf(value));
            }
        }
    }

    public void clearModifiedValues() {
        modifiedValues.clear();
        updatedObject = null;
        for (ConfigItem item : keyDescriptions) {
            ViewHolder holder = holderMap.get(item.key);
            if (holder != null) {
                holder.etModifiedValue.setText("");
            }
        }
    }

    private Object convertType(Class<?> type, String value) {
        if (type == int.class || type == Integer.class) {
            return Integer.parseInt(value);
        }
        if (type == long.class || type == Long.class) {
            return Long.parseLong(value);
        }
        if (type == float.class || type == Float.class) {
            return Float.parseFloat(value);
        }
        if (type == double.class || type == Double.class) {
            return Double.parseDouble(value);
        }
        if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(value);
        }
        return value;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvKey;
        TextView tvKeyDescription;
        TextView tvOriginalValue;
        EditText etModifiedValue;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvKey = itemView.findViewById(R.id.tvKey);
            tvKeyDescription = itemView.findViewById(R.id.tvKeyDescription);
            tvOriginalValue = itemView.findViewById(R.id.tvOriginalValue);
            etModifiedValue = itemView.findViewById(R.id.etModifiedValue);
        }
    }
}
