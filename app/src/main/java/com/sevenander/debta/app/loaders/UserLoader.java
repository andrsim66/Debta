package com.sevenander.debta.app.loaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.sevenander.debta.app.data.DebtaContract;
import com.sevenander.debta.app.pojo.DCheck;
import com.sevenander.debta.app.pojo.User;
import com.sevenander.debta.app.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by andrii on 02.04.15.
 */
public class UserLoader extends AsyncTaskLoader<ArrayList<User>> {

    private Uri mUri;
    private String[] mProjection;
    private String mSelection;
    private String[] mSelectionArgs;
    private String mSortOrder;

    private String mGroupId;

    private ArrayList<User> mUsers;
    private HashMap<String, Double> userDebtMap;

    public UserLoader(Context context, Uri uri, String[] projection, String selection,
                      String[] selectionArgs, String sortOrder, String groupId) {
        super(context);
        this.mUri = uri;
        this.mProjection = projection;
        this.mSelection = selection;
        this.mSelectionArgs = selectionArgs;
        this.mSortOrder = sortOrder;
        this.mGroupId = groupId;
        this.userDebtMap = new HashMap<>();
    }

    public UserLoader(Context context, Uri uri, String[] projection, String selection,
                      String[] selectionArgs, String sortOrder) {
        super(context);
        this.mUri = uri;
        this.mProjection = projection;
        this.mSelection = selection;
        this.mSelectionArgs = selectionArgs;
        this.mSortOrder = sortOrder;
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
        Cursor cursorUser = null;
        if (mUri != null)
            cursorUser = getContext().getContentResolver().query(mUri, mProjection, mSelection,
                    mSelectionArgs, mSortOrder);
        if ((cursorUser == null) ||
                (cursorUser != null && cursorUser.getCount() == 0)) {
            ParseQuery<User> query = ParseQuery.getQuery("_" + DebtaContract.UserEntry.TABLE_NAME);
            query.whereContainedIn(DebtaContract.UserEntry.COLUMN_OBJECT_ID,
                    Arrays.asList(mSelectionArgs));
            try {
                users = new ArrayList<>(query.find());

                Utils.saveToDB(getContext(), Utils.setUserContentValues(users),
                        DebtaContract.UserEntry.CONTENT_URI);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            users = Utils.getUserFromCursor(cursorUser);
        }

        if (users != null && mGroupId != null) {
            for (int i = 0; i < users.size(); i++) {
                loadCheques(users.get(i).getObjectId());
            }
        }
        return users;
    }


    private void loadCheques(String userId) {
        Uri uri = DebtaContract.DCheckEntry.buildDCheckUri();

        String[] projection = new String[]{DebtaContract.DCheckEntry.COLUMN_DEBT};

        String selection = DebtaContract.DCheckEntry.COLUMN_IS_PAID + " = ? AND " +
                DebtaContract.DCheckEntry.COLUMN_GROUP_ID + " = ? AND " +
                DebtaContract.DCheckEntry.COLUMN_USER_ID + " = ?";

        String[] selectionArgs = new String[3];
        selectionArgs[0] = "0";
        selectionArgs[1] = mGroupId;
        selectionArgs[2] = userId;

        Cursor cursorCheque = null;
        if (mUri != null)
            cursorCheque = getContext().getContentResolver().query(
                    uri, projection, selection, selectionArgs, null);

        double userDebtSum = 0;

        if ((cursorCheque == null) ||
                (cursorCheque != null && cursorCheque.getCount() == 0)) {
            List<DCheck> cheques;
            try {
                ParseQuery<DCheck> query = ParseQuery.getQuery(DebtaContract.DCheckEntry.TABLE_NAME);
                query.whereEqualTo(DebtaContract.DCheckEntry.COLUMN_IS_PAID, false);
                query.whereEqualTo(DebtaContract.DCheckEntry.COLUMN_GROUP_ID, mGroupId);
                query.whereEqualTo(DebtaContract.DCheckEntry.COLUMN_USER_ID, userId);
                cheques = query.find();
                for (int i = 0; i < cheques.size(); i++) {
                    userDebtSum += cheques.get(i).getDebt();
                }
                Utils.saveToDB(getContext(), Utils.setDCheckContentValues(cheques),
                        DebtaContract.DCheckEntry.CONTENT_URI);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            cursorCheque.moveToFirst();
            do {
                userDebtSum += cursorCheque.getDouble(0);
            } while (cursorCheque.moveToNext());
            cursorCheque.close();
        }

        userDebtMap.put(userId, userDebtSum);
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

        if (mGroupId != null)
            for (int i = 0; i < users.size(); i++) {
                users.get(i).setDebt(userDebtMap.get(users.get(i).getObjectId()));
            }

        mUsers = new ArrayList<>(users);

        if (isStarted()) {
            super.deliverResult(users);
        }
    }
}
