package com.geekyouup.android.ustopwatch.adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;

@Deprecated
public class TabsAdapter extends FragmentPagerAdapter {
    private final ArrayList<TabInfo> mTabs = new ArrayList<>();
    private final AppCompatActivity mAppCompatActivity;

    static final class TabInfo {
        private final Class<?> clazz;
        private final Bundle args;
        private final String title;

        TabInfo(String _title, Class<?> _class, Bundle _args) {
            title = _title;
            clazz = _class;
            args = _args;
        }
    }

    public TabsAdapter(AppCompatActivity activity) {
        super(activity.getSupportFragmentManager());
        mAppCompatActivity = activity;
    }

    public void addTab(String title, Class<?> clazz, Bundle args) {
        TabInfo info = new TabInfo(title, clazz, args);
        mTabs.add(info);
        notifyDataSetChanged();
    }

    public void addTab(String title, Class<?> clazz) {
        addTab(title, clazz, null);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTabs.get(position).title;
    }

    @Override
    public int getCount() {
        return mTabs.size();
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        TabInfo info = mTabs.get(position);
        return Fragment.instantiate(mAppCompatActivity, info.clazz.getName(), info.args);
    }
}