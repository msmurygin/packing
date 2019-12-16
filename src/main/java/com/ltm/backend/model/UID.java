package com.ltm.backend.model;

import java.io.Serializable;
import java.util.Objects;

public class UID implements Serializable {
    private static final String TRUE = "1";

    private String serialNumber;
    private String orderKey;
    private String cartonGroup;
    private String cartonType;
    private String cartonDescription;
    private String putawayClass;
    private String lot;
    private String loc;
    private String id;
    private String sku;
    private String storerKey;
    private double qty;
    private String pickDetailKey;
    private String itrnKey;
    private String sortLocation;
    private String carrierName;
    private String susr1;

    public UID() {
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getOrderKey() {
        return orderKey;
    }

    public void setOrderKey(String orderKey) {
        this.orderKey = orderKey;
    }

    public String getCartonGroup() {
        return cartonGroup;
    }

    public void setCartonGroup(String cartonGroup) {
        this.cartonGroup = cartonGroup;
    }

    public String getPutawayClass() {
        return putawayClass;
    }

    public void setPutawayClass(String putawayClass) {
        this.putawayClass = putawayClass;
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

    public String getLot() {
        return lot;
    }

    public void setLot(String lot) {
        this.lot = lot;
    }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getStorerKey() {
        return storerKey;
    }

    public void setStorerKey(String storerKey) {
        this.storerKey = storerKey;
    }

    public double getQty() {
        return qty;
    }

    public void setQty(double qty) {
        this.qty = qty;
    }

    public String getPickDetailKey() {
        return pickDetailKey;
    }

    public void setPickDetailKey(String pickDetailKey) {
        this.pickDetailKey = pickDetailKey;
    }

    public String getItrnKey() {
        return itrnKey;
    }

    public void setItrnKey(String itrnKey) {
        this.itrnKey = itrnKey;
    }

    public String getSortLocation() {
        return sortLocation;
    }

    public void setSortLocation(String sortLocation) {
        this.sortLocation = sortLocation;
    }

    public String getCarrierName() {
        return carrierName;
    }

    public void setCarrierName(String carrierName) {
        this.carrierName = carrierName;
    }

    public String getSusr1() {
        return susr1;
    }

    public void setSusr1(String susr1) {
        this.susr1 = susr1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UID uid = (UID) o;
        return Double.compare(uid.qty, qty) == 0 &&
            Objects.equals(serialNumber, uid.serialNumber) &&
            Objects.equals(orderKey, uid.orderKey) &&
            Objects.equals(cartonGroup, uid.cartonGroup) &&
            Objects.equals(cartonType, uid.cartonType) &&
            Objects.equals(cartonDescription, uid.cartonDescription) &&
            Objects.equals(putawayClass, uid.putawayClass) &&
            Objects.equals(lot, uid.lot) &&
            Objects.equals(loc, uid.loc) &&
            Objects.equals(id, uid.id) &&
            Objects.equals(sku, uid.sku) &&
            Objects.equals(storerKey, uid.storerKey) &&
            Objects.equals(pickDetailKey, uid.pickDetailKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serialNumber, orderKey, cartonGroup, cartonType, cartonDescription, putawayClass, lot,
            loc, id, sku, storerKey, qty, pickDetailKey);
    }

    @Override
    public String toString() {
        return "UID{" +
            "serialNumber='" + serialNumber + '\'' +
            ", orderKey='" + orderKey + '\'' +
            ", cartonGroup='" + cartonGroup + '\'' +
            ", cartonType='" + cartonType + '\'' +
            ", cartonDescription='" + cartonDescription + '\'' +
            ", putawayClass='" + putawayClass + '\'' +
            ", lot='" + lot + '\'' +
            ", loc='" + loc + '\'' +
            ", id='" + id + '\'' +
            ", sku='" + sku + '\'' +
            ", storerKey='" + storerKey + '\'' +
            ", qty=" + qty +
            ", pickDetailKey='" + pickDetailKey + '\'' +
            ", itrnKey='" + itrnKey + '\'' +
            '}';
    }

    public boolean isMultiPackagingBanned() {
        return !TRUE.equals(susr1);
    }
}
