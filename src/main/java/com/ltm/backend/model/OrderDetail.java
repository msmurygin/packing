package com.ltm.backend.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OrderDetail implements Serializable {

    /**
     * отображается номер заказа, которому принадлежит данный УИТ.
     */
    private String orderKey;

    /**
     * класс сочетаемости товаров (sku.putawayclass) из данного заказ
     */
    private String putawayClass;

    /**
     *количество заказанных штук в разрезе каждого класса сочетаемости.
     */
    private double sumOpenQty;

    /**
     * Кол-во упакованного товара
     */
    private double packedQty;

    /**
     * тип упаковки, выбранный системой для упаковки данного заказа
     */
    private String cartonType;

    /**
     * Описание типа упаковки
     */
    private String cartonDescription;

    /**
     * расчётное количество посылок под каждый класс сочетаемости в рамках данного заказа
     */
    private int estimatedParcelsQty;

    /**
     * обновляемое поле (при каждом вводе информации в поле C6 или I6) с информацией об уже упакованном количестве и общем количестве.
     */
    private String packed;

    /**
     * скартонизированный список картонов
     */
    private List<CartonizedUID> cartonizedUIDS;

    private List<String> pickDetailList;

    /*
    When sort is done all picks moves to this caseid
     */
    private String caseId;

    private boolean closed;

    /**
     * Тип короба, который ввел пользователь.
     */
    private String selectedCartonType;

    public OrderDetail() {
    }

    public OrderDetail(String orderKey, String putawayClass, double sumOpenQty, String cartonType,
                       String cartonDescription, int estimatedParcelsQty, String packed,
                       List<CartonizedUID> cartonizedUIDS) {
        this.orderKey = orderKey;
        this.putawayClass = putawayClass;
        this.sumOpenQty = sumOpenQty;
        this.cartonType = cartonType;
        this.cartonDescription = cartonDescription;
        this.estimatedParcelsQty = estimatedParcelsQty;
        this.packed = packed;
        this.cartonizedUIDS = cartonizedUIDS;
    }

    public String getOrderKey() {
        return orderKey;
    }

    public void setOrderKey(String orderKey) {
        this.orderKey = orderKey;
    }

    public String getPutawayClass() {
        return putawayClass;
    }

    public void setPutawayClass(String putawayClass) {
        this.putawayClass = putawayClass;
    }

    public double getSumOpenQty() {
        return sumOpenQty;
    }

    public void setSumOpenQty(double sumOpenQty) {
        this.sumOpenQty = sumOpenQty;
    }

    public String getCartonType() {
        return cartonType;
    }

    public void setCartonType(String cartonType) {
        this.cartonType = cartonType;
    }

    public String getCartonDescription() {
        return  cartonDescription;
    }

    public void setCartonDescription(String cartonDescription) {
        this.cartonDescription = cartonDescription;
    }

    public int getEstimatedParcelsQty() {
        return estimatedParcelsQty;
    }

    public void setEstimatedParcelsQty(int estimatedParcelsQty) {
        this.estimatedParcelsQty = estimatedParcelsQty;
    }

    public String getPacked() {
        return packed;
    }

    public void setPacked(String packed) {
        this.packed = packed;
    }

    public void addUID(UID cUid){
        if (this.cartonizedUIDS == null) this.cartonizedUIDS = new ArrayList<>();
        this.cartonizedUIDS.add(new CartonizedUID(cUid, cartonType));
    }

    public void setCartonizedUIDS(List<CartonizedUID> cartonizedUIDS) {
        this.cartonizedUIDS = cartonizedUIDS;
    }

    public List<CartonizedUID> getCartonizedUIDS() {
        return cartonizedUIDS;
    }

    public double getPackedQty() {
        return packedQty;
    }

    public void setPackedQty(double packedQty) {
        this.packedQty = packedQty;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public List<String> getPickDetailList() {
        return pickDetailList;
    }

    public void setPickDetailList(List<String> pickDetailList) {
        this.pickDetailList = pickDetailList;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    @Override
    public String toString() {
        return "OrderDetail{" +
                "orderKey='" + orderKey + '\'' +
                ", putawayClass='" + putawayClass + '\'' +
                ", sumOpenQty=" + sumOpenQty +
                ", packedQty=" + packedQty +
                ", cartonType='" + cartonType + '\'' +
                ", cartonDescription='" + cartonDescription + '\'' +
                ", estimatedParcelsQty=" + estimatedParcelsQty +
                ", packed='" + packed + '\'' +
                ", cartonizedUIDS=" + cartonizedUIDS +
                ", pickDetailList=" + pickDetailList +
                ", closed=" + closed +
                '}';
    }

    public void removePickDetailKey(UID uid) {
        this.pickDetailList.remove(uid.getPickDetailKey());
    }


    public String getSelectedCartonType() {
        return selectedCartonType;
    }

    public void setSelectedCartonType(String selectedCartonType) {
        this.selectedCartonType = selectedCartonType;
    }
}
