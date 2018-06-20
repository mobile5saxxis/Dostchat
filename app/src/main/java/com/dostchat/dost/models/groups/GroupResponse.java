package com.dostchat.dost.models.groups;

import io.realm.RealmList;

/**
 * Created by Abderrahim El imame on 01/11/2015.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */
public class GroupResponse {
    private boolean success;
    private String message;
    private int groupID;
    private String groupImage;
    private RealmList<MembersGroupModel> membersGroupModels;

    public GroupResponse() {

    }

    public RealmList<MembersGroupModel> getMembersGroupModels() {
        return membersGroupModels;
    }

    public void setMembersGroupModels(RealmList<MembersGroupModel> membersGroupModels) {
        this.membersGroupModels = membersGroupModels;
    }


    public String getGroupImage() {
        return groupImage;
    }

    public void setGroupImage(String groupImage) {
        this.groupImage = groupImage;
    }

    public int getGroupID() {
        return groupID;
    }

    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
