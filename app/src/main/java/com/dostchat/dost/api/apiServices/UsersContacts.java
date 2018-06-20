package com.dostchat.dost.api.apiServices;

import android.content.Context;

import com.dostchat.dost.api.APIContact;
import com.dostchat.dost.api.APIService;
import com.dostchat.dost.app.EndPoints;
import com.dostchat.dost.helpers.PreferenceManager;
import com.dostchat.dost.helpers.UtilsPhone;
import com.dostchat.dost.models.JoinModel;
import com.dostchat.dost.models.NetworkModel;
import com.dostchat.dost.models.calls.CallsInfoModel;
import com.dostchat.dost.models.calls.CallsModel;
import com.dostchat.dost.models.users.VersionResponse;
import com.dostchat.dost.models.users.contacts.ContactsModel;
import com.dostchat.dost.models.users.contacts.SyncContacts;
import com.dostchat.dost.models.users.contacts.UsersBlockModel;
import com.dostchat.dost.models.users.status.EditStatus;
import com.dostchat.dost.models.users.status.StatusModel;
import com.dostchat.dost.models.users.status.StatusResponse;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class UsersContacts {
    private APIContact mApiContact;
    private Context mContext;
    private Realm realm;
    private APIService mApiService;

    public UsersContacts(Realm realm, Context context, APIService mApiService) {
        this.mContext = context;
        this.realm = realm;
        this.mApiService = mApiService;

    }

    public UsersContacts(Context context, APIService mApiService) {
        this.mContext = context;
        this.mApiService = mApiService;

    }

    /**
     * method to initialize the api contact
     *
     * @return return value
     */
    private APIContact initializeApiContact() {
        if (mApiContact == null) {
            mApiContact = this.mApiService.RootService(APIContact.class, PreferenceManager.getToken(mContext), EndPoints.BASE_URL);
        }
        return mApiContact;
    }

    /**
     * method to get general user information
     *
     * @param userID this is parameter  getContact for method
     * @return return value
     */
    public Observable<ContactsModel> getContact(int userID) {
        ContactsModel contactsModel = realm.where(ContactsModel.class).equalTo("id", userID).findFirst();
        return Observable.just(contactsModel);
    }

    /**
     * method to get all contacts
     *
     * @return return value
     */
    public Observable<RealmResults<ContactsModel>> getAllContacts() {
        RealmResults<ContactsModel> contactsModel = realm.where(ContactsModel.class).notEqualTo("id", PreferenceManager.getID(mContext)).equalTo("Exist", true).findAllSorted("Linked", Sort.DESCENDING, "username", Sort.ASCENDING).sort("Activate", Sort.DESCENDING);
        return Observable.just(contactsModel);
    }

    /**
     * method to get linked contacts
     *
     * @return return value
     */
    public Observable<RealmResults<ContactsModel>> getLinkedContacts() {
        RealmResults<ContactsModel> contactsModel = realm.where(ContactsModel.class).notEqualTo("id", PreferenceManager.getID(mContext)).equalTo("Exist", true).equalTo("Linked", true).equalTo("Activate", true).findAllSorted("username", Sort.ASCENDING);
        return Observable.just(contactsModel);
    }

    /**
     * method to get linked contacts
     *
     * @return return value
     */
    public Observable<RealmResults<UsersBlockModel>> getBlockedContacts() {
        RealmResults<UsersBlockModel> contactsModel = realm.where(UsersBlockModel.class).notEqualTo("contactsModel.id", PreferenceManager.getID(mContext)).equalTo("contactsModel.Linked", true).equalTo("contactsModel.Activate", true).findAllSorted("contactsModel.username", Sort.ASCENDING);
        return Observable.just(contactsModel);
    }

    /**
     * method to update(syncing) contacts
     *
     * @param ListString this is parameter for  updateContacts method
     * @return return value
     */
    public Observable<List<ContactsModel>> updateContacts(SyncContacts ListString) {
        return initializeApiContact().contacts(ListString)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(this::copyOrUpdateContacts);
    }

    /**
     * method to get user information from the server
     *
     * @param userID this is parameter for getContactInfo method
     * @return return  value
     */
    public Observable<ContactsModel> getContactInfo(int userID) {
        return initializeApiContact().contact(userID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(this::copyOrUpdateContactInfo);
    }

    /**
     * method to get user status from server
     *
     * @return return value
     */
    public Observable<List<StatusModel>> getUserStatus() {
        return initializeApiContact().status()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(this::copyOrUpdateStatus);
    }

    /**
     * method to delete user status
     *
     * @param status this is parameter for deleteStatus method
     * @return return  value
     */
    public Observable<StatusResponse> deleteStatus(String status) {
        return initializeApiContact().deleteStatus(status)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);
    }

    /**
     * method to delete all user status
     *
     * @return return value
     */
    public Observable<StatusResponse> deleteAllStatus() {
        return initializeApiContact().deleteAllStatus()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);
    }

    /**
     * method to update user status
     *
     * @param statusID this is parameter for updateStatus method
     * @return return  value
     */
    public Observable<StatusResponse> updateStatus(int statusID) {
        return initializeApiContact().updateStatus(statusID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);
    }

    /**
     * method to edit user status
     *
     * @param newStatus this is the first parameter for editStatus method
     * @param statusID  this is the second parameter for editStatus method
     * @return return  value
     */
    public Observable<StatusResponse> editStatus(String newStatus, int statusID) {
        EditStatus editStatus = new EditStatus();
        editStatus.setNewStatus(newStatus);
        editStatus.setStatusID(statusID);
        return initializeApiContact().editStatus(editStatus)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);
    }

    /**
     * method to edit username
     *
     * @param newName this is parameter for editUsername method
     * @return return  value
     */
    public Observable<StatusResponse> editUsername(String newName) {
        EditStatus editUsername = new EditStatus();
        editUsername.setNewStatus(newName);
        return initializeApiContact().editUsername(editUsername)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);
    }

    /**
     * method to edit group name
     *
     * @param newName this is the first parameter for editGroupName method
     * @param groupID this is the second parameter for editGroupName method
     * @return return  value
     */
    public Observable<StatusResponse> editGroupName(String newName, int groupID) {
        EditStatus editGroupName = new EditStatus();
        editGroupName.setNewStatus(newName);
        editGroupName.setStatusID(groupID);
        return initializeApiContact().editGroupName(editGroupName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);
    }

    /**
     * method to get all status
     *
     * @return return value
     */
    public Observable<RealmResults<StatusModel>> getAllStatus() {
        RealmResults<StatusModel> statusModels = realm.where(StatusModel.class).equalTo("userID", PreferenceManager.getID(mContext)).findAllSorted("id", Sort.DESCENDING);
        return Observable.just(statusModels);
    }

    /**
     * method to get current status fron local
     *
     * @return return value
     */
    public Observable<StatusModel> getCurrentStatusFromLocal() {
        StatusModel statusModels = realm.where(StatusModel.class).equalTo("userID", PreferenceManager.getID(mContext)).equalTo("current", 1).findFirst();
        return Observable.just(statusModels);
    }

    /**
     * method to delete user status
     *
     * @param phone this is parameter for deleteStatus method
     * @return return  value
     */
    public Observable<JoinModel> deleteAccount(String phone, String country) {
        return initializeApiContact().deleteAccount(phone, country)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> response);
    }


    /**
     * method to copy or update user status
     *
     * @param statusModels this is parameter for copyOrUpdateStatus method
     * @return return  value
     */
    private List<StatusModel> copyOrUpdateStatus(List<StatusModel> statusModels) {
        realm.beginTransaction();
        List<StatusModel> statusModels1 = realm.copyToRealmOrUpdate(statusModels);
        realm.commitTransaction();
        return statusModels1;
    }

    /**
     * method to copy or update contacts list
     *
     * @param mListContacts this is parameter for copyOrUpdateContacts method
     * @return return  value
     */
    private List<ContactsModel> copyOrUpdateContacts(List<ContactsModel> mListContacts) {
        realm.beginTransaction();
        List<ContactsModel> realmContacts = realm.copyToRealmOrUpdate(mListContacts);
        realm.commitTransaction();
        return realmContacts;
    }

    /**
     * method to copy or update user information
     *
     * @param contactsModel this is parameter for copyOrUpdateContactInfo method
     * @return return  value
     */
    private ContactsModel copyOrUpdateContactInfo(ContactsModel contactsModel) {
        ContactsModel realmContact;
        if (UtilsPhone.checkIfContactExist(mContext, contactsModel.getPhone())) {
            realm.beginTransaction();
            contactsModel.setExist(true);
            realmContact = realm.copyToRealmOrUpdate(contactsModel);
            realm.commitTransaction();
        } else {
            realm.beginTransaction();
            contactsModel.setExist(false);
            realmContact = realm.copyToRealmOrUpdate(contactsModel);
            realm.commitTransaction();

        }
        return realmContact;
    }


    /**
     * method to get ads info
     *
     * @return return  value
     */
    public Observable<StatusResponse> getAdsInformation() {
        return initializeApiContact().getAdsInformation()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);
    }

    /**
     * method to get ads info
     *
     * @return return  value
     */
    public Observable<StatusResponse> getInterstitialAdInformation() {
        return initializeApiContact().getInterstitialAdInformation()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);
    }

    /**
     * method to get app version info
     *
     * @return return  value
     */
    public Observable<VersionResponse> getApplicationVersion() {
        return initializeApiContact().getApplicationVersion()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(applicationVersion -> applicationVersion);
    }

    /**
     * method to get app privacy & terms
     *
     * @return return  value
     */
    public Observable<StatusResponse> getPrivacyTerms() {
        return initializeApiContact().getPrivacyTerms()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(privacyTerms -> privacyTerms);
    }

    /**
     * *
     * method to get all calls
     *
     * @return return value
     */
    public Observable<RealmResults<CallsModel>> getAllCalls() {
        RealmResults<CallsModel> callsModel = realm.where(CallsModel.class).findAllSorted("date", Sort.DESCENDING);
        return Observable.just(callsModel);
    }

    /**
     * *
     * method to get all calls details
     *
     * @return return value
     */
    public Observable<RealmResults<CallsInfoModel>> getAllCallsDetails(int callID) {
        RealmResults<CallsInfoModel> callsInfoModel = realm.where(CallsInfoModel.class)
                .equalTo("callId", callID)
                .findAllSorted("date", Sort.DESCENDING);
        return Observable.just(callsInfoModel);
    }

    /**
     * method to get general call information
     *
     * @param callID this is parameter  getContact for method
     * @return return value
     */
    public Observable<CallsModel> getCallDetails(int callID) {
        CallsModel callsModel = realm.where(CallsModel.class).equalTo("id", callID).findFirst();
        return Observable.just(callsModel);
    }


    public Observable<NetworkModel> checkIfUserSession() {
        return initializeApiContact().checkNetwork()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(networkModel -> networkModel);
    }
}
