package com.example.deviceinfo.util;

import android.content.Context;
import android.widget.Toast;

public class UiUtils {

    public static void toast(Context context, String s) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }
}
