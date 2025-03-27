package com.geekyouup.android.ustopwatch.adapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

public class TabsFragmentAdapter extends FragmentStateAdapter {

    private final ArrayList<Fragment> mTabs = new ArrayList<>();
    private final ArrayList<String> mTitles = new ArrayList<>();
    /**
     * @noinspection unused
     */
    private final AppCompatActivity mAppCompatActivity;

    public TabsFragmentAdapter(AppCompatActivity activity) {
        super(activity);
        mAppCompatActivity = activity;
    }

    public void addTab(String title, Fragment fragment) {
        mTitles.add(title);
        mTabs.add(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return mTabs.get(position);
    }

    public String getFragmentTitle(int position) {
        return mTitles.get(position);
    }

    @Override
    public int getItemCount() {
        return mTabs.size();
    }
}
