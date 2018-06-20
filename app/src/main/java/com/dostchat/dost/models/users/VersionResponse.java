package com.dostchat.dost.models.users;

/**
 * Created by Abderrahim El imame on 03/05/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class VersionResponse {
    private boolean success;
    private int message;


    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getMessage() {
        return message;
    }

    public void setMessage(int message) {
        this.message = message;
    }

}
