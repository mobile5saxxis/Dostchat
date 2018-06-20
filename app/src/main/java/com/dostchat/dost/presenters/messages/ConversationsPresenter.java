package com.dostchat.dost.presenters.messages;

import com.crashlytics.android.Crashlytics;
import com.dostchat.dost.api.APIService;
import com.dostchat.dost.api.apiServices.ConversationsService;
import com.dostchat.dost.api.apiServices.GroupsService;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.app.DostChatApp;
import com.dostchat.dost.fragments.home.ConversationsFragment;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.notifications.NotificationsManager;
import com.dostchat.dost.interfaces.Presenter;
import com.dostchat.dost.models.groups.GroupsModel;
import com.dostchat.dost.models.messages.ConversationsModel;
import com.dostchat.dost.models.messages.MessagesModel;
import com.dostchat.dost.models.users.Pusher;

import java.util.List;

import de.greenrobot.event.EventBus;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class ConversationsPresenter implements Presenter {
    private final ConversationsFragment conversationsFragmentView;
    private final Realm realm;
    private ConversationsService mConversationsService;
    private GroupsService mGroupsService;

    public ConversationsPresenter(ConversationsFragment conversationsFragment) {
        this.conversationsFragmentView = conversationsFragment;
        this.realm = DostChatApp.getRealmDatabaseInstance();
    }


    @Override
    public void onStart() {
    }

    @Override
    public void onCreate() {
        if (!EventBus.getDefault().isRegistered(conversationsFragmentView))
            EventBus.getDefault().register(conversationsFragmentView);
        APIService mApiService = APIService.with(conversationsFragmentView.getContext());
        mConversationsService = new ConversationsService(realm);
        mGroupsService = new GroupsService(realm, conversationsFragmentView.getContext(), mApiService);
        loadData();


    }

    private boolean checkIfGroupExist(int groupId, Realm realm) {
        RealmQuery<GroupsModel> query = realm.where(GroupsModel.class).equalTo("id", groupId);
        return query.count() != 0;

    }

    private void updateGroups() {
        Observable.create((ObservableOnSubscribe<List<GroupsModel>>) subscriber -> {
            try {
                mGroupsService.updateGroups().subscribe(groupsModels -> {
                    subscriber.onNext(groupsModels);
                    subscriber.onComplete();
                }, subscriber::onError);

            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(groupsModelList -> {
            AppHelper.LogCat("groupsModelList " + groupsModelList.size());
        }, AppHelper::LogCat);
        if (mGroupsService.checkIfZeroExist()) {
            realm.executeTransactionAsync(realm1 -> {
                RealmResults<MessagesModel> messagesModel = realm1.where(MessagesModel.class).equalTo("id", 0).findAll();
                for (MessagesModel messagesModel1 : messagesModel) {
                    messagesModel1.deleteFromRealm();
                }
            }, () -> {
                AppHelper.LogCat("messagesModel with 0 id removed");
                NotificationsManager.SetupBadger(conversationsFragmentView.getActivity());
            }, error -> {
                AppHelper.LogCat("messagesModel with 0 id failed to remove " + error.getMessage());
            });
        }

    }

    private void loadData() {
        conversationsFragmentView.onShowLoading();
        try {
            updateGroups();
            mConversationsService.getConversations().subscribe(conversationsModels -> {
                conversationsFragmentView.UpdateConversation(conversationsModels);
                conversationsFragmentView.onHideLoading();
            }, conversationsFragmentView::onErrorLoading, conversationsFragmentView::onHideLoading);
        } catch (Exception e) {
            AppHelper.LogCat("conversation presenter " + e.getMessage());
            Crashlytics.logException(e);
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
        EventBus.getDefault().unregister(conversationsFragmentView);
        realm.close();
    }

    @Override
    public void onLoadMore() {
    }

    @Override
    public void onRefresh() {
        AppHelper.LogCat("onRefresh called ");
        loadData();
    }

    @Override
    public void onStop() {

    }

    public void getGroupInfo(int groupID) {
        AppHelper.LogCat("update image group profile");
        mGroupsService.getGroupInfo(groupID).subscribe(groupsModel -> {
            int ConversationID = getConversationGroupId(groupsModel.getId());
            if (ConversationID != 0) {
                realm.executeTransaction(realm1 -> {
                    ConversationsModel conversationsModel = realm1.where(ConversationsModel.class).equalTo("id", ConversationID).findFirst();
                    conversationsModel.setRecipientImage(groupsModel.getGroupImage());
                    conversationsModel.setRecipientUsername(groupsModel.getGroupName());
                    realm1.copyToRealmOrUpdate(conversationsModel);
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_UPDATE_CONVERSATION_OLD_ROW, ConversationID));
                });
            }
        }, throwable -> AppHelper.LogCat("Get group info conversation presenter " + throwable.getMessage()));
    }

    private int getConversationGroupId(int GroupID) {
        try {
            ConversationsModel conversationsModel = realm.where(ConversationsModel.class).equalTo("groupID", GroupID).findFirst();
            return conversationsModel.getId();
        } catch (Exception e) {
            AppHelper.LogCat("Conversation id Exception ContactFragment" + e.getMessage());
            return 0;
        }
    }

    public void getGroupInfo(int groupID, MessagesModel messagesModel) {
        AppHelper.LogCat("group id exited " + groupID);
        mGroupsService.getGroupInfo(groupID).subscribe(groupsModel -> conversationsFragmentView.sendGroupMessage(groupsModel, messagesModel), throwable -> AppHelper.LogCat("Get group info conversation presenter " + throwable.getMessage()));
    }

    public void getGroupInfo(int groupID, int conversationID) {
        AppHelper.LogCat("group id created " + groupID);
        mGroupsService.getGroupInfo(groupID).subscribe(groupsModel -> conversationsFragmentView.sendGroupMessage(groupsModel, conversationID), throwable -> AppHelper.LogCat("Get group info conversation presenter  2 " + throwable.getMessage()));
    }
}
