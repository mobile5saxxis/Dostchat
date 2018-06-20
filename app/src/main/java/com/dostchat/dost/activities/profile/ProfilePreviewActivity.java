package com.dostchat.dost.activities.profile;


import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.AppCompatImageView;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dostchat.dost.R;
import com.dostchat.dost.activities.messages.MessagesActivity;
import com.dostchat.dost.animations.AnimationsUtil;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.app.EndPoints;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.Files.FilesManager;
import com.dostchat.dost.helpers.Files.cache.ImageLoader;
import com.dostchat.dost.helpers.Files.cache.MemoryCache;
import com.dostchat.dost.helpers.UtilsPhone;
import com.dostchat.dost.helpers.UtilsString;
import com.dostchat.dost.helpers.call.CallManager;
import com.dostchat.dost.models.groups.GroupsModel;
import com.dostchat.dost.models.users.contacts.ContactsModel;
import com.dostchat.dost.presenters.users.ProfilePreviewPresenter;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;
import jp.wasabeef.picasso.transformations.BlurTransformation;

/**
 * Created by Abderrahim El imame on 27/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class ProfilePreviewActivity extends Activity {
    @BindView(R.id.userProfileName)
    EmojiconTextView userProfileName;
    @BindView(R.id.ContactBtn)
    AppCompatImageView ContactBtn;
    @BindView(R.id.AboutBtn)
    AppCompatImageView AboutBtn;
    @BindView(R.id.CallBtn)
    AppCompatImageView CallBtn;
    @BindView(R.id.CallVideoBtn)
    AppCompatImageView CallVideoBtn;
    @BindView(R.id.userProfilePicture)
    AppCompatImageView userProfilePicture;
    @BindView(R.id.actionProfileArea)
    LinearLayout actionProfileArea;
    @BindView(R.id.invite)
    TextView actionProfileInvite;
    @BindView(R.id.containerProfile)
    LinearLayout containerProfile;
    @BindView(R.id.containerProfileInfo)
    LinearLayout containerProfileInfo;


    public int userID;
    public int groupID;
    public int conversationID;
    private boolean isGroup;
    private long Duration = 500;
    private Intent mIntent;
    private boolean isImageLoaded = false;
    private String ImageUrl;
    private String ImageUrlFile;
    private ProfilePreviewPresenter mProfilePresenter;
    private ContactsModel contactsModel;
    private MemoryCache memoryCache;


    private void setTypeFaces() {
        if (AppConstants.ENABLE_FONTS_TYPES) {
            userProfileName.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            actionProfileInvite.setTypeface(AppHelper.setTypeFace(this, "Futura"));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppHelper.isAndroid5()) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getWindow().setStatusBarColor(AppHelper.getColor(this, R.color.colorPrimaryDark));
        }

        // Make us non-modal, so that others can receive touch events.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        // but notify us that it happened.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);

        setContentView(R.layout.activity_profile_perview);
        ButterKnife.bind(this);
        initializerView();
        setTypeFaces();
        setupProgressBar();
        memoryCache = new MemoryCache();
        isGroup = getIntent().getExtras().getBoolean("isGroup");
        userID = getIntent().getExtras().getInt("userID");

        if (getIntent().hasExtra("groupID")) {
            isGroup = getIntent().getExtras().getBoolean("isGroup");
            groupID = getIntent().getExtras().getInt("groupID");
            conversationID = getIntent().getExtras().getInt("conversationID");
        }
        mProfilePresenter = new ProfilePreviewPresenter(this);
        mProfilePresenter.onCreate();
        if (AppHelper.isAndroid5()) {
            containerProfileInfo.post(() -> AnimationsUtil.show(containerProfileInfo, Duration));
        }
        if (isGroup) {
            CallBtn.setVisibility(View.GONE);
            CallVideoBtn.setVisibility(View.GONE);
        } else {
            CallBtn.setVisibility(View.VISIBLE);
            CallVideoBtn.setVisibility(View.VISIBLE);
        }


    }

    /**
     * method to initialize the view
     */
    private void initializerView() {
        if (AppHelper.isAndroid5()) {
            userProfilePicture.setTransitionName(getString(R.string.user_image_transition));
            userProfileName.setTransitionName(getString(R.string.user_name_transition));
        }
        ContactBtn.setOnClickListener(v -> {
            if (isGroup) {
                Intent messagingIntent = new Intent(this, MessagesActivity.class);
                messagingIntent.putExtra("conversationID", conversationID);
                messagingIntent.putExtra("groupID", groupID);
                messagingIntent.putExtra("isGroup", true);
                startActivity(messagingIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            } else {
                Intent messagingIntent = new Intent(this, MessagesActivity.class);
                messagingIntent.putExtra("conversationID", 0);
                messagingIntent.putExtra("recipientID", userID);
                messagingIntent.putExtra("isGroup", false);
                startActivity(messagingIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            }
        });
        AboutBtn.setOnClickListener(v -> {
            if (isGroup) {
                if (AppHelper.isAndroid5()) {
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, new Pair<>(userProfilePicture, "userAvatar"), new Pair<>(userProfileName, "userName"));
                    mIntent = new Intent(this, ProfileActivity.class);
                    mIntent.putExtra("groupID", groupID);
                    mIntent.putExtra("isGroup", true);
                    startActivity(mIntent, options.toBundle());
                    finish();
                } else {
                    mIntent = new Intent(this, ProfileActivity.class);
                    mIntent.putExtra("groupID", groupID);
                    mIntent.putExtra("isGroup", true);
                    startActivity(mIntent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                }
            } else {
                if (AppHelper.isAndroid5()) {
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, new Pair<>(userProfilePicture, "userAvatar"), new Pair<>(userProfileName, "userName"));
                    Intent mIntent = new Intent(this, ProfileActivity.class);
                    mIntent.putExtra("userID", userID);
                    mIntent.putExtra("isGroup", false);
                    startActivity(mIntent, options.toBundle());
                    finish();
                } else {
                    mIntent = new Intent(this, ProfileActivity.class);
                    mIntent.putExtra("userID", userID);
                    mIntent.putExtra("isGroup", false);
                    startActivity(mIntent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                }
            }

        });
        CallBtn.setOnClickListener(v -> {
            if (!isGroup) {
                CallManager.callContact(this, true, false, userID);
            }
        });
        CallVideoBtn.setOnClickListener(v -> {
            if (!isGroup) {
                CallManager.callContact(this, true, true, userID);
            }
        });
        containerProfile.setOnClickListener(v -> {
            if (AppHelper.isAndroid5())
                containerProfileInfo.post(() -> AnimationsUtil.hide(this, containerProfileInfo, Duration));
            else
                finish();
        });
        containerProfileInfo.setOnClickListener(v -> {
            if (AppHelper.isAndroid5())
                containerProfileInfo.post(() -> AnimationsUtil.hide(this, containerProfileInfo, Duration));
            else
                finish();
        });
        userProfilePicture.setOnClickListener(v -> {
            if (isImageLoaded) {
                if (ImageUrlFile != null) {
                    if (FilesManager.isFilePhotoProfileExists(this, FilesManager.getProfileImage(ImageUrlFile))) {
                        AppHelper.LaunchImagePreviewActivity(this, AppConstants.PROFILE_IMAGE, ImageUrlFile);
                    } else {
                        AppHelper.LaunchImagePreviewActivity(this, AppConstants.PROFILE_IMAGE_FROM_SERVER, ImageUrlFile);
                    }
                }

            }
        });

    }

    /**
     * method to setup the progressBar
     */
    private void setupProgressBar() {
        ProgressBar mProgress = (ProgressBar) findViewById(R.id.progress_bar);
        mProgress.getIndeterminateDrawable().setColorFilter(Color.parseColor("#0EC654"),
                PorterDuff.Mode.SRC_IN);


    }


    /**
     * method to show user information
     *
     * @param contactsModels this is parameter for  ShowContact method
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void ShowContact(ContactsModel contactsModels) {
        contactsModel = contactsModels;
        try {
            UpdateUI(contactsModels, null);
        } catch (Exception e) {
            AppHelper.LogCat(" Profile preview Exception" + e.getMessage());
        }
    }

    /**
     * method to show group information
     *
     * @param groupsModel this is parameter for   ShowGroup method
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void ShowGroup(GroupsModel groupsModel) {

        try {
            UpdateUI(null, groupsModel);
        } catch (Exception e) {
            AppHelper.LogCat(" Profile preview Exception" + e.getMessage());
        }

    }

    /**
     * method to update the UI
     *
     * @param mContactsModel this is the first parameter for  UpdateUI  method
     * @param mGroupsModel   this is the second parameter for   UpdateUI  method
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void UpdateUI(ContactsModel mContactsModel, GroupsModel mGroupsModel) {


        String imageUrlHolder;
        if (isGroup) {
            ImageUrlFile = mGroupsModel.getGroupImage();
            ImageUrl = EndPoints.PROFILE_PREVIEW_IMAGE_URL + ImageUrlFile;
            imageUrlHolder = EndPoints.PROFILE_PREVIEW_HOLDER_IMAGE_URL + ImageUrlFile;

            if (mGroupsModel.getGroupName() != null) {
                String groupname = UtilsString.unescapeJava(mGroupsModel.getGroupName());
                if (groupname.length() > 18)
                    userProfileName.setText(groupname.substring(0, 18) + "... " + "");
                else
                    userProfileName.setText(groupname);

            }
            Bitmap bitmap = ImageLoader.GetCachedBitmapImage(memoryCache, ImageUrlFile, this, mGroupsModel.getId(), AppConstants.GROUP, AppConstants.PROFILE_PREVIEW);
            if (bitmap != null) {
                userProfilePicture.setImageBitmap(bitmap);
                isImageLoaded = true;
            } else {
                Bitmap holderBitmap = ImageLoader.GetCachedBitmapImage(memoryCache, ImageUrlFile, this, mGroupsModel.getId(), AppConstants.GROUP, AppConstants.ROW_PROFILE);

                Bitmap blurredbitmap = ImageLoader.BlurBitmap(holderBitmap, this);
                Drawable drawable;
                if (blurredbitmap != null)
                    drawable = new BitmapDrawable(getResources(), blurredbitmap);
                else
                    drawable = AppHelper.getDrawable(this, R.drawable.image_holder_gp);
                Picasso.with(this)
                        .load(imageUrlHolder)
                        .transform(new BlurTransformation(this, AppConstants.BLUR_RADIUS))
                        .centerCrop()
                        .placeholder(drawable)
                        .error(drawable)
                        .resize(AppConstants.PROFILE_PREVIEW_BLUR_IMAGE_SIZE, AppConstants.PROFILE_PREVIEW_BLUR_IMAGE_SIZE)
                        .into(userProfilePicture, new Callback() {
                            @Override
                            public void onSuccess() {
                                AppHelper.LogCat("onSuccess ProfilePreviewActivity");
                                Picasso.with(ProfilePreviewActivity.this)
                                        .load(ImageUrl)
                                        .resize(AppConstants.PROFILE_PREVIEW_IMAGE_SIZE, AppConstants.PROFILE_PREVIEW_IMAGE_SIZE)
                                        .centerCrop()
                                        .noPlaceholder()
                                        .into(userProfilePicture);
                                isImageLoaded = true;
                                ImageLoader.DownloadImage(memoryCache, ImageUrl, ImageUrlFile, ProfilePreviewActivity.this, mGroupsModel.getId(), AppConstants.GROUP, AppConstants.PROFILE_PREVIEW);
                            }

                            @Override
                            public void onError() {
                                isImageLoaded = false;
                                userProfilePicture.setImageDrawable(drawable);
                                AppHelper.LogCat("onError ProfilePreviewActivity");
                            }
                        });
            }
            actionProfileArea.setVisibility(View.VISIBLE);

        } else {

            if (mContactsModel.isLinked() && mContactsModel.isActivate()) {
                actionProfileArea.setVisibility(View.VISIBLE);
                actionProfileInvite.setVisibility(View.GONE);
            } else {
                actionProfileArea.setVisibility(View.GONE);
                actionProfileInvite.setVisibility(View.VISIBLE);
            }
            ImageUrlFile = mContactsModel.getImage();
            ImageUrl = EndPoints.PROFILE_PREVIEW_IMAGE_URL + ImageUrlFile;
            imageUrlHolder = EndPoints.PROFILE_PREVIEW_HOLDER_IMAGE_URL + ImageUrlFile;
            String name = UtilsPhone.getContactName(this, mContactsModel.getPhone());
            if (name != null) {
                userProfileName.setText(name);
            } else {
                userProfileName.setText(mContactsModel.getPhone());
            }

            Bitmap bitmap = ImageLoader.GetCachedBitmapImage(memoryCache, ImageUrlFile, this, mContactsModel.getId(), AppConstants.USER, AppConstants.PROFILE_PREVIEW);
            if (bitmap != null) {
                userProfilePicture.setImageBitmap(bitmap);
                isImageLoaded = true;
            } else {

                Bitmap holderBitmap = ImageLoader.GetCachedBitmapImage(memoryCache, ImageUrlFile, this, mContactsModel.getId(), AppConstants.USER, AppConstants.ROW_PROFILE);
                Bitmap blurredbitmap = ImageLoader.BlurBitmap(holderBitmap, this);
                Drawable drawable;
                if (blurredbitmap != null)
                    drawable = new BitmapDrawable(getResources(), blurredbitmap);
                else
                    drawable = AppHelper.getDrawable(this, R.drawable.image_holder_up);
                Picasso.with(this)
                        .load(imageUrlHolder)
                        .transform(new BlurTransformation(this, AppConstants.BLUR_RADIUS))
                        .centerCrop()
                        .placeholder(drawable)
                        .error(drawable)
                        .resize(AppConstants.PROFILE_PREVIEW_BLUR_IMAGE_SIZE, AppConstants.PROFILE_PREVIEW_BLUR_IMAGE_SIZE)
                        .into(userProfilePicture, new Callback() {
                            @Override
                            public void onSuccess() {
                                AppHelper.LogCat("onSuccess ProfilePreviewActivity ");
                                Picasso.with(ProfilePreviewActivity.this)
                                        .load(ImageUrl)
                                        .resize(AppConstants.PROFILE_PREVIEW_IMAGE_SIZE, AppConstants.PROFILE_PREVIEW_IMAGE_SIZE)
                                        .centerCrop()
                                        .noPlaceholder()
                                        .into(userProfilePicture);
                                isImageLoaded = true;
                                ImageLoader.DownloadImage(memoryCache, ImageUrl, ImageUrlFile, ProfilePreviewActivity.this, mContactsModel.getId(), AppConstants.USER, AppConstants.PROFILE_PREVIEW);
                            }

                            @Override
                            public void onError() {
                                isImageLoaded = false;
                                userProfilePicture.setImageDrawable(drawable);
                                AppHelper.LogCat("onError ProfilePreviewActivity");
                            }
                        });
            }

        }
    }

    public void onErrorLoading(Throwable throwable) {
        AppHelper.LogCat(throwable.getMessage());
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mProfilePresenter.onDestroy();
    }


    @Override
    public void onBackPressed() {
        if (AppHelper.isAndroid5())
            containerProfileInfo.post(() -> AnimationsUtil.hide(this, containerProfileInfo, Duration));
        else
            finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // If we've received a touch notification that the user has touched
        // outside the app, finish the activity.
        if (MotionEvent.ACTION_OUTSIDE == event.getAction()) {
            if (AppHelper.isAndroid5())
                containerProfileInfo.post(() -> AnimationsUtil.hide(this, containerProfileInfo, Duration));
            else
                finish();
            return true;
        }

        return super.onTouchEvent(event);
    }


}
