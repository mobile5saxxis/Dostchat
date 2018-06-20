package com.dostchat.dost.helpers.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.dostchat.dost.R;
import com.dostchat.dost.activities.messages.MessagesActivity;
import com.dostchat.dost.activities.popups.MessagesPopupActivity;
import com.dostchat.dost.activities.settings.PreferenceSettingsManager;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.app.DostChatApp;
import com.dostchat.dost.app.EndPoints;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.Files.cache.ImageLoader;
import com.dostchat.dost.helpers.Files.cache.MemoryCache;
import com.dostchat.dost.helpers.PreferenceManager;
import com.dostchat.dost.helpers.UtilsPhone;
import com.dostchat.dost.helpers.UtilsString;
import com.dostchat.dost.models.messages.MessagesModel;
import com.dostchat.dost.ui.CropSquareTransformation;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

import io.realm.Realm;
import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Created by Abderrahim El imame on 6/19/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class NotificationsManager {


    private static NotificationManager mNotificationManager;
    private static String username;
    private static int numMessages = 0;
    private static MemoryCache memoryCache;

    public static void showUserNotification(Context mContext, Intent resultIntent, Intent messagingPopupIntent, String phone, String message, int userId, String Avatar) {
        memoryCache = new MemoryCache();
        String text = UtilsString.unescapeJava(message);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        // Adds the back stack
        stackBuilder.addParentStack(MessagesActivity.class);
        // Adds the Intent to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        // Gets a PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


        TaskStackBuilder stackPopupBuilder = TaskStackBuilder.create(mContext);
        // Adds the back stack

        stackPopupBuilder.addParentStack(MessagesPopupActivity.class);
        // Adds the Intent to the top of the stack
        stackPopupBuilder.addNextIntent(messagingPopupIntent);
        // Gets a PendingIntent containing the entire back stack
        PendingIntent resultMessagingPopupIntent = stackPopupBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationCompat.Builder mNotifyBuilder;
        try {

            String name = UtilsPhone.getContactName(mContext, phone);
            if (name != null) {
                username = name;
            } else {
                username = phone;
            }


        } catch (Exception e) {
            AppHelper.LogCat(" " + e.getMessage());
        }
        ++numMessages;
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.ic_reply_black_24dp, mContext.getString(R.string.reply_message), resultMessagingPopupIntent).build();
        mNotifyBuilder = new NotificationCompat.Builder(mContext)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(action)
                .setContentTitle(username)
                .setContentText(text)
                .setNumber(numMessages)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(resultPendingIntent)
                .setPriority(Notification.PRIORITY_HIGH);

        Bitmap bitmap = ImageLoader.GetCachedBitmapImage(memoryCache, Avatar, mContext, userId, AppConstants.USER, AppConstants.ROW_PROFILE);
        if (bitmap != null) {
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, AppConstants.NOTIFICATIONS_IMAGE_SIZE, AppConstants.NOTIFICATIONS_IMAGE_SIZE, false);
            Bitmap circleBitmap = Bitmap.createBitmap(scaledBitmap.getWidth(), scaledBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            BitmapShader shader = new BitmapShader(scaledBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            Paint paint = new Paint();
            paint.setShader(shader);
            paint.setAntiAlias(true);
            Canvas c = new Canvas(circleBitmap);
            c.drawCircle(scaledBitmap.getWidth() / 2, scaledBitmap.getHeight() / 2, scaledBitmap.getWidth() / 2, paint);
            mNotifyBuilder.setLargeIcon(circleBitmap);
        } else {
            Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    mNotifyBuilder.setLargeIcon(bitmap);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.image_holder_ur_circle);
                    mNotifyBuilder.setLargeIcon(bitmap);
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.image_holder_ur_circle);
                    mNotifyBuilder.setLargeIcon(bitmap);
                }
            };
            Picasso.with(mContext)
                    .load(EndPoints.ROWS_IMAGE_URL + Avatar)
                    .transform(new CropSquareTransformation())
                    .resize(AppConstants.NOTIFICATIONS_IMAGE_SIZE, AppConstants.NOTIFICATIONS_IMAGE_SIZE)
                    .into(target);
        }

        if (PreferenceSettingsManager.conversation_tones(mContext)) {

            Uri uri = PreferenceSettingsManager.getDefault_message_notifications_settings_tone(mContext);
            if (uri != null)
                mNotifyBuilder.setSound(uri);
            else {
                int defaults = 0;
                defaults = defaults | Notification.DEFAULT_SOUND;
                mNotifyBuilder.setDefaults(defaults);
            }


        }

        if (PreferenceSettingsManager.getDefault_message_notifications_settings_vibrate(mContext)) {
            long[] vibrate = new long[]{2000, 2000, 2000, 2000, 2000};
            mNotifyBuilder.setVibrate(vibrate);
        } else {
            int defaults = 0;
            defaults = defaults | Notification.DEFAULT_VIBRATE;
            mNotifyBuilder.setDefaults(defaults);
        }


        String colorLight = PreferenceSettingsManager.getDefault_message_notifications_settings_light(mContext);
        if (colorLight != null) {
            mNotifyBuilder.setLights(Color.parseColor(colorLight), 1500, 1500);
        } else {
            int defaults = 0;
            defaults = defaults | Notification.DEFAULT_LIGHTS;
            mNotifyBuilder.setDefaults(defaults);
        }


        mNotifyBuilder.setAutoCancel(true);

        mNotificationManager.notify(userId, mNotifyBuilder.build());

    }


    public static void showGroupNotification(Context mContext, Intent resultIntent, Intent messagingGroupPopupIntent, String groupName, String message, int groupId, String Avatar) {
        memoryCache = new MemoryCache();

        String text = UtilsString.unescapeJava(message);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        // Adds the back stack
        stackBuilder.addParentStack(MessagesActivity.class);
        // Adds the Intent to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        // Gets a PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        TaskStackBuilder stackGroupPopupBuilder = TaskStackBuilder.create(mContext);
        stackGroupPopupBuilder.addParentStack(MessagesPopupActivity.class);
        // Adds the Intent to the top of the stack
        stackGroupPopupBuilder.addNextIntent(messagingGroupPopupIntent);
        // Gets a PendingIntent containing the entire back stack
        PendingIntent resultMessagingGroupPopupIntent = stackGroupPopupBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


        final NotificationCompat.Builder mNotifyBuilder;


        ++numMessages;
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.ic_reply_black_24dp, mContext.getString(R.string.reply_message), resultMessagingGroupPopupIntent).build();
        mNotifyBuilder = new NotificationCompat.Builder(mContext)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(action)
                .setContentTitle(groupName)
                .setContentText(text)
                .setNumber(numMessages)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(resultPendingIntent)
                .setPriority(Notification.PRIORITY_HIGH);
        Bitmap bitmap = ImageLoader.GetCachedBitmapImage(memoryCache, Avatar, mContext, groupId, AppConstants.USER, AppConstants.ROW_PROFILE);
        if (bitmap != null) {
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, AppConstants.NOTIFICATIONS_IMAGE_SIZE, AppConstants.NOTIFICATIONS_IMAGE_SIZE, false);
            Bitmap circleBitmap = Bitmap.createBitmap(scaledBitmap.getWidth(), scaledBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            BitmapShader shader = new BitmapShader(scaledBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            Paint paint = new Paint();
            paint.setShader(shader);
            paint.setAntiAlias(true);
            Canvas c = new Canvas(circleBitmap);
            c.drawCircle(scaledBitmap.getWidth() / 2, scaledBitmap.getHeight() / 2, scaledBitmap.getWidth() / 2, paint);
            mNotifyBuilder.setLargeIcon(circleBitmap);
        } else {
            Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    mNotifyBuilder.setLargeIcon(bitmap);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.image_holder_gr_circle);
                    mNotifyBuilder.setLargeIcon(bitmap);
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.image_holder_gr_circle);
                    mNotifyBuilder.setLargeIcon(bitmap);
                }
            };
            Picasso.with(mContext)
                    .load(EndPoints.ROWS_IMAGE_URL + Avatar)
                    .transform(new CropSquareTransformation())
                    .resize(AppConstants.NOTIFICATIONS_IMAGE_SIZE, AppConstants.NOTIFICATIONS_IMAGE_SIZE)
                    .into(target);
        }
        mNotifyBuilder.setAutoCancel(true);


        if (PreferenceSettingsManager.conversation_tones(mContext)) {

            Uri uri = PreferenceSettingsManager.getDefault_message_group_notifications_settings_tone(mContext);
            if (uri != null)
                mNotifyBuilder.setSound(uri);
            else {
                int defaults = 0;
                defaults = defaults | Notification.DEFAULT_SOUND;
                mNotifyBuilder.setDefaults(defaults);
            }


        }

        if (PreferenceSettingsManager.getDefault_message_group_notifications_settings_vibrate(mContext)) {
            long[] vibrate = new long[]{2000, 2000, 2000, 2000, 2000};
            mNotifyBuilder.setVibrate(vibrate);
        } else {
            int defaults = 0;
            defaults = defaults | Notification.DEFAULT_VIBRATE;
            mNotifyBuilder.setDefaults(defaults);
        }


        String colorLight = PreferenceSettingsManager.getDefault_message_group_notifications_settings_light(mContext);
        if (colorLight != null) {
            mNotifyBuilder.setLights(Color.parseColor(colorLight), 1500, 1500);
        } else {
            int defaults = 0;
            defaults = defaults | Notification.DEFAULT_LIGHTS;
            mNotifyBuilder.setDefaults(defaults);
        }


        mNotificationManager.notify(groupId, mNotifyBuilder.build());

    }

    /**
     * method to get manager for notification
     */
    public static boolean getManager() {
        if (mNotificationManager != null) {
            return true;
        } else {
            return false;
        }

    }

    /***
     * method to cancel a specific notification
     *
     * @param index
     */
    public static void cancelNotification(int index) {
        numMessages = 0;
        mNotificationManager.cancel(index);
    }

    /**
     * method to set badger counter for the app
     */
    public static void SetupBadger(Context mContext) {

        int messageBadgeCounter = 0;
        Realm realm = DostChatApp.getRealmDatabaseInstance();
        String DeviceName = android.os.Build.MANUFACTURER;
        String[] DevicesName = {
                "Sony",
                "Samsung",
                "LG",
                "HTC",
                "Xiaomi",
                "ASUS",
                "ADW",
                "NOVA",
                "Huawei",
                "ZUK",
                "APEX"
        };

        for (String device : DevicesName) {
            if (DeviceName.equals(device.toLowerCase())) {
                try {
                    List<MessagesModel> messagesModels = realm.where(MessagesModel.class)
                            .notEqualTo("id", 0)
                            .equalTo("status", AppConstants.IS_WAITING)
                            .notEqualTo("senderID", PreferenceManager.getID(mContext))
                            .findAll();

                    if (messagesModels.size() != 0) {
                        messageBadgeCounter = messagesModels.size();
                    }
                    try {
                        ShortcutBadger.applyCount(mContext.getApplicationContext(), messageBadgeCounter);
                    } catch (Exception e) {
                        AppHelper.LogCat(" ShortcutBadger Exception " + e.getMessage());
                    }
                } catch (Exception e) {
                    AppHelper.LogCat(" ShortcutBadger Exception " + e.getMessage());
                }
                break;
            }
        }
        realm.close();

    }
}
