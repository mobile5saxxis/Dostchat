package com.dostchat.dost.presenters.users;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.provider.MediaStore;

import com.dostchat.dost.R;
import com.dostchat.dost.activities.profile.EditProfileActivity;
import com.dostchat.dost.activities.profile.EditUsernameActivity;
import com.dostchat.dost.api.APIService;
import com.dostchat.dost.api.apiServices.UsersContacts;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.app.DostChatApp;
import com.dostchat.dost.fragments.bottomSheets.BottomSheetEditProfile;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.Files.FilesManager;
import com.dostchat.dost.helpers.PermissionHandler;
import com.dostchat.dost.helpers.PreferenceManager;
import com.dostchat.dost.interfaces.Presenter;
import com.dostchat.dost.models.users.Pusher;

import java.io.File;

import de.greenrobot.event.EventBus;
import io.realm.Realm;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class EditProfilePresenter implements Presenter {
    private EditProfileActivity view;
    private EditUsernameActivity editUsernameActivity;
    private Realm realm;
    private UsersContacts mUsersContacts;
    private boolean isEditUsername = false;

    public EditProfilePresenter(EditProfileActivity editProfileActivity) {
        this.view = editProfileActivity;
        this.realm = DostChatApp.getRealmDatabaseInstance();

    }

    public EditProfilePresenter() {
        this.realm = DostChatApp.getRealmDatabaseInstance();
    }

    public EditProfilePresenter(EditUsernameActivity editUsernameActivity, boolean b) {
        this.isEditUsername = b;
        this.editUsernameActivity = editUsernameActivity;
        this.realm = DostChatApp.getRealmDatabaseInstance();
    }


    @Override
    public void onStart() {

    }

    @Override
    public void
    onCreate() {
        APIService mApiService;
        if (!isEditUsername) {
            mApiService = APIService.with(view);
            mUsersContacts = new UsersContacts(realm, view, mApiService);
            loadData();
        } else {
            mApiService = APIService.with(editUsernameActivity);
            this.mUsersContacts = new UsersContacts(this.realm, editUsernameActivity, mApiService);
        }
    }

    public void loadData() {
        try {
            mUsersContacts.getContact(PreferenceManager.getID(view)).subscribe(view::ShowContact, view::onErrorLoading);
            mUsersContacts.getContactInfo(PreferenceManager.getID(view)).subscribe(view::ShowContact, view::onErrorLoading);
        } catch (Exception e) {
            AppHelper.LogCat(" EditProfileActivity Exception " + e.getMessage());
        }

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onDestroy() {
        realm.close();
    }

    @Override
    public void onLoadMore() {

    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onStop() {

    }


    public void onActivityResult(BottomSheetEditProfile bottomSheetEditProfile, int requestCode, int resultCode, Intent data) {
        String imagePath = null;
        if (resultCode == Activity.RESULT_OK) {
            if (PermissionHandler.checkPermission(bottomSheetEditProfile.getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                AppHelper.LogCat("Read contact data permission already granted.");

                switch (requestCode) {
                    case AppConstants.SELECT_PROFILE_PICTURE:
                        imagePath = FilesManager.getPath(bottomSheetEditProfile.getActivity(), data.getData());
                        break;
                    case AppConstants.SELECT_PROFILE_CAMERA:
                        if (data.getData() != null) {
                            imagePath = FilesManager.getPath(bottomSheetEditProfile.getActivity(), data.getData());
                        } else {
                            try {
                                String[] projection = new String[]{MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA, MediaStore
                                        .Images.ImageColumns.BUCKET_DISPLAY_NAME, MediaStore.Images.ImageColumns.DATE_TAKEN, MediaStore.Images
                                        .ImageColumns.MIME_TYPE};
                                final Cursor cursor = bottomSheetEditProfile.getActivity().getContentResolver()
                                        .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.ImageColumns
                                                .DATE_TAKEN + " DESC");

                                if (cursor != null && cursor.moveToFirst()) {
                                    String imageLocation = cursor.getString(1);
                                    cursor.close();
                                    File imageFile = new File(imageLocation);
                                    if (imageFile.exists()) {
                                        imagePath = imageFile.getPath();
                                    }
                                }
                            } catch (Exception e) {
                                AppHelper.LogCat("error" + e);
                            }
                        }
                        break;
                }


                if (imagePath != null) {
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_IMAGE_PROFILE_PATH, imagePath));
                } else {
                    AppHelper.LogCat("imagePath is null");
                }
            } else {
                AppHelper.LogCat("Please request Read contact data permission.");
                PermissionHandler.requestPermission(bottomSheetEditProfile.getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }

    }


    public void EditCurrentName(String name) {
        mUsersContacts.editUsername(name).subscribe(statusResponse -> {
            if (statusResponse.isSuccess()) {
                AppHelper.Snackbar(editUsernameActivity.getBaseContext(), editUsernameActivity.findViewById(R.id.ParentLayoutStatusEdit), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_USERNAME_PROFILE_UPDATED));
                editUsernameActivity.finish();
            } else {
                AppHelper.Snackbar(editUsernameActivity.getBaseContext(), editUsernameActivity.findViewById(R.id.ParentLayoutStatusEdit), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
            }
        }, AppHelper::LogCat);

    }

}