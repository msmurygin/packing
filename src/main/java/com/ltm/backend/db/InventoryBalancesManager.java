package com.ltm.backend.db;

import com.ltm.backend.exception.UserException;
import com.ltm.backend.model.Parcel;


public interface InventoryBalancesManager {


    void updateInventory(Parcel parcel) throws UserException;

    void updatePickdetail(Parcel parcel, String sortTableKey) throws UserException;

    void updateLotxLocxId(Parcel parcel, String sortTableKey, String userId) throws UserException;

    void updateSkuxLoc(Parcel parcel, String sortTableKey, String userId) throws UserException;

    void updatePickDetailCaseId(Parcel currentParcel, String userId);

    void insertIntoDropId(Parcel parcel, String sortTableKey, String userId) throws UserException;

    void insertIntoDropidDetail(Parcel parcel, String userId) throws UserException;

    void insertIntoTaskDetail(Parcel parcel, String sortTableKey, String userId) throws UserException;

    void updateSerialInventory(Parcel parcel, String sortTableKey, String userId) throws UserException;

    void insertITRN(Parcel parcel, String sortTableKey, String userId) throws UserException;

    void insertItrnSerial(Parcel parcel, String sortTableKey, String userId) throws UserException;

    void updateLotXIdDetail(Parcel parcel, String userId) throws UserException;
}
