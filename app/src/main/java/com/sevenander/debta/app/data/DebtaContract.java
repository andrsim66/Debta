package com.sevenander.debta.app.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Created by andrii on 26.03.15.
 */
public class DebtaContract {

    public static final String CONTENT_AUTHORITY = "com.sevenander.debta.app";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_DCHECK = "DCheck";
    public static final String PATH_DCHECK_ITEM = "DCheckItem";
    public static final String PATH_DGROUP = "DGroup";
    public static final String PATH_USER = "User";

    public static final class DCheckEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_DCHECK).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DCHECK;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DCHECK;

        public static final String TABLE_NAME = "DCheck";

        public static final String COLUMN_OBJECT_ID = "objectId";
        public static final String COLUMN_GROUP_ID = "groupId";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_DEBT = "debt";
        public static final String COLUMN_USER_ID = "userId";
        public static final String COLUMN_ITEMS = "items";
        public static final String COLUMN_USERS = "confirmedUsers";
        public static final String COLUMN_IS_PAID = "isPaid";
        public static final String COLUMN_IS_CONFIRMED = "isConfirmed";
        public static final String COLUMN_EDIT_ID = "editId";

        public static Uri buildDCheckUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildDCheckUri() {
            return CONTENT_URI.buildUpon().path("/" + TABLE_NAME + "/*").build();
        }
    }

    public static final class DCheckItemEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_DCHECK_ITEM).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DCHECK_ITEM;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DCHECK_ITEM;

        public static final String TABLE_NAME = "DCheckItem";

        public static final String COLUMN_OBJECT_ID = "objectId";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_DEBT = "debt";
        public static final String COLUMN_USERS = "users";

        public static Uri buildDCheckItemUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildDCheckItemUri() {
            return CONTENT_URI.buildUpon().path("/" + TABLE_NAME + "/*").build();
        }
    }

    public static final class DGroupEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_DGROUP).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DGROUP;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DGROUP;

        public static final String TABLE_NAME = "DGroup";

        public static final String COLUMN_OBJECT_ID = "objectId";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_ADMIN_ID = "adminId";
        public static final String COLUMN_MEMBERS = "members";

        public static Uri buildDGroupUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildDGroupUri() {
            return CONTENT_URI.buildUpon().path("/" + TABLE_NAME + "/*").build();
        }
    }

    public static final class UserEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_USER).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_USER;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_USER;

        public static final String TABLE_NAME = "User";

        public static final String COLUMN_OBJECT_ID = "objectId";
        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_FNAME = "fName";
        public static final String COLUMN_LNAME = "lName";

        public static Uri buildUserUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildUserUri() {
            return CONTENT_URI.buildUpon().path("/" + TABLE_NAME + "/*").build();
        }
    }

    public static long normalizeDate(long startDate) {
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }
}
