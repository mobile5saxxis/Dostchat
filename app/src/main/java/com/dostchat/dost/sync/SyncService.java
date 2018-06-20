package com.dostchat.dost.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.services.BootService;
import com.dostchat.dost.services.MainService;

/**
 * Created by Abderrahim El imame on 01/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class SyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private SyncAdapter mSyncAdapter = null;

    public SyncService() {
        super();
    }

    @Override
    public void onCreate() {
        AppHelper.LogCat("Sync Service created.");
        if (!AppHelper.isServiceRunning(this, MainService.class))
            startService(new Intent(this, BootService.class));
        synchronized (sSyncAdapterLock) {
            if (mSyncAdapter == null) {
                mSyncAdapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        AppHelper.LogCat("Sync Service binded.");
        return mSyncAdapter.getSyncAdapterBinder();

    }
}