package com.dostchat.dost.presenters.users;


import com.dostchat.dost.activities.settings.SettingsActivity;
import com.dostchat.dost.api.APIService;
import com.dostchat.dost.api.apiServices.UsersContacts;
import com.dostchat.dost.app.DostChatApp;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.PreferenceManager;
import com.dostchat.dost.interfaces.Presenter;

import io.realm.Realm;

/**
 * Created by Abderrahim El imame on 20/02/2016. Email : abderrahim.elimame@gmail.com
 */
public class SettingsPresenter implements Presenter {
    private final SettingsActivity view;
    private final Realm realm;
    private UsersContacts mUsersContacts;

    public SettingsPresenter(SettingsActivity settingsActivity) {
        this.view = settingsActivity;
        this.realm = DostChatApp.getRealmDatabaseInstance();
    }

    @Override
    public void onStart() {

    }

    @Override
    public void
    onCreate() {
        APIService mApiService = APIService.with(view);
        mUsersContacts = new UsersContacts(realm, view, mApiService);
        try {
            loadData();
        } catch (Exception e) {
            AppHelper.LogCat("get contact settings Activity " + e.getMessage());
        }
    }

    public void loadData() {
        mUsersContacts.getContact(PreferenceManager.getID(view)).subscribe(view::ShowContact, AppHelper::LogCat);
        mUsersContacts.getContactInfo(PreferenceManager.getID(view)).subscribe(view::ShowContact, AppHelper::LogCat);
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