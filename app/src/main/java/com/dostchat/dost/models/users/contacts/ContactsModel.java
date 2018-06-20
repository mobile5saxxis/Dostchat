package com.dostchat.dost.models.users.contacts;

import com.google.gson.annotations.Expose;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class ContactsModel extends RealmObject {
    @PrimaryKey
    private int id;
    private int contactID;
    private String username;
    private String phone;
    private String phoneTmp;
    private boolean Linked;
    private boolean Activate;
    private boolean Exist;
    private String image;
    private String status;
    private String status_date;
    @Expose
    private String userState;

    public ContactsModel() {

    }
    public String getPhoneTmp() {
        return phoneTmp;
    }

    public void setPhoneTmp(String phoneTmp) {
        this.phoneTmp = phoneTmp;
    }
    public String getUserState() {
        return userState;
    }

    public void setUserState(String userState) {
        this.userState = userState;
    }


    public boolean isActivate() {
        return Activate;
    }

    public void setActivate(boolean activate) {
        Activate = activate;
    }

    public boolean isExist() {
        return Exist;
    }

    public void setExist(boolean exist) {
        Exist = exist;
    }


    public int getContactID() {
        return contactID;
    }

    public void setContactID(int contactID) {
        this.contactID = contactID;
    }


    public String getStatus_date() {
        return status_date;
    }

    public void setStatus_date(String status_date) {
        this.status_date = status_date;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isLinked() {
        return Linked;
    }

    public void setLinked(boolean linked) {
        Linked = linked;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
