package com.dostchat.dost.helpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.DrawableRes;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatDrawableManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.dostchat.dost.BuildConfig;
import com.dostchat.dost.R;
import com.dostchat.dost.activities.media.ImagePreviewActivity;
import com.dostchat.dost.activities.media.VideoPlayerActivity;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.app.DostChatApp;
import com.dostchat.dost.models.messages.MessagesModel;
import com.dostchat.dost.services.MainService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.List;

import io.realm.Realm;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created by Abderrahim on 09/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class AppHelper {

    private static ProgressDialog mDialog;
    private static Dialog dialog;

    /**
     * method to show the progress dialog
     *
     * @param mContext this is parameter for showDialog method
     */
    public static void showDialog(Context mContext, String message) {
        mDialog = new ProgressDialog(mContext);
        mDialog.setMessage(message);
        mDialog.setIndeterminate(true);
        mDialog.setCancelable(true);
        mDialog.show();
    }

    /**
     * method to show the progress dialog
     *
     * @param mContext this is parameter for showDialog method
     */
    public static void showDialog(Context mContext, String message, boolean cancelable) {
        mDialog = new ProgressDialog(mContext);
        mDialog.setMessage(message);
        mDialog.setIndeterminate(true);
        mDialog.setCancelable(cancelable);
        mDialog.show();
    }

    /**
     * method to hide the progress dialog
     */
    public static void hideDialog() {
        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    /**
     * method for get a custom CustomToast
     *
     * @param Message this is the second parameter for CustomToast  method
     */
    public static void CustomToast(Context mContext, String Message) {

        LinearLayout CustomToastLayout = new LinearLayout(mContext.getApplicationContext());
        CustomToastLayout.setBackgroundResource(R.drawable.bg_custom_toast);
        CustomToastLayout.setGravity(Gravity.TOP);
        TextView message = new TextView(mContext.getApplicationContext());
        message.setTextColor(Color.WHITE);
        message.setTextSize(13);
        message.setPadding(20, 20, 20, 20);
        message.setGravity(Gravity.CENTER);
        message.setText(Message);
        CustomToastLayout.addView(message);
        Toast toast = new Toast(mContext.getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(CustomToastLayout);
        toast.setGravity(Gravity.CENTER, 0, 50);
        toast.show();
    }

    /**
     * method to check if android version is lollipop
     *
     * @return this return value
     */
    public static boolean isAndroid5() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * method to check if android version is lollipop
     *
     * @return this return value
     */
    public static boolean isJelly17() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    }

    /**
     * method to check if android version is Marsh
     *
     * @return this return value
     */
    public static boolean isAndroid6() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * method to check if android version is Kitkat
     *
     * @return this return value
     */
    public static boolean isKitkat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    /**
     * method to get color
     *
     * @param context this is the first parameter for getColor  method
     * @param id      this is the second parameter for getColor  method
     * @return return value
     */
    public static int getColor(Context context, int id) {
        if (isAndroid5()) {
            return ContextCompat.getColor(context, id);
        } else {
            return context.getResources().getColor(id);
        }
    }

    /**
     * method to get drawable
     *
     * @param context this is the first parameter for getDrawable  method
     * @param id      this is the second parameter for getDrawable  method
     * @return return value
     */
    public static Drawable getDrawable(Context context, int id) {
        if (isAndroid5()) {
            return ContextCompat.getDrawable(context, id);
        } else {
            return context.getResources().getDrawable(id);
        }
    }

    /**
     * shake EditText error
     *
     * @param mContext this is the first parameter for showErrorEditText  method
     * @param editText this is the second parameter for showErrorEditText  method
     */
    private void showErrorEditText(Context mContext, EditText editText) {
        Animation shake = AnimationUtils.loadAnimation(mContext, R.anim.shake);
        editText.startAnimation(shake);
    }

    /**
     * method for LogCat
     *
     * @param Message this is  parameter for LogCat  method
     */
    public static void LogCat(String Message) {
        if (AppConstants.DEBUGGING_MODE) {
            if (Message != null) {
                if (!BuildConfig.DEBUG) {
                    return;
                }
                Logger.e(Message);
            }
        }
    }


    /**
     * method for Log cat Throwable
     *
     * @param Message this is  parameter for LogCatThrowable  method
     */
    public static void LogCat(Throwable Message) {
        if (AppConstants.DEBUGGING_MODE)
            LogCat("LogCatThrowable " + Message.getMessage());
    }

    /**
     * method to export realm database
     *
     * @param mContext this is parameter for CustomToast  method
     */
    public static void ExportRealmDatabase(Context mContext) {

        // init realm
        Realm realm = DostChatApp.getRealmDatabaseInstance();

        File exportRealmFile = null;
        // get or create an "whatsClone.realm" file
        exportRealmFile = new File(mContext.getExternalCacheDir(), "whatsClone.realm");

        // if "whatsClone.realm" already exists, delete
        exportRealmFile.delete();

        // copy current realm to "export.realm"
        realm.writeCopyTo(exportRealmFile);

        realm.close();

        // init email intent and add export.realm as attachment
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, "abderrahim.elimame@gmail.com");
        intent.putExtra(Intent.EXTRA_SUBJECT, "this is ur local realm database whatsClone");
        intent.putExtra(Intent.EXTRA_TEXT, "Hi man");
        Uri u = Uri.fromFile(exportRealmFile);
        intent.putExtra(Intent.EXTRA_STREAM, u);

        // start email intent
        mContext.startActivity(Intent.createChooser(intent, "Choose an application"));
    }


    /**
     * method to loadJSONFromAsset json files from asset directory
     *
     * @param mContext this is  parameter for loadJSONFromAsset  method
     * @return return value
     */
    public static String loadJSONFromAsset(Context mContext) {
        String json = null;
        try {
            InputStream is = mContext.getAssets().open("country_phones.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    /**
     * method to launch the activities
     *
     * @param mContext  this is the first parameter for LaunchActivity  method
     * @param mActivity this is the second parameter for LaunchActivity  method
     */
    public static void LaunchActivity(Activity mContext, Class mActivity) {
        Intent mIntent = new Intent(mContext, mActivity);
        mContext.startActivity(mIntent);
        mContext.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * method to launch the activities
     *
     * @param mContext  this is the first parameter for LaunchActivity  method
     * @param ImageType this is the second parameter for LaunchActivity  method
     */
    public static void LaunchImagePreviewActivity(Activity mContext, String ImageType, String identifier) {
        Intent mIntent = new Intent(mContext, ImagePreviewActivity.class);
        mIntent.putExtra("ImageType", ImageType);
        mIntent.putExtra("Identifier", identifier);
        mIntent.putExtra("SaveIntent", false);
        mContext.startActivity(mIntent);
        mContext.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * method to launch the activities
     *
     * @param mContext   this is the first parameter for LaunchActivity  method
     * @param identifier this is the second parameter for LaunchActivity  method
     */
    public static void LaunchVideoPreviewActivity(Activity mContext, String identifier, boolean isSent) {
        Intent mIntent = new Intent(mContext, VideoPlayerActivity.class);
        mIntent.putExtra("Identifier", identifier);
        mIntent.putExtra("isSent", isSent);
        mContext.startActivity(mIntent);
        mContext.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * method to convert dp  to pixel
     *
     * @param dp this is  parameter for dpToPx  method
     * @return return value
     */
    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    /**
     * method to convert pixel to dp
     *
     * @param px this is  parameter for pxToDp  method
     * @return return value
     */
    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    /**
     * method to show snack bar
     *
     * @param mContext    this is the first parameter for Snackbar  method
     * @param view        this is the second parameter for Snackbar  method
     * @param Message     this is the thirded parameter for Snackbar  method
     * @param colorId     this is the fourth parameter for Snackbar  method
     * @param TextColorId this is the fifth parameter for Snackbar  method
     */
    public static void Snackbar(Context mContext, View view, String Message, int colorId, int TextColorId) {
        Snackbar snackbar = Snackbar.make(view, Message, Snackbar.LENGTH_LONG);
        View snackView = snackbar.getView();
        snackView.setBackgroundColor(ContextCompat.getColor(mContext, colorId));
        TextView snackbarTextView = (TextView) snackView.findViewById(android.support.design.R.id.snackbar_text);
        snackbarTextView.setTextColor(ContextCompat.getColor(mContext, TextColorId));
        snackbar.show();
    }

    /**
     * method to check if activity is running or not
     *
     * @param mContext     this is the first parameter for isActivityRunning  method
     * @param activityName this is the second parameter for isActivityRunning  method
     * @return return value
     */
    public static boolean isActivityRunning(Context mContext, String activityName) {
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(3);
        for (ActivityManager.RunningTaskInfo task : tasks) {
            if ((mContext.getPackageName() + "." + activityName).equals(task.topActivity.getClassName())) {
                return true;
            }
        }

        return false;
    }


    /**
     * method to copy text
     *
     * @param context       this is the first parameter for copyText  method
     * @param messagesModel this is the second parameter for copyText  method
     * @return return value
     */
    public static boolean copyText(Context context, MessagesModel messagesModel) {
        String message = UtilsString.unescapeJava(messagesModel.getMessage());
        int sdk = Build.VERSION.SDK_INT;
        if (sdk < Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(message);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(context.getString(R.string.message_copy), message);
            clipboard.setPrimaryClip(clip);
        }
        return true;
    }


    public static void shareIntent(File Url, Activity mActivity, String subject, String type) {
        if (Url != null) {
            Uri bmpUri = Uri.fromFile(Url);
            if (bmpUri != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                if (subject != null) {
                    shareIntent.putExtra(Intent.EXTRA_TEXT, subject);
                }
                shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                switch (type) {
                    case AppConstants.SENT_TEXT:
                        shareIntent.setType("text/*");
                        break;
                    case AppConstants.SENT_IMAGES:
                        shareIntent.setType("image/*");
                        break;
                    case AppConstants.SENT_VIDEOS:
                        shareIntent.setType("video/mp4");
                        break;
                    case AppConstants.SENT_AUDIO:
                        shareIntent.setType("audio/mp3");
                        break;
                    case AppConstants.SENT_DOCUMENTS:
                        shareIntent.setType("application/pdf");
                        break;
                }
                mActivity.startActivity(Intent.createChooser(shareIntent, mActivity.getString(R.string.shareItem)));
            } else {
                CustomToast(mActivity, mActivity.getString(R.string.oops_something));
            }
        } else {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            if (subject != null) {
                shareIntent.putExtra(Intent.EXTRA_TEXT, subject);
            }

            if (type.equals(AppConstants.SENT_TEXT)) {
                shareIntent.setType("plain/text");
            }
            mActivity.startActivity(Intent.createChooser(shareIntent, mActivity.getString(R.string.shareItem)));
        }

    }

    public static String getAppVersion(Context mContext) {
        PackageInfo packageinfo;
        try {
            packageinfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            return packageinfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            AppHelper.LogCat(" getAppVersion NameNotFoundException " + e.getMessage());
            return null;
        }
    }

    public static int getAppVersionCode(Context mContext) {
        PackageInfo packageinfo;
        try {
            packageinfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            return packageinfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            AppHelper.LogCat(" getAppVersion NameNotFoundException " + e.getMessage());
            return 0;
        }
    }

    /**
     * method to paly sound
     *
     * @param context
     * @param sounds
     * @return
     */
    public static MediaPlayer playSound(Context context, String sounds) {
        MediaPlayer mMediaPlayer = new MediaPlayer();

        try {
            if (((AudioManager) context.getSystemService(Context.AUDIO_SERVICE)).getRingerMode() == 2) {
                AssetFileDescriptor afd = context.getAssets().openFd(sounds);
                mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mMediaPlayer;
    }


    @SuppressLint("NewApi")
    public static void showPermissionDialog(final Activity mActivity) {

        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }

        dialog = new Dialog(mActivity);
        if (android.os.Build.VERSION.RELEASE.startsWith("1.") || android.os.Build.VERSION.RELEASE.startsWith("2.0") || android.os.Build.VERSION.RELEASE.startsWith("2.1")) {
            //No dialog title on pre-froyo devices
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        } else if (mActivity.getResources().getDisplayMetrics().densityDpi == DisplayMetrics.DENSITY_LOW || mActivity.getResources().getDisplayMetrics().densityDpi == DisplayMetrics.DENSITY_MEDIUM) {
            Display display = ((WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            int rotation = display.getRotation();
            if (rotation == 90 || rotation == 270) {
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            } else {
                dialog.setTitle(R.string.permission_alert);
            }
        } else {
            dialog.setTitle(R.string.permission_alert);
        }

        RelativeLayout layout = (RelativeLayout) LayoutInflater.from(mActivity).inflate(R.layout.custom_dialog_permissions, null);

        TextView tv = (TextView) layout.findViewById(R.id.message);
        tv.setText(R.string.permission_alert_msg);

        TextView allowButton = (TextView) layout.findViewById(R.id.allow);
        allowButton.setText(mActivity.getString(R.string.ok));
        allowButton.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", mActivity.getPackageName(), null);
            intent.setData(uri);
            mActivity.startActivityForResult(intent, AppConstants.CONTACTS_PERMISSION_REQUEST_CODE);
            dialog.dismiss();
            mActivity.finish();
        });
        dialog.setCancelable(false);
        dialog.setContentView(layout);

        try {
            if (dialog != null) {
                dialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void hidePermissionsDialog() {
        if (dialog != null)
            dialog.dismiss();
    }


    public static Drawable getVectorDrawable(Context mContext, @DrawableRes int id) {
        return AppCompatDrawableManager.get().getDrawable(mContext, id);
    }


    public static int getToolbarHeight(Context context) {
        final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                new int[]{R.attr.actionBarSize});
        int toolbarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

        return toolbarHeight;
    }

    public static boolean isServiceRunning(Context mContext, Class<?> serviceClass) {
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        // Loop through the running services
        for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                // If the service is running then return true
                return true;
            }
        }
        return false;
    }

    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

    static boolean isAppRunning(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        for (int i = 0; i < procInfos.size(); i++) {
            if (procInfos.get(i).processName.equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    private static final Hashtable<String, Typeface> cache = new Hashtable<>();

    public static Typeface setTypeFace(Context c, String name) {
        synchronized (cache) {
            if (!cache.containsKey(name)) {
                try {
                    Typeface t = Typeface.createFromAsset(c.getAssets(), "fonts/" + name + ".otf");
                    cache.put(name, t);
                } catch (Exception e) {
                    LogCat(e);
                    return null;
                }
            }
            return cache.get(name);
        }
    }

    /**
     * delete cache method
     *
     * @param context
     */
    public static void deleteCache(Context context) {
        LogCat("here deleteCache method");
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
            LogCat(" deleteCache method Exception " + e.getMessage());
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
            return dir.delete();
        } else
            return dir != null && dir.isFile() && dir.delete();
    }

    public static void freeMemory() {
        System.runFinalization();
        Runtime.getRuntime().gc();
        System.gc();
    }


    public static String fixRotation(File file) {
        int rotation = getRotation(file.getPath());
        if (rotation == 0 || rotation == ExifInterface.ORIENTATION_NORMAL) return file.getPath();
        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);
        Bitmap bitmapSource = BitmapFactory.decodeFile(file.getPath());
        Bitmap cropped = Bitmap.createBitmap(bitmapSource, 0, 0, bitmapSource.getWidth(), bitmapSource.getHeight(), matrix, true);
        try {
            FileOutputStream out = new FileOutputStream(file);
            cropped.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            return file.getPath();
        } catch (Exception ignored) {
        }
        return file.getPath();
    }

    private static int getRotation(String filePath) {
        int rotate = 0;
        try {
            ExifInterface exif = new ExifInterface(filePath);
            switch (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (IOException e) {
            LogCat(e);
            rotate = 0;
        }

        return rotate;
    }

    public static void restartService() {
        if (PreferenceManager.getToken(DostChatApp.getInstance()) == null) return;

        DostChatApp.getInstance().stopService(new Intent(DostChatApp.getInstance(), MainService.class));
        DostChatApp.getInstance().startService(new Intent(DostChatApp.getInstance(), MainService.class));
    }
}
