package com.ltm.backend.model;

import java.io.Serializable;

public class PickDetail implements Serializable {
    private final double qty;
    private final String putawayClass;
    private final double cubeStd; // cube of one each
    private final double width;
    private final double height;
    private final double length;
    private final String pickDetailKey;
    private final String caseId;

    public PickDetail(double qty,
                      String putawayClass,
                      double cubeStd,
                      double width,
                      double height,
                      double length,
                      String pickDetailKey,
                      String caseId) {
        this.qty = qty;
        this.putawayClass = putawayClass;
        this.cubeStd = cubeStd;
        this.width = width;
        this.height = height;
        this.length = length;
        this.pickDetailKey = pickDetailKey;
        this.caseId = caseId;
    }

    public double getQty() {
        return qty;
    }

    public String getPutawayClass() {
        return putawayClass;
    }

    public double getCubeStd() {
        return cubeStd;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getLength() {
        return length;
    }

    public String getPickDetailKey() {
        return pickDetailKey;
    }

    public String getCaseId() {
        return caseId;
    }

}
