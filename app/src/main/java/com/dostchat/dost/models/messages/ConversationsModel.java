package com.dostchat.dost.models.messages;

import com.google.gson.annotations.Expose;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class ConversationsModel extends RealmObject {
    @PrimaryKey
    private int id;
    private int Status;
    private String MessageDate;
    private String LastMessage;
    private int LastMessageId;
    private String RecipientPhone;
    private String RecipientUsername;// or GroupName
    private String RecipientImage;//or GroupImage
    private String UnreadMessageCounter;
    private int RecipientID;
    private RealmList<MessagesModel> Messages;

    /**
     * field of groups
     */
    @Expose
    private int CreatorID;
    @Expose
    private boolean isGroup;
    @Expose
    private boolean createdOnline;
    @Expose
    private int groupID; //groupID

    public ConversationsModel() {

    }

    public int getGroupID() {
        return groupID;
    }

    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }

    public boolean getCreatedOnline() {
        return createdOnline;
    }

    public void setCreatedOnline(boolean createdOnline) {
        this.createdOnline = createdOnline;
    }


    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }


    public int getCreatorID() {
        return CreatorID;
    }

    public void setCreatorID(int creatorID) {
        CreatorID = creatorID;
    }


    public int getLastMessageId() {
        return LastMessageId;
    }

    public void setLastMessageId(int lastMessageId) {
        LastMessageId = lastMessageId;
    }



    public RealmList<MessagesModel> getMessages() {
        return Messages;
    }

    public void setMessages(RealmList<MessagesModel> messages) {
        Messages = messages;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRecipientID() {
        return RecipientID;
    }

    public void setRecipientID(int recipientID) {
        RecipientID = recipientID;
    }

    public String getUnreadMessageCounter() {
        return UnreadMessageCounter;
    }

    public void setUnreadMessageCounter(String unreadMessageCounter) {
        UnreadMessageCounter = unreadMessageCounter;
    }


    public String getRecipientImage() {
        return RecipientImage;
    }

    public void setRecipientImage(String recipientImage) {
        RecipientImage = recipientImage;
    }

    public String getRecipientUsername() {
        return RecipientUsername;
    }

    public void setRecipientUsername(String recipientUsername) {
        RecipientUsername = recipientUsername;
    }

    public String getRecipientPhone() {
        return RecipientPhone;
    }

    public void setRecipientPhone(String recipientPhone) {
        RecipientPhone = recipientPhone;
    }

    public String getLastMessage() {
        return LastMessage;
    }

    public void setLastMessage(String lastMessage) {
        LastMessage = lastMessage;
    }

    public String getMessageDate() {
        return MessageDate;
    }

    public void setMessageDate(String messageDate) {
        MessageDate = messageDate;
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int status) {
        Status = status;
    }


}
