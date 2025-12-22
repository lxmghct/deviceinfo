package com.example.deviceinfo;

import static com.example.deviceinfo.util.LocationUtils.*;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.deviceinfo.fragment.ConfigEditorFragment;
import com.example.deviceinfo.fragment.config_editor.model.BaseConfig;
import com.example.deviceinfo.pojo.WifiData;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final int REQ_PERMISSION = 1001;

    private TextView locationContent;

    private WifiManager wifiManager;
    private ConnectivityManager connectivityManager;
    private LocationManager locationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationContent = findViewById(R.id.location_content);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        buildLocationManager(locationManager, this);

        requestPermissionsIfNeeded();

        findViewById(R.id.btn_get_network_info).setOnClickListener(v -> loadNetworkInfo());
        findViewById(R.id.btn_get_location_info).setOnClickListener(v -> loadLocationInfo());

    }

    private void requestPermissionsIfNeeded() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                REQ_PERMISSION
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void createOrUpdateReflectAdapter(BaseConfig obj) {
        if (getSupportFragmentManager().findFragmentByTag("ConfigEditorFragment") != null) {
            ConfigEditorFragment fragment = (ConfigEditorFragment)
                    getSupportFragmentManager().findFragmentByTag("ConfigEditorFragment");
            if (fragment != null) {
                fragment.setTargetConfig(obj);
            }
            return;
        }
        getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragment_container_view,
                        ConfigEditorFragment.newInstance(
                                obj,
                                WifiData.keys,
                                "ssid"
                        ),
                        "ConfigEditorFragment"
                )
                .commit();
    }

    private void loadNetworkInfo() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        WifiData data = WifiData.fromWifiInfo(wifiInfo);
        createOrUpdateReflectAdapter(data);
    }

    private void loadLocationInfo() {
        locationContent.setText("【定位信息】\n" + getLocationInfo(locationManager, this));
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
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
