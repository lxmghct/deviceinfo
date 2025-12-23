package com.example.deviceinfo.pojo;

import android.net.wifi.WifiInfo;
import android.os.Build;

import com.example.deviceinfo.fragment.config_editor.model.BaseConfig;

import java.io.Serializable;
import java.util.List;

public class WifiData extends BaseConfig implements Serializable {

    /** 网络状态，来自 android.net.NetworkInfo.getType()
     * 1: WIFI, 0: MOBILE, -1: 无网络(getType返回的是 null)
     */
    public Integer networkType;

    /** Wi-Fi 名称（仅展示用，不参与强校验） */
    public String ssid;

    /** AP 的 MAC 地址（核心校验字段） */
    public String bssid;

    /** 加密类型，如 WPA2-PSK、WPA3、OPEN */
    public Integer securityType;

    /** 频率，单位 MHz（如 5220） */
    public Integer frequency;

    /** Wi-Fi 标准，如 11ac / 11ax / 11be */
    public Integer wifiStandard;

    /** 信号强度，例如-30、-90 */
    public Integer rssi;

    public static List<ConfigItem> keyDescriptions = List.of(
            new ConfigItem("networkType", "网络类型: 1: WIFI, 0: MOBILE, -1: 无网络"),
            new ConfigItem("ssid", "Wi-Fi 名称 (SSID)"),
            new ConfigItem("bssid", "AP 的 MAC 地址 (BSSID)"),
            new ConfigItem("securityType", "加密类型"),
            new ConfigItem("frequency", "频率 (MHz)"),
            new ConfigItem("wifiStandard", "Wi-Fi 标准"),
            new ConfigItem("rssi", "信号强度 (RSSI)")
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
        WifiData data = new WifiData();
        data.networkType = networkType;
        data.ssid = wifiInfo.getSSID();
        // wifi info 返回的 ssid 有时会带引号，需要去掉
        if (data.ssid != null && data.ssid.length() >= 2 &&
                data.ssid.startsWith("\"") && data.ssid.endsWith("\"")) {
            data.ssid = data.ssid.substring(1, data.ssid.length() - 1);
        }
        data.bssid = wifiInfo.getBSSID();
        data.frequency = wifiInfo.getFrequency();
        data.rssi = wifiInfo.getRssi();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // 安卓12 及以上
            data.securityType = wifiInfo.getCurrentSecurityType();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 安卓11 及以上
            data.wifiStandard = wifiInfo.getWifiStandard();
        }
        return data;
    }

}
