package com.example.deviceinfo.util;


import android.content.Context;

public class XposedUtils {

    public static boolean isXposedActivated(Context context) {
        try {
            context.getSharedPreferences("test_prefs", Context.MODE_WORLD_READABLE);
            return true;
        } catch (SecurityException e) {
            // New XSharedPreferences 仅当启用模块时才允许使用 MODE_WORLD_READABLE
            return false;
        }
    }

}
