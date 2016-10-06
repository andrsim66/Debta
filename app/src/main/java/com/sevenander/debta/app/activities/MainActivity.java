package com.sevenander.debta.app.activities;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.astuetz.PagerSlidingTabStrip;
import com.sevenander.debta.app.R;
import com.sevenander.debta.app.adapters.DGroupPagerAdapter;
import com.sevenander.debta.app.sync.DebtaSyncAdapter;
import com.sevenander.debta.app.utils.Const;
import com.sevenander.debta.app.utils.Logger;
import com.sevenander.debta.app.utils.Utils;

import java.util.Date;

public class MainActivity extends ActionBarActivity {

    private Toolbar mToolbar;
    private PagerSlidingTabStrip mTabsDGroup;
    private View mViewShadow;
    private ViewPager mPagerDGroup;
    private DGroupPagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        boolean isClear = intent.getBooleanExtra(Const.KEY_ACTION_CLEAR, false);
        if (isClear) clearNotification(DebtaSyncAdapter.NOTIFICATION_ID);

        setupToolbar();
        initViews();
        setupViews();

        DebtaSyncAdapter.initializeSyncAdapter(MainActivity.this);

        long diff = (System.currentTimeMillis() - Utils.getLastSyncTime(MainActivity.this));
        Logger.d("lastSyncTime = " + new Date(Utils.getLastSyncTime(MainActivity.this)).toString());
        Logger.d("Diff = " + diff);
        if (diff > (18 * 1000)) {
            DebtaSyncAdapter.syncImmediately(MainActivity.this);
        }
    }

    private void clearNotification(int id) {
        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(id);
    }

    private void setupToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        }
    }

    private void initViews() {
        mViewShadow = findViewById(R.id.vTabShadow);
        mTabsDGroup = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        mPagerDGroup = (ViewPager) findViewById(R.id.pager);
    }

    private void setupViews() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            mViewShadow.setVisibility(View.GONE);
        }
        mAdapter = new DGroupPagerAdapter(getSupportFragmentManager(), MainActivity.this);
        mPagerDGroup.setAdapter(mAdapter);
        mPagerDGroup.setPageMargin((int) getResources().getDimension(R.dimen.page_margin));
        mTabsDGroup.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        mTabsDGroup.setTextColor(getResources().getColor(R.color.white));
        mTabsDGroup.setTextSize((int) getResources().getDimension(R.dimen.text_size_14));
        mTabsDGroup.setIndicatorColor(getResources().getColor(R.color.white));
        mTabsDGroup.setDividerColor(getResources().getColor(android.R.color.transparent));
        mTabsDGroup.setIndicatorHeight((int) getResources().getDimension(R.dimen.tabs_indicator_height));
        mTabsDGroup.setViewPager(mPagerDGroup);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
