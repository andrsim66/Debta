package com.sevenander.debta.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.sevenander.debta.app.R;
import com.sevenander.debta.app.adapters.UserListAdapter;
import com.sevenander.debta.app.data.DebtaContract;
import com.sevenander.debta.app.loaders.UserLoader;
import com.sevenander.debta.app.pojo.DGroup;
import com.sevenander.debta.app.pojo.User;
import com.sevenander.debta.app.utils.Const;
import com.sevenander.debta.app.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends ActionBarActivity implements AdapterView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<ArrayList<User>>, View.OnClickListener,
        SwipeRefreshLayout.OnRefreshListener {

    private Toolbar mToolbar;
    private View mViewShadow;
    private FrameLayout mProgressBar;
    private SwipeRefreshLayout mSrlUsers;
    private ListView mLvUsers;
    private TextView mTvTotal;
    private FloatingActionsMenu mFabMenu;
    private FloatingActionButton mFabAddCheque;
    private FloatingActionButton mFabEditGroup;

    private UserListAdapter mAdapter;

    private ArrayList<User> mUsers;
    private ArrayList<String> mUserIds;
    private String mGroupId;
    private String mGroupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        getExtras();
        setupToolbar();
        initViews();
        setupViews();
        initLoader();
    }

    private void getExtras() {
        Intent intent = getIntent();
        mGroupName = intent.getStringExtra(Const.KEY_DGROUP_NAME);
        mGroupId = intent.getStringExtra(Const.KEY_DGROUP_ID);
        mUserIds = intent.getStringArrayListExtra(Const.KEY_USER_IDS);
    }

    private void setupToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setTitle(mGroupName);
        }
    }

    private void initLoader() {
        Bundle args = new Bundle();
        args.putString(Const.KEY_STRING_URI, DebtaContract.UserEntry.buildUserUri().toString());
        args.putStringArrayList(Const.KEY_USER_IDS, mUserIds);
        getSupportLoaderManager().initLoader(Const.USER_LOADER, args, this);
    }

    private void initViews() {
        mViewShadow = findViewById(R.id.vShadow);
        mProgressBar = (FrameLayout) findViewById(R.id.fl_progress_user_list_container);
        mSrlUsers = (SwipeRefreshLayout) findViewById(R.id.srl_users);
        mLvUsers = (ListView) findViewById(R.id.lv_users);
        mTvTotal = (TextView) findViewById(R.id.tv_total_users);
        mFabMenu = (FloatingActionsMenu) findViewById(R.id.fam_user_list);
        mFabAddCheque = (FloatingActionButton) findViewById(R.id.fab_add_dcheck);
        mFabEditGroup = (FloatingActionButton) findViewById(R.id.fab_add_users);
    }

    private void setupViews() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            mViewShadow.setVisibility(View.GONE);
        }
        mLvUsers.setOnItemClickListener(this);
        mFabAddCheque.setOnClickListener(this);
        mFabEditGroup.setOnClickListener(this);
        mSrlUsers.setColorSchemeResources(R.color.colorPrimary);
        mSrlUsers.setOnRefreshListener(this);
        mFabMenu.collapse();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFabMenu.collapse();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_delete_dgroup) {
            deleteDGroup();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void deleteDGroup() {
        String selection = DebtaContract.DGroupEntry.COLUMN_OBJECT_ID + " = ?";
        String[] selectionArgs = {mGroupId};
        getContentResolver().delete(DebtaContract.DGroupEntry.CONTENT_URI,
                selection, selectionArgs);

        DGroup dGroup = new DGroup();
        dGroup.setObjectId(mGroupId);
        dGroup.deleteInBackground(new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(UserListActivity.this, DCheckListActivity.class);
        intent.putStringArrayListExtra(Const.KEY_USER_IDS, mUserIds);
        intent.putExtra(Const.KEY_DGROUP_ID, mGroupId);
        intent.putExtra(Const.KEY_USER_ID, mUsers.get(position).getObjectId());
        intent.putExtra(Const.KEY_USER_FNAME, mUsers.get(position).getFName());
        startActivityForResult(intent, Const.REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == Const.REQUEST_CODE_EDIT_DGROUP && resultCode == Activity.RESULT_OK) {
            mGroupName = intent.getStringExtra(Const.KEY_DGROUP_NAME);
            mUserIds = intent.getStringArrayListExtra(Const.KEY_USER_IDS);
        }
        onRefresh();
    }

    @Override
    public Loader<ArrayList<User>> onCreateLoader(int id, Bundle args) {
        String sUri = args.getString(Const.KEY_STRING_URI);
        ArrayList<String> uIds = args.getStringArrayList(Const.KEY_USER_IDS);

        Uri uri = sUri == null ? null : Uri.parse(sUri);
        String[] selectionArgs = uIds.toArray(new String[uIds.size()]);
        String selection = DebtaContract.UserEntry.COLUMN_OBJECT_ID;
        if (selectionArgs.length > 0) {
            selection += " IN (" + Utils.makePlaceholders(selectionArgs.length) + ")";
        } else {
            selection = null;
            selectionArgs = null;
        }

        return new UserLoader(
                UserListActivity.this,
                uri,
                null,
                selection,
                selectionArgs,
                null,
                mGroupId);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<User>> loader, ArrayList<User> data) {
        mProgressBar.setVisibility(View.GONE);
        mAdapter = new UserListAdapter(UserListActivity.this, R.layout.item_user, data);
        mLvUsers.setAdapter(mAdapter);
        mUsers = data;
        mTvTotal.setText(Utils.formatMoney(calcTotal(data)));
        mSrlUsers.setRefreshing(false);
    }

    private double calcTotal(List<User> users) {
        double total = 0;
        for (int i = 0; i < users.size(); i++) {
            total += users.get(i).getDebt();
        }
        return total;
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<User>> loader) {
        mAdapter.setData(null);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.fab_add_dcheck:
                intent = new Intent(UserListActivity.this, DCheckEditActivity.class);
                intent.putStringArrayListExtra(Const.KEY_USER_IDS, mUserIds);
                intent.putExtra(Const.KEY_DGROUP_ID, mGroupId);
                startActivityForResult(intent, Const.REQUEST_CODE);
                break;
            case R.id.fab_add_users:
                intent = new Intent(UserListActivity.this, DGroupEditActivity.class);
                intent.putExtra(Const.KEY_CALL_EXTRA, Const.OTHER_CALL);
                intent.putExtra(Const.KEY_IS_EDIT, true);
                intent.putExtra(Const.KEY_USER_IDS, mUserIds);
                intent.putExtra(Const.KEY_DGROUP_NAME, mGroupName);
                intent.putExtra(Const.KEY_DGROUP_ID, mGroupId);
                startActivityForResult(intent, Const.REQUEST_CODE_EDIT_DGROUP);
                break;
        }
    }

    @Override
    public void onRefresh() {
        Bundle args = new Bundle();
        args.putString(Const.KEY_STRING_URI, null);
        args.putStringArrayList(Const.KEY_USER_IDS, mUserIds);
        getSupportLoaderManager().restartLoader(Const.USER_LOADER, args, this);
    }
}
