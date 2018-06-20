package com.dostchat.dost.activities.settings;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.dostchat.dost.R;
import com.dostchat.dost.activities.profile.EditProfileActivity;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.app.EndPoints;
import com.dostchat.dost.app.DostChatApp;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.Files.cache.ImageLoader;
import com.dostchat.dost.helpers.Files.cache.MemoryCache;
import com.dostchat.dost.helpers.RateHelper;
import com.dostchat.dost.helpers.UtilsString;
import com.dostchat.dost.helpers.images.DostChatImageLoader;
import com.dostchat.dost.interfaces.NetworkListener;
import com.dostchat.dost.models.users.Pusher;
import com.dostchat.dost.models.users.contacts.ContactsModel;
import com.dostchat.dost.presenters.users.SettingsPresenter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;


/**
 * Created by Abderrahim El imame on 27/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class SettingsActivity extends AppCompatActivity implements NetworkListener {


    @BindView(R.id.userAvatar)
    ImageView userAvatar;

    @BindView(R.id.user_status)
    EmojiconTextView userStatus;

    @BindView(R.id.userName)
    TextView userName;

    @BindView(R.id.chats_settings_text)
    TextView chats;

    @BindView(R.id.account_settings_text)
    TextView account;

    @BindView(R.id.notifications_settings_text)
    TextView notifications;

    @BindView(R.id.about_help_settings_text)
    TextView aboutHelp;

    @BindView(R.id.mainSettings)
    NestedScrollView mView;
    private ContactsModel mContactsModel;
    private SettingsPresenter mSettingsPresenter;
    private MemoryCache memoryCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        setupToolbar();
        setTypeFaces();
        memoryCache = new MemoryCache();
        mSettingsPresenter = new SettingsPresenter(this);
        mSettingsPresenter.onCreate();
        EventBus.getDefault().register(this);

    }

    private void setTypeFaces() {
        if (AppConstants.ENABLE_FONTS_TYPES) {
            userName.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            userStatus.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            chats.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            account.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            notifications.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            aboutHelp.setTypeface(AppHelper.setTypeFace(this, "Futura"));
        }
    }


    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @SuppressWarnings("unused")
    @OnClick(R.id.settingsHead)
    public void launchEditProfile(View v) {
        RateHelper.significantEvent(this);
        if (AppHelper.isAndroid5()) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, new Pair<>(userAvatar, "userAvatar"), new Pair<>(userName, "userName"), new Pair<>
                    (userStatus, "userStatus"));
            Intent mIntent = new Intent(this, EditProfileActivity.class);
            startActivity(mIntent, options.toBundle());
        } else {
            AppHelper.LaunchActivity(this, EditProfileActivity.class);
        }
    }


    @SuppressWarnings("unused")
    @OnClick(R.id.chats_settings)
    public void launchChatsSettings() {
        RateHelper.significantEvent(this);
        AppHelper.LaunchActivity(this, ChatsSettingsActivity.class);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.account_settings)
    public void launchAccountSettings() {
        RateHelper.significantEvent(this);
        AppHelper.LaunchActivity(this, AccountSettingsActivity.class);
    }


    @SuppressWarnings("unused")
    @OnClick(R.id.notifications_settings)
    public void launchNotificationSettings() {
        RateHelper.significantEvent(this);
        AppHelper.LaunchActivity(this, NotificationsSettingsActivity.class);
    }


    @SuppressWarnings("unused")
    @OnClick(R.id.about_help_settings)
    public void launchAboutSettings() {
        AppHelper.LaunchActivity(this, AboutHelpActivity.class);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.ll_faq)
    public void launchFAQ() {
        AppHelper.LaunchActivity(this, FAQActivity.class);
    }

    public void ShowContact(ContactsModel contactsModels) {
        mContactsModel = contactsModels;
        try {
            if (mContactsModel.getStatus() != null) {
                String status = UtilsString.unescapeJava(mContactsModel.getStatus());
                userStatus.setText(status);
            } else {
                userStatus.setText(getString(R.string.no_status));
            }
            if (mContactsModel.getUsername() != null) {
                userName.setText(mContactsModel.getUsername());
            } else {
                userName.setText(getString(R.string.no_username));
            }
            Bitmap bitmap = ImageLoader.GetCachedBitmapImage(memoryCache, mContactsModel.getImage(), this, mContactsModel.getId(), AppConstants.USER, AppConstants.SETTINGS_PROFILE);
            if (bitmap != null) {
                ImageLoader.SetBitmapImage(bitmap, userAvatar);
            } else {
             /*   Target target = new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        userAvatar.setImageBitmap(bitmap);
                        ImageLoader.DownloadImage(memoryCache, EndPoints.SETTINGS_IMAGE_URL + mContactsModel.getImage(), mContactsModel.getImage(), SettingsActivity.this, mContactsModel.getId(), AppConstants.USER, AppConstants.SETTINGS_PROFILE);
                        ImageLoader.DownloadImage(memoryCache, EndPoints.EDIT_PROFILE_IMAGE_URL + mContactsModel.getImage(), mContactsModel.getImage(), SettingsActivity.this, mContactsModel.getId(), AppConstants.USER, AppConstants.EDIT_PROFILE);

                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                        userAvatar.setImageDrawable(errorDrawable);
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                        userAvatar.setImageDrawable(placeHolderDrawable);
                    }
                };*/
                BitmapImageViewTarget target = new BitmapImageViewTarget(userAvatar) {
                    @Override
                    public void onResourceReady(final Bitmap bitmap, GlideAnimation anim) {
                        super.onResourceReady(bitmap, anim);
                        userAvatar.setImageBitmap(bitmap);
                        ImageLoader.DownloadImage(memoryCache, EndPoints.SETTINGS_IMAGE_URL + mContactsModel.getImage(), mContactsModel.getImage(), SettingsActivity.this, mContactsModel.getId(), AppConstants.USER, AppConstants.SETTINGS_PROFILE);
                        ImageLoader.DownloadImage(memoryCache, EndPoints.EDIT_PROFILE_IMAGE_URL + mContactsModel.getImage(), mContactsModel.getImage(), SettingsActivity.this, mContactsModel.getId(), AppConstants.USER, AppConstants.EDIT_PROFILE);

                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);
                        userAvatar.setImageDrawable(errorDrawable);
                    }

                    @Override
                    public void onLoadStarted(Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                        userAvatar.setImageDrawable(placeholder);
                    }
                };
                DostChatImageLoader.loadCircleImage(this, EndPoints.SETTINGS_IMAGE_URL + mContactsModel.getImage(), target, R.drawable.image_holder_ur_circle, AppConstants.SETTINGS_IMAGE_SIZE);

            }
        } catch (Exception e) {
            AppHelper.LogCat("" + e);
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSettingsPresenter.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(Pusher pusher) {
        switch (pusher.getAction()) {
            case AppConstants.EVENT_BUS_USERNAME_PROFILE_UPDATED:
            case AppConstants.EVENT_BUS_UPDATE_CURRENT_SATUS:
            case AppConstants.EVENT_BUS_MINE_IMAGE_PROFILE_UPDATED:
                mSettingsPresenter.loadData();
                break;
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }


    @Override
    protected void onResume() {
        super.onResume();
        DostChatApp.getInstance().setConnectivityListener(this);
    }

    /**
     * Callback will be triggered when there is change in
     * network connection
     */
    @Override
    public void onNetworkConnectionChanged(boolean isConnecting, boolean isConnected) {
        if (!isConnecting && !isConnected) {
            AppHelper.Snackbar(this, mView, getString(R.string.connection_is_not_available), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
        } else if (isConnecting && isConnected) {
            AppHelper.Snackbar(this, mView, getString(R.string.connection_is_available), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
        } else {
            AppHelper.Snackbar(this, mView, getString(R.string.waiting_for_network), AppConstants.MESSAGE_COLOR_WARNING, AppConstants.TEXT_COLOR);
        }
    }
}
