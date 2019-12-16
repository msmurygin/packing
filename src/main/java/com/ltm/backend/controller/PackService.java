package com.ltm.backend.controller;

import com.ltm.backend.exception.ScanUIDException;
import com.ltm.backend.exception.UserException;
import com.ltm.backend.model.OrderDetail;
import com.ltm.backend.model.Parcel;
import com.ltm.backend.model.UID;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

public interface PackService extends Serializable {
    String REPRINT_NEEDED = "REPRINT_NEEDED";

    /**
     * Сотрудник сканирует в поле C6 номер УИТа из ячейки сортировки.
     */
    UID uiIdScan(String theScannedUID);

    /**
     * Если просканированный УИТ принадлежит заказу, который ещё не полностью отсортирован, выдаётся сообщение:
     */
    boolean validateUID(UID uid);

    /**
     * Если данный УИТ принадлежит заказу, по которому ещё не было операций упаковки, система делает активным блок интерфейса G2-J13 и L2-R13
     */
    boolean isThisIsTheFirstScanValidation(UID uid);

    /**
     * ???
     */
    boolean validateScannedCaseType(String pCase1, String pCase2);

    /**
     * Система находит открытую посылку с товарами того же класса сочетаемости
     */
    boolean isOpenParcelExist(UIDScanResult theScanResult);

    /**
     * При нажатии кнопки «Закрыть» посылку (D22).
     */
    void closeParcel( Parcel parcel) throws  SQLException, UserException;

    /**
     * После закрытия посылки обновляем БД
     */
    void  updateDB(Parcel parcel) throws UserException;

    void doPackGeneral(Object theScannedUIDObj, String userInputCartonType) throws UserException,  SQLException;

    UIDScanResult doUIDScan(String theScanned)  throws ScanUIDException;

    void removeAll();

    int getOrderDetailSize();

    void makeParcelMapKeyClosed(Parcel currentParcel) ;
}
