package com.dostchat.dost.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by saxxis25 on 3/25/2017.
 */

public class Operator implements Parcelable {

    private String id;
    private String optype_name;
    private String op_id;
    private String op_id2;
    private String op_code;
    private String dth_code;
    private String operator_type;
    private String imageLocation;
    private String type_description;
    private String published;

    public Operator(){

    }

    public Operator(String id, String optype_name, String op_id, String op_id2, String op_code, String dth_code,
                    String operator_type, String imageLocation, String type_description, String published){
        this.id = id;
        this.optype_name = optype_name;
        this.op_id = op_id;
        this.op_id2 = op_id2;
        this.op_code = op_code;
        this.dth_code = dth_code;
        this.operator_type = operator_type;
        this.imageLocation = imageLocation;
        this.type_description = type_description;
        this.published = published;
    }


    protected Operator(Parcel in) {
        id = in.readString();
        optype_name = in.readString();
        op_id = in.readString();
        op_id2 = in.readString();
        op_code = in.readString();
        dth_code = in.readString();
        operator_type = in.readString();
        imageLocation = in.readString();
        type_description = in.readString();
        published = in.readString();
    }

    public static final Creator<Operator> CREATOR = new Creator<Operator>() {
        @Override
        public Operator createFromParcel(Parcel in) {
            return new Operator(in);
        }

        @Override
        public Operator[] newArray(int size) {
            return new Operator[size];
        }
    };

    public void setId(String id) {
        this.id = id;
    }

    public void setDth_code(String dth_code) {
        this.dth_code = dth_code;
    }

    public void setOptype_name(String optype_name) {
        this.optype_name = optype_name;
    }

    public void setImageLocation(String imageLocation) {
        this.imageLocation = imageLocation;
    }

    public void setOp_code(String op_code) {
        this.op_code = op_code;
    }

    public void setOp_dth(String op_dth) {
        this.operator_type = op_dth;
    }

    public void setOp_id(String op_id) {
        this.op_id = op_id;
    }

    public void setOp_id2(String op_id2) {
        this.op_id2 = op_id2;
    }

    public void setPublished(String published) {
        this.published = published;
    }

    public void setType_description(String type_description) {
        this.type_description = type_description;
    }

    public String getDth_code() {
        return dth_code;
    }

    public String getId() {
        return id;
    }

    public String getImageLocation() {
        return imageLocation;
    }

    public String getOp_code() {
        return op_code;
    }

    public String getOp_dth() {
        return operator_type;
    }

    public String getOp_id() {
        return op_id;
    }

    public String getOp_id2() {
        return op_id2;
    }

    public String getOptype_name() {
        return optype_name;
    }

    public String getPublished() {
        return published;
    }

    public String getType_description() {
        return type_description;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(optype_name);
        dest.writeString(op_id);
        dest.writeString(op_id2);
        dest.writeString(op_code);
        dest.writeString(dth_code);
        dest.writeString(operator_type);
        dest.writeString(imageLocation);
        dest.writeString(type_description);
        dest.writeString(published);
    }
}

