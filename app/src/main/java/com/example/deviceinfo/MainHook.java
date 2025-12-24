package com.example.deviceinfo;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.example.deviceinfo.content_provider.ProviderConstants;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XposedBridge;

public class MainHook implements IXposedHookLoadPackage {

    private static final String PACKAGE_NAME = "com.example.deviceinfo";

    private Bundle getDataFromProvider(Context context, String className) {
        Uri uri = Uri.parse("content://" + PACKAGE_NAME + ".configprovider");
        Bundle extras = new Bundle();
        extras.putString(ProviderConstants.EXTRA_CLASS_NAME, className);
        try {
            return context.getContentResolver()
                    .call(uri, ProviderConstants.METHOD_GET_CURRENT_CONFIG, null, extras);
        } catch (Throwable t) {
            XposedBridge.log("Error calling content provider: " + t.getMessage());
            return null;
        }
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
        if (lpparam.packageName.equals(PACKAGE_NAME)) {
            return;
        }

        XposedHelpers.findAndHookMethod(
                Application.class,
                "attach",
                Context.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        Context context = (Context) param.args[0];
                        Context providerContext;
                        try {
                            providerContext = context.createPackageContext(
                                    "com.example.deviceinfo",
                                    Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE
                            );
                        } catch (Exception e) {
                            XposedBridge.log("Failed to create provider context: " + e.getMessage());
                            return;
                        }
                        XposedBridge.log("Context acquired: " + providerContext);

                        hookNetwork(lpparam, providerContext);
                        hookLocation(lpparam, providerContext);
                    }
                }
        );
    }

    private void hookNetwork(XC_LoadPackage.LoadPackageParam lpparam, Context context) {
        Bundle bundle = getDataFromProvider(context, "WifiData");
        if (bundle == null) {
            return;
        }
        // 1. 网络类型
        int networkType = bundle.getInt("networkType", -1);
        XposedHelpers.findAndHookMethod("android.net.NetworkInfo", lpparam.classLoader,
                "getType", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if (networkType == -1) {
                            param.setResult(null);
                        } else if (networkType == ConnectivityManager.TYPE_MOBILE || networkType == ConnectivityManager.TYPE_WIFI) {
                            param.setResult(networkType);
                        }
                    }
                });
        // TODO: NetworkInfo.getTypeName, NetworkInfo.getSubType
        if (networkType == -1) {
            XposedHelpers.findAndHookMethod("android.net.ConnectivityManager", lpparam.classLoader,
                    "getNetworkCapabilities", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            param.setResult(null);
                        }
                    });
            return;
        }
        XposedHelpers.findAndHookMethod(
                "android.net.NetworkCapabilities", lpparam.classLoader,
                "hasTransport", int.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        int transportType = (int) param.args[0];
                        if (networkType == ConnectivityManager.TYPE_WIFI && transportType == NetworkCapabilities.TRANSPORT_WIFI) {
                            param.setResult(true);
                        } else if (networkType == ConnectivityManager.TYPE_MOBILE && transportType == NetworkCapabilities.TRANSPORT_CELLULAR) {
                            param.setResult(true);
                        } else {
                            param.setResult(false);
                        }
                    }
                });
        if (networkType != ConnectivityManager.TYPE_WIFI) {
            return;
        }

        XposedHelpers.findAndHookMethod("android.net.wifi.WifiInfo", lpparam.classLoader,
                "getSSID", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        String ssid = bundle.getString("ssid", null);
                        if (ssid != null) {
                            param.setResult("\"" + ssid + "\"");
                        }
                    }
                });

        hookMethodOfString(bundle, lpparam, "android.net.wifi.WifiInfo", "getBSSID", "bssid");

        hookMethodOfInt(bundle, lpparam, "android.net.wifi.WifiInfo", "getFrequency", "frequency");

        hookMethodOfInt(bundle, lpparam, "android.net.wifi.WifiInfo", "getRssi", "rssi");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hookMethodOfInt(bundle, lpparam, "android.net.wifi.WifiInfo", "getCurrentSecurityType", "securityType");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            hookMethodOfInt(bundle, lpparam, "android.net.wifi.WifiInfo", "getWifiStandard", "wifiStandard");
        }

        // TODO: Hook ScanResult

    }

    private void hookLocation(XC_LoadPackage.LoadPackageParam lpparam, Context context) {
        Bundle bundle = getDataFromProvider(context, "LocationData");
        if (bundle == null) {
            return;
        }

        hookMethodOfDouble(bundle, lpparam, "android.location.Location", "getLatitude", "latitude");
        hookMethodOfDouble(bundle, lpparam, "android.location.Location", "getLongitude", "longitude");
        hookMethodOfFloat(bundle, lpparam, "android.location.Location", "getAccuracy", "horizontalAccuracy");
        hookMethodOfDouble(bundle, lpparam, "android.location.Location", "getAltitude", "altitude");
        hookMethodOfFloat(bundle, lpparam, "android.location.Location", "getVerticalAccuracyMeters", "verticalAccuracy");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            hookMethodOfDouble(bundle, lpparam, "android.location.Location", "getMslAltitudeMeters", "mslAltitude");
            hookMethodOfFloat(bundle, lpparam, "android.location.Location", "getMslAltitudeAccuracyMeters", "mslAltitudeAccuracy");
        }
        hookMethodOfFloat(bundle, lpparam, "android.location.Location", "getSpeed", "speed");
        hookMethodOfFloat(bundle, lpparam, "android.location.Location", "getSpeedAccuracyMetersPerSecond", "speedAccuracy");
        hookMethodOfFloat(bundle, lpparam, "android.location.Location", "getBearing", "bearing");
        hookMethodOfFloat(bundle, lpparam, "android.location.Location", "getBearingAccuracyDegrees", "bearingAccuracy");

        XposedHelpers.findAndHookMethod("android.location.Location", lpparam.classLoader,
                "isFromMockProvider", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        if (bundle.containsKey("isMock")) {
                            boolean isMock = bundle.getBoolean("isMock", false);
                            param.setResult(isMock);
                        }
                    }
                });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            XposedHelpers.findAndHookMethod("android.location.Location", lpparam.classLoader,
                    "getElapsedRealtimeMillis", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            if (bundle.containsKey("elapsedRealtimeMillis")) {
                                long elapsedRealtimeMillis = bundle.getLong("elapsedRealtimeMillis", Long.MIN_VALUE);
                                if (elapsedRealtimeMillis != Long.MIN_VALUE) {
                                    param.setResult(elapsedRealtimeMillis);
                                }
                            }
                        }
                    });
        }
    }


    private void hookMethodOfString(Bundle bundle, XC_LoadPackage.LoadPackageParam lpparam, String className, String methodName, String key) {
        XposedHelpers.findAndHookMethod(className, lpparam.classLoader,
                methodName, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        String val = bundle.getString(key, null);
                        if (val != null) {
                            param.setResult(val);
                        }
                    }
                });
    }


    private void hookMethodOfInt(Bundle bundle, XC_LoadPackage.LoadPackageParam lpparam, String className, String methodName, String key) {
        XposedHelpers.findAndHookMethod(className, lpparam.classLoader,
                methodName, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        int val = bundle.getInt(key, Integer.MIN_VALUE);
                        if (val != Integer.MIN_VALUE) {
                            param.setResult(val);
                        }
                    }
                });
    }

    private void hookMethodOfFloat(Bundle bundle, XC_LoadPackage.LoadPackageParam lpparam, String className, String methodName, String key) {
        XposedHelpers.findAndHookMethod(className, lpparam.classLoader,
                methodName, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        float val = bundle.getFloat(key, Float.NaN);
                        if (!Float.isNaN(val)) {
                            param.setResult(val);
                        }
                    }
                });
    }

    private void hookMethodOfDouble(Bundle bundle, XC_LoadPackage.LoadPackageParam lpparam, String className, String methodName, String key) {
        XposedHelpers.findAndHookMethod(className, lpparam.classLoader,
                methodName, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        double val = bundle.getDouble(key, Double.NaN);
                        if (!Double.isNaN(val)) {
                            param.setResult(val);
                        }
                    }
                });
    }
}
