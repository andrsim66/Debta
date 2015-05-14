package com.sevenander.debta.app.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.sevenander.debta.app.R;
import com.sevenander.debta.app.fragments.DGroupFragment;

/**
 * Created by andrii on 19.04.15.
 */
public class DGroupPagerAdapter extends FragmentPagerAdapter {

    private Context mContext;
    private String[] mTitles;

    public DGroupPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.mContext = context;
        mTitles = new String[2];
        mTitles[0] = mContext.getResources().getString(R.string.tab1_title);
        mTitles[1] = mContext.getResources().getString(R.string.tab2_title);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }

    @Override
    public int getCount() {
        return mTitles.length;
    }

    @Override
    public Fragment getItem(int position) {
        return DGroupFragment.newInstance(position);
    }
}
