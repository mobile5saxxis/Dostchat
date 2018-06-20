package com.dostchat.dost.models;

import android.os.Parcel;
import android.os.Parcelable;


import java.util.ArrayList;

/**
 * Created by saxxis25 on 3/30/2017.
 */

public class Category implements Parcelable{

    private int id;
    private int mIcon;
    private String mTitle;


    public Category() {

    }

    public Category(int i, int icon, String title) {
        this.id = i;
        this.mIcon = icon;
        this.mTitle = title;
    }

    protected Category(Parcel in) {
        id = in.readInt();
        mIcon = in.readInt();
        mTitle = in.readString();
    }

    public static final Creator<Category> CREATOR = new Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getmIcon() {
        return mIcon;
    }

    public void setmIcon(int mIcon) {
        this.mIcon = mIcon;
    }

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(mIcon);
        dest.writeString(mTitle);
    }



}
