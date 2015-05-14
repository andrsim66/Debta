package com.sevenander.debta.app.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Uri;

import com.sevenander.debta.app.data.DebtaContract;
import com.sevenander.debta.app.pojo.DCheck;
import com.sevenander.debta.app.pojo.DCheckItem;
import com.sevenander.debta.app.pojo.DGroup;
import com.sevenander.debta.app.pojo.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

/**
 * Created by andrii on 02.03.15.
 */
public class Utils {

    public static void saveLoginState(String sessionToken, Context con) {
        Logger.d("Saving login state");
        SharedPreferences prefs = con.getSharedPreferences("loginStatePrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("sessionToken", sessionToken);
        editor.commit();
        Logger.d("Session token saved " + sessionToken);
    }

    public static String restoreLoginState(Context con) {
        Logger.d("Restoring login state");
        SharedPreferences prefs = con.getSharedPreferences("loginStatePrefs", Context.MODE_PRIVATE);
        String sessionToken = prefs.getString("sessionToken", null);
        Logger.d("Session token restored " + sessionToken);
        return sessionToken;
    }

    public static void saveLastSyncTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("lastSyncPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("lastSync", System.currentTimeMillis());
        editor.commit();
    }

    public static long getLastSyncTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("lastSyncPrefs", Context.MODE_PRIVATE);
        return prefs.getLong("lastSync", 0);
    }

    public static Typeface getRobotoMediumTypeface(Context context) {
        Typeface typeface = Typeface.createFromAsset(context.getAssets()
                , "fonts/Roboto-Medium.ttf");
        return typeface;
    }

