package com.sevenander.debta.app.activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.sevenander.debta.app.R;
import com.sevenander.debta.app.data.DebtaContract;
import com.sevenander.debta.app.pojo.User;
import com.sevenander.debta.app.sync.DebtaSyncAdapter;
import com.sevenander.debta.app.utils.Utils;

public class SettingsActivity extends ActionBarActivity implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener {

    private Toolbar mToolbar;
    private View mViewShadow;
    private RelativeLayout mRlEditName;
    private RelativeLayout mRlEditUsername;
    private RelativeLayout mRlNotifications;
    private RelativeLayout mRlLogout;
    private TextView mTvName;
    private TextView mTvUsername;
    private CheckBox mCbNotifications;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setupToolbar();
        initViews();
        setupViews();
    }

    private void setupToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
    }

    private void initViews() {
        mViewShadow = findViewById(R.id.vShadow);
        mRlEditName = (RelativeLayout) findViewById(R.id.rl_edit_name);
        mRlEditUsername = (RelativeLayout) findViewById(R.id.rl_edit_username);
        mRlNotifications = (RelativeLayout) findViewById(R.id.rl_notifications);
        mRlLogout = (RelativeLayout) findViewById(R.id.rl_logout);
        mTvName = (TextView) findViewById(R.id.tv_edit_name_value);
        mTvUsername = (TextView) findViewById(R.id.tv_edit_username_value);
        mCbNotifications = (CheckBox) findViewById(R.id.cb_notifications);
    }

    private void setupViews() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            mViewShadow.setVisibility(View.GONE);
        }

        mTvName.setText(((User) User.getCurrentUser()).getFName() +
                " " + ((User) User.getCurrentUser()).getLName());
        mTvUsername.setText(User.getCurrentUser().getUsername());
        mCbNotifications.setChecked(Utils.getNotificationSettings(SettingsActivity.this));
        mCbNotifications.setOnCheckedChangeListener(this);
        mRlEditName.setOnClickListener(this);
        mRlEditUsername.setOnClickListener(this);
        mRlNotifications.setOnClickListener(this);
        mRlLogout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_edit_name:
                showInputNameDialog();
                break;
            case R.id.rl_edit_username:
                showInputUsernameDialog();
                break;
            case R.id.rl_notifications:
                mCbNotifications.setChecked(mCbNotifications.isChecked() ? false : true);
                break;
            case R.id.rl_logout:
                ContentResolver.cancelSync(DebtaSyncAdapter.getSyncAccount(SettingsActivity.this),
                        DebtaContract.CONTENT_AUTHORITY);
                User.logOut();
                Utils.clearSharedPreferences(SettingsActivity.this);
                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }

    private TextView tvFName;
    private TextView tvLName;

    private void showInputNameDialog() {
        final User currentUser = (User) User.getCurrentUser();
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.settings_name_input_dialog_title)
                .customView(R.layout.dialog_edit_name, false)
                .positiveText(android.R.string.ok)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        if (tvFName.getText().length() > 0
                                && tvLName.getText().length() > 0) {

                            currentUser.setFName(tvFName.getText().toString());
                            currentUser.setLName(tvLName.getText().toString());
                            currentUser.saveInBackground();

                            mTvName.setText(currentUser.getFName() + " " + currentUser.getLName());
                        } else {
                            dialog.dismiss();
                        }
                    }
                }).build();

        tvFName = (EditText) dialog.getCustomView().findViewById(R.id.tv_fname_input);
        tvLName = (EditText) dialog.getCustomView().findViewById(R.id.tv_lname_input);

        tvFName.setText(currentUser.getFName());
        tvLName.setText(currentUser.getLName());

        dialog.show();
    }

    private void showInputUsernameDialog() {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.settings_username_input_dialog_title)
                .input(R.string.input_dialog_username_hint, 0, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        if (input.length() > 0) {
                            User.getCurrentUser().setUsername(input.toString());
                            User.getCurrentUser().saveInBackground();
                            mTvUsername.setText(input.toString());
                        }
                    }
                }).build();

        dialog.getInputEditText().setText(User.getCurrentUser().getUsername());

        dialog.show();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Utils.saveNotificationSettings(SettingsActivity.this, isChecked);
    }
}
