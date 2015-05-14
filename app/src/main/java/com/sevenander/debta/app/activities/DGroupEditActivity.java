package com.sevenander.debta.app.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.sevenander.debta.app.R;
import com.sevenander.debta.app.adapters.UserSearchListAdapter;
import com.sevenander.debta.app.data.DebtaContract;
import com.sevenander.debta.app.loaders.UserLoader;
import com.sevenander.debta.app.loaders.UserSearchLoader;
import com.sevenander.debta.app.pojo.DGroup;
import com.sevenander.debta.app.pojo.User;
import com.sevenander.debta.app.utils.Const;
import com.sevenander.debta.app.utils.Utils;
import com.wefika.flowlayout.FlowLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by andrii on 02.03.15.
 */
public class DGroupEditActivity extends ActionBarActivity implements SearchView.OnQueryTextListener,
        SearchView.OnCloseListener, View.OnClickListener, AdapterView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<ArrayList<User>> {

    private Toolbar mToolbar;
    private View mViewShadow;
    private ProgressBar mProgressBar;
    private ListView mLvSearchResult;
    private Button mBCreate;
    private Button mBSkip;
    private FlowLayout mFlContainer;

    private UserSearchListAdapter mAdapter;

    private ArrayList<User> mResult;
    private Map<String, Boolean> mChecked;
    private DGroup mDGroup;
    private String mDGroupName;
    private String mGroupId;
    private String mCurFilter;
    private boolean mIsEdit;
    private int mCallNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dgroup_edit);

        mChecked = new HashMap<>();

        getExtras();
        setupToolbar();
        initViews();
        setupViews();
        initLoaders();
        if (mIsEdit)
            invalidateButton();
    }

    private void getExtras() {
        Intent intent = getIntent();
        mCallNumber = intent.getIntExtra(Const.KEY_CALL_EXTRA, -1);
        mIsEdit = intent.getBooleanExtra(Const.KEY_IS_EDIT, false);
        if (mIsEdit) {
            mGroupId = intent.getStringExtra(Const.KEY_DGROUP_ID);
            mDGroupName = intent.getStringExtra(Const.KEY_DGROUP_NAME);
            ArrayList<String> ids = intent.getStringArrayListExtra(Const.KEY_USER_IDS);
            for (int i = 0; i < ids.size(); i++) {
                mChecked.put(ids.get(i), true);
            }
        }
    }

    private void setupToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            if (mIsEdit) {
                getSupportActionBar().setTitle(getResources()
                        .getString(R.string.dgroup_edit_activity_title_edit));
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                mToolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
                mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });
            } else {
                getSupportActionBar().setTitle(getResources()
                        .getString(R.string.dgroup_edit_activity_title_new));
            }
        }
    }


    private void initViews() {
        mViewShadow = findViewById(R.id.vShadow);
        mLvSearchResult = (ListView) findViewById(R.id.lv_search_result);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_search_progress);
        mBCreate = (Button) findViewById(R.id.b_add_group);
        mBSkip = (Button) findViewById(R.id.b_add_group_skip);
        mFlContainer = (FlowLayout) findViewById(R.id.ll_container);
    }

    private void setupViews() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            mViewShadow.setVisibility(View.GONE);
        }

        mBCreate.setOnClickListener(this);
        if (mCallNumber == Const.FIRST_CALL) {
            mBSkip.setVisibility(View.VISIBLE);
            mBSkip.setOnClickListener(this);
        } else if (mCallNumber == Const.OTHER_CALL) {
            mBSkip.setVisibility(View.GONE);
        }

        if (mIsEdit) {
            mBCreate.setText(getResources().getString(R.string.dgroup_edit_button_save));
            mBCreate.setTextColor(getResources().getColor(R.color.blue));
            mBCreate.setEnabled(true);
        }

        mLvSearchResult.setOnItemClickListener(this);
    }

    public void updateViews(boolean isChecked) {
        mDGroup = new DGroup();

        if (isChecked) {
            mBCreate.setTextColor(getResources().getColor(R.color.blue));
            mBCreate.setEnabled(true);
        } else {
            mBCreate.setTextColor(getResources().getColor(R.color.grey));
            mBCreate.setEnabled(false);
        }

        getSupportLoaderManager().restartLoader(Const.USER_LOADER, null, this);
    }

    private void initLoaders() {
        getSupportLoaderManager().initLoader(Const.USER_SEARCH_LOADER, null, this);
        getSupportLoaderManager().initLoader(Const.USER_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(getResources().getString(R.string.dgroup_edit_search_hint));
        searchView.setOnQueryTextListener(this);
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);

        searchView.findViewById(android.support.v7.appcompat.R.id.search_plate)
                .setBackgroundColor(Color.TRANSPARENT);

        ((EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text))
                .setHintTextColor(getResources().getColor(R.color.white));
        return super.onCreateOptionsMenu(menu);
    }

    private TextView addTextView(String text) {
        TextView tvUserName = new TextView(DGroupEditActivity.this);
        tvUserName.setText(text);
        tvUserName.setBackground(getResources().getDrawable(R.drawable.rounded_bg));
        tvUserName.setTextSize(TypedValue.COMPLEX_UNIT_SP, Const.TEXT_SIZE_16);
        tvUserName.setPadding(Const.MARGIN_16, Const.MARGIN_8, Const.MARGIN_16, Const.MARGIN_8);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = Const.MARGIN_16;
        tvUserName.setLayoutParams(params);
        return tvUserName;
    }

    private LinearLayout addTextViewContainer(String uid, TextView textView) {
        LinearLayout llTextViewContainer = new LinearLayout(DGroupEditActivity.this);
        llTextViewContainer.setOrientation(LinearLayout.HORIZONTAL);
        llTextViewContainer.setGravity(Gravity.CENTER_VERTICAL);
        llTextViewContainer.setBackground(getResources().getDrawable(R.drawable.rounded_bg));
        FlowLayout.LayoutParams params = new FlowLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = Const.MARGIN_16;
        params.bottomMargin = Const.MARGIN_8;
        llTextViewContainer.setLayoutParams(params);

        ImageView imageView = new ImageView(DGroupEditActivity.this);
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_cancel_grey600_24dp));
        imageView.setOnClickListener(clickListener(uid));
        LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ivParams.rightMargin = Const.MARGIN_16;
        imageView.setLayoutParams(ivParams);

        llTextViewContainer.addView(textView);
        llTextViewContainer.addView(imageView);
        return llTextViewContainer;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        if (query.length() > 1) {
            mProgressBar.setVisibility(View.VISIBLE);
            mCurFilter = query;
            getSupportLoaderManager().restartLoader(Const.USER_SEARCH_LOADER, null, this);
        }
        return true;
    }

    @Override
    public boolean onClose() {
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.b_add_group:
                if (mIsEdit) {
                    showInputDialog(mDGroupName);
                } else {
                    showInputDialog("");
                }
                break;
            case R.id.b_add_group_skip:
                Intent intent = new Intent(DGroupEditActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
        }
    }

    private View.OnClickListener clickListener(final String uid) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChecked.put(uid, false);
                invalidateButton();
            }
        };
    }

    private void showInputDialog(String groupName) {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.dgroup_edit_name_input_dialog_title)
                .input(R.string.input_dialog_group_hint, 0, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        saveDGroup(input.toString());
                    }
                }).build();

        dialog.getInputEditText().setText(groupName);

        dialog.show();
    }

    private void saveDGroup(String title) {
        if (title == null || title.length() == 0)
            return;

        mDGroup.setName(title);
        mDGroup.setAdminId(User.getCurrentUser().getObjectId());
        ArrayList<String> members = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : mChecked.entrySet()) {
            if (entry.getValue()) members.add(entry.getKey());

        }
        mDGroup.setMembers(members);

        if (mIsEdit) {
            mDGroup.setObjectId(mGroupId);
            String selection = DebtaContract.DGroupEntry.COLUMN_OBJECT_ID + " = ?";
            String[] selectionArgs = {mGroupId};
            getContentResolver().update(DebtaContract.DGroupEntry.CONTENT_URI,
                    Utils.setDGroupContentValues(mDGroup), selection, selectionArgs);
        }

        mDGroup.saveInBackground();

        if (mCallNumber == Const.FIRST_CALL) {
            Intent intent = new Intent(DGroupEditActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (mCallNumber == Const.OTHER_CALL) {
            Intent intent = new Intent();
            intent.putExtra(Const.KEY_DGROUP_NAME, title);
            intent.putExtra(Const.KEY_USER_IDS, getUserIds());
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private ArrayList<String> getUserIds() {
        ArrayList<String> ids = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : mChecked.entrySet()) {
            if (entry.getValue()) {
                ids.add(entry.getKey());
            }
        }
        return ids;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String uid = mResult.get(position).getObjectId();
        if (mChecked.get(uid) == null) {
            mChecked.put(uid, true);
        } else {
            mChecked.put(uid, !mChecked.get(uid));
        }
        invalidateButton();
        mResult.clear();
        mAdapter.notifyDataSetChanged();
    }

    private void invalidateButton() {
        int count = 0;
        if (mChecked.size() > 0) {
            for (boolean entry : mChecked.values()) {
                if (entry) {
                    updateViews(true);
                    break;
                } else
                    count++;
            }
            if (count == mChecked.size())
                updateViews(false);
        } else
            updateViews(false);
    }

    @Override
    public Loader<ArrayList<User>> onCreateLoader(int id, Bundle args) {
        if (id == Const.USER_SEARCH_LOADER)
            return new UserSearchLoader(DGroupEditActivity.this, mCurFilter);
        else {
            ArrayList<String> ids = new ArrayList<>();
            for (Map.Entry<String, Boolean> entry : mChecked.entrySet()) {
                if (entry.getValue()) {
                    ids.add(entry.getKey());
                }
            }

            String[] selectionArgs = ids.toArray(new String[ids.size()]);
            if (selectionArgs.length == 0) {
                selectionArgs = new String[]{"-1"};
            }

            return new UserLoader(
                    DGroupEditActivity.this,
                    null,
                    null,
                    null,
                    selectionArgs,
                    null);
        }
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<User>> loader, ArrayList<User> data) {
        if (loader.getId() == Const.USER_SEARCH_LOADER) {
            mProgressBar.setVisibility(View.GONE);
            mAdapter = new UserSearchListAdapter(DGroupEditActivity.this,
                    R.layout.item_user_search, data);
            mLvSearchResult.setAdapter(mAdapter);
            mResult = data;
        } else {
            mFlContainer.removeAllViews();
            for (int i = 0; i < data.size(); i++) {
                mFlContainer.addView(addTextViewContainer(data.get(i).getObjectId(),
                        addTextView(data.get(i).getFName() + " " + data.get(i).getLName())));
            }

        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<User>> loader) {
        if (loader.getId() == Const.USER_SEARCH_LOADER)
            mAdapter.setData(null);
        else
            mFlContainer.removeAllViews();
    }
}
