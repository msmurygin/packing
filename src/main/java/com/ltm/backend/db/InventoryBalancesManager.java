package com.ltm.backend.db;

import com.ltm.backend.exception.UserException;
import com.ltm.backend.model.Parcel;


public interface InventoryBalancesManager {


    void init() throws UserException;

    void updateInventory(Parcel parcel) throws UserException;

    void updatePickdetail(Parcel parcel) throws UserException;

    void updateLotxLocxId(final Parcel parcel) throws UserException;

    void updateSkuxLoc(Parcel parcel) throws UserException;

    void updatePickDetailCaseId(Parcel currentParcel);

    void insertIntoDropId(Parcel parcel) throws UserException;

    void insertIntoDropidDetail(Parcel parcel) throws UserException;

    void insertIntoTaskDetail(Parcel parcel) throws UserException;

    void updateSerialInventory(Parcel parcel) throws UserException;

    void insertITRN(Parcel parcel) throws UserException;

    void insertItrnSerial(Parcel parcel) throws UserException;

    void updateLotXIdDetail(Parcel parcel) throws UserException;
}
