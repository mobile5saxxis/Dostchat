package com.dostchat.dost.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by saxxis25 on 4/26/2017.
 */

public class Plan implements Parcelable {

    private String amount;
    private String validity;
    private String talktime;
    private String service;
    private String planname;

    public Plan(){}

    public Plan(String amount,String validity,String talktime,String service,String planname){
        this.amount = amount;
        this.validity = validity;
        this.talktime = talktime;
        this.service = service;
        this.planname = planname;
    }

    protected Plan(Parcel in) {
        amount = in.readString();
        validity = in.readString();
        talktime = in.readString();
        service = in.readString();
        planname = in.readString();
    }

    public static final Creator<Plan> CREATOR = new Creator<Plan>() {
        @Override
        public Plan createFromParcel(Parcel in) {
            return new Plan(in);
        }

        @Override
        public Plan[] newArray(int size) {
            return new Plan[size];
        }
    };

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setPlanname(String planname) {
        this.planname = planname;
    }

    public void setValidity(String validity) {
        this.validity = validity;
    }

    public void setService(String service) {
        this.service = service;
    }

    public void setTalktime(String talktime) {
        this.talktime = talktime;
    }

    public String getAmount() {
        return amount;
    }

    public String getPlanname() {
        return planname;
    }

    public String getService() {
        return service;
    }

    public String getTalktime() {
        return talktime;
    }

    public String getValidity() {
        return validity;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(amount);
        dest.writeString(validity);
        dest.writeString(talktime);
        dest.writeString(service);
        dest.writeString(planname);
    }
}
