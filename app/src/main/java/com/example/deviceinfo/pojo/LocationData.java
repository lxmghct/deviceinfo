package com.example.deviceinfo.pojo;

import com.example.deviceinfo.fragment.config_editor.model.BaseConfig;

import java.io.Serializable;

public class LocationData extends BaseConfig implements Serializable {

    public String id;

    /** 纬度 */
    public Double latitude;

    /** 经度 */
    public Double longitude;

    public static java.util.List<String> keys = java.util.List.of(
            "latitude",
            "longitude"
    );

    public static final String KEY_OF_DEFAULT_NAME = "id";
}
