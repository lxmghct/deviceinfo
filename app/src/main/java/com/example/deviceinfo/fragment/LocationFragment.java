package com.example.deviceinfo.fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_location_page, container, false);
        context = requireContext();

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000,
                    1,
                    location -> {
                    }
            );
        }

        FragmentTransaction ft = getParentFragmentManager().beginTransaction();
        locationConfigFragment = ConfigEditorFragment.newInstance(new LocationData(), this::loadLocationInfo);
        ft.replace(R.id.location_fragment_container, locationConfigFragment, LOCATION_FRAGMENT_TAG);
        ft.commit();

        rootView.findViewById(R.id.btnOpenMap).setOnClickListener(v -> {
            UiUtils.toast(context, "功能开发中，敬请期待！");
        });

        return rootView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.post(this::loadLocationInfo);
        view.post(locationConfigFragment::getCurrentConfig);
    }

    private void loadLocationInfo() {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            UiUtils.toast(context, "无定位权限，无法获取位置信息");
            return;
        }
        Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (locationConfigFragment != null && gpsLocation != null) {
            LocationData locationData = LocationData.fromLocation(gpsLocation);
            locationConfigFragment.setTargetConfig(locationData);
        }
    }

}
