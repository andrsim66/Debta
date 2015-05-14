package com.sevenander.debta.app;

import android.app.Application;
import android.content.Context;

import com.parse.Parse;
import com.parse.ParseObject;
import com.sevenander.debta.app.pojo.DCheck;
import com.sevenander.debta.app.pojo.DCheckItem;
import com.sevenander.debta.app.pojo.DGroup;
import com.sevenander.debta.app.pojo.User;

/**
 * Created by andrii on 02.03.15.
 */
public class App extends Application {
    public static final String TAG = App.class.getSimpleName();

    private static App app;
    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        this.setAppContext(getApplicationContext());
        ParseObject.registerSubclass(User.class);
        ParseObject.registerSubclass(DGroup.class);
        ParseObject.registerSubclass(DCheck.class);
        ParseObject.registerSubclass(DCheckItem.class);
        Parse.initialize(this, getResources().getString(R.string.parse_application_id),
                getResources().getString(R.string.parse_client_id));
        
    }

    public static App getInstance() {
        return app;
    }

    public static Context getAppContext() {
        return appContext;
    }

    public void setAppContext(Context mAppContext) {
        this.appContext = mAppContext;
    }

}
