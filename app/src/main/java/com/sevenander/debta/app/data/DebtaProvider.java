package com.sevenander.debta.app.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by andrii on 28.03.15.
 */
public class DebtaProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private DebtaDbHelper mOpenHelper;

    static final int DCHECK_ITEM = 100;
    static final int DCHECK_ITEMS = 101;
    static final int DCHECK = 200;
    static final int DCHECKS = 201;
    static final int DGROUP = 300;
    static final int DGROUPS = 301;
    static final int USER = 400;
    static final int USERS = 401;

    @Override
    public boolean onCreate() {
        mOpenHelper = new DebtaDbHelper(getContext());
        return true;
    }

    private Cursor getDGroups(String selection, String[] selectionArgs) {
        return mOpenHelper.getReadableDatabase().query(
                DebtaContract.DGroupEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
    }

    private Cursor getUsers(String selection, String[] selectionArgs) {
        return mOpenHelper.getReadableDatabase().query(
                DebtaContract.UserEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
    }

    private Cursor getCheques(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return mOpenHelper.getReadableDatabase().query(
                DebtaContract.DCheckEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getChequeItems(String selection, String[] selectionArgs) {
        return mOpenHelper.getReadableDatabase().query(
                DebtaContract.DCheckItemEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case DGROUPS: {
                retCursor = getDGroups(selection, selectionArgs);
                break;
            }
            case USERS: {
                retCursor = getUsers(selection, selectionArgs);
                break;
            }
            case DCHECKS: {
                retCursor = getCheques(projection, selection, selectionArgs, sortOrder);
                break;
            }
            case DCHECK_ITEMS:
                retCursor = getChequeItems(selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);
        switch (match) {
            // Student: Uncomment and fill out these two cases
            case DCHECK_ITEMS:
                return DebtaContract.DCheckItemEntry.CONTENT_TYPE;
            case DCHECK:
                return DebtaContract.DCheckEntry.CONTENT_TYPE;
            case DCHECKS:
                return DebtaContract.DCheckEntry.CONTENT_TYPE;
            case DGROUP:
                return DebtaContract.DGroupEntry.CONTENT_TYPE;
            case DGROUPS:
                return DebtaContract.DGroupEntry.CONTENT_TYPE;
            case USERS:
                return DebtaContract.UserEntry.CONTENT_TYPE;
            case USER:
                return DebtaContract.UserEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case DCHECK: {
                normalizeDate(values);
                long _id = db.insertWithOnConflict(DebtaContract.DCheckEntry.TABLE_NAME,
                        null, values, SQLiteDatabase.CONFLICT_REPLACE);
                if (_id > 0)
                    returnUri = DebtaContract.DCheckEntry.buildDCheckUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case DCHECK_ITEM: {
                long _id = db.insertWithOnConflict(DebtaContract.DCheckItemEntry.TABLE_NAME,
                        null, values, SQLiteDatabase.CONFLICT_REPLACE);
                if (_id > 0)
                    returnUri = DebtaContract.DCheckItemEntry.buildDCheckItemUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case DGROUP: {
                long _id = db.insertWithOnConflict(DebtaContract.DGroupEntry.TABLE_NAME,
                        null, values, SQLiteDatabase.CONFLICT_REPLACE);
                if (_id > 0)
                    returnUri = DebtaContract.DGroupEntry.buildDGroupUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case USER: {
                long _id = db.insertWithOnConflict(DebtaContract.UserEntry.TABLE_NAME,
                        null, values, SQLiteDatabase.CONFLICT_REPLACE);
                if (_id > 0)
                    returnUri = DebtaContract.UserEntry.buildUserUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if (null == selection) selection = "1";
        switch (match) {
            case DCHECK:
                rowsDeleted = db.delete(
                        DebtaContract.DCheckEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case DCHECK_ITEM:
                rowsDeleted = db.delete(
                        DebtaContract.DCheckItemEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case DGROUP:
                rowsDeleted = db.delete(
                        DebtaContract.DGroupEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case USER:
                rowsDeleted = db.delete(
                        DebtaContract.UserEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case DCHECK:
                normalizeDate(values);
                rowsUpdated = db.update(DebtaContract.DCheckEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case DCHECK_ITEM:
                rowsUpdated = db.update(DebtaContract.DCheckItemEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case DGROUP:
                rowsUpdated = db.update(DebtaContract.DGroupEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case USER:
                rowsUpdated = db.update(DebtaContract.UserEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;
        switch (match) {
            case DCHECK:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        normalizeDate(value);
                        long _id = db.insertWithOnConflict(DebtaContract.DCheckEntry.TABLE_NAME,
                                null, value, SQLiteDatabase.CONFLICT_REPLACE);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case DCHECK_ITEM:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insertWithOnConflict(DebtaContract.DCheckItemEntry.TABLE_NAME,
                                null, value, SQLiteDatabase.CONFLICT_REPLACE);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case DGROUP:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insertWithOnConflict(DebtaContract.DGroupEntry.TABLE_NAME,
                                null, value, SQLiteDatabase.CONFLICT_REPLACE);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case USER:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insertWithOnConflict(DebtaContract.UserEntry.TABLE_NAME,
                                null, value, SQLiteDatabase.CONFLICT_REPLACE);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DebtaContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, DebtaContract.PATH_DCHECK_ITEM, DCHECK_ITEM);
        matcher.addURI(authority, DebtaContract.PATH_DCHECK_ITEM + "/*", DCHECK_ITEMS);
        matcher.addURI(authority, DebtaContract.PATH_DCHECK, DCHECK);
        matcher.addURI(authority, DebtaContract.PATH_DCHECK + "/*", DCHECKS);
        matcher.addURI(authority, DebtaContract.PATH_DGROUP, DGROUP);
        matcher.addURI(authority, DebtaContract.PATH_DGROUP + "/*", DGROUPS);
        matcher.addURI(authority, DebtaContract.PATH_USER, USER);
        matcher.addURI(authority, DebtaContract.PATH_USER + "/*", USERS);

        return matcher;
    }

    private void normalizeDate(ContentValues values) {
        if (values.containsKey(DebtaContract.DCheckEntry.COLUMN_DATE)) {
            long dateValue = values.getAsLong(DebtaContract.DCheckEntry.COLUMN_DATE);
            values.put(DebtaContract.DCheckEntry.COLUMN_DATE, DebtaContract.normalizeDate(dateValue));
        }
    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
