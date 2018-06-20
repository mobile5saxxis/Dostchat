package com.dostchat.dost.presenters.users;


import com.dostchat.dost.R;
import com.dostchat.dost.activities.profile.ProfileActivity;
import com.dostchat.dost.api.APIService;
import com.dostchat.dost.api.apiServices.GroupsService;
import com.dostchat.dost.api.apiServices.MessagesService;
import com.dostchat.dost.api.apiServices.UsersContacts;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.app.DostChatApp;
import com.dostchat.dost.fragments.media.DocumentsFragment;
import com.dostchat.dost.fragments.media.LinksFragment;
import com.dostchat.dost.fragments.media.MediaFragment;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.PreferenceManager;
import com.dostchat.dost.helpers.UtilsPhone;
import com.dostchat.dost.helpers.notifications.NotificationsManager;
import com.dostchat.dost.interfaces.Presenter;
import com.dostchat.dost.models.groups.GroupsModel;
import com.dostchat.dost.models.groups.MembersGroupModel;
import com.dostchat.dost.models.messages.ConversationsModel;
import com.dostchat.dost.models.messages.MessagesModel;
import com.dostchat.dost.models.users.Pusher;
import com.dostchat.dost.models.users.contacts.ContactsModel;

import org.joda.time.DateTime;

import java.util.List;

import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

