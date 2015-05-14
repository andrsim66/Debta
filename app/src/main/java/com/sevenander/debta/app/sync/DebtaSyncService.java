package com.sevenander.debta.app.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by andrii on 28.03.15.
 */
public class DebtaSyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static DebtaSyncAdapter sDebtaSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sDebtaSyncAdapter == null) {
                sDebtaSyncAdapter = new DebtaSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sDebtaSyncAdapter.getSyncAdapterBinder();
    }
}
