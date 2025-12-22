package com.example.deviceinfo.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.deviceinfo.fragment.LocationFragment;
import com.example.deviceinfo.fragment.SettingsFragment;
import com.example.deviceinfo.fragment.WifiFragment;

public class MainPagerAdapter extends FragmentStateAdapter {

    public MainPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new WifiFragment();
            case 1:
                return new LocationFragment();
            default:
                return new SettingsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
