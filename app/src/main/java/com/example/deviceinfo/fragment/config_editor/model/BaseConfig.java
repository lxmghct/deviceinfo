package com.example.deviceinfo.fragment.config_editor.model;

import java.io.Serializable;

public abstract class BaseConfig implements Serializable {
    public String configId;
    public String configName;
    public long createdAt;
    public long updatedAt;
}
