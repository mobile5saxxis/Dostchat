package com.dostchat.dost.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.dostchat.dost.helpers.PreferenceManager;

/**
 * Created by Abderrahim El imame on 6/25/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class BootService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        new Handler().postDelayed(() -> {
            if (PreferenceManager.getToken(BootService.this) != null) {
                startService(new Intent(BootService.this, MainService.class));
            }
        }, 1000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // service On start
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        // service finished
        super.onDestroy();
    }
}
