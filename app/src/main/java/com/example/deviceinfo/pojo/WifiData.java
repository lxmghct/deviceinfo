package com.example.deviceinfo.pojo;

import android.net.wifi.WifiInfo;
import android.os.Build;

import com.example.deviceinfo.fragment.config_editor.model.BaseConfig;

import java.io.Serializable;
import java.util.List;

public class WifiData extends BaseConfig implements Serializable {

    public static List<ConfigItem> keyDescriptions = List.of(
            new ConfigItem("networkType", Integer.class, "网络类型: 1: WIFI, 0: MOBILE, -1: 无网络"),
            new ConfigItem("ssid", String.class, "Wi-Fi 名称 (SSID)"),
            new ConfigItem("bssid", String.class, "AP 的 MAC 地址 (BSSID)"),
            new ConfigItem("securityType", Integer.class, "加密类型(安卓12及以上)"),
            new ConfigItem("frequency", Integer.class, "频率 (MHz)"),
            new ConfigItem("wifiStandard", Integer.class, "Wi-Fi 标准(安卓11及以上)"),
            new ConfigItem("rssi", Integer.class, "信号强度 (RSSI)")
    );

    public static final String KEY_OF_DEFAULT_NAME = "ssid";

    @Override
    public List<ConfigItem> getConfigItems() {
        return keyDescriptions;
    }

    @Override
    public String getKeyOfDefaultName() {
        return KEY_OF_DEFAULT_NAME;
    }

    public static WifiData fromWifiInfo(WifiInfo wifiInfo, int networkType) {
        WifiData wifiData = new WifiData();
        wifiData.data.put("networkType", networkType);
        String ssid = wifiInfo.getSSID();
        // wifi info 返回的 ssid 有时会带引号，需要去掉
        if (ssid != null && ssid.length() >= 2 && ssid.startsWith("\"") && ssid.endsWith("\"")) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        wifiData.data.put("ssid", ssid);
        wifiData.data.put("bssid", wifiInfo.getBSSID());
        wifiData.data.put("frequency", wifiInfo.getFrequency());
        wifiData.data.put("rssi", wifiInfo.getRssi());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // 安卓12 及以上
            wifiData.data.put("securityType", wifiInfo.getCurrentSecurityType());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 安卓11 及以上
            wifiData.data.put("wifiStandard", wifiInfo.getWifiStandard());
        }
        return wifiData;
    }

}
