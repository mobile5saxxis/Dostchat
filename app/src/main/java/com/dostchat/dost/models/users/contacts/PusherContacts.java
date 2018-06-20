package com.dostchat.dost.models.users.contacts;

import java.util.List;

/**
 * Created by Abderrahim El imame on 04/05/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class PusherContacts {
    private String action;
    private List<ContactsModel> contactsModelList;
    private Throwable throwable;

    public PusherContacts(String action) {
        this.action = action;
    }

    public PusherContacts(String action, Throwable throwable) {
        this.action = action;
        this.throwable = throwable;
    }

    public PusherContacts(String action, List<ContactsModel> contactsModelList) {
        this.action = action;
        this.contactsModelList = contactsModelList;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public List<ContactsModel> getContactsModelList() {
        return contactsModelList;
    }

    public void setContactsModelList(List<ContactsModel> contactsModelList) {
        this.contactsModelList = contactsModelList;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}