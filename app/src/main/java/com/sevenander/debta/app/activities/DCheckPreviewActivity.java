package com.sevenander.debta.app.activities;

import android.app.Activity;
import android.content.ContentValues;
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
import android.widget.FrameLayout;
import android.widget.ListView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.SaveCallback;
import com.sevenander.debta.app.R;
import com.sevenander.debta.app.adapters.DCheckItemsListAdapter;
import com.sevenander.debta.app.data.DebtaContract;
import com.sevenander.debta.app.loaders.DCheckItemLoader;
import com.sevenander.debta.app.pojo.DCheck;
import com.sevenander.debta.app.pojo.DCheckItem;
import com.sevenander.debta.app.pojo.User;
import com.sevenander.debta.app.utils.Const;
import com.sevenander.debta.app.utils.Utils;

import java.util.ArrayList;
import java.util.Date;

public class DCheckPreviewActivity extends ActionBarActivity implements View.OnClickListener,
        LoaderManager.LoaderCallbacks<ArrayList<DCheckItem>>, SwipeRefreshLayout.OnRefreshListener {

    private Toolbar mToolbar;
    private View mViewShadow;
    private FrameLayout mProgressBar;
    private SwipeRefreshLayout mSrlItems;
    private ListView mLvChequeItems;
    private FloatingActionButton mFabEdit;

    private DCheckItemsListAdapter mAdapter;

    private ArrayList<String> mChequeItemsIds;
    private ArrayList<String> mUserIds;
    private String mObjectId;
    private String mGroupId;
    private String mUserId;
    private String mEditId;
    private long mDateLong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getExtras();

        if (Utils.isMyUid(mUserId))
            setContentView(R.layout.activity_dcheck_preview_my);
        else
            setContentView(R.layout.activity_dcheck_preview);

        setupToolbar();
        initLoader();
        initViews();
        setupViews();
    }

    private void getExtras() {
        Intent intent = getIntent();
        mDateLong = intent.getLongExtra(Const.KEY_CDHECK_DATE, 0);
        mObjectId = intent.getStringExtra(Const.KEY_OBJECT_ID);
        mGroupId = intent.getStringExtra(Const.KEY_DGROUP_ID);
        mEditId = intent.getStringExtra(Const.KEY_EDIT_ID);
        mUserId = intent.getStringExtra(Const.KEY_USER_ID);
        mUserIds = intent.getStringArrayListExtra(Const.KEY_USER_IDS);
        mChequeItemsIds = intent.getStringArrayListExtra(Const.KEY_DCHECK_ITEM_IDS);
    }

    private void setupToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            if (!Utils.isMyUid(mUserId)) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                mToolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
                mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });
            }

            Date date = new Date();
            if (mDateLong != 0) date.setTime(mDateLong);
            getSupportActionBar().setTitle(Utils.formatDate(date));
        }
    }

    private void initLoader() {
        Bundle args = new Bundle();
        args.putString(Const.KEY_STRING_URI,
                DebtaContract.DCheckItemEntry.buildDCheckItemUri().toString());
        args.putStringArrayList(Const.KEY_DCHECK_ITEM_IDS, mChequeItemsIds);
        getSupportLoaderManager().initLoader(Const.DCHECK_ITEM_LOADER, args, this);
    }

    private void initViews() {
        mViewShadow = findViewById(R.id.vShadow);
        mProgressBar = (FrameLayout) findViewById(R.id.fl_progress_dcheck_preview_container);
        mLvChequeItems = (ListView) findViewById(R.id.lv_dcheck_items_preview);
        mSrlItems = (SwipeRefreshLayout) findViewById(R.id.srl_dcheck_items);
        if (!mUserId.equals(User.getCurrentUser().getObjectId())) {
            mFabEdit = (FloatingActionButton) findViewById(R.id.fab_edit);
        }
    }

    private void setupViews() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            mViewShadow.setVisibility(View.GONE);
        }

        if (!mUserId.equals(User.getCurrentUser().getObjectId())) {
            mFabEdit.setOnClickListener(this);
        }

        mProgressBar.setVisibility(View.VISIBLE);
        mSrlItems.setOnRefreshListener(this);
        mSrlItems.setColorSchemeResources(R.color.colorPrimary);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mUserId.equals(User.getCurrentUser().getObjectId()))
            getMenuInflater().inflate(R.menu.menu_dcheck_preview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_paid:
                setChequePaid();
                return true;
            case R.id.action_delete:
                deleteCheque();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Const.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            mChequeItemsIds = data.getStringArrayListExtra(Const.KEY_DCHECK_ITEM_IDS);
            mEditId = data.getStringExtra(Const.KEY_EDIT_ID);
        }
        onRefresh();
    }

    private void setChequePaid() {
        DCheck dCheck = new DCheck();
        dCheck.setObjectId(mObjectId);
        dCheck.setPaid(true);

        ContentValues dCheckValues = new ContentValues();
        dCheckValues.put(DebtaContract.DCheckEntry.COLUMN_IS_PAID, dCheck.isPaid() ? 1 : 0);
        String selection = DebtaContract.DCheckEntry.COLUMN_OBJECT_ID + " = ?";
        String[] selectionArgs = {mObjectId};

        getContentResolver().update(DebtaContract.DCheckEntry.CONTENT_URI, dCheckValues,
                selection, selectionArgs);

        dCheck.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });
    }

    private void deleteCheque() {
        String selection = DebtaContract.DCheckEntry.COLUMN_OBJECT_ID + " = ?";
        String[] selectionArgs = {mObjectId};
        getContentResolver().delete(DebtaContract.DCheckEntry.CONTENT_URI,
                selection, selectionArgs);

        DCheck cheque = new DCheck();
        cheque.setObjectId(mObjectId);
        cheque.deleteInBackground(new DeleteCallback() {
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_edit:
                Intent intent = new Intent(DCheckPreviewActivity.this, DCheckEditActivity.class);
                intent.putExtra(Const.KEY_CDHECK_DATE, mDateLong);
                intent.putExtra(Const.KEY_DGROUP_ID, mGroupId);
                intent.putExtra(Const.KEY_EDIT_ID, mEditId);
                intent.putExtra(Const.KEY_USER_ID, mUserId);
                intent.putExtra(Const.KEY_IS_EDIT, true);
                intent.putStringArrayListExtra(Const.KEY_USER_IDS, mUserIds);
                startActivityForResult(intent, Const.REQUEST_CODE);
                break;
        }
    }

    @Override
    public Loader<ArrayList<DCheckItem>> onCreateLoader(int id, Bundle args) {
        ArrayList<String> chequeItemIds = args.getStringArrayList(Const.KEY_DCHECK_ITEM_IDS);

        Uri uri = Uri.parse(args.getString(Const.KEY_STRING_URI));
        String selection = makePlaceholders(chequeItemIds.size());
        String[] selectionArgs = chequeItemIds.toArray(new String[chequeItemIds.size()]);

        return new DCheckItemLoader(
                DCheckPreviewActivity.this,
                uri,
                null,
                selection,
                selectionArgs,
                null
        );
    }

    private String makePlaceholders(int len) {
        StringBuilder sb = new StringBuilder();
        sb.append(DebtaContract.DCheckItemEntry.COLUMN_OBJECT_ID + " = ? ");
        for (int i = 1; i < len; i++) {
            sb.append("OR " + DebtaContract.DCheckItemEntry.COLUMN_OBJECT_ID + " = ? ");
        }
        return sb.toString();
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<DCheckItem>> loader, ArrayList<DCheckItem> data) {
        mProgressBar.setVisibility(View.GONE);
        mAdapter = new DCheckItemsListAdapter(
                DCheckPreviewActivity.this, R.layout.item_dcheckitem, data, false);
        mLvChequeItems.setAdapter(mAdapter);
        mSrlItems.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<DCheckItem>> loader) {
        mAdapter.setData(null);
    }

    @Override
    public void onRefresh() {
        Bundle args = new Bundle();
        args.putString(Const.KEY_STRING_URI,
                DebtaContract.DCheckItemEntry.buildDCheckItemUri().toString());
        args.putStringArrayList(Const.KEY_DCHECK_ITEM_IDS, mChequeItemsIds);
        getSupportLoaderManager().restartLoader(Const.DCHECK_ITEM_LOADER, args, this);
    }
}
