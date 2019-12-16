package com.ltm.backend.model;

import java.io.Serializable;

public class CartonizedUID implements Serializable  {

    private UID uid;

    private String cartonType;


    public String getCartonType() {
        return cartonType;
    }

    public void setCartonType(String cartonType) {
        this.cartonType = cartonType;
    }


    public CartonizedUID(UID uid, String cartonType) {
        this.uid = uid;
        this.cartonType = cartonType;
    }


    public UID getUid() {
        return uid;
    }

    public void setUid(UID uid) {
        this.uid = uid;
    }
}
