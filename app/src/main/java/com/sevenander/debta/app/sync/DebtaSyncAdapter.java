package com.sevenander.debta.app.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.sevenander.debta.app.R;
import com.sevenander.debta.app.activities.MainActivity;
import com.sevenander.debta.app.data.DebtaContract;
import com.sevenander.debta.app.pojo.DCheck;
import com.sevenander.debta.app.pojo.DCheckItem;
import com.sevenander.debta.app.pojo.DGroup;
import com.sevenander.debta.app.pojo.User;
import com.sevenander.debta.app.utils.Const;
import com.sevenander.debta.app.utils.Logger;
import com.sevenander.debta.app.utils.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by andrii on 28.03.15.
 */
public class DebtaSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = DebtaSyncAdapter.class.getSimpleName();
    // Interval at which to sync with the weather, in seconds.
    // 60 seconds (1 minute) * 30 = 30 mins
    public static final int SYNC_INTERVAL = 60 * 30;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    public static final int NOTIFICATION_ID = 1;

    private int newChequesCount;

    private Set<String> downloadedDGroupIds;
    private Set<String> downloadedUserIds;
    private Set<String> downloadedDCheckIds;
    private Set<String> downloadedDCheckItemIds;

    public DebtaSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Logger.d("Start sync");

        downloadedDGroupIds = new HashSet<>();
        downloadedUserIds = new HashSet<>();
        downloadedDCheckIds = new HashSet<>();
        downloadedDCheckItemIds = new HashSet<>();

        loadDGroups();
        loadDGroupsM();
        deleteRedundantData();

        if (newChequesCount > 0)
            notifyNewDChecks(newChequesCount);

        Utils.saveLastSyncTime(getContext());

        Logger.d("Finish sync");
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        DebtaSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount,
                context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    private void loadDGroups() {
        List<DGroup> dGroups = new ArrayList<>();
        String uid = User.getCurrentUser().getObjectId();
        ParseQuery<DGroup> query = ParseQuery.getQuery(DebtaContract.DGroupEntry.TABLE_NAME);
        query.whereEqualTo(DebtaContract.DGroupEntry.COLUMN_MEMBERS, uid);
        try {
            dGroups = query.find();

            Utils.saveToDB(getContext(), Utils.setDGroupContentValues(dGroups),
                    DebtaContract.DGroupEntry.CONTENT_URI);

            downloadedDGroupIds.addAll(Utils.getDGroupIds(dGroups));
        } catch (ParseException e) {
            Logger.e(e.getMessage(), e);
        }

        if (dGroups != null) {
            newChequesCount = 0;
            for (int i = 0; i < dGroups.size(); i++) {
                loadDChecks(dGroups.get(i).getObjectId(), uid, true);
            }
        }
    }


    private void loadDGroupsM() {
        List<DGroup> dGroups = new ArrayList<>();
        String uid = User.getCurrentUser().getObjectId();

        ParseQuery<DGroup> query = ParseQuery.getQuery(DebtaContract.DGroupEntry.TABLE_NAME);
        query.whereMatches(DebtaContract.DGroupEntry.COLUMN_ADMIN_ID, uid);

        try {
            dGroups = query.find();

            Utils.saveToDB(getContext(), Utils.setDGroupContentValues(dGroups),
                    DebtaContract.DGroupEntry.CONTENT_URI);

            downloadedDGroupIds.addAll(Utils.getDGroupIds(dGroups));

        } catch (ParseException e) {
            Logger.e(e.getMessage(), e);
        }

        if (dGroups != null) {
            for (int i = 0; i < dGroups.size(); i++) {
                ArrayList<String> uids = new ArrayList<>(dGroups.get(i).getMembers());
                if (i == 0)
                    uids.add(User.getCurrentUser().getObjectId());
                loadUsers(dGroups.get(i).getObjectId(), uids);
            }
        }

    }

    private void loadUsers(final String groupId, List<String> userIds) {
        List<User> users = new ArrayList<>();
        ParseQuery<User> query = ParseQuery.getQuery("_" + DebtaContract.UserEntry.TABLE_NAME);
        query.whereContainedIn(DebtaContract.UserEntry.COLUMN_OBJECT_ID, userIds);

        try {
            users = query.find();
            Utils.saveToDB(getContext(), Utils.setUserContentValues(users),
                    DebtaContract.UserEntry.CONTENT_URI);

            downloadedUserIds.addAll(userIds);
        } catch (ParseException e) {
            Logger.e(e.getMessage(), e);
        }

        if (users != null) {
            for (int i = 0; i < users.size(); i++) {
                loadDChecks(groupId, users.get(i).getObjectId(), false);
            }
        }
    }


    private void loadDChecks(final String groupId, final String userId, boolean calcCheques) {
        List<DCheck> dChecks = new ArrayList<>();

        ParseQuery<DCheck> query = ParseQuery.getQuery(DebtaContract.DCheckEntry.TABLE_NAME);
        query.whereEqualTo(DebtaContract.DCheckEntry.COLUMN_IS_PAID, false);
        query.whereEqualTo(DebtaContract.DCheckEntry.COLUMN_GROUP_ID, groupId);
        query.whereEqualTo(DebtaContract.DCheckEntry.COLUMN_USER_ID, userId);

        try {
            dChecks = query.find();

            Utils.saveToDB(getContext(), Utils.setDCheckContentValues(dChecks),
                    DebtaContract.DCheckEntry.CONTENT_URI);

            downloadedDCheckIds.addAll(Utils.getDCheckIds(dChecks));

            for (int i = 0; i < dChecks.size(); i++) {
                if (calcCheques && !dChecks.get(i).isConfirmed())
                    newChequesCount++;
            }

        } catch (ParseException e) {
            Logger.e(e.getMessage(), e);
        }

        if (dChecks != null) {
            for (int i = 0; i < dChecks.size(); i++) {
                loadDCheckItems(dChecks.get(i).getItems());
            }
        }
    }


    private void loadDCheckItems(List<String> itemIds) {
        List<DCheckItem> dCheckItems;
        ParseQuery<DCheckItem> query = ParseQuery.getQuery(DebtaContract.DCheckItemEntry.TABLE_NAME);
        query.whereContainedIn(DebtaContract.DCheckItemEntry.COLUMN_OBJECT_ID, itemIds);
        try {
            dCheckItems = query.find();

            Utils.saveToDB(getContext(), Utils.setDCheckItemContentValues(dCheckItems),
                    DebtaContract.DCheckItemEntry.CONTENT_URI);

            downloadedDCheckItemIds.addAll(Utils.getDCheckItemIds(dCheckItems));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void deleteRedundantData() {
        String selection = null;
        String[] selectionArgs = null;
        if (downloadedDGroupIds.size() > 0) {
            selectionArgs = downloadedDGroupIds.toArray(new String[downloadedDGroupIds.size()]);
            selection = DebtaContract.DGroupEntry.COLUMN_OBJECT_ID +
                    " NOT IN (" + Utils.makePlaceholders(selectionArgs.length) + ")";
        }
        getContext().getContentResolver().delete(DebtaContract.DGroupEntry.CONTENT_URI,
                selection, selectionArgs);


        selection = null;
        selectionArgs = null;
        if (downloadedUserIds.size() > 0) {
            selectionArgs = downloadedUserIds.toArray(new String[downloadedUserIds.size()]);
            selection = DebtaContract.UserEntry.COLUMN_OBJECT_ID +
                    " NOT IN (" + Utils.makePlaceholders(selectionArgs.length) + ")";
        }
        getContext().getContentResolver().delete(DebtaContract.UserEntry.CONTENT_URI,
                selection, selectionArgs);

        selection = null;
        selectionArgs = null;
        if (downloadedDCheckIds.size() > 0) {
            selectionArgs = downloadedDCheckIds.toArray(new String[downloadedDCheckIds.size()]);
            selection = DebtaContract.DCheckEntry.COLUMN_OBJECT_ID +
                    " NOT IN (" + Utils.makePlaceholders(selectionArgs.length) + ")";
        }
        getContext().getContentResolver().delete(DebtaContract.DCheckEntry.CONTENT_URI,
                selection, selectionArgs);

        selection = null;
        selectionArgs = null;
        if (downloadedDCheckItemIds.size() > 0) {
            selectionArgs = downloadedDCheckItemIds.toArray(
                    new String[downloadedDCheckItemIds.size()]);
            selection = DebtaContract.DCheckItemEntry.COLUMN_OBJECT_ID +
                    " NOT IN (" + Utils.makePlaceholders(selectionArgs.length) + ")";
        }
        getContext().getContentResolver().delete(DebtaContract.DCheckItemEntry.CONTENT_URI,
                selection, selectionArgs);
    }

    private void notifyNewDChecks(int count) {
        Context context = getContext();
        //checking the last update and notify if it' the first of the day
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//        String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);
//        boolean displayNotifications = prefs.getBoolean(displayNotificationsKey,
//                Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));

//        if (displayNotifications) {

        Resources resources = context.getResources();
        Bitmap largeIcon = BitmapFactory.decodeResource(resources,
                R.mipmap.ic_launcher);
        String title = context.getString(R.string.app_name);

        // Define the text of the forecast.
        String contentText = "You have " + count + " unseen checks";

        // NotificationCompatBuilder is a very convenient way to build backward-compatible
        // notifications.  Just throw in some data.
        long[] vibrate = {0, 100, 200, 300};
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getContext())
                        .setColor(resources.getColor(R.color.colorPrimary))
                        .setSmallIcon(R.drawable.ic_account_balance_wallet_white_24dp)
                        .setLargeIcon(largeIcon)
                        .setVibrate(vibrate)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setContentTitle(title)
                        .setContentText(contentText);

        // Make something interesting happen when the user clicks on the notification.
        // In this case, opening the app is sufficient.
        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.putExtra(Const.KEY_ACTION_CLEAR, true);
        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        // NOTIFICATION_ID allows you to update the notification later on.
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

//        }
    }
}
