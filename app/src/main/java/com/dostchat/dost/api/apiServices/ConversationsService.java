package com.dostchat.dost.api.apiServices;

import com.dostchat.dost.models.messages.ConversationsModel;

import io.reactivex.Observable;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;


/**
 * Created by Abderrahim El imame on 20/04/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class ConversationsService {
    private Realm realm;

    public ConversationsService(Realm realm) {
        this.realm = realm;

    }

    /**
     * method to get Conversations list
     *
     * @return return value
     */
    public Observable<RealmResults<ConversationsModel>> getConversations() {
        RealmResults<ConversationsModel> conversationsModels = realm.where(ConversationsModel.class).findAllSorted("LastMessageId", Sort.DESCENDING);
        return Observable.just(conversationsModels);
    }


}
