package com.example.deviceinfo.fragment;

import static com.example.deviceinfo.util.LocationUtils.buildLocationManager;

import android.content.Context;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.deviceinfo.R;
import com.example.deviceinfo.pojo.LocationData;

public class LocationFragment extends Fragment {

    private LocationManager locationManager;

    public final String LOCATION_FRAGMENT_TAG = "location_fragment";

    private ConfigEditorFragment locationConfigFragment = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_location_page, container, false);
        Context context = requireContext();

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        buildLocationManager(locationManager, context);

        FragmentTransaction ft = getParentFragmentManager().beginTransaction();
        locationConfigFragment = ConfigEditorFragment.newInstance(new LocationData(), LocationData.keys, LocationData.KEY_OF_DEFAULT_NAME);
        ft.replace(R.id.location_fragment_container, locationConfigFragment, LOCATION_FRAGMENT_TAG);
        ft.commit();

        rootView.findViewById(R.id.btn_get_location_info).setOnClickListener(v -> loadLocationInfo());
        return rootView;
    }


    private void loadLocationInfo() {
    }
}
