package com.example.deviceinfo.pojo;

import com.example.deviceinfo.fragment.config_editor.model.BaseConfig;

import java.io.Serializable;
import java.util.List;

public class LocationData extends BaseConfig implements Serializable {

    public String id;

    /** 纬度 */
    public Double latitude;

    /** 经度 */
    public Double longitude;

    public static List<ConfigItem> keyDescriptions = List.of(
            new ConfigItem("latitude", "纬度"),
            new ConfigItem("longitude", "经度")
    );

    public static final String KEY_OF_DEFAULT_NAME = "id";

    @Override
    public List<ConfigItem> getConfigItems() {
        return keyDescriptions;
    }

    @Override
    public String getKeyOfDefaultName() {
        return KEY_OF_DEFAULT_NAME;
    }
}
