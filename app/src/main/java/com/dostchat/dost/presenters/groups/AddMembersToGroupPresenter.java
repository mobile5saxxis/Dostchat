package com.dostchat.dost.presenters.groups;

import com.dostchat.dost.activities.groups.AddMembersToGroupActivity;
import com.dostchat.dost.api.APIService;
import com.dostchat.dost.app.DostChatApp;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.interfaces.Presenter;
import com.dostchat.dost.api.apiServices.UsersContacts;

import io.realm.Realm;

/**
 * Created by Abderrahim El imame on 26/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class AddMembersToGroupPresenter implements Presenter {
    private final AddMembersToGroupActivity view;
    private final Realm realm;


    public AddMembersToGroupPresenter(AddMembersToGroupActivity addMembersToGroupActivity) {
        this.view = addMembersToGroupActivity;
        this.realm = DostChatApp.getRealmDatabaseInstance();

    }


    @Override
    public void onStart() {

    }

    @Override
    public void onCreate() {

        APIService mApiService = APIService.with(view);
        UsersContacts mUsersContacts = new UsersContacts(realm, view, mApiService);
        mUsersContacts.getLinkedContacts().subscribe(view::ShowContacts, throwable -> AppHelper.LogCat("AddMembersToGroupPresenter "+throwable.getMessage()));

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
}