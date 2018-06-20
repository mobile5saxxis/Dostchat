package com.dostchat.dost.models;

public class Login {

    private String username;

    private String session;

    private String status;

    private int userid;

    private String message;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Login{" +
                "username='" + username + '\'' +
                ", session='" + session + '\'' +
                ", status='" + status + '\'' +
                ", userid='" + userid + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
