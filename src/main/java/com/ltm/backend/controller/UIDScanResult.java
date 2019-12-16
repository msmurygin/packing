package com.ltm.backend.controller;

import com.ltm.backend.model.UID;

import java.io.Serializable;

public class UIDScanResult implements Serializable {

    private final UID uid;
    private final boolean needToGenerateNewDropId;

    // Флаг первого сканирования
    private final boolean isFirstScan;

    // Флаг когда юзер указывает отличную от предложенного системного типа картона,
    // система предлагает подтвердить, после чего данный флаг true
    private boolean isCartonConfirmed = false;


    public UIDScanResult(UID uid, boolean isFirstScan, boolean needToGenerateNewDropId) {
        this.uid = uid;
        this.isFirstScan = isFirstScan;
        this.needToGenerateNewDropId = needToGenerateNewDropId;
    }

    public UIDScanResult(UID uid, boolean isFirstScan) {
        this(uid, isFirstScan, false);
    }

    public UID getUid() {
        return uid;
    }

    public boolean isFirstScan() {
        return isFirstScan;
    }

    public boolean isCartonConfirmed() {
        return isCartonConfirmed;
    }

    public boolean isNeedToGenerateNewDropId() {
        return needToGenerateNewDropId;
    }

    public void confirmCartonTypeInput() {
        this.isCartonConfirmed = true;
    }
}
