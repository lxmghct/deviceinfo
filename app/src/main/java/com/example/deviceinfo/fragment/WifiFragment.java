package com.example.deviceinfo.fragment;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.deviceinfo.R;
import com.example.deviceinfo.pojo.WifiData;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


public class WifiFragment extends Fragment {

    private WifiManager wifiManager;
    private ConnectivityManager connectivityManager;

    public final String WIFI_FRAGMENT_TAG = "wifi_fragment";


    private ConfigEditorFragment wifiConfigFragment = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_wifi_page, container, false);

        Context context = requireContext();

        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        rootView.findViewById(R.id.btn_get_network_info).setOnClickListener(v -> loadNetworkInfo());

        FragmentTransaction ft = getParentFragmentManager().beginTransaction();
        wifiConfigFragment = ConfigEditorFragment.newInstance(new WifiData());
        ft.replace(R.id.wifi_fragment_container, wifiConfigFragment, WIFI_FRAGMENT_TAG);
        ft.commit();

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 等待视图加载完成后再加载网络信息
        view.post(this::loadNetworkInfo);
    }

    private void loadNetworkInfo() {
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        Integer type = (info != null) ? info.getType() : null;
        // 1: WIFI, 0: MOBILE, null: 无网络
        Log.d("WifiFragment", "Active Network Type: " + type);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        WifiData data = WifiData.fromWifiInfo(wifiInfo, type == null ? -1 : type);
        if (wifiConfigFragment != null) {
            wifiConfigFragment.setTargetConfig(data);
        }
    }

}
