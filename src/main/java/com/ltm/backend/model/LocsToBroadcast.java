package com.ltm.backend.model;

import java.io.Serializable;

public class LocsToBroadcast  implements Serializable {

    private String sortLocation;

    private String dropId;

    private String areaKey;

    private String orderKey;


    public String getSortLocation() {
        return sortLocation;
    }

    public void setSortLocation(String sortLocation) {
        this.sortLocation = sortLocation;
    }

    public String getDropId() {
        return dropId;
    }

    public void setDropId(String dropId) {
        this.dropId = dropId;
    }

    public String getAreaKey() {
        return areaKey;
    }

    public void setAreaKey(String areaKey) {
        this.areaKey = areaKey;
    }

    public String getOrderKey() {
        return orderKey;
    }

    public void setOrderKey(String orderKey) {
        this.orderKey = orderKey;
    }

    public LocsToBroadcast() {
    }

    public LocsToBroadcast(String sortLocation, String dropId, String areaKey, String pOrderKey) {
        this.sortLocation = sortLocation;
        this.dropId = dropId;
        this.areaKey = areaKey;
        this.orderKey = pOrderKey;
    }
}
