package com.example.deviceinfo.fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.deviceinfo.R;
import com.example.deviceinfo.pojo.WifiData;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.List;

public class WifiFragment extends Fragment {

    private Context context;

    private WifiManager wifiManager;
    private ConnectivityManager connectivityManager;

    public final String WIFI_FRAGMENT_TAG = "wifi_fragment";


    private ConfigEditorFragment wifiConfigFragment = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_wifi_page, container, false);

        context = requireContext();

        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        rootView.findViewById(R.id.btn_get_network_info).setOnClickListener(v -> loadNetworkInfo());

        FragmentTransaction ft = getParentFragmentManager().beginTransaction();
        wifiConfigFragment = ConfigEditorFragment.newInstance(new WifiData(), WifiData.keys, WifiData.KEY_OF_DEFAULT_NAME);
        ft.replace(R.id.wifi_fragment_container, wifiConfigFragment, WIFI_FRAGMENT_TAG);
        ft.commit();

        return rootView;
    }

    private void loadNetworkInfo() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        WifiData data = WifiData.fromWifiInfo(wifiInfo);
        if (wifiConfigFragment != null) {
            wifiConfigFragment.setTargetConfig(data);
        }
    }


    private String getNetworkTypeInfo() {
        StringBuilder sb = new StringBuilder();

        Network network = connectivityManager.getActiveNetwork();
        sb.append("getActiveNetwork() result=\n").append(network).append("\n");

        if (network != null) {
            NetworkCapabilities cap = connectivityManager.getNetworkCapabilities(network);
            sb.append("getNetworkCapabilities() result=\n").append(cap).append("\n");
        } else {
            sb.append("getNetworkCapabilities() result=\nnull\n");
        }

        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        sb.append("getActiveNetworkInfo() result=\n").append(info).append("\n");

        return sb.toString();
    }

    private String getCurrentWifiInfo() {
        WifiInfo info = wifiManager.getConnectionInfo();
        return "getConnectionInfo() result=\n" + info + "\n";
    }

    private String getScanWifiInfo() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return "permissionCheck=DENIED -> 无定位权限\n";
        }

        List<ScanResult> list = wifiManager.getScanResults();
        StringBuilder sb = new StringBuilder();
        sb.append("getScanResults() result=\n");
        for (ScanResult result : list) {
            sb.append(result).append("\n");
        }
        return sb.toString();
    }
}
