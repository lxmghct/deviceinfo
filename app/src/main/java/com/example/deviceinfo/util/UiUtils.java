package com.example.deviceinfo.util;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

public class UiUtils {

    // 用于全局控制是否显示 Toast / 防止多个 Toast 重叠
    public static boolean isToastEnabled = true;

    public static void toast(Context context, String s) {
        if (!isToastEnabled) return;
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }

    public static void enableToast(boolean enable) {
        isToastEnabled = enable;
    }

    // 隐藏键盘
    public static void hideKeyboardAndClearFocus(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        View temp = view.findFocus();
        if (temp != null) {
            temp.clearFocus();
        }
    }
}
