package com.example.deviceinfo.pojo;

import android.location.Location;
import android.os.Build;

import com.example.deviceinfo.fragment.config_editor.model.BaseConfig;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class LocationData extends BaseConfig implements Serializable {

    public static List<ConfigItem> keyDescriptions = List.of(
            new ConfigItem("latitude", Double.class, "纬度"),
            new ConfigItem("longitude", Double.class, "经度"),
            new ConfigItem("horizontalAccuracy", Float.class, "水平精度，误差范围(米)"),
            new ConfigItem("altitude", Double.class, "GPS原始高度，WGS84(米)"),
            new ConfigItem("verticalAccuracy", Float.class, "垂直方向的误差范围(米)"),
            new ConfigItem("mslAltitude", Double.class, "相对于平均海平面的高度(米)(Android 14+)"),
            new ConfigItem("mslAltitudeAccuracy", Float.class, "mslAltitude的误差范围(米)(Android 14+)"),
            new ConfigItem("speed", Float.class, "速度(米/秒)"),
            new ConfigItem("speedAccuracy", Float.class, "speed的误差范围(米/秒)"),
            new ConfigItem("bearing", Float.class, "方位角，正北方向顺时针计(度)"),
            new ConfigItem("bearingAccuracy", Float.class, "bearing的误差范围(度)"),
            new ConfigItem("isMock", Boolean.class, "是否为模拟位置"),
            new ConfigItem("elapsedRealtimeMillis", Long.class, "位置被测量时的系统运行时间(毫秒)(Android 13+)")
    );

    @Override
    public List<ConfigItem> getConfigItems() {
        return keyDescriptions;
    }

    @Override
    public String getKeyOfDefaultName() {
        return null;
    }

    public static LocationData fromLocation(Location location) {
        LocationData obj = new LocationData();
        Map<String, Object> map = obj.data;
        map.put("latitude", location.getLatitude());
        map.put("longitude", location.getLongitude());
        if (location.hasAccuracy()) {
            map.put("horizontalAccuracy", location.getAccuracy());
        }
        if (location.hasAltitude()) {
            map.put("altitude", location.getAltitude());
            if (location.hasVerticalAccuracy()) {
                map.put("verticalAccuracy", location.getVerticalAccuracyMeters());
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14+
            if (location.hasMslAltitude()) {
                obj.data.put("mslAltitude", location.getMslAltitudeMeters());
                if (location.hasMslAltitudeAccuracy()) {
                    obj.data.put("mslAltitudeAccuracy", location.getMslAltitudeAccuracyMeters());
                }
            }
        }
        if (location.hasSpeed()) {
            map.put("speed", location.getSpeed());
            if (location.hasSpeedAccuracy()) {
                map.put("speedAccuracy", location.getSpeedAccuracyMetersPerSecond());
            }
        }
        if (location.hasBearing()) {
            map.put("bearing", location.getBearing());
            if (location.hasBearingAccuracy()) {
                map.put("bearingAccuracy", location.getBearingAccuracyDegrees());
            }
        }
        map.put("isMock", location.isFromMockProvider());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            map.put("elapsedRealtimeMillis", location.getElapsedRealtimeMillis());
        }
        return obj;
    }
}
