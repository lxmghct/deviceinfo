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
        XposedHelpers.findAndHookMethod(
                "android.net.NetworkInfo",
                lpparam.classLoader,
                "getType",
                new XC_MethodHook() {
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
            XposedHelpers.findAndHookMethod(
                    "android.net.ConnectivityManager",
                    lpparam.classLoader,
                    "getNetworkCapabilities",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            param.setResult(null);
                        }
                    });
            return;
        }
        XposedHelpers.findAndHookMethod(
                "android.net.NetworkCapabilities",
                lpparam.classLoader,
                "hasTransport",
                int.class,
                new XC_MethodHook() {
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
                            param.setResult(ssid);
                        }
                    }
                });

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
        XposedHelpers.findAndHookMethod("android.net.wifi.WifiInfo", lpparam.classLoader,
                "getBSSID", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        String bssid = bundle.getString("bssid", null);
                        if (bssid != null) {
                            param.setResult(bssid);
                        }
                    }
                });
        XposedHelpers.findAndHookMethod("android.net.wifi.WifiInfo", lpparam.classLoader,
                "getFrequency", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        int frequency = bundle.getInt("frequency", -1);
                        if (frequency != -1) {
                            param.setResult(frequency);
                        }
                    }
                });
        XposedHelpers.findAndHookMethod("android.net.wifi.WifiInfo", lpparam.classLoader,
                "getRssi", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        int rssi = bundle.getInt("rssi", -127);
                        if (rssi != -127) {
                            param.setResult(rssi);
                        }
                    }
                });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            XposedHelpers.findAndHookMethod("android.net.wifi.WifiInfo", lpparam.classLoader,
                    "getCurrentSecurityType", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            int securityType = bundle.getInt("securityType", -1);
                            if (securityType != -1) {
                                param.setResult(securityType);
                            }
                        }
                    });
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            XposedHelpers.findAndHookMethod("android.net.wifi.WifiInfo", lpparam.classLoader,
                    "getWifiStandard", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            int wifiStandard = bundle.getInt("wifiStandard", -1);
                            if (wifiStandard != -1) {
                                param.setResult(wifiStandard);
                            }
                        }
                    });
        }

        // TODO: Hook ScanResult

    }
}
