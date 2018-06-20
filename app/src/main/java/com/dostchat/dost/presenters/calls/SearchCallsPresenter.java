package com.dostchat.dost.presenters.calls;


import com.dostchat.dost.activities.search.SearchCallsActivity;
import com.dostchat.dost.api.APIService;
import com.dostchat.dost.api.apiServices.UsersContacts;
import com.dostchat.dost.app.DostChatApp;
import com.dostchat.dost.interfaces.Presenter;

import io.realm.Realm;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class SearchCallsPresenter implements Presenter {
    private SearchCallsActivity mSearchCallsActivity;
    private Realm realm;
    private UsersContacts mUsersContacts;


    public SearchCallsPresenter(SearchCallsActivity mSearchCallsActivity) {
        this.mSearchCallsActivity = mSearchCallsActivity;
        this.realm = DostChatApp.getRealmDatabaseInstance();
    }


    @Override
    public void onStart() {

    }

    @Override
    public void onCreate() {
        APIService mApiService = APIService.with(this.mSearchCallsActivity);
        mUsersContacts = new UsersContacts(realm, this.mSearchCallsActivity, mApiService);
        loadLocalData();
    }

    private void loadLocalData() {
        mUsersContacts.getAllCalls().subscribe(mSearchCallsActivity::ShowCalls, mSearchCallsActivity::onErrorLoading);
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