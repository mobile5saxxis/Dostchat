package com.dostchat.dost.models.sos;

import java.util.ArrayList;
import java.util.List;

public class SOSResponse {
    private List<Result> result;

    private boolean success;

    public List<Result> getResult() {

        if (result == null) {
            result = new ArrayList<>();
        }

        return result;
    }

    public void setResult(List<Result> result) {
        this.result = result;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "SOSResponse{" +
                "result=" + result +
                ", success=" + success +
                '}';
    }
}
