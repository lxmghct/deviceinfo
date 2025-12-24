package com.example.deviceinfo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.deviceinfo.R;
import com.example.deviceinfo.util.XposedUtils;

public class SettingsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_setting_page, container, false);
        TextView xposedInfo = rootView.findViewById(R.id.xposedInfo);
        boolean isXposedActive = XposedUtils.isXposedActivated(requireContext());
        xposedInfo.setText(isXposedActive ? "Xposed 已激活" : "Xposed 未激活，请激活模块后重启应用");
        return rootView;
    }
}