import static com.dostchat.dost.app.AppConstants.EVENT_BUS_DELETE_CONVERSATION_ITEM;
import static com.dostchat.dost.app.AppConstants.EVENT_BUS_EXIT_NEW_GROUP;
import static com.dostchat.dost.app.AppConstants.EVENT_BUS_MESSAGE_COUNTER;
import static com.dostchat.dost.app.AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_OLD_ROW;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class ProfilePresenter implements Presenter {
    private ProfileActivity profileActivity;
    private MediaFragment mediaFragment;
    private DocumentsFragment documentFragment;
    private LinksFragment linksFragment;
    private final Realm realm;
    private int groupID;
    private int userID;
    private GroupsService mGroupsService;
    private MessagesService mMessagesService;
    private APIService mApiService;

    public ProfilePresenter(ProfileActivity profileActivity) {
        this.profileActivity = profileActivity;
        this.realm = DostChatApp.getRealmDatabaseInstance();

    }

    public ProfilePresenter(MediaFragment mediaFragment) {
        this.mediaFragment = mediaFragment;
        this.realm = DostChatApp.getRealmDatabaseInstance();

    }

    public ProfilePresenter(DocumentsFragment documentFragment) {
        this.documentFragment = documentFragment;
        this.realm = DostChatApp.getRealmDatabaseInstance();

    }

    public ProfilePresenter(LinksFragment linksFragment) {
        this.linksFragment = linksFragment;
        this.realm = DostChatApp.getRealmDatabaseInstance();

    }


    @Override
    public void onStart() {

    }

    @Override
    public void onCreate() {

        mMessagesService = new MessagesService(realm);
        if (profileActivity != null) {
            if (!EventBus.getDefault().isRegistered(profileActivity))
                EventBus.getDefault().register(profileActivity);
            mApiService = APIService.with(profileActivity);
            if (profileActivity.getIntent().hasExtra("userID")) {
                userID = profileActivity.getIntent().getExtras().getInt("userID");
                loadContactData(userID);
                try {
                    loadUserMediaData(userID);
                } catch (Exception e) {
                    AppHelper.LogCat("Media Execption");
                }

            }


            if (profileActivity.getIntent().hasExtra("groupID")) {
                groupID = profileActivity.getIntent().getExtras().getInt("groupID");
                loadGroupData(groupID);
                try {
                    loadGroupMediaData(groupID);
                } catch (Exception e) {
                    AppHelper.LogCat("Media Execption");
                }
            }
        } else {

            if (mediaFragment != null) {
                mApiService = APIService.with(mediaFragment.getActivity());

                if (mediaFragment.getActivity().getIntent().hasExtra("userID")) {
                    userID = mediaFragment.getActivity().getIntent().getExtras().getInt("userID");
                    try {
                        loadUserMediaData(userID);
                    } catch (Exception e) {
                        AppHelper.LogCat("Media Execption");
                    }

                }
                if (mediaFragment.getActivity().getIntent().hasExtra("groupID")) {
                    groupID = mediaFragment.getActivity().getIntent().getExtras().getInt("groupID");
                    try {
                        loadGroupMediaData(groupID);
                    } catch (Exception e) {
                        AppHelper.LogCat("Media Execption");
                    }

                }
            } else if (documentFragment != null) {
                mApiService = APIService.with(documentFragment.getActivity());

                if (documentFragment.getActivity().getIntent().hasExtra("userID")) {
                    userID = documentFragment.getActivity().getIntent().getExtras().getInt("userID");
                    try {
                        loadUserMediaData(userID);
                    } catch (Exception e) {
                        AppHelper.LogCat("Media Execption");
                    }

                }
                if (documentFragment.getActivity().getIntent().hasExtra("groupID")) {
                    groupID = documentFragment.getActivity().getIntent().getExtras().getInt("groupID");
                    try {
                        loadGroupMediaData(groupID);
                    } catch (Exception e) {
                        AppHelper.LogCat("Media Execption");
                    }

                }
            } else if (linksFragment != null) {
                mApiService = APIService.with(linksFragment.getActivity());

                if (linksFragment.getActivity().getIntent().hasExtra("userID")) {
                    userID = linksFragment.getActivity().getIntent().getExtras().getInt("userID");
                    try {
                        loadUserMediaData(userID);
                    } catch (Exception e) {
                        AppHelper.LogCat("Media Execption");
                    }

                }
                if (linksFragment.getActivity().getIntent().hasExtra("groupID")) {
                    groupID = linksFragment.getActivity().getIntent().getExtras().getInt("groupID");
                    try {
                        loadGroupMediaData(groupID);
                    } catch (Exception e) {
                        AppHelper.LogCat("Media Execption");
                    }

                }
            }


        }


    }


    private void loadUserMediaData(int userID) {
        if (profileActivity != null)
            mMessagesService.getUserMedia(userID, PreferenceManager.getID(profileActivity)).subscribe(profileActivity::ShowMedia, profileActivity::onErrorLoading);
        if (mediaFragment != null)
            mMessagesService.getUserMedia(userID, PreferenceManager.getID(mediaFragment.getActivity())).subscribe(mediaFragment::ShowMedia, mediaFragment::onErrorLoading);
        else if (documentFragment != null)
            mMessagesService.getUserDocuments(userID, PreferenceManager.getID(documentFragment.getActivity())).subscribe(documentFragment::ShowMedia, documentFragment::onErrorLoading);
        else if (linksFragment != null)
            mMessagesService.getUserLinks(userID, PreferenceManager.getID(linksFragment.getActivity())).subscribe(linksFragment::ShowMedia, linksFragment::onErrorLoading);

    }

    private void loadGroupMediaData(int groupID) {
        if (profileActivity != null)
            mMessagesService.getGroupMedia(groupID).subscribe(profileActivity::ShowMedia, profileActivity::onErrorLoading);
        else if (mediaFragment != null)
            mMessagesService.getGroupMedia(groupID).subscribe(mediaFragment::ShowMedia, mediaFragment::onErrorLoading);
        else if (documentFragment != null)
            mMessagesService.getGroupDocuments(groupID).subscribe(documentFragment::ShowMedia, documentFragment::onErrorLoading);
        else if (linksFragment != null)
            mMessagesService.getGroupLinks(groupID).subscribe(linksFragment::ShowMedia, linksFragment::onErrorLoading);
    }

    private void loadContactData(int userID) {
        UsersContacts mUsersContacts = new UsersContacts(realm, profileActivity, mApiService);
        try {
            mUsersContacts.getContact(userID).subscribe(profileActivity::ShowContact, profileActivity::onErrorLoading);
            mUsersContacts.getContactInfo(userID).subscribe(profileActivity::ShowContact, profileActivity::onErrorLoading);
        } catch (Exception e) {
            AppHelper.LogCat("" + e.getMessage());
        }


    }

    private void loadGroupData(int groupID) {
        mGroupsService = new GroupsService(realm, profileActivity, mApiService);
        mGroupsService.getGroup(groupID).subscribe(profileActivity::ShowGroup, profileActivity::onErrorLoading);
        mGroupsService.getGroupInfo(groupID).subscribe(profileActivity::ShowGroup, profileActivity::onErrorLoading);
        mGroupsService.getGroupMembers(groupID).subscribe(profileActivity::ShowGroupMembers, profileActivity::onErrorLoading);
        mGroupsService.updateGroupMembers(groupID).subscribe(membersGroupModels -> {
            mGroupsService.getGroupMembers(groupID).subscribe(profileActivity::ShowGroupMembers, profileActivity::onErrorLoading);
        }, profileActivity::onErrorLoading);
    }


    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onDestroy() {
        if (profileActivity != null)
            EventBus.getDefault().unregister(profileActivity);
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

    public void updateUIGroupData(int groupID) {
        mGroupsService.getGroup(groupID).subscribe(profileActivity::UpdateGroupUI, profileActivity::onErrorLoading);
        mGroupsService.getGroupInfo(groupID).subscribe(profileActivity::UpdateGroupUI, profileActivity::onErrorLoading);
        mGroupsService.getGroupMembers(groupID).subscribe(profileActivity::ShowGroupMembers, profileActivity::onErrorLoading);
    }

    public void ExitGroup() {
        mGroupsService.ExitGroup(groupID).subscribe(statusResponse -> {
            if (statusResponse.isSuccess()) {
                realm.executeTransactionAsync(realm1 -> {
                    DateTime current = new DateTime();
                    String sendTime = String.valueOf(current);
                    ConversationsModel conversationsModel = realm1.where(ConversationsModel.class).equalTo("groupID", groupID).findFirst();
                    ContactsModel contactsModel = realm1.where(ContactsModel.class).equalTo("id", PreferenceManager.getID(profileActivity)).findFirst();
                    String name = UtilsPhone.getContactName(profileActivity, contactsModel.getPhone());


                    int lastID = 1;
                    try {

                        List<MessagesModel> messagesModel1 = realm1.where(MessagesModel.class).findAll();
                        lastID = messagesModel1.size();
                        lastID++;

                        AppHelper.LogCat("last ID group message" + lastID);

                    } catch (Exception e) {
                        AppHelper.LogCat("last message  ID   0 Exception" + e.getMessage());
                    }

                    RealmList<MessagesModel> messagesModelRealmList = conversationsModel.getMessages();
                    MessagesModel messagesModel = new MessagesModel();
                    messagesModel.setId(lastID);
                    messagesModel.setDate(sendTime);
                    messagesModel.setSenderID(contactsModel.getId());
                    messagesModel.setStatus(AppConstants.IS_WAITING);

                    if (name != null) {
                        messagesModel.setUsername(name);
                    } else {
                        messagesModel.setUsername(contactsModel.getPhone());
                    }
                    messagesModel.setGroup(true);
                    messagesModel.setMessage(AppConstants.LEFT_GROUP);
                    messagesModel.setGroupID(groupID);
                    messagesModel.setConversationID(conversationsModel.getId());
                    messagesModel.setImageFile("null");
                    messagesModel.setVideoFile("null");
                    messagesModel.setAudioFile("null");
                    messagesModel.setDocumentFile("null");
                    messagesModel.setVideoThumbnailFile("null");
                    messagesModel.setFileUpload(true);
                    messagesModel.setFileDownLoad(true);
                    messagesModel.setFileSize("0");
                    messagesModel.setDuration("0");
                    messagesModelRealmList.add(messagesModel);
                    conversationsModel.setLastMessage(AppConstants.LEFT_GROUP);
                    conversationsModel.setLastMessageId(lastID);
                    conversationsModel.setMessages(messagesModelRealmList);
                    conversationsModel.setStatus(AppConstants.IS_WAITING);
                    conversationsModel.setUnreadMessageCounter("0");
                    conversationsModel.setCreatedOnline(true);
                    realm1.copyToRealmOrUpdate(conversationsModel);
                    MembersGroupModel membersGroupModel = realm1.where(MembersGroupModel.class).equalTo("userId", PreferenceManager.getID(profileActivity)).findFirst();
                    membersGroupModel.setLeft(true);
                    membersGroupModel.setAdmin(false);
                    realm1.copyToRealmOrUpdate(membersGroupModel);
                    EventBus.getDefault().post(new Pusher(EVENT_BUS_EXIT_NEW_GROUP, groupID, messagesModel));
                    EventBus.getDefault().post(new Pusher(EVENT_BUS_NEW_MESSAGE_CONVERSATION_OLD_ROW, conversationsModel.getId()));
                }, () -> {
                    AppHelper.hideDialog();
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_EXIT_GROUP, statusResponse.getMessage()));
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_EXIT_THIS_GROUP, groupID));
                }, error -> {
                    AppHelper.hideDialog();
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_EXIT_GROUP, profileActivity.getString(R.string.failed_exit_group)));
                    AppHelper.LogCat("error while exiting group" + error.getMessage());
                });
            } else {
                AppHelper.hideDialog();
                AppHelper.Snackbar(profileActivity, profileActivity.findViewById(R.id.containerProfile), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
            }
        }, throwable -> {
            try {
                AppHelper.hideDialog();
                profileActivity.onErrorExiting();
            } catch (Exception e) {
                AppHelper.LogCat(e);
            }
        });

    }

    public void DeleteGroup() {
        mGroupsService.DeleteGroup(groupID).subscribe(statusResponse -> {
            if (statusResponse.isSuccess()) {
                AppHelper.hideDialog();
                ConversationsModel conversationsModel = realm.where(ConversationsModel.class).equalTo("groupID", groupID).findFirst();
                int conversationID = conversationsModel.getId();
                EventBus.getDefault().post(new Pusher(EVENT_BUS_DELETE_CONVERSATION_ITEM, conversationID));
                realm.executeTransactionAsync(realm1 -> {
                    RealmResults<MessagesModel> messagesModel1 = realm1.where(MessagesModel.class).equalTo("conversationID", conversationID).findAll();
                    messagesModel1.deleteAllFromRealm();
                }, () -> {
                    AppHelper.LogCat("Message Deleted  successfully  ProfilePresenter");

                    realm.executeTransactionAsync(realm1 -> {
                        ConversationsModel conversationsModel1 = realm1.where(ConversationsModel.class).equalTo("id", conversationID).findFirst();
                        conversationsModel1.deleteFromRealm();
                        GroupsModel groupsModel = realm1.where(GroupsModel.class).equalTo("id", groupID).findFirst();
                        groupsModel.deleteFromRealm();
                    }, () -> {
                        AppHelper.LogCat("Conversation deleted successfully ProfilePresenter");
                        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_DELETE_GROUP, statusResponse.getMessage()));
                        EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
                        NotificationsManager.SetupBadger(profileActivity);
                    }, error -> {
                        AppHelper.LogCat("Delete conversation failed  ProfilePresenter" + error.getMessage());

                    });
                }, error -> {

                    AppHelper.LogCat("Delete message failed ProfilePresenter" + error.getMessage());

                });

            } else {
                AppHelper.hideDialog();
                AppHelper.Snackbar(profileActivity, profileActivity.findViewById(R.id.containerProfile), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
            }
        }, throwable -> {
            try {
                AppHelper.hideDialog();
                profileActivity.onErrorDeleting();
            } catch (Exception e) {
                AppHelper.LogCat(e);
            }

        });
    }
}