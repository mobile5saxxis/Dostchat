package com.dostchat.dost.helpers.call;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;

import com.dostchat.dost.R;
import com.dostchat.dost.activities.call.CallActivity;
import com.dostchat.dost.activities.call.CallAlertActivity;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.app.DostChatApp;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.PermissionHandler;
import com.dostchat.dost.helpers.PreferenceManager;
import com.dostchat.dost.models.users.contacts.ContactsModel;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;
import io.socket.client.Ack;
import io.socket.client.Socket;

/**
 * Created by Abderrahim El imame on 12/21/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class CallManager {


    /**
     * method to call a user
     */
    public static void callContact(Activity mActivity, boolean isNeedFinish, boolean isVideoCall, int userID) {
        if (isVideoCall) {
            if (PermissionHandler.checkPermission(mActivity, Manifest.permission.CAMERA)) {

            } else {
                AppHelper.LogCat("Please request camera  permission.");
                PermissionHandler.requestPermission(mActivity, Manifest.permission.CAMERA);
            }

            if (PermissionHandler.checkPermission(mActivity, Manifest.permission.RECORD_AUDIO)) {

            } else {
                AppHelper.LogCat("Please request Record audio permission.");
                PermissionHandler.requestPermission(mActivity, Manifest.permission.RECORD_AUDIO);
                return;
            }
        } else {

            if (PermissionHandler.checkPermission(mActivity, Manifest.permission.RECORD_AUDIO)) {


            } else {
                AppHelper.LogCat("Please request Record audio permission.");
                PermissionHandler.requestPermission(mActivity, Manifest.permission.RECORD_AUDIO);
                return;
            }
        }


        if (!isNetworkAvailable(mActivity)) {
            AlertDialog.Builder alert = new AlertDialog.Builder(mActivity);
            alert.setMessage(mActivity.getString(R.string.you_couldnt_call_this_user_network));
            alert.setPositiveButton(R.string.ok, (dialog, which) -> {
            });
            alert.setCancelable(false);
            alert.show();
        } else {
            DostChatApp app = (DostChatApp) mActivity.getApplication();
            Socket mSocket;
            mSocket = app.getSocket();
            if (mSocket == null) {
                DostChatApp.connectSocket();
                mSocket = app.getSocket();
            }
            if (mSocket != null) {
                if (!mSocket.connected())
                    mSocket.connect();
                if (mSocket.connected()) {
                    JSONObject data = new JSONObject();
                    try {
                        data.put("recipientId", userID);
                        data.put("senderId", PreferenceManager.getID(mActivity));
                        AppHelper.LogCat("socket not null");
                        mSocket.emit(AppConstants.SOCKET_CALL_USER_PING, data, (Ack) argObjects -> {
                            JSONObject dataString = (JSONObject) argObjects[0];
                            try {
                                boolean connected = dataString.getBoolean("connected");
                                String recipientId = dataString.getString("recipientId");
                                if (connected) {
                                    AppHelper.LogCat("User  connected and ready to call him connecteddd  " + recipientId);
                                    makeCall(isNeedFinish, mActivity, recipientId, isVideoCall, userID);
                                } else {
                                    AppHelper.LogCat("User  not connected and not ready to call him mess 2" + recipientId);
                                    Intent mIntent = new Intent(mActivity, CallAlertActivity.class);
                                    mActivity.startActivity(mIntent);
                                    mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                }
                            } catch (JSONException e) {
                                Intent mIntent = new Intent(mActivity, CallAlertActivity.class);
                                mActivity.startActivity(mIntent);
                                mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            }
                        });
                    } catch (Exception e) {

                        Intent mIntent = new Intent(mActivity, CallAlertActivity.class);
                        mActivity.startActivity(mIntent);
                        mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                } else {

                    Intent mIntent = new Intent(mActivity, CallAlertActivity.class);
                    mActivity.startActivity(mIntent);
                    mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            } else {
                Intent mIntent = new Intent(mActivity, CallAlertActivity.class);
                mActivity.startActivity(mIntent);
                mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }

        }
    }


    private static void makeCall(boolean isNeedFinish, Activity mActivity, String callerSocketId, boolean isVideoCall, int userID) {
        if (callerSocketId.isEmpty() || callerSocketId.equals(PreferenceManager.getSocketID(mActivity))) {
            return;
        }
        Realm realm = DostChatApp.getRealmDatabaseInstance();
        ContactsModel contactsModelCaller = realm.where(ContactsModel.class).equalTo("id", PreferenceManager.getID(mActivity)).findFirst();
        ContactsModel contactsModel = realm.where(ContactsModel.class).equalTo("id", userID).findFirst();
        String recipientPhone = contactsModel.getPhone();
        String callerImage = contactsModel.getImage();
        String userImage = contactsModelCaller.getImage();
        Intent intent = new Intent(mActivity, CallActivity.class);
        intent.putExtra(AppConstants.USER_SOCKET_ID, PreferenceManager.getSocketID(mActivity));
        intent.putExtra(AppConstants.USER_PHONE, PreferenceManager.getPhone(mActivity));
        intent.putExtra(AppConstants.CALLER_SOCKET_ID, callerSocketId);
        intent.putExtra(AppConstants.CALLER_PHONE, recipientPhone);
        intent.putExtra(AppConstants.CALLER_IMAGE, callerImage);
        intent.putExtra(AppConstants.USER_IMAGE, userImage);
        intent.putExtra(AppConstants.IS_ACCEPTED_CALL, false);
        intent.putExtra(AppConstants.IS_VIDEO_CALL, isVideoCall);
        intent.putExtra(AppConstants.CALLER_ID, userID);
        mActivity.startActivity(intent);
        if (isNeedFinish)
            mActivity.finish();
        mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        realm.close();
    }

    private static boolean isNetworkAvailable(Context mContext) {
        ConnectivityManager cm = (ConnectivityManager) mContext.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
