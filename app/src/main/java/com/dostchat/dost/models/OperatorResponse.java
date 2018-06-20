package com.dostchat.dost.models;

import java.util.ArrayList;
import java.util.List;

public class OperatorResponse {
    private String status;
    private ArrayList<Operator> data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList<Operator> getData() {

        if (data == null) {
            data = new ArrayList<>();
        }

        return data;
    }

    public void setData(ArrayList<Operator> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "OperatorResponse{" +
                "status='" + status + '\'' +
                ", data=" + data +
                '}';
    }
}
