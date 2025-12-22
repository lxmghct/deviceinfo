package com.example.deviceinfo.pojo;

import android.net.wifi.WifiInfo;
import android.os.Build;

import com.example.deviceinfo.fragment.config_editor.model.BaseConfig;

import java.io.Serializable;
import java.util.List;

public class WifiData extends BaseConfig implements Serializable {

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

    public static List<String> keys = List.of(
            "ssid",
            "bssid",
            "securityType",
            "frequency",
            "wifiStandard",
            "rssi"
    );

    public static final String KEY_OF_DEFAULT_NAME = "ssid";

    public static WifiData fromWifiInfo(WifiInfo wifiInfo) {
        WifiData data = new WifiData();
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
