package com.dostchat.dost.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import com.dostchat.dost.R;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.app.DostChatApp;
import com.dostchat.dost.models.users.contacts.ContactsModel;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Abderrahim El imame on 10/17/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class PermissionHandler {


    /**
     * method to check for permissions
     *
     * @param activity   this is the first parameter for checkPermission  method
     * @param permission this is the second parameter for checkPermission  method
     * @return return value
     */
    public static boolean checkPermission(Activity activity, String permission) {
        if (AppHelper.isAndroid6()) {
            int result = ContextCompat.checkSelfPermission(activity, permission);
            return result == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    /**
     * method to request permissions
     *
     * @param mActivity  this is the first parameter for requestPermission  method
     * @param permission this is the second parameter for requestPermission  method
     */
    public static void requestPermission(Activity mActivity, String permission) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permission)) {
            String title = null;
            String Message = null;
            switch (permission) {
                case Manifest.permission.CAMERA:
                    title = mActivity.getString(R.string.camera_permission);
                    Message = mActivity.getString(R.string.camera_permission_message);
                    break;
                case Manifest.permission.RECORD_AUDIO:
                    title = mActivity.getString(R.string.audio_permission);
                    Message = mActivity.getString(R.string.record_audio_permission_message);
                    break;

                case Manifest.permission.MODIFY_AUDIO_SETTINGS:
                    title = mActivity.getString(R.string.camera_permission);
                    Message = mActivity.getString(R.string.settings_audio_permission_message);
                    break;
                case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                    title = mActivity.getString(R.string.storage_permission);
                    Message = mActivity.getString(R.string.write_storage_permission_message);
                    break;
                case Manifest.permission.READ_EXTERNAL_STORAGE:
                    title = mActivity.getString(R.string.storage_permission);
                    Message = mActivity.getString(R.string.read_storage_permission_message);
                    break;
                case Manifest.permission.READ_CONTACTS:
                    title = mActivity.getString(R.string.contacts_permission);
                    Message = mActivity.getString(R.string.read_contacts_permission_message);
                    break;
                case Manifest.permission.WRITE_CONTACTS:
                    title = mActivity.getString(R.string.contacts_permission);
                    Message = mActivity.getString(R.string.write_contacts_permission_message);
                    break;

                case Manifest.permission.RECEIVE_SMS:
                    title = mActivity.getString(R.string.receive_sms_permission);
                    Message = mActivity.getString(R.string.receive_sms_permission_message);
                    break;

                case Manifest.permission.READ_SMS:
                    title = mActivity.getString(R.string.read_sms_permission);
                    Message = mActivity.getString(R.string.read_sms_permission_message);
                    break;
                case Manifest.permission.CALL_PHONE:
                    title = mActivity.getString(R.string.call_phone_permission);
                    Message = mActivity.getString(R.string.call_phone_permission_message);
                    break;
                case Manifest.permission.GET_ACCOUNTS:
                    title = mActivity.getString(R.string.get_accounts_permission);
                    Message = mActivity.getString(R.string.get_accounts_permission_message);
                    break;

            }

            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle(title);
            builder.setMessage(Message);
            builder.setPositiveButton(mActivity.getString(R.string.yes), (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", mActivity.getPackageName(), null);
                intent.setData(uri);
                if (permission.equals(Manifest.permission.READ_CONTACTS)) {
                    mActivity.startActivityForResult(intent, AppConstants.CONTACTS_PERMISSION_REQUEST_CODE);
                } else {
                    mActivity.startActivityForResult(intent, AppConstants.PERMISSION_REQUEST_CODE);
                }
            });
            builder.setNegativeButton(R.string.no_thanks, (dialog, which) -> {
                if (permission.equals(Manifest.permission.READ_CONTACTS)) {
                    Realm realm = DostChatApp.getRealmDatabaseInstance();
                    realm.executeTransactionAsync(realm1 -> {
                        RealmResults<ContactsModel> contactsModel = realm1.where(ContactsModel.class).findAll();
                        if (contactsModel.size() != 0) {
                            contactsModel.deleteAllFromRealm();
                        }
                    }, dialog::dismiss, AppHelper::LogCat);
                } else {
                    dialog.dismiss();
                }
            });
            builder.show();
        } else {
            if (permission.equals(Manifest.permission.READ_CONTACTS)) {
                ActivityCompat.requestPermissions(mActivity, new String[]{permission}, AppConstants.CONTACTS_PERMISSION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(mActivity, new String[]{permission}, AppConstants.PERMISSION_REQUEST_CODE);
            }
        }
    }
}
