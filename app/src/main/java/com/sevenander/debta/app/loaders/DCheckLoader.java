package com.sevenander.debta.app.loaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.sevenander.debta.app.data.DebtaContract;
import com.sevenander.debta.app.pojo.DCheck;
import com.sevenander.debta.app.utils.Utils;

import java.util.ArrayList;

/**
 * Created by andrii on 02.04.15.
 */
public class DCheckLoader extends AsyncTaskLoader<ArrayList<DCheck>> {

    private Uri mUri;
    private String[] mProjection;
    private String mSelection;
    private String[] mSelectionArgs;
    private String mSortOrder;

    private ArrayList<DCheck> mCheques;

    public DCheckLoader(Context context, Uri uri, String[] projection, String selection,
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
        if (mCheques != null) {
            deliverResult(mCheques);
        }

        if (takeContentChanged() || mCheques == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public void onCanceled(ArrayList<DCheck> cheques) {
        super.onCanceled(cheques);
    }

    @Override
    public ArrayList<DCheck> loadInBackground() {
        ArrayList<DCheck> cheques = new ArrayList<>();
        Cursor cursorCheques = null;
        if (mUri != null)
            cursorCheques = getContext().getContentResolver().query(mUri, mProjection, mSelection,
                    mSelectionArgs, mSortOrder);

        if ((cursorCheques == null) ||
                (cursorCheques != null && cursorCheques.getCount() == 0)) {
            ParseQuery<DCheck> query = ParseQuery.getQuery(DebtaContract.DCheckEntry.TABLE_NAME);
            query.whereEqualTo(DebtaContract.DCheckEntry.COLUMN_IS_PAID,
                    mSelectionArgs[0].equals("0") ? false : true);
            query.whereEqualTo(DebtaContract.DCheckEntry.COLUMN_GROUP_ID, mSelectionArgs[1]);
            query.whereEqualTo(DebtaContract.DCheckEntry.COLUMN_USER_ID, mSelectionArgs[2]);
            query.addDescendingOrder(DebtaContract.DCheckEntry.COLUMN_DATE);
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

        if (mCheques != null)
            mCheques = null;
    }

    @Override
    public void deliverResult(ArrayList<DCheck> cheques) {
        if (isReset()) {
            return;
        }

        mCheques = new ArrayList<>(cheques);

        if (isStarted()) {
            super.deliverResult(cheques);
        }
    }
}
