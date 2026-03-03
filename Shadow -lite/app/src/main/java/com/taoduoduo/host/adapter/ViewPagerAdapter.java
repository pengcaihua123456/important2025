package com.taoduoduo.host.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.taoduoduo.host.fragment.HomeFragment;
import com.taoduoduo.host.fragment.DoubleElevenFragment;
import com.taoduoduo.host.fragment.CategoryFragment;
import com.taoduoduo.host.fragment.CartFragment;
import com.taoduoduo.host.fragment.MineFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new HomeFragment();
            case 1:
                return new DoubleElevenFragment();
            case 2:
                return new CategoryFragment();
            case 3:
                return new CartFragment();
            case 4:
                return new MineFragment();
            default:
                return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}