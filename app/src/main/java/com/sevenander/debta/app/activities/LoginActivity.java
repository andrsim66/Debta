package com.sevenander.debta.app.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.sevenander.debta.app.R;
import com.sevenander.debta.app.data.DebtaContract;
import com.sevenander.debta.app.pojo.User;
import com.sevenander.debta.app.utils.Const;
import com.sevenander.debta.app.utils.Logger;
import com.sevenander.debta.app.utils.Utils;


public class LoginActivity extends ActionBarActivity implements View.OnClickListener {

    private EditText mEtUsername;
    private EditText mEtPassword;
    private Button mBLogin;
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupViews();
    }

    private void initViews() {
        mEtUsername = (EditText) findViewById(R.id.et_username);
        mEtPassword = (EditText) findViewById(R.id.et_password);
        mBLogin = (Button) findViewById(R.id.b_login);
        mDialog = new ProgressDialog(LoginActivity.this);
    }

    private void setupViews() {
        mDialog.dismiss();
        mBLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (Utils.isNetworkAvailable(LoginActivity.this)) {
            login();
        } else {
            Toast.makeText(LoginActivity.this, R.string.network_not_available,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void login() {
        final String username = mEtUsername.getText().toString().trim();
        final String password = mEtPassword.getText().toString().trim();

        // Validate the log in data
        boolean validationError = false;
        StringBuilder validationErrorMessage = new StringBuilder(getString(R.string.error_intro));
        if (username.length() == 0) {
            validationError = true;
            validationErrorMessage.append(getString(R.string.error_blank_username));
        }
        if (password.length() == 0) {
            if (validationError) {
                validationErrorMessage.append(getString(R.string.error_join));
            }
            validationError = true;
            validationErrorMessage.append(getString(R.string.error_blank_password));
        }
        validationErrorMessage.append(getString(R.string.error_end));

        // If there is a validation error, display the error
        if (validationError) {
            Toast.makeText(LoginActivity.this, validationErrorMessage.toString(), Toast.LENGTH_LONG)
                    .show();
            return;
        }

        // Set up a progress dialog
        mDialog.setMessage(getResources().getString(R.string.progress_loading));
        mDialog.show();

        ParseQuery<User> query = ParseQuery.getQuery("_" + DebtaContract.UserEntry.TABLE_NAME);
        query.whereEqualTo(DebtaContract.UserEntry.COLUMN_USERNAME, username);
        query.getFirstInBackground(new GetCallback<User>() {
            public void done(User receivedUser, ParseException e) {
                if (e == null) {
                    Logger.d("exists");
                    mDialog.setMessage(getString(R.string.progress_login));
                    loginInBG(username, password);
                } else {
                    Logger.d("don't exists");
                    mDialog.setMessage(getString(R.string.progress_signup));
                    signUpBG(username, password);
                }
            }
        });
    }


    private void loginInBG(String username, String password) {
        User.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                mDialog.dismiss();
                if (e != null) {
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                } else {
                    Utils.saveLoginState(user.getSessionToken(), LoginActivity.this);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                    finish();
                }
            }
        });
    }

    private void signUpBG(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);

        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                mDialog.dismiss();
                if (e == null) {
                    Utils.saveLoginState(User.getCurrentUser().getSessionToken(), LoginActivity.this);
                    Intent intent = new Intent(LoginActivity.this, DGroupEditActivity.class);
                    intent.putExtra(Const.KEY_CALL_EXTRA, Const.FIRST_CALL);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}
