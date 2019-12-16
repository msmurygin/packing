package com.ltm.backend.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PickDetail implements Serializable {


    private String uom;
    private double uomqty;
    private double qty;
    private String putawayClass;
    private double cubicCapacity; // cube of one pick
    private double cubeStd; // cube of one each
    private double width;
    private double height;
    private double length;
    private String pickDetailKeyTemp;
    private String caseIdTemp;

    private List<String> pickDetailKeyList;
    private List<String> caseIdList;



    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public double getUomqty() {
        return uomqty;
    }

    public void setUomqty(double uomqty) {
        this.uomqty = uomqty;
    }

    public double getQty() {
        return qty;
    }

    public void setQty(double qty) {
        this.qty = qty;
    }


    public String getPutawayClass() {
        return putawayClass;
    }

    public void setPutawayClass(String putawayClass) {
        this.putawayClass = putawayClass;
    }

    public double getCubicCapacity() {
        return cubicCapacity;
    }

    public void setCubicCapacity(double cubicCapacity) {
        this.cubicCapacity = cubicCapacity;
    }

    public double getCubeStd() {
        return cubeStd;
    }

    public void setCubeStd(double cubeStd) {
        this.cubeStd = cubeStd;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getLength() {
        return length;
    }

    public PickDetail( double qty, String putawayClass, double cubicCapacity, double cubeStd, double width, double height, double length, String pickDetailKeyTemp, String caseIdTemp) {
        this.qty = qty;
        this.putawayClass = putawayClass;
        this.cubicCapacity = cubicCapacity;
        this.cubeStd = cubeStd;
        this.width = width;
        this.height = height;
        this.length = length;
        this.pickDetailKeyTemp = pickDetailKeyTemp;
        this.caseIdTemp = caseIdTemp;
    }

    public String getPickDetailKeyTemp() {
        return pickDetailKeyTemp;
    }

    public void setPickDetailKeyTemp(String pickDetailKeyTemp) {
        this.pickDetailKeyTemp = pickDetailKeyTemp;
    }


    public List<String> getPickDetailKeyList() {
        return pickDetailKeyList;
    }

    public void addPickDetailKeyToList(String pickDetailKeyTemp) {
        if ( this.pickDetailKeyList == null)
            this.pickDetailKeyList = new ArrayList<>();


        this.pickDetailKeyList.add(pickDetailKeyTemp);
    }


    public String getCaseIdTemp() {
        return caseIdTemp;
    }

    public void setCaseIdTemp(String caseIdTemp) {
        this.caseIdTemp = caseIdTemp;
    }



    public List<String> getCaseIdList() {
        return caseIdList;
    }

    public void addCaseIdToList(String caseIdTemp) {
        if ( this.caseIdList == null)
            this.caseIdList = new ArrayList<>();


        this.caseIdList.add(caseIdTemp);
    }
}
