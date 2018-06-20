package com.dostchat.dost.models.users.status;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Abderrahim El imame on 28/04/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class StatusModel extends RealmObject {
    @PrimaryKey
    private int id;
    private String status;
    private String currentStatus;
    private int currentStatusID;
    private int userID;
    private int current;

    public StatusModel() {

    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getCurrentStatusID() {
        return currentStatusID;
    }

    public void setCurrentStatusID(int currentStatusID) {
        this.currentStatusID = currentStatusID;
    }


    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
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


}
