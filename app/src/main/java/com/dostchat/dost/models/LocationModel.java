package com.dostchat.dost.models;

import java.util.List;

/**
 * Created by manoj on 07/02/18.
 */

public class LocationModel {
    public boolean success;
    public Resut result;

    @Override
    public String toString() {
        return "LocationModel{" +
                "success=" + success +
                ", result=" + result.toString() +
                '}';
    }

    public LocationModel() {

    }

    public class Resut {
        public String gps;
        public String areaname;

        @Override
        public String toString() {
            return "Resut{" +
                    "gps='" + gps + '\'' +
                    ", areaname='" + areaname + '\'' +
                    '}';
        }

        public Resut() {

        }
    }
}
