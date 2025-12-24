package com.example.deviceinfo.fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.deviceinfo.R;
import com.example.deviceinfo.pojo.LocationData;
import com.example.deviceinfo.util.UiUtils;

public class LocationFragment extends Fragment {

    private LocationManager locationManager;
    private Context context;

    public final String LOCATION_FRAGMENT_TAG = "location_fragment";

    private ConfigEditorFragment locationConfigFragment = null;

    private boolean isRequestingLocation = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_location_page, container, false);
        context = requireContext();

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        FragmentTransaction ft = getParentFragmentManager().beginTransaction();
        locationConfigFragment = ConfigEditorFragment.newInstance(new LocationData(), this::loadLocationInfo);
        ft.replace(R.id.location_fragment_container, locationConfigFragment, LOCATION_FRAGMENT_TAG);
        ft.commit();

        rootView.findViewById(R.id.btnOpenMap).setOnClickListener(v -> UiUtils.toast(context, "功能开发中，敬请期待！"));

        return rootView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.post(this::getLastKnownLocation);
        view.post(locationConfigFragment::getCurrentConfig);
    }

    private void getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            UiUtils.toast(context, "无定位权限，无法获取位置信息");
            return;
        }
        Location last = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (last != null) {
            LocationData locationData = LocationData.fromLocation(last);
            locationConfigFragment.setTargetConfig(locationData);
        }
    }

    private void loadLocationInfo() {
        if (isRequestingLocation) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            UiUtils.toast(context, "无定位权限，无法获取位置信息");
            return;
        }
        LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                // 拿到最新位置
                LocationData locationData = LocationData.fromLocation(location);
                locationConfigFragment.setTargetConfig(locationData);
                // 用完立刻停止定位
                locationManager.removeUpdates(this);
                isRequestingLocation = false;
                UiUtils.toast(context, "位置信息已更新");
            }
        };
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0,
                listener
        );
        isRequestingLocation = true;
        UiUtils.toast(context, "正在获取定位");
    }

}
