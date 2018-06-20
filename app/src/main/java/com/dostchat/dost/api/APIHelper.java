package com.dostchat.dost.api;

import com.dostchat.dost.api.apiServices.UsersContacts;
import com.dostchat.dost.app.DostChatApp;

/**
 * Created by Abderrahim El imame on 4/11/17.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class APIHelper {

    public static UsersContacts initialApiUsersContacts() {
        APIService mApiService = APIService.with(DostChatApp.getInstance());
        return new UsersContacts(DostChatApp.getRealmDatabaseInstance(), DostChatApp.getInstance(), mApiService);
    }
}
