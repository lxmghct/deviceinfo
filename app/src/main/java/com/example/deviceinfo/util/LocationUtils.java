package com.example.deviceinfo.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;

import androidx.core.app.ActivityCompat;

import java.util.List;

public class LocationUtils {

    public static void buildLocationManager(LocationManager locationManager, Context context) {
//        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //监听GPS定位信息是否改变
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,//指定GPS定位的提供者
                    1000,//间隔时间
                    1,//位置更新之间的最小距离
                    location -> { //GPS信息发生改变时回调
                    }
            );
        }
    }

    public static String getLocationInfo(LocationManager locationManager, Context context) {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return "permissionCheck=DENIED -> 无定位权限\n";
        }

        StringBuilder sb = new StringBuilder();

        //第一种方式：获取所有的LocationProvider名称，通过LocationManager对象的getAllProviders()来实现
        List<String> providerNames = locationManager.getAllProviders();
        sb.append("getAllProviders() result=\n\t").append(providerNames).append("\n");
        //第二种方法：通过名称获得LocationProvider
//        LocationProvider locationProvider = locationManager.getProvider(LocationManager.PASSIVE_PROVIDER);
//        sb.append("getProvider(PASSIVE_PROVIDER) result=\n\t").append(locationProvider).append("\n");

        // 根据allProviders获取到的定位方式，遍历获取Location
        sb.append("各定位方式的LastKnownLocation结果=\n");
        for (String p : providerNames) {
            Location loc = locationManager.getLastKnownLocation(p);
            sb.append("Provider: ").append(p).append(" -> ").append(loc == null ? "[null]" : loc).append("\n");
        }

        return sb.toString();
    }
}