    public static void saveNotificationSettings(Context context, boolean state) {
        SharedPreferences prefs = context.getSharedPreferences("notifPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("notif", state);
        editor.commit();
    }

    public static boolean getNotificationSettings(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("notifPrefs", Context.MODE_PRIVATE);
        return prefs.getBoolean("notif", true);
    }

    public static void clearSharedPreferences(Context context) {
        SharedPreferences loginStatePrefs = context.getSharedPreferences("loginStatePrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor loginStatePrefsEditor = loginStatePrefs.edit();
        loginStatePrefsEditor.clear();
        loginStatePrefsEditor.commit();

        SharedPreferences notifPrefs = context.getSharedPreferences("notifPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor notifPrefsEditor = notifPrefs.edit();
        notifPrefsEditor.clear();
        notifPrefsEditor.commit();

        SharedPreferences syncPrefs = context.getSharedPreferences("lastSyncPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor syncPrefsEditor = syncPrefs.edit();
        syncPrefsEditor.clear();
        syncPrefsEditor.commit();
    }

    public static String formatMoney(double sum) {
        DecimalFormat df = new DecimalFormat("0.00");
        return "$ " + df.format(sum);
    }

    public static String formatDate(Date date) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.ENGLISH);
        return df.format(date);
    }

    public static ArrayList<String> getDGroupIds(List<DGroup> list) {
        ArrayList<String> result = new ArrayList<>();
        for (DGroup dGroup : list)
            result.add(dGroup.getObjectId());
        return result;
    }

    public static List<String> getDCheckIds(List<DCheck> dChecks) {
        List<String> result = new ArrayList<>();
        for (DCheck dCheck : dChecks)
            result.add(dCheck.getObjectId());
        return result;
    }

    public static List<String> getDCheckItemIds(List<DCheckItem> dCheckItems) {
        List<String> result = new ArrayList<>();
        for (DCheckItem dCheckItem : dCheckItems)
            result.add(dCheckItem.getObjectId());
        return result;
    }

    public static List<String> getUserIds(List<User> list) {
        List<String> result = new ArrayList<>();
        for (User user : list)
            result.add(user.getObjectId());
        return result;
    }

    public static boolean isNetworkAvailable(Context context) {
        return ((ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null;
    }

    public static boolean isMyUid(String uid) {
        return uid.equals(User.getCurrentUser().getObjectId());
    }

    public static void saveToDB(Context context, Vector<ContentValues> cvVector, Uri uri) {
        if (cvVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cvVector.size()];
            cvVector.toArray(cvArray);
            context.getContentResolver().bulkInsert(uri, cvArray);
        }
    }

    public static ArrayList<DGroup> getDGroupsFromCursor(Cursor cursor) {
        ArrayList<DGroup> dGroups = new ArrayList<>();
        cursor.moveToFirst();
        do {
            DGroup dGroup = new DGroup();
            dGroup.setObjectId(cursor.getString(Const.COL_DGROUP_OBJ_ID));
            dGroup.setName(cursor.getString(Const.COL_DGROUP_NAME));
            dGroup.setAdminId(cursor.getString(Const.COL_DGROUP_ADMIN_ID));
            try {
                JSONObject json = new JSONObject(
                        cursor.getString(Const.COL_DGROUP_MEMBERS));
                JSONArray jsonMembers = json.getJSONArray(
                        DebtaContract.DGroupEntry.COLUMN_MEMBERS);
                ArrayList<String> members = new ArrayList<>();
                for (int j = 0; j < jsonMembers.length(); j++) {
                    members.add(jsonMembers.get(j).toString());
                }
                dGroup.setMembers(members);
            } catch (JSONException e) {
                dGroup.setMembers(new ArrayList<String>());
                e.printStackTrace();
            }
            dGroups.add(dGroup);
        } while (cursor.moveToNext());
        return dGroups;
    }

    public static ArrayList<DCheck> getDChecksFromCursor(Cursor cursor) {
        ArrayList<DCheck> dChecks = new ArrayList<>();
        cursor.moveToFirst();
        do {
            DCheck dCheck = new DCheck();
            dCheck.setObjectId(cursor.getString(Const.COL_DCHECK_OBJ_ID));
            dCheck.setGroupId(cursor.getString(Const.COL_DCHECK_GROUP_ID));
            dCheck.setDate(new Date(
                    cursor.getLong(Const.COL_DCHECK_DATE)));
            dCheck.setDebt(cursor.getDouble(Const.COL_DCHECK_DEBT));
            dCheck.setUserId(cursor.getString(Const.COL_DCHECK_USER_ID));
            dCheck.setEditId(cursor.getString(Const.COL_DCHECK_EDIT_ID));
            dCheck.setPaid(
                    cursor.getInt(Const.COL_DCHECK_IS_PAID) == 1 ? true : false);
            dCheck.setConfirmed(
                    cursor.getInt(Const.COL_DCHECK_IS_CONFIRMED) == 1 ? true : false);

            try {
                JSONObject json = new JSONObject(
                        cursor.getString(Const.COL_DCHECK_ITEMS));
                JSONArray jsonItems = json.getJSONArray(
                        DebtaContract.DCheckEntry.COLUMN_ITEMS);
                ArrayList<String> items = new ArrayList<>();
                for (int j = 0; j < jsonItems.length(); j++) {
                    items.add(jsonItems.get(j).toString());
                }
                dCheck.setItems(items);
            } catch (JSONException e) {
                dCheck.setItems(new ArrayList<String>());
                e.printStackTrace();
            }

            try {
                JSONObject json = new JSONObject(
                        cursor.getString(Const.COL_DCHECK_USERS));
                JSONArray jsonUsers = json.getJSONArray(
                        DebtaContract.DCheckEntry.COLUMN_USERS);
                ArrayList<String> users = new ArrayList<>();
                for (int j = 0; j < jsonUsers.length(); j++) {
                    users.add(jsonUsers.get(j).toString());
                }
                dCheck.setConfirmedUsers(users);
            } catch (JSONException e) {
                dCheck.setConfirmedUsers(new ArrayList<String>());
                e.printStackTrace();
            }
            dChecks.add(dCheck);
        } while (cursor.moveToNext());
        return dChecks;
    }

    public static ArrayList<DCheckItem> getDCheckItemsFromCursor(Cursor cursor) {
        ArrayList<DCheckItem> dCheckItems = new ArrayList<>();
        cursor.moveToFirst();
        do {
            DCheckItem dCheckItem = new DCheckItem();
            dCheckItem.setObjectId(cursor.getString(Const.COL_DCHECK_ITEM_OBJ_ID));
            dCheckItem.setName(cursor.getString(Const.COL_DCHECK_ITEM_NAME));
            dCheckItem.setDebt(cursor.getDouble(Const.COL_DCHECK_ITEM_DEBT));
            dCheckItem.setPrice(cursor.getDouble(Const.COL_DCHECK_ITEM_PRICE));

            try {
                JSONObject json = new JSONObject(
                        cursor.getString(Const.COL_DCHECK_ITEM_USERS));
                JSONArray jsonUsers = json.getJSONArray(
                        DebtaContract.DCheckItemEntry.COLUMN_USERS);
                ArrayList<String> users = new ArrayList<>();
                for (int j = 0; j < jsonUsers.length(); j++) {
                    users.add(jsonUsers.get(j).toString());
                }
                dCheckItem.setUsers(users);
            } catch (JSONException e) {
                dCheckItem.setUsers(new ArrayList<String>());
                e.printStackTrace();
            }
            dCheckItems.add(dCheckItem);
        } while (cursor.moveToNext());
        return dCheckItems;
    }

    public static ArrayList<User> getUserFromCursor(Cursor cursor) {
        ArrayList<User> users = new ArrayList<>();
        cursor.moveToFirst();
        do {
            User user = new User();
            user.setObjectId(cursor.getString(Const.COL_USER_OBJ_ID));
            user.setUsername(cursor.getString(Const.COL_USER_USERNAME));
            user.setFName(cursor.getString(Const.COL_USER_FNAME));
            user.setLName(cursor.getString(Const.COL_USER_LNAME));
            users.add(user);
        } while (cursor.moveToNext());
        return users;
    }

    public static Vector<ContentValues> setDCheckContentValues(List<DCheck> dChecks) {
        Vector<ContentValues> cvVector = new Vector<>(dChecks.size());

        for (int i = 0; i < dChecks.size(); i++) {
            DCheck dCheck = dChecks.get(i);
            ContentValues dCheckValues = new ContentValues();

            dCheckValues.put(DebtaContract.DCheckEntry.COLUMN_OBJECT_ID, dCheck.getObjectId());
            dCheckValues.put(DebtaContract.DCheckEntry.COLUMN_GROUP_ID, dCheck.getGroupId());
            dCheckValues.put(DebtaContract.DCheckEntry.COLUMN_DEBT, dCheck.getDebt());
            dCheckValues.put(DebtaContract.DCheckEntry.COLUMN_IS_PAID, dCheck.isPaid() ? 1 : 0);
            dCheckValues.put(DebtaContract.DCheckEntry.COLUMN_IS_CONFIRMED, dCheck.isConfirmed() ? 1 : 0);
            dCheckValues.put(DebtaContract.DCheckEntry.COLUMN_USER_ID, dCheck.getUserId());
            dCheckValues.put(DebtaContract.DCheckEntry.COLUMN_EDIT_ID, dCheck.getEditId());
            try {
                JSONObject items = new JSONObject();
                items.put(DebtaContract.DCheckEntry.COLUMN_ITEMS, new JSONArray(dCheck.getItems()));
                dCheckValues.put(DebtaContract.DCheckEntry.COLUMN_ITEMS, items.toString());
                JSONObject users = new JSONObject();
                users.put(DebtaContract.DCheckEntry.COLUMN_USERS, dCheck.getConfirmedUsers() == null ?
                        new JSONArray() : new JSONArray(dCheck.getConfirmedUsers()));
                dCheckValues.put(DebtaContract.DCheckEntry.COLUMN_USERS, users.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            dCheckValues.put(DebtaContract.DCheckEntry.COLUMN_DATE, dCheck.getDate().getTime());

            cvVector.add(dCheckValues);
        }
        return cvVector;
    }

    public static Vector<ContentValues> setDCheckItemContentValues(List<DCheckItem> dCheckItems) {
        Vector<ContentValues> cvVector = new Vector<>(dCheckItems.size());

        for (int i = 0; i < dCheckItems.size(); i++) {
            DCheckItem dCheckItem = dCheckItems.get(i);
            ContentValues checkItemValues = new ContentValues();

            checkItemValues.put(DebtaContract.DCheckItemEntry.COLUMN_OBJECT_ID, dCheckItem.getObjectId());
            checkItemValues.put(DebtaContract.DCheckItemEntry.COLUMN_NAME, dCheckItem.getName());
            checkItemValues.put(DebtaContract.DCheckItemEntry.COLUMN_DEBT, dCheckItem.getDebt());
            checkItemValues.put(DebtaContract.DCheckItemEntry.COLUMN_PRICE, dCheckItem.getPrice());
            try {
                JSONObject users = new JSONObject();
                users.put(DebtaContract.DCheckItemEntry.COLUMN_USERS, dCheckItem.getUsers() == null ?
                        new JSONArray() : new JSONArray(dCheckItem.getUsers()));
                checkItemValues.put(DebtaContract.DCheckItemEntry.COLUMN_USERS, users.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            cvVector.add(checkItemValues);
        }
        return cvVector;
    }

    public static Vector<ContentValues> setDGroupContentValues(List<DGroup> dGroups) {
        Vector<ContentValues> cvVector = new Vector<>(dGroups.size());

        for (int i = 0; i < dGroups.size(); i++) {
            DGroup dGroup = dGroups.get(i);
            ContentValues dgroupValues = new ContentValues();

            dgroupValues.put(DebtaContract.DGroupEntry.COLUMN_OBJECT_ID, dGroup.getObjectId());
            dgroupValues.put(DebtaContract.DGroupEntry.COLUMN_NAME, dGroup.getName());
            dgroupValues.put(DebtaContract.DGroupEntry.COLUMN_ADMIN_ID, dGroup.getAdminId());
            JSONObject members = new JSONObject();
            try {
                members.put(DebtaContract.DGroupEntry.COLUMN_MEMBERS, new JSONArray(dGroup.getMembers()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            dgroupValues.put(DebtaContract.DGroupEntry.COLUMN_MEMBERS, members.toString());
            cvVector.add(dgroupValues);
        }
        return cvVector;
    }

    public static ContentValues setDGroupContentValues(DGroup dGroup) {
        ContentValues dgroupValues = new ContentValues();

        dgroupValues.put(DebtaContract.DGroupEntry.COLUMN_OBJECT_ID, dGroup.getObjectId());
        dgroupValues.put(DebtaContract.DGroupEntry.COLUMN_NAME, dGroup.getName());
        dgroupValues.put(DebtaContract.DGroupEntry.COLUMN_ADMIN_ID, dGroup.getAdminId());
        JSONObject members = new JSONObject();
        try {
            members.put(DebtaContract.DGroupEntry.COLUMN_MEMBERS, new JSONArray(dGroup.getMembers()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        dgroupValues.put(DebtaContract.DGroupEntry.COLUMN_MEMBERS, members.toString());
        return dgroupValues;
    }

    public static Vector<ContentValues> setUserContentValues(List<User> users) {
        Vector<ContentValues> cvVector = new Vector<>(users.size());

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            ContentValues userValues = new ContentValues();

            userValues.put(DebtaContract.UserEntry.COLUMN_OBJECT_ID, user.getObjectId());
            userValues.put(DebtaContract.UserEntry.COLUMN_USERNAME, user.getUsername());
            userValues.put(DebtaContract.UserEntry.COLUMN_FNAME, user.getFName());
            userValues.put(DebtaContract.UserEntry.COLUMN_LNAME, user.getLName());
            cvVector.add(userValues);
        }
        return cvVector;
    }

    public static void outSelection(String title, Cursor c) {
        if (c != null) {
            Logger.d("-------" + title + ". " + c.getCount() + " rows");
            if (c.moveToFirst()) {
                StringBuilder sb = new StringBuilder();
                do {
                    sb.setLength(0);
                    for (String cn : c.getColumnNames()) {
                        if (c.getType(c.getColumnIndex(cn)) == Cursor.FIELD_TYPE_STRING) {
                            sb.append(cn + " = " + c.getString(c.getColumnIndex(cn)) + "; ");
                        } else if (c.getType(c.getColumnIndex(cn)) == Cursor.FIELD_TYPE_INTEGER) {
                            sb.append(cn + " = " + c.getInt(c.getColumnIndex(cn)) + "; ");
                        }
                    }
                    Logger.d(sb.toString());
                } while (c.moveToNext());
                Logger.d("------------end of table-------");
            }
        } else
            Logger.d("DG" + ". Cursor is null");
    }

    public static String makePlaceholders(int len) {
        StringBuilder sb = new StringBuilder(len * 2 - 1);
        sb.append("?");
        for (int i = 1; i < len; i++) {
            sb.append(",?");
        }
        return sb.toString();
    }

}
