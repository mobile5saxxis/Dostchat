package com.dostchat.dost.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v7.app.AlertDialog;

import com.dostchat.dost.R;
import com.dostchat.dost.activities.main.WelcomeActivity;
import com.dostchat.dost.api.APIHelper;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.ForegroundRuning;
import com.dostchat.dost.helpers.PreferenceManager;
import com.dostchat.dost.interfaces.NetworkListener;
import com.dostchat.dost.services.MainService;

/**
 * Created by Abderrahim El imame on 8/18/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class NetworkChangeListener extends BroadcastReceiver {

    private boolean is_Connected = false;
    public static NetworkListener networkListener;

    public NetworkChangeListener() {
        super();
    }

    @Override
    public void onReceive(Context mContext, Intent intent) {
        if (PreferenceManager.getToken(mContext) != null) {
            APIHelper.initialApiUsersContacts().checkIfUserSession().subscribe(networkModel -> {
                if (networkModel.isConnected()) {
                    is_Connected = true;
                } else {
                    is_Connected = false;
                    if (ForegroundRuning.get().isForeground()) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
                        alert.setMessage(R.string.your_session_expired);
                        alert.setPositiveButton(R.string.ok, (dialog, which) -> {
                            PreferenceManager.setToken(mContext, null);
                            PreferenceManager.setID(mContext, 0);
                            PreferenceManager.setSocketID(mContext, null);
                            PreferenceManager.setPhone(mContext, null);
                            PreferenceManager.setIsWaitingForSms(mContext, false);
                            PreferenceManager.setMobileNumber(mContext, null);
                            Intent mIntent = new Intent(mContext.getApplicationContext(), WelcomeActivity.class);
                            mIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.getApplicationContext().startActivity(mIntent);
                        });
                        alert.setCancelable(false);
                        alert.show();
                    }
                }
            }, throwable -> {
                AppHelper.LogCat("checkIfUserSession NetworkListener " + throwable.getMessage());
                is_Connected = false;
            });

            new Handler().postDelayed(() -> isNetworkAvailable(mContext), 2000);
        }
    }

    private void isNetworkAvailable(Context mContext) {
        ConnectivityManager cm = (ConnectivityManager) mContext.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean is_Connecting = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (networkListener != null) {
            networkListener.onNetworkConnectionChanged(is_Connecting, is_Connected);
        }

        if (!is_Connecting && !is_Connected) {
            mContext.getApplicationContext().stopService(new Intent(mContext.getApplicationContext(), MainService.class));
            AppHelper.LogCat("Connection is not available");
        } else if (is_Connecting && is_Connected) {
            AppHelper.LogCat("Connection is available");
            AppHelper.restartService();
        } else {
            AppHelper.LogCat("Connection is available but waiting for network");
        }
    }
}
