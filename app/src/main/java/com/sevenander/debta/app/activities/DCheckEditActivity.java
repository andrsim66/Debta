package com.sevenander.debta.app.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;
import com.sevenander.debta.app.R;
import com.sevenander.debta.app.adapters.DCheckItemsListAdapter;
import com.sevenander.debta.app.data.DebtaContract;
import com.sevenander.debta.app.loaders.DCheckEditLoader;
import com.sevenander.debta.app.loaders.UserLoader;
import com.sevenander.debta.app.pojo.DCheck;
import com.sevenander.debta.app.pojo.DCheckItem;
import com.sevenander.debta.app.pojo.User;
import com.sevenander.debta.app.utils.Const;
import com.sevenander.debta.app.utils.Logger;
import com.sevenander.debta.app.utils.Utils;
import com.sevenander.debta.app.views.NoScrollListView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class DCheckEditActivity extends ActionBarActivity implements View.OnClickListener,
        LoaderManager.LoaderCallbacks<ArrayList<DCheckItem>> {

    private Toolbar mToolbar;
    private View mViewShadow;
    private FrameLayout mProgressBar;
    private NoScrollListView mLvDCheckItems;
    private LinearLayout mLlDate;
    private TextView mTvDate;
    private FloatingActionButton mFabAddItem;
    private EditText mEtPriceInput;

    private DCheckItemsListAdapter mAdapter;

    private boolean mIsEdit;
    private DCheckItem mTmpDCheckItem;
    private String mGroupId;
    private String mUserId;
    private String mEditId;
    private Date mDate;
    private ArrayList<User> mUsers;
    private ArrayList<DCheckItem> mDCheckItems;
    private ArrayList<DCheckItem> mDCheckItemsNew;
    private ArrayList<String> mUserIds;
    private ArrayList<String> mReturnItemIds;
    private HashMap<String, String> mDCheckToEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dcheck_edit);

        mDCheckToEdit = new HashMap<>();
        mDCheckItems = new ArrayList<>();
        mDCheckItemsNew = new ArrayList<>();
        mReturnItemIds = new ArrayList<>();
        mUsers = new ArrayList<>();

        getExtras();
        setupToolbar();
        initViews();
        setupViews();
        initUserLoader();

        if (mIsEdit)
            initLoader();
    }

    private void getExtras() {
        Intent intent = getIntent();
        mUserIds = intent.getStringArrayListExtra(Const.KEY_USER_IDS);
        mUserIds.add(User.getCurrentUser().getObjectId());
        mGroupId = intent.getStringExtra(Const.KEY_DGROUP_ID);
        mIsEdit = intent.getBooleanExtra(Const.KEY_IS_EDIT, false);

        if (mIsEdit) {
            mEditId = intent.getStringExtra(Const.KEY_EDIT_ID);
            mUserId = intent.getStringExtra(Const.KEY_USER_ID);
            long dateLong = intent.getLongExtra(Const.KEY_CDHECK_DATE, 0);
            mDate = new Date();
            if (dateLong != 0)
                mDate.setTime(dateLong);
        }
    }

    private void setupToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mToolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
            getSupportActionBar().setTitle(getResources()
                    .getString(R.string.dcheck_edit_activity_title));
        }
    }

    private void initViews() {
        mViewShadow = findViewById(R.id.vShadow);
        mProgressBar = (FrameLayout) findViewById(R.id.fl_progress_dcheck_edit_container);
        mLvDCheckItems = (NoScrollListView) findViewById(R.id.lv_dcheck_items);
        mLlDate = (LinearLayout) findViewById(R.id.ll_date_container);
        mTvDate = (TextView) findViewById(R.id.tv_dcheck_date);
        mFabAddItem = (FloatingActionButton) findViewById(R.id.fab_add_dcheck_item);
    }

    private void setupViews() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            mViewShadow.setVisibility(View.GONE);
        }

        if (mIsEdit) {
            mTvDate.setText(Utils.formatDate(mDate));
            mLlDate.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.GONE);
        }

        mAdapter = new DCheckItemsListAdapter(DCheckEditActivity.this,
                R.layout.item_dcheckitem, mDCheckItems, true);
        mLvDCheckItems.setAdapter(mAdapter);
        mLlDate.setOnClickListener(this);
        mFabAddItem.setOnClickListener(this);
    }

    private void initLoader() {
        Bundle args = new Bundle();
        args.putString(Const.KEY_STRING_URI, DebtaContract.DCheckEntry.buildDCheckUri().toString());
        args.putString(Const.KEY_EDIT_ID, mEditId);
        args.putSerializable(Const.KEY_DCHECKS_TO_EDIT, mDCheckToEdit);
        getSupportLoaderManager().initLoader(Const.DCHECK_ITEM_LOADER, args, this);
    }

    private void initUserLoader() {
        Bundle args = new Bundle();
        args.putString(Const.KEY_STRING_URI, DebtaContract.UserEntry.buildUserUri().toString());
        args.putStringArrayList(Const.KEY_USER_IDS, mUserIds);
        getSupportLoaderManager().initLoader(Const.USER_LOADER, args, new UserLoaderCallback());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dcheck_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save_cheque) {
            saveClicked();
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveClicked() {
        if (mDCheckItemsNew.size() > 0)
            saveDCheckItems();
        else
            finish();
    }

    private void saveDCheckItems() {
        ParseObject.saveAllInBackground(mDCheckItemsNew, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Utils.saveToDB(DCheckEditActivity.this,
                            Utils.setDCheckItemContentValues(mDCheckItemsNew),
                            DebtaContract.DCheckItemEntry.CONTENT_URI);
                    saveDChecks();
                } else {
                    Logger.d("Couldn't save. " + e.getMessage());
                }
            }
        });
    }

    private void saveDChecks() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mDCheckItemsNew.size(); i++) {
            sb.append(mDCheckItemsNew.get(i).getObjectId());
        }
        List<DCheck> split = splitCheques(
                sb.toString().equals(mEditId) ? sb.toString() + "1" : sb.toString());
        ParseObject.saveAllInBackground(split, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    if (mIsEdit) {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra(Const.KEY_DCHECK_ITEM_IDS, mReturnItemIds);
                        returnIntent.putExtra(Const.KEY_EDIT_ID, mEditId);
                        setResult(RESULT_OK, returnIntent);
                    } else
                        setResult(RESULT_OK);
                } else {
                    Logger.d("Couldn't save. " + e.getMessage());
                }
                finish();
            }
        });
    }

    private ArrayList<DCheck> splitCheques(String editId) {
        ArrayList<DCheck> cheques = new ArrayList<>();
        for (int i = 0; i < mUserIds.size(); i++) {
            if (!Utils.isMyUid(mUserIds.get(i))) {
                DCheck cheque = new DCheck();
                if (mDate != null)
                    cheque.setDate(mDate);
                else
                    cheque.setDate(new Date());
                cheque.setGroupId(mGroupId);

                cheque.setUserId(mUserIds.get(i));
                cheque.setEditId(editId);
                cheque.setPaid(false);
                cheque.setConfirmed(false);
                ArrayList<DCheckItem> items = new ArrayList<>();
                for (int j = 0; j < mDCheckItemsNew.size(); j++) {
                    if (mDCheckItemsNew.get(j).getUsers().contains(mUserIds.get(i))) {
                        items.add(mDCheckItemsNew.get(j));
                    }
                }
                cheque.setItems(Utils.getDCheckItemIds(items));
                cheque.setDebt(calcTotalDebt(items));
                if (cheque.getItems().size() > 0)
                    cheques.add(cheque);
                if (mIsEdit) {
                    if (mDCheckToEdit.containsKey(mUserIds.get(i)))
                        cheque.setObjectId(mDCheckToEdit.get(mUserIds.get(i)));
                    if (mUserIds.get(i).equals(mUserId)) {
                        mReturnItemIds = new ArrayList<>(Utils.getDCheckItemIds(items));
                        this.mEditId = editId;
                    }
                }
            }
        }
        return cheques;
    }

    private void datePickerDialog() {
        Calendar c = Calendar.getInstance();

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {

                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, monthOfYear, dayOfMonth);
                        mDate = calendar.getTime();
                        mTvDate.setText(Utils.formatDate(calendar.getTime()));
                    }
                }, year, month, day);
        dpd.setTitle(R.string.date_picker_dialog_title);
        dpd.show();
    }

    private void showInputDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.dcheck_item_name_input_dialog_title)
                .input(R.string.dcheck_item_name_input_dialog_hint, 0, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        if (input.length() > 0) {
                            mTmpDCheckItem.setName(input.toString());
                            showNumberInputDialog();
                        }
                    }
                }).show();
    }


    private void showNumberInputDialog() {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.dcheck_item_price_input_dialog_title)
                .customView(R.layout.dialog_edit_dcheck, false)
                .positiveText(android.R.string.ok)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        if (mEtPriceInput.getText().length() > 0) {
                            try {
                                mTmpDCheckItem.setPrice(
                                        Double.parseDouble(mEtPriceInput.getText().toString()));
                                showMultiChoice();
                            } catch (NumberFormatException e) {
                                showNumberInputDialog();
                                Toast.makeText(DCheckEditActivity.this,
                                        R.string.dcheck_item_price_input_dialog_error,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }).build();

        mEtPriceInput = (EditText) dialog.getCustomView().findViewById(R.id.tv_price_input);

        dialog.show();
    }

    private void showMultiChoice() {
        if (mUsers != null && mUsers.size() > 0) {

            CharSequence[] tmp = new CharSequence[mUsers.size()];
            for (int i = 0; i < tmp.length; i++) {
                tmp[i] = mUsers.get(i).getFName() + " " + mUsers.get(i).getLName();
            }
            final CharSequence[] items = tmp;
            final ArrayList<Integer> selectedItems = new ArrayList<>();
            new MaterialDialog.Builder(this)
                    .title(R.string.dcheck_item_multichoise_dialog_title)
                    .items(items)
                    .itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMultiChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                            selectedItems.clear();
                            selectedItems.addAll(Arrays.asList(which));
                            return true; // allow selection
                        }
                    })
                    .alwaysCallMultiChoiceCallback()
                    .positiveText(R.string.dcheck_item_multichoise_dialog_ok_button)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            multiChoiceOkClick(selectedItems);
                        }
                    })
                    .show();
        }
    }

    private void multiChoiceOkClick(List<Integer> selectedItems) {
        if (selectedItems.size() > 0) {
            if (selectedItems.size() == 1) {
                if (!Utils.isMyUid(mUsers.get(selectedItems.get(0)).getObjectId())) {
                    addSelectedUsers(selectedItems);
                } else {
                    Toast.makeText(DCheckEditActivity.this,
                            R.string.dcheck_item_multichoise_dialog_error, Toast.LENGTH_SHORT)
                            .show();
                    showMultiChoice();
                }
            } else {
                addSelectedUsers(selectedItems);
                mLlDate.setVisibility(View.VISIBLE);
            }
        } else {
            showMultiChoice();
        }
    }

    private void addSelectedUsers(List<Integer> selectedItems) {
        ArrayList<String> users = new ArrayList<>();
        for (int i = 0; i < selectedItems.size(); i++) {
            users.add(this.mUsers.get(selectedItems.get(i)).getObjectId());
        }
        mTmpDCheckItem.setUsers(users);

        calcItemDebt(mTmpDCheckItem);
        mDCheckItemsNew.add(mTmpDCheckItem);

        if (mDCheckItemsNew.size() >= 1) {
            mLlDate.setVisibility(View.VISIBLE);
            Calendar c = Calendar.getInstance();
            DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.ENGLISH);
            String formattedDate = df.format(c.getTime());
            mTvDate.setText(formattedDate);
        }

        mAdapter.setData(mDCheckItemsNew);
    }

    private double calcTotalDebt(List<DCheckItem> DCheckItems) {
        double sum = 0;
        int size1 = DCheckItems.size();
        for (int i = 0; i < size1; i++) {
            DCheckItem DCheckItem = DCheckItems.get(i);
            sum += DCheckItem.getDebt();
        }
        return sum;
    }

    private void calcItemDebt(DCheckItem DCheckItem) {
        double sum = 0;
        int size = DCheckItem.getUsers().size();
        sum += (DCheckItem.getPrice() / size);
        DCheckItem.setDebt(sum);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_add_dcheck_item:
                mTmpDCheckItem = new DCheckItem();
                showInputDialog();
                break;
            case R.id.ll_date_container:
                datePickerDialog();
                break;
        }
    }

    public void removeItem(DCheckItem dCheckItem) {
        mDCheckItemsNew.remove(dCheckItem);
    }

    public void hideDate() {
        mLlDate.setVisibility(View.GONE);
    }

    @Override
    public Loader<ArrayList<DCheckItem>> onCreateLoader(int id, Bundle args) {
        Uri uri = Uri.parse(args.getString(Const.KEY_STRING_URI));
        String editId = args.getString(Const.KEY_EDIT_ID);
        HashMap<String, String> dChecksToEdit = (HashMap<String, String>)
                args.getSerializable(Const.KEY_DCHECKS_TO_EDIT);
        String selection = DebtaContract.DCheckEntry.COLUMN_EDIT_ID + " = ?";
        String[] selectionArgs = {editId};

        return new DCheckEditLoader(
                DCheckEditActivity.this,
                uri,
                null,
                selection,
                selectionArgs,
                null,
                dChecksToEdit
        );
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<DCheckItem>> loader, ArrayList<DCheckItem> data) {
        mProgressBar.setVisibility(View.GONE);
        mAdapter = new DCheckItemsListAdapter(
                DCheckEditActivity.this, R.layout.item_dcheckitem, data, true);
        mLvDCheckItems.setAdapter(mAdapter);
        mDCheckItemsNew.addAll(data);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<DCheckItem>> loader) {
        mAdapter.setData(null);
    }

    private class UserLoaderCallback implements LoaderManager.LoaderCallbacks<ArrayList<User>> {
        @Override
        public Loader<ArrayList<User>> onCreateLoader(int id, Bundle args) {
            String sUri = args.getString(Const.KEY_STRING_URI);
            Uri uri = sUri == null ? null : Uri.parse(sUri);

            ArrayList<String> uIds = args.getStringArrayList(Const.KEY_USER_IDS);
            String[] selectionArgs = uIds.toArray(new String[uIds.size()]);

            String selection = DebtaContract.UserEntry.COLUMN_OBJECT_ID;
            if (selectionArgs.length > 0) {
                selection += " IN (" + Utils.makePlaceholders(selectionArgs.length) + ")";
            } else {
                selection = null;
                selectionArgs = null;
            }

            return new UserLoader(
                    DCheckEditActivity.this,
                    uri,
                    null,
                    selection,
                    selectionArgs,
                    null);
        }

        @Override
        public void onLoadFinished(Loader<ArrayList<User>> loader, ArrayList<User> data) {
            mUsers.clear();
            mUsers.addAll(data);
        }

        @Override
        public void onLoaderReset(Loader<ArrayList<User>> loader) {

        }
    }
}
