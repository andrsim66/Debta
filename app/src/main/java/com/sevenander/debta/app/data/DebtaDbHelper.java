package com.sevenander.debta.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by andrii on 26.03.15.
 */
public class DebtaDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "debta.db";

    public DebtaDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_USER_TABLE = "CREATE TABLE " +
                DebtaContract.UserEntry.TABLE_NAME + " (" +
                DebtaContract.UserEntry._ID + " INTEGER PRIMARY KEY," +
                DebtaContract.UserEntry.COLUMN_OBJECT_ID + " TEXT UNIQUE NOT NULL, " +
                DebtaContract.UserEntry.COLUMN_USERNAME + " TEXT NOT NULL, " +
                DebtaContract.UserEntry.COLUMN_FNAME + " TEXT NOT NULL, " +
                DebtaContract.UserEntry.COLUMN_LNAME + " TEXT NOT NULL, " +
                " UNIQUE (" + DebtaContract.UserEntry.COLUMN_OBJECT_ID +
                ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_CHEQUE_ITEM_TABLE = "CREATE TABLE " +
                DebtaContract.DCheckItemEntry.TABLE_NAME + " (" +
                DebtaContract.DCheckItemEntry._ID + " INTEGER PRIMARY KEY," +
                DebtaContract.DCheckItemEntry.COLUMN_OBJECT_ID + " TEXT UNIQUE NOT NULL, " +
                DebtaContract.DCheckItemEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                DebtaContract.DCheckItemEntry.COLUMN_PRICE + " REAL NOT NULL, " +
                DebtaContract.DCheckItemEntry.COLUMN_DEBT + " REAL NOT NULL, " +
                DebtaContract.DCheckItemEntry.COLUMN_USERS + " TEXT NOT NULL, " +
                " UNIQUE (" + DebtaContract.DCheckItemEntry.COLUMN_OBJECT_ID +
                ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_CHEQUE_TABLE = "CREATE TABLE " +
                DebtaContract.DCheckEntry.TABLE_NAME + " (" +
                DebtaContract.DCheckEntry._ID + " INTEGER PRIMARY KEY," +
                DebtaContract.DCheckEntry.COLUMN_OBJECT_ID + " TEXT UNIQUE NOT NULL, " +
                DebtaContract.DCheckEntry.COLUMN_GROUP_ID + " TEXT NOT NULL, " +
                DebtaContract.DCheckEntry.COLUMN_DATE + " INTEGER NOT NULL, " +
                DebtaContract.DCheckEntry.COLUMN_DEBT + " REAL NOT NULL, " +
                DebtaContract.DCheckEntry.COLUMN_USER_ID + " TEXT NOT NULL, " +
                DebtaContract.DCheckEntry.COLUMN_ITEMS + " TEXT NOT NULL, " +
                DebtaContract.DCheckEntry.COLUMN_USERS + " TEXT NOT NULL, " +
                DebtaContract.DCheckEntry.COLUMN_IS_PAID + " INTEGER NOT NULL, " +
                DebtaContract.DCheckEntry.COLUMN_IS_CONFIRMED + " INTEGER NOT NULL, " +
                DebtaContract.DCheckEntry.COLUMN_EDIT_ID + " TEXT NOT NULL, " +
                " UNIQUE (" + DebtaContract.DCheckEntry.COLUMN_OBJECT_ID +
                ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_DGROUP_TABLE = "CREATE TABLE " +
                DebtaContract.DGroupEntry.TABLE_NAME + " (" +
                DebtaContract.DGroupEntry._ID + " INTEGER PRIMARY KEY," +
                DebtaContract.DGroupEntry.COLUMN_OBJECT_ID + " TEXT UNIQUE NOT NULL, " +
                DebtaContract.DGroupEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                DebtaContract.DGroupEntry.COLUMN_ADMIN_ID + " TEXT NOT NULL, " +
                DebtaContract.DGroupEntry.COLUMN_MEMBERS + " TEXT NOT NULL, " +
                " UNIQUE (" + DebtaContract.DGroupEntry.COLUMN_OBJECT_ID +
                ") ON CONFLICT REPLACE);";

        db.execSQL(SQL_CREATE_USER_TABLE);
        db.execSQL(SQL_CREATE_CHEQUE_ITEM_TABLE);
        db.execSQL(SQL_CREATE_CHEQUE_TABLE);
        db.execSQL(SQL_CREATE_DGROUP_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DebtaContract.UserEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DebtaContract.DCheckItemEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DebtaContract.DCheckEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DebtaContract.DGroupEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

}
