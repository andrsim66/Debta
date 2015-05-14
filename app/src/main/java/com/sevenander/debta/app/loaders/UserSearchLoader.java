package com.sevenander.debta.app.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.sevenander.debta.app.data.DebtaContract;
import com.sevenander.debta.app.pojo.User;

import java.util.ArrayList;

/**
 * Created by andrii on 11.04.15.
 */
public class UserSearchLoader extends AsyncTaskLoader<ArrayList<User>> {

    private ArrayList<User> mUsers;
    private String mCurFilter;

    public UserSearchLoader(Context context, String curFilter) {
        super(context);
        this.mCurFilter = curFilter;
    }

    @Override
    protected void onStartLoading() {
        if (mUsers != null) {
            deliverResult(mUsers);
        }

        if (takeContentChanged() || mUsers == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public void onCanceled(ArrayList<User> users) {
        super.onCanceled(users);
    }

    @Override
    public ArrayList<User> loadInBackground() {
        ArrayList<User> users = new ArrayList<>();
        if (mCurFilter != null) {
            ParseQuery<User> query = ParseQuery.getQuery("_" + DebtaContract.UserEntry.TABLE_NAME);
            query.whereMatches(DebtaContract.UserEntry.COLUMN_FNAME, mCurFilter, "i");
            query.whereNotEqualTo(DebtaContract.UserEntry.COLUMN_OBJECT_ID,
                    User.getCurrentUser().getObjectId());

            try {
                users = new ArrayList<>(query.find());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return users;
    }

    @Override
    protected void onReset() {
        onStopLoading();

        if (mUsers != null)
            mUsers = null;
    }

    @Override
    public void deliverResult(ArrayList<User> users) {
        if (isReset()) {
            return;
        }

        mUsers = new ArrayList<>(users);

        if (isStarted()) {
            super.deliverResult(users);
        }
    }
}
