package com.dostchat.dost.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dostchat.dost.services.BootService;


/**
 * Created by Abderrahim El imame on 12/8/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        String iAction = intent.getAction();
        if (iAction.equals("android.intent.action.BOOT_COMPLETED")) {
            context.startService(new Intent(context, BootService.class));
        }
    }
}