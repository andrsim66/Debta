package com.sevenander.debta.app.loaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.sevenander.debta.app.data.DebtaContract;
import com.sevenander.debta.app.pojo.DCheck;
import com.sevenander.debta.app.pojo.DCheckItem;
import com.sevenander.debta.app.utils.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by andrii on 22.04.15.
 */
public class DCheckEditLoader extends AsyncTaskLoader<ArrayList<DCheckItem>> {

    private Uri mUri;
    private String[] mProjection;
    private String mSelection;
    private String[] mSelectionArgs;
    private String mSortOrder;

    private Map<String, String> mDCheckToEdit;
    private ArrayList<DCheckItem> mDCheckItems;

    public DCheckEditLoader(Context context, Uri uri, String[] projection, String selection,
                            String[] selectionArgs, String sortOrder,
                            Map<String, String> dCheckToEdit) {
        super(context);
        this.mUri = uri;
        this.mProjection = projection;
        this.mSelection = selection;
        this.mSelectionArgs = selectionArgs;
        this.mSortOrder = sortOrder;
        this.mDCheckToEdit = dCheckToEdit;
    }

    @Override
    protected void onStartLoading() {
        if (mDCheckItems != null) {
            deliverResult(mDCheckItems);
        }

        if (takeContentChanged() || mDCheckItems == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public void onCanceled(ArrayList<DCheckItem> DCheckItems) {
        super.onCanceled(DCheckItems);
    }

    @Override
    public ArrayList<DCheckItem> loadInBackground() {
        ArrayList<DCheck> dChecks = loadDChecks();

        Set<String> itemIds = new HashSet<>();
        for (int i = 0; i < dChecks.size(); i++) {
            itemIds.addAll(dChecks.get(i).getItems());
            mDCheckToEdit.put(dChecks.get(i).getUserId(), dChecks.get(i).getObjectId());
        }

        ArrayList<String> dCheckItemsIds = new ArrayList<>(itemIds);

        Uri uri = DebtaContract.DCheckItemEntry.buildDCheckItemUri();
        String selection = makePlaceholders(dCheckItemsIds.size());
        String[] selectionArgs = dCheckItemsIds.toArray(new String[dCheckItemsIds.size()]);

        ArrayList<DCheckItem> dCheckItems = new ArrayList<>();

        Cursor cursorDCheckItems = getContext().getContentResolver().query(uri, null, selection,
                selectionArgs, null);//todo sort by alphabet

        if ((cursorDCheckItems == null) ||
                (cursorDCheckItems != null && cursorDCheckItems.getCount() == 0)) {
            ParseQuery<DCheckItem> query = ParseQuery.getQuery(DebtaContract.DCheckItemEntry.TABLE_NAME);
            query.whereContainedIn(DebtaContract.DCheckItemEntry.COLUMN_OBJECT_ID, itemIds);
            try {
                dCheckItems = new ArrayList<>(query.find());

                Utils.saveToDB(getContext(), Utils.setDCheckItemContentValues(dCheckItems),
                        DebtaContract.DCheckItemEntry.CONTENT_URI);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            dCheckItems = Utils.getDCheckItemsFromCursor(cursorDCheckItems);
        }

        return dCheckItems;
    }

    private String makePlaceholders(int len) {
        StringBuilder sb = new StringBuilder();
        sb.append(DebtaContract.DCheckItemEntry.COLUMN_OBJECT_ID + " = ? ");
        for (int i = 1; i < len; i++) {
            sb.append("OR " + DebtaContract.DCheckItemEntry.COLUMN_OBJECT_ID + " = ? ");
        }
        return sb.toString();
    }

    private ArrayList<DCheck> loadDChecks() {
        ArrayList<DCheck> cheques = new ArrayList<>();
        Cursor cursorCheques = null;
        if (mUri != null)
            cursorCheques = getContext().getContentResolver().query(mUri, mProjection, mSelection,
                    mSelectionArgs, mSortOrder);

        if ((cursorCheques == null) ||
                (cursorCheques != null && cursorCheques.getCount() == 0)) {
            ParseQuery<DCheck> query = ParseQuery.getQuery(DebtaContract.DCheckEntry.TABLE_NAME);
            query.whereEqualTo(DebtaContract.DCheckEntry.COLUMN_EDIT_ID, mSelectionArgs[0]);
            try {
                cheques = new ArrayList<>(query.find());

                Utils.saveToDB(getContext(), Utils.setDCheckContentValues(cheques),
                        DebtaContract.DCheckEntry.CONTENT_URI);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            cheques = Utils.getDChecksFromCursor(cursorCheques);
        }

        return cheques;
    }

    @Override
    protected void onReset() {
        onStopLoading();

        if (mDCheckItems != null)
            mDCheckItems = null;
    }

    @Override
    public void deliverResult(ArrayList<DCheckItem> DCheckItems) {
        if (isReset()) {
            return;
        }

        mDCheckItems = new ArrayList<>(DCheckItems);

        if (isStarted()) {
            super.deliverResult(DCheckItems);
        }
    }
}
