package com.sevenander.debta.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.sevenander.debta.app.utils.Utils;
import com.sevenander.debta.app.pojo.User;

/**
 * Created by andrii on 02.03.15.
 */
public class DispatchActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String token = Utils.restoreLoginState(DispatchActivity.this);

        if (token != null) {
            User.becomeInBackground(token, new LogInCallback() {
                public void done(ParseUser user, ParseException e) {
                    if (user != null) {
                        // The current user is now set to user.
                        Intent intent = new Intent(DispatchActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else {
                        // The token could not be validated.
                        Intent intent = new Intent(DispatchActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                    finish();
                }
            });
        } else {
            Intent intent = new Intent(DispatchActivity.this, LoginActivity.class);
            startActivity(intent);
            // close this activity
            finish();
        }
    }
}
