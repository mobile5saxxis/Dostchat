package com.dostchat.dost.helpers.Files.backup;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import com.dostchat.dost.R;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.app.DostChatApp;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.Files.FilesManager;
import com.dostchat.dost.helpers.PreferenceManager;
import com.dostchat.dost.models.calls.CallsInfoModel;
import com.dostchat.dost.models.calls.CallsModel;
import com.dostchat.dost.models.messages.ConversationsModel;
import com.dostchat.dost.models.messages.MessagesModel;
import com.dostchat.dost.models.users.contacts.UsersBlockModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.Realm;

/**
 * Created by Abderrahim El imame on 10/31/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class RealmBackupRestore {

    private final static String TAG = RealmBackupRestore.class.getName();


    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static File backup(Activity mActivity) {
        Realm realm = DostChatApp.getRealmDatabaseInstance();
        // First check if we have storage permissions
        checkStoragePermissions(mActivity);
        File exportRealmFile;
        AppHelper.LogCat("Realm DB Path = " + realm.getPath());
        // create a backup file
        exportRealmFile = new File(FilesManager.getImagesCachePath(mActivity), AppConstants.EXPORT_REALM_FILE_NAME);
        // if backup file already exists, delete it
        exportRealmFile.delete();

        // copy current realm to backup file
        realm.writeCopyTo(exportRealmFile);

        String msg = "File exported to Path: " + FilesManager.getImagesCachePath(mActivity) + "/" + AppConstants.EXPORT_REALM_FILE_NAME;
        AppHelper.LogCat(msg);
        realm.close();
        return exportRealmFile;
    }

    public static boolean restore(Activity mActivity) {
        checkStoragePermissions(mActivity);
        //Restore
        String restoreFilePath = FilesManager.getImagesCachePath(mActivity) + "/" + AppConstants.EXPORT_REALM_FILE_NAME;
        File file = FilesManager.getImagesCachePath(mActivity);
        AppHelper.LogCat("oldFilePath = " + restoreFilePath);
        AppHelper.LogCat("Data restore is done");
        if (copyBundledRealmFile(restoreFilePath, mActivity.getString(R.string.app_name) + PreferenceManager.getID(mActivity) + ".realm", mActivity)) {
            file.delete();
            return true;
        } else {
            return false;
        }
    }

    private static boolean copyBundledRealmFile(String oldFilePath, String outFileName, Activity mActivity) {
        try {
            File file = new File(mActivity.getApplicationContext().getFilesDir(), outFileName);

            FileOutputStream outputStream = new FileOutputStream(file);

            FileInputStream inputStream = new FileInputStream(new File(oldFilePath));

            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, bytesRead);
            }
            outputStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void checkStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permissionWrite = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionRead = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionWrite != PackageManager.PERMISSION_GRANTED || permissionRead != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }

    public static void deleteData(Activity activity) {
        checkStoragePermissions(activity);

        try {
            DostChatApp.DeleteRealmDatabaseInstance();
            //Realm file has been deleted.
            AppHelper.LogCat(" Realm file has been deleted.");
        } catch (Exception ex) {
            ex.printStackTrace();
            //No Realm file to  remove.
            AppHelper.LogCat(" Failed to delete realm file or there is No Realm file to  remove");
        }
        clearApplicationData(activity);
        PreferenceManager.clearPreferences(activity);
    }


    private static void clearApplicationData(Context mContext) {
        File cache = mContext.getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib") /*&& !s.equals("files")*/) {
                    boolean deleted = deleteDir(new File(appDir, s));
                    if (!deleted) {
                        AppHelper.LogCat("ImagesCached not deleted ");
                    } else {
                        AppHelper.LogCat("ImagesCached deleted");
                        AppHelper.LogCat("File /data/data/" + mContext.getPackageName() + "/" + s + " DELETED");
                    }

                }
            }
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }

    public static int getMessageLastId() {
        Realm realm = DostChatApp.getRealmDatabaseInstance();
        AtomicInteger messagesModelLastId;
        try {
            messagesModelLastId = new AtomicInteger(realm.where(MessagesModel.class).max("id").intValue());
            AppHelper.LogCat("last message id " + messagesModelLastId);
            return messagesModelLastId.incrementAndGet();
        } catch (Exception e) {
            AppHelper.LogCat("last message id Exception" + e.getMessage());
            return 1;
        } finally {
            realm.close();
        }
    }

    public static int getConversationLastId() {
        Realm realm = DostChatApp.getRealmDatabaseInstance();
        AtomicInteger conversationModelLastId;
        try {
            conversationModelLastId = new AtomicInteger(realm.where(ConversationsModel.class).max("id").intValue());
            AppHelper.LogCat("last Conversation id " + conversationModelLastId);
            return conversationModelLastId.incrementAndGet();
        } catch (Exception e) {
            AppHelper.LogCat("last Conversation id Exception" + e.getMessage());
            return 1;
        } finally {
            realm.close();
        }
    }

    public static int getCallLastId() {
        Realm realm = DostChatApp.getRealmDatabaseInstance();
        AtomicInteger callsModelLastId;
        try {
            callsModelLastId = new AtomicInteger(realm.where(CallsModel.class).max("id").intValue());
            AppHelper.LogCat("callsModelLastId " + callsModelLastId);
            return callsModelLastId.incrementAndGet();
        } catch (Exception e) {
            AppHelper.LogCat("callsModelLastId Exception" + e.getMessage());
            return 1;
        } finally {
            realm.close();
        }
    }


    public static int getCallInfoLastId() {
        Realm realm = DostChatApp.getRealmDatabaseInstance();
        AtomicInteger CallsInfoModelLastId;
        try {
            CallsInfoModelLastId = new AtomicInteger(realm.where(CallsInfoModel.class).max("id").intValue());
            AppHelper.LogCat("CallsInfoModelLastId " + CallsInfoModelLastId);
            return CallsInfoModelLastId.incrementAndGet();
        } catch (Exception e) {
            AppHelper.LogCat("CallsInfoModelLastId Exception" + e.getMessage());
            return 1;
        } finally {
            realm.close();
        }
    }

    public static int getBlockUserLastId() {
        Realm realm = DostChatApp.getRealmDatabaseInstance();
        AtomicInteger BlockModelLastId;
        try {
            BlockModelLastId = new AtomicInteger(realm.where(UsersBlockModel.class).max("id").intValue());
            AppHelper.LogCat("BlockModelLastId " + BlockModelLastId);
            return BlockModelLastId.incrementAndGet();
        } catch (Exception e) {
            AppHelper.LogCat("BlockModelLastId Exception" + e.getMessage());
            return 1;
        } finally {
            realm.close();
        }
    }
}
