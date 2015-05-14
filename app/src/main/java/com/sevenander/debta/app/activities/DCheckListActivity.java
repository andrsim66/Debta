package com.sevenander.debta.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.sevenander.debta.app.R;
import com.sevenander.debta.app.adapters.DCheckListAdapter;
import com.sevenander.debta.app.data.DebtaContract;
import com.sevenander.debta.app.loaders.DCheckLoader;
import com.sevenander.debta.app.pojo.DCheck;
import com.sevenander.debta.app.utils.Const;
import com.sevenander.debta.app.utils.Utils;

import java.util.ArrayList;

public class DCheckListActivity extends ActionBarActivity implements AdapterView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<ArrayList<DCheck>>, View.OnClickListener,
        SwipeRefreshLayout.OnRefreshListener {

    private Toolbar mToolbar;
    private View mViewShadow;
    private SwipeRefreshLayout mSrlDChecks;
    private ListView mLvDChecks;
    private TextView mTvTotal;
    private FrameLayout mProgressBar;
    private FloatingActionButton mFabAddDCheck;

    private DCheckListAdapter mAdapter;

    private ArrayList<DCheck> mDChecks;
    private ArrayList<String> mUserIds;
    private String mUserId;
    private String mGroupId;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dcheck_list);

        getExtras();
        setupToolbar();
        initViews();
        setupViews();
        initLoader();
    }

    private void getExtras() {
        Intent intent = getIntent();
        mGroupId = intent.getStringExtra(Const.KEY_DGROUP_ID);
        mUserIds = intent.getStringArrayListExtra(Const.KEY_USER_IDS);
        mUserId = intent.getStringExtra(Const.KEY_USER_ID);
        if (!Utils.isMyUid(mUserId)) {
            userName = intent.getStringExtra(Const.KEY_USER_FNAME);
        }
    }

    private void setupToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            if (Utils.isMyUid(mUserId)) {
                getSupportActionBar().setTitle(getResources()
                        .getString(R.string.dcheck_list_activity_title_my));
            } else {
                getSupportActionBar().setTitle(userName + getResources()
                        .getString(R.string.dcheck_list_activity_title));
            }
        }
    }

    private void initLoader() {
        Bundle args = new Bundle();
        args.putString(Const.KEY_STRING_URI, DebtaContract.DCheckEntry.buildDCheckUri().toString());
        args.putString(Const.KEY_DGROUP_ID, mGroupId);
        args.putString(Const.KEY_USER_ID, mUserId);
        getSupportLoaderManager().initLoader(Const.DCHECK_LOADER, args, this);
    }

    private void initViews() {
        mViewShadow = findViewById(R.id.vShadow);
        mProgressBar = (FrameLayout) findViewById(R.id.fl_progress_dchecks_container);
        mSrlDChecks = (SwipeRefreshLayout) findViewById(R.id.srl_dchecks);
        mLvDChecks = (ListView) findViewById(R.id.lv_dchecks);
        mTvTotal = (TextView) findViewById(R.id.tv_total_dchecks);
        mFabAddDCheck = (FloatingActionButton) findViewById(R.id.fab_add_dcheck);
    }

    private void setupViews() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            mViewShadow.setVisibility(View.GONE);
        }

        if (Utils.isMyUid(mUserId))
            mFabAddDCheck.setVisibility(View.GONE);
        else {
            mFabAddDCheck.setVisibility(View.VISIBLE);
            mFabAddDCheck.setOnClickListener(this);
        }

        mProgressBar.setVisibility(View.VISIBLE);
        mLvDChecks.setOnItemClickListener(this);
        mSrlDChecks.setColorSchemeResources(R.color.colorPrimary);
        mSrlDChecks.setOnRefreshListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        onRefresh();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (Utils.isMyUid(mUserId)) {
            DCheck dCheck = mDChecks.get(position);
            if (!dCheck.isConfirmed()) {
                dCheck.setConfirmed(true);
                dCheck.saveInBackground();
            }
        }

        Intent intent = new Intent(DCheckListActivity.this, DCheckPreviewActivity.class);
        intent.putExtra(Const.KEY_USER_ID, mUserId);
        intent.putExtra(Const.KEY_USER_IDS, mUserIds);
        intent.putExtra(Const.KEY_DGROUP_ID, mGroupId);
        intent.putExtra(Const.KEY_EDIT_ID, mDChecks.get(position).getEditId());
        intent.putExtra(Const.KEY_OBJECT_ID, mDChecks.get(position).getObjectId());
        intent.putExtra(Const.KEY_CDHECK_DATE, mDChecks.get(position).getDate().getTime());
        intent.putStringArrayListExtra(Const.KEY_DCHECK_ITEM_IDS,
                new ArrayList<>(mDChecks.get(position).getItems()));

        startActivityForResult(intent, Const.REQUEST_CODE);
    }

    @Override
    public Loader<ArrayList<DCheck>> onCreateLoader(int id, Bundle args) {
        String sUri = args.getString(Const.KEY_STRING_URI);
        String groupId = args.getString(Const.KEY_DGROUP_ID);
        String userId = args.getString(Const.KEY_USER_ID);

        Uri uri = sUri == null ? null : Uri.parse(sUri);

        String selection = DebtaContract.DCheckEntry.COLUMN_IS_PAID + " = ? AND " +
                DebtaContract.DCheckEntry.COLUMN_GROUP_ID + " = ? AND " +
                DebtaContract.DCheckEntry.COLUMN_USER_ID + " = ?";

        String[] selectionArgs = {"0", groupId, userId};

        String sortOrder = DebtaContract.DCheckEntry.COLUMN_DATE + " DESC";

        return new DCheckLoader(
                DCheckListActivity.this,
                uri,
                null,
                selection,
                selectionArgs,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<DCheck>> loader, ArrayList<DCheck> data) {
        mProgressBar.setVisibility(View.GONE);
        mAdapter = new DCheckListAdapter(DCheckListActivity.this, R.layout.item_dgroup, data);
        mLvDChecks.setAdapter(mAdapter);
        mDChecks = data;
        mTvTotal.setText(Utils.formatMoney(calcTotal()));
        mSrlDChecks.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<DCheck>> loader) {
        mAdapter.setData(null);
    }

    private double calcTotal() {
        double total = 0;
        for (int i = 0; i < mDChecks.size(); i++) {
            total += mDChecks.get(i).getDebt();
        }
        return total;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_add_dcheck:
                Intent intent = new Intent(DCheckListActivity.this, DCheckEditActivity.class);
                intent.putStringArrayListExtra(Const.KEY_USER_IDS, mUserIds);
                intent.putExtra(Const.KEY_DGROUP_ID, mGroupId);
                startActivityForResult(intent, Const.REQUEST_CODE);
                break;
        }
    }

    @Override
    public void onRefresh() {
        Bundle args = new Bundle();
        args.putString(Const.KEY_STRING_URI, null);
        args.putString(Const.KEY_DGROUP_ID, mGroupId);
        args.putString(Const.KEY_USER_ID, mUserId);
        getSupportLoaderManager().restartLoader(Const.DCHECK_LOADER, args, this);
    }
}
