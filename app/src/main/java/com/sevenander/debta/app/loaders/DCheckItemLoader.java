package com.sevenander.debta.app.loaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.sevenander.debta.app.data.DebtaContract;
import com.sevenander.debta.app.pojo.DCheckItem;
import com.sevenander.debta.app.utils.Utils;

import java.util.ArrayList;

/**
 * Created by andrii on 14.04.15.
 */
public class DCheckItemLoader extends AsyncTaskLoader<ArrayList<DCheckItem>> {

    private Uri mUri;
    private String[] mProjection;
    private String mSelection;
    private String[] mSelectionArgs;
    private String mSortOrder;

    private ArrayList<DCheckItem> mDCheckItems;

    public DCheckItemLoader(Context context, Uri uri, String[] projection, String selection,
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
        ArrayList<DCheckItem> dCheckItems = new ArrayList<>();

        Cursor cursorDCheckItems = getContext().getContentResolver().query(mUri, mProjection,
                mSelection, mSelectionArgs, mSortOrder);

        if ((cursorDCheckItems == null) ||
                (cursorDCheckItems != null && cursorDCheckItems.getCount() == 0)) {
            ArrayList<String> itemIds = new ArrayList<>();
            for (int i = 0; i < mSelectionArgs.length; i++) {
                itemIds.add(mSelectionArgs[i]);
            }

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
