package com.sevenander.debta.app.loaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.sevenander.debta.app.data.DebtaContract;
import com.sevenander.debta.app.pojo.DCheck;
import com.sevenander.debta.app.pojo.DGroup;
import com.sevenander.debta.app.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by andrii on 30.03.15.
 */
public class DGroupLoader extends AsyncTaskLoader<ArrayList<DGroup>> {

    private Uri mUri;
    private String[] mProjection;
    private String mSelection;
    private String[] mSelectionArgs;
    private String mSortOrder;

    private int mTabPos;

    private ArrayList<DGroup> mDGroups;

    private HashMap<String, Double> mGroupDebtMap;
    private int mCountTotalDebtFinish;
    private int mDGroupsCount;

    public DGroupLoader(Context context, Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder, int tabPos) {
        super(context);
        this.mUri = uri;
        this.mProjection = projection;
        this.mSelection = selection;
        this.mSelectionArgs = selectionArgs;
        this.mSortOrder = sortOrder;
        this.mTabPos = tabPos;
        this.mGroupDebtMap = new HashMap<>();
    }

    @Override
    protected void onStartLoading() {
        if (mDGroups != null) {
            deliverResult(mDGroups);
        }

        if (takeContentChanged() || mDGroups == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public void onCanceled(ArrayList<DGroup> dGroups) {
        super.onCanceled(dGroups);
    }

    @Override
    public ArrayList<DGroup> loadInBackground() {
        ArrayList<DGroup> dGroups = new ArrayList<>();

        Cursor cursorDGroup = null;
        if (mUri != null)
            cursorDGroup = getContext().getContentResolver().query(mUri, mProjection, mSelection,
                    mSelectionArgs, mSortOrder);

        if ((cursorDGroup == null) ||
                (cursorDGroup != null && cursorDGroup.getCount() == 0)) {
            ParseQuery<DGroup> query = ParseQuery.getQuery(DebtaContract.DGroupEntry.TABLE_NAME);
            if (mTabPos == 0) {
                query.whereEqualTo(DebtaContract.DGroupEntry.COLUMN_MEMBERS,
                        mSelectionArgs[0].replace("*", ""));
            } else {
                query.whereMatches(DebtaContract.DGroupEntry.COLUMN_ADMIN_ID, mSelectionArgs[0]);
            }
            try {
                dGroups = new ArrayList<>(query.find());

                Utils.saveToDB(getContext(), Utils.setDGroupContentValues(dGroups),
                        DebtaContract.DGroupEntry.CONTENT_URI);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            dGroups = Utils.getDGroupsFromCursor(cursorDGroup);
        }

        if (dGroups != null) {
            mDGroupsCount = dGroups.size();
            for (int i = 0; i < dGroups.size(); i++) {
                Map<String, Double> userDebtMap = new HashMap<>();
                double[] totalGroupDebt = new double[1];
                int[] countGroupDebtFinish = new int[1];
                if (mTabPos == 0) {
                    loadCheques(dGroups.get(i).getObjectId(), mSelectionArgs[0].replace("*", ""),
                            1, userDebtMap, totalGroupDebt, countGroupDebtFinish);
                } else {
                    ArrayList<String> users = new ArrayList<>(dGroups.get(i).getMembers());
                    for (int j = 0; j < users.size(); j++) {
                        loadCheques(dGroups.get(i).getObjectId(), users.get(j),
                                users.size(), userDebtMap, totalGroupDebt, countGroupDebtFinish);
                    }
                }
            }
        }
        return dGroups;
    }

    private void loadCheques(String groupId, String userId, int usersListSize,
                             Map<String, Double> userDebtMap, double[] totalGroupDebt,
                             int[] countGroupDebtFinish) {
        Uri uri = DebtaContract.DCheckEntry.buildDCheckUri();

        String[] projection = new String[]{DebtaContract.DCheckEntry.COLUMN_DEBT};

        String selection = DebtaContract.DCheckEntry.COLUMN_IS_PAID + " = ? AND " +
                DebtaContract.DCheckEntry.COLUMN_GROUP_ID + " = ? AND " +
                DebtaContract.DCheckEntry.COLUMN_USER_ID + " = ?";

        String[] selectionArgs = {"0", groupId, userId};

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
                query.whereEqualTo(DebtaContract.DCheckEntry.COLUMN_GROUP_ID, groupId);
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
        totalGroupDebt[0] += userDebtSum;

        if (countGroupDebtFinish[0] == usersListSize - 1) {
            ArrayList<Double> userDebtValues = new ArrayList<>(userDebtMap.values());
            double groupDebtSum = 0;
            for (int i = 0; i < userDebtValues.size(); i++) {
                groupDebtSum += userDebtValues.get(i);
            }
            mGroupDebtMap.put(groupId, groupDebtSum);
            if (mCountTotalDebtFinish < mDGroupsCount - 1) {
                mCountTotalDebtFinish++;
            }
        } else if (countGroupDebtFinish[0] < usersListSize - 1) {
            countGroupDebtFinish[0]++;
        }
    }

    @Override
    protected void onReset() {
        onStopLoading();

        if (mDGroups != null)
            mDGroups = null;
    }

    @Override
    public void deliverResult(ArrayList<DGroup> dGroups) {
        if (isReset()) {
            return;
        }

        for (int i = 0; i < dGroups.size(); i++) {
            dGroups.get(i).setDebt(mGroupDebtMap.get(dGroups.get(i).getObjectId()));
        }

        mDGroups = new ArrayList<>(dGroups);

        if (isStarted()) {
            super.deliverResult(dGroups);
        }
    }
}
