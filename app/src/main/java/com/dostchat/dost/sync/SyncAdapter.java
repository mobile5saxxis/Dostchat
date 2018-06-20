package com.dostchat.dost.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import com.dostchat.dost.api.APIService;
import com.dostchat.dost.api.apiServices.UsersContacts;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.app.DostChatApp;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.PreferenceManager;
import com.dostchat.dost.helpers.UtilsPhone;
import com.dostchat.dost.models.users.contacts.ContactsModel;
import com.dostchat.dost.models.users.contacts.PusherContacts;
import com.dostchat.dost.models.users.contacts.SyncContacts;

import java.util.List;

import de.greenrobot.event.EventBus;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;

/**
 * Created by Abderrahim El imame on 01/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private UsersContacts mUsersContacts;
    private Realm realm;
    private Context mContext;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        AppHelper.LogCat("Sync Adapter created." + "Sync Adapter created.");
        realm = DostChatApp.getRealmDatabaseInstance();
        initializer(context);
        this.mContext = context;
    }

    private void initializer(Context mContext) {
        APIService mApiService = APIService.with(mContext);
        mUsersContacts = new UsersContacts(realm, mContext, mApiService);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        AppHelper.LogCat("Sync Adapter called." + "Sync Adapter called.");
        if (PreferenceManager.getToken(mContext) != null) {
            Observable.create((ObservableOnSubscribe<List<ContactsModel>>) subscriber -> {
                try {

                    List<ContactsModel> contactsList = UtilsPhone.GetPhoneContacts(mContext);
                    subscriber.onNext(contactsList);
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(contactsList -> {
                        SyncContacts syncContacts = new SyncContacts();
                        syncContacts.setContactsModelList(contactsList);
                        mUsersContacts.updateContacts(syncContacts).subscribe(contactsModelList -> {
                            if (contactsModelList != null)
                                EventBus.getDefault().post(new PusherContacts(AppConstants.EVENT_BUS_UPDATE_CONTACTS_LIST, contactsModelList));
                        }, throwable -> {
                            EventBus.getDefault().post(new PusherContacts(AppConstants.EVENT_BUS_UPDATE_CONTACTS_LIST_THROWABLE, throwable));
                        });

                    }, throwable -> {
                        EventBus.getDefault().post(new PusherContacts(AppConstants.EVENT_BUS_UPDATE_CONTACTS_LIST_THROWABLE, throwable));
                    });

        }
    }

}
