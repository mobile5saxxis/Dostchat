package com.dostchat.dost.models.sos;

public class SOSAddResponse {
    private String result;

    private boolean success;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "SOSAddResponse [result = " + result + ", success = " + success + "]";
    }
}
