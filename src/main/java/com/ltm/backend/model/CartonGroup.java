package com.ltm.backend.model;


import java.io.Serializable;

/**
 * date : 14/03/2019
 * Author: Maxim Smurygin
 * This table describe part of
 * wmwhse1.CARTONIZATION table data
 */
public class CartonGroup  implements Serializable {

    /**
     *
     */
    private String cartonGroup;


    /**
     *
     */
    private String cartonType;


    /**
     *
     */
    private String cartonDescription;


    /**
     * default constructor
     */
    public CartonGroup() {
    }


    /**
     *
     * @param cartonGroup
     * @param cartonType
     * @param cartonDescription
     */
    public CartonGroup(String cartonGroup, String cartonType, String cartonDescription) {
        this.cartonGroup = cartonGroup;
        this.cartonType = cartonType;
        this.cartonDescription = cartonDescription;
    }


    public String getCartonGroup() {
        return cartonGroup;
    }

    public void setCartonGroup(String cartonGroup) {
        this.cartonGroup = cartonGroup;
    }

    public String getCartonType() {
        return cartonType;
    }

    public void setCartonType(String cartonType) {
        this.cartonType = cartonType;
    }

    public String getCartonDescription() {
        return cartonDescription;
    }

    public void setCartonDescription(String cartonDescription) {
        this.cartonDescription = cartonDescription;
    }
}
