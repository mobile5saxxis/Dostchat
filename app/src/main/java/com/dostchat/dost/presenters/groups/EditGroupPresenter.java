package com.dostchat.dost.presenters.groups;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.provider.MediaStore;

import com.dostchat.dost.R;
import com.dostchat.dost.activities.groups.EditGroupActivity;
import com.dostchat.dost.api.APIService;
import com.dostchat.dost.api.apiServices.UsersContacts;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.app.DostChatApp;
import com.dostchat.dost.fragments.bottomSheets.BottomSheetEditGroupImage;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.Files.FilesManager;
import com.dostchat.dost.helpers.PermissionHandler;
import com.dostchat.dost.interfaces.Presenter;
import com.dostchat.dost.models.groups.GroupsModel;
import com.dostchat.dost.models.messages.ConversationsModel;
import com.dostchat.dost.models.users.Pusher;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.socket.client.Socket;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class EditGroupPresenter implements Presenter {
    private EditGroupActivity view;
    private BottomSheetEditGroupImage bottomSheetEditGroupImage;
    private Realm realm;
    private UsersContacts mUsersContacts;
    private APIService mApiService;
    private Socket mSocket;


    public EditGroupPresenter(EditGroupActivity editGroupActivity) {
        this.view = editGroupActivity;
        this.realm = DostChatApp.getRealmDatabaseInstance();

    }

    public EditGroupPresenter(BottomSheetEditGroupImage bottomSheetEditGroupImage) {
        this.bottomSheetEditGroupImage = bottomSheetEditGroupImage;
        this.realm = DostChatApp.getRealmDatabaseInstance();

    }


    @Override
    public void onStart() {

    }

    @Override
    public void
    onCreate() {
        if (view != null) {

            this.mApiService = APIService.with(view);
            this.mUsersContacts = new UsersContacts(this.realm, view, this.mApiService);
            connectToChatServer();
        } else if (bottomSheetEditGroupImage != null) {
            this.mApiService = APIService.with(bottomSheetEditGroupImage.getActivity());
            this.mUsersContacts = new UsersContacts(this.realm, bottomSheetEditGroupImage.getActivity(), this.mApiService);
        }
    }


    /**
     * method to connect to the chat sever by socket
     */
    private void connectToChatServer() {

        DostChatApp app = (DostChatApp) view.getApplication();
        mSocket = app.getSocket();

        if (mSocket == null) {
            DostChatApp.connectSocket();
            mSocket = app.getSocket();
        }
        if (!mSocket.connected())
            mSocket.connect();


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


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String imagePath = null;
        if (resultCode == Activity.RESULT_OK) {
            if (PermissionHandler.checkPermission(bottomSheetEditGroupImage.getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                AppHelper.LogCat("Read storage data permission already granted.");
                switch (requestCode) {
                    case AppConstants.SELECT_ADD_NEW_CONTACT:
                        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_CONTACT_ADDED));
                        break;
                    case AppConstants.SELECT_PROFILE_PICTURE:
                        imagePath = FilesManager.getPath(bottomSheetEditGroupImage.getActivity(), data.getData());
                        break;
                    case AppConstants.SELECT_PROFILE_CAMERA:
                        if (data.getData() != null) {
                            imagePath = FilesManager.getPath(bottomSheetEditGroupImage.getActivity(), data.getData());
                        } else {
                            try {
                                String[] projection = new String[]{MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA, MediaStore
                                        .Images.ImageColumns.BUCKET_DISPLAY_NAME, MediaStore.Images.ImageColumns.DATE_TAKEN, MediaStore.Images
                                        .ImageColumns.MIME_TYPE};
                                final Cursor cursor = bottomSheetEditGroupImage.getActivity().getContentResolver()
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
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_PATH_GROUP, imagePath));
                } else {
                    AppHelper.LogCat("imagePath is null");
                }
            } else {
                AppHelper.LogCat("Please request Read contact data permission.");
                PermissionHandler.requestPermission(bottomSheetEditGroupImage.getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
            }

        }

    }


    public void EditCurrentName(String name, int groupID) {
        mUsersContacts.editGroupName(name, groupID).subscribe(statusResponse -> {
            if (statusResponse.isSuccess()) {
                realm.executeTransactionAsync(realm1 -> {
                            GroupsModel groupsModel = realm1.where(GroupsModel.class).equalTo("id", groupID).findFirst();
                            groupsModel.setGroupName(name);
                            realm1.copyToRealmOrUpdate(groupsModel);
                        }, () -> realm.executeTransactionAsync(realm1 -> {
                            ConversationsModel conversationsModel = realm1.where(ConversationsModel.class).equalTo("groupID", groupID).findFirst();
                            conversationsModel.setRecipientUsername(name);
                            realm1.copyToRealmOrUpdate(conversationsModel);
                        }, () -> {
                            AppHelper.Snackbar(view.getBaseContext(), view.findViewById(R.id.ParentLayoutStatusEdit), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
                            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_UPDATE_GROUP_NAME, groupID));
                            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_CREATE_GROUP));

                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("groupId", groupID);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (mSocket != null)
                                mSocket.emit(AppConstants.SOCKET_IMAGE_GROUP_UPDATED, jsonObject);
                            view.finish();
                        }, error -> AppHelper.LogCat("error update group name in conversation model " + error.getMessage())),
                        error -> AppHelper.LogCat("error update group name in group model  " + error.getMessage()));
            } else {
                AppHelper.Snackbar(view.getBaseContext(), view.findViewById(R.id.ParentLayoutStatusEdit), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
            }
        }, AppHelper::LogCat);

    }

}