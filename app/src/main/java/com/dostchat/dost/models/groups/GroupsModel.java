package com.dostchat.dost.models.groups;

import com.dostchat.dost.models.messages.MessagesModel;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class GroupsModel extends RealmObject {
    @PrimaryKey
    private int id;
    private String CreatedDate;
    private int Status;
    private String GroupName;
    private String GroupImage;
    private int CreatorID;
    private String Creator;
    private RealmList<MembersGroupModel> Members;
    private RealmList<MessagesModel> Messages;

    public GroupsModel() {
    }
    public RealmList<MessagesModel> getMessages() {
        return Messages;
    }

    public void setMessages(RealmList<MessagesModel> messages) {
        Messages = messages;
    }

    public RealmList<MembersGroupModel> getMembers() {
        return Members;
    }

    public void setMembers(RealmList<MembersGroupModel> members) {
        Members = members;
    }

    public int getCreatorID() {
        return CreatorID;
    }

    public void setCreatorID(int creatorID) {
        CreatorID = creatorID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getCreator() {
        return Creator;
    }

    public void setCreator(String creator) {
        Creator = creator;
    }

    public String getGroupName() {
        return GroupName;
    }

    public void setGroupName(String groupName) {
        GroupName = groupName;
    }

    public String getGroupImage() {
        return GroupImage;
    }

    public void setGroupImage(String groupImage) {
        GroupImage = groupImage;
    }


    public String getCreatedDate() {
        return CreatedDate;
    }

    public void setCreatedDate(String createdDate) {
        CreatedDate = createdDate;
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int status) {
        Status = status;
    }
}
