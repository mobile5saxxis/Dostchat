package com.dostchat.dost.models.sos;

public class Result {
    private String id;

    private String phone_number;

    private String name;

    private String user_id;

    private String createdDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public String toString() {
        return "Result{" +
                "id='" + id + '\'' +
                ", phone_number='" + phone_number + '\'' +
                ", name='" + name + '\'' +
                ", user_id='" + user_id + '\'' +
                ", createdDate='" + createdDate + '\'' +
                '}';
    }
}
