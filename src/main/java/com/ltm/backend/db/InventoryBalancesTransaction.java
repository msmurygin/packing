package com.ltm.backend.db;

import com.ltm.MyUI;
import com.ltm.backend.exception.UserException;
import com.ltm.backend.model.Parcel;
import com.ltm.backend.utils.SessionUtils;
import com.vaadin.ui.UI;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;


public class InventoryBalancesTransaction extends InventoryBalancesDAO implements InventoryBalancesManager {


    private PlatformTransactionManager transactionManager;

    public void setTransactionManager(PlatformTransactionManager txManager){
        this.transactionManager = txManager;
    }


    @Override
    public void updateInventory(Parcel parcel) throws UserException {


        TransactionDefinition txDef = new DefaultTransactionDefinition();
        TransactionStatus txStatus = transactionManager.getTransaction(txDef);
        try {

            SessionUtils sessionUtils = ((MyUI) UI.getCurrent()).getCurrentSessionUtils();
            String userId = sessionUtils.getUserId();
            String sortTableKey = sessionUtils.getUserTable().getSortTableKey();


            updatePickdetail(parcel, sortTableKey);

            updateLotxLocxId(parcel, sortTableKey, userId);

            updateSkuxLoc(parcel, sortTableKey, userId);

            insertIntoDropId(parcel, sortTableKey, userId);

            updatePickDetailCaseId(parcel, userId);

            insertIntoTaskDetail(parcel, sortTableKey, userId);

            updateSerialInventory(parcel, sortTableKey, userId);

            insertITRN(parcel, sortTableKey, userId);

            insertItrnSerial(parcel, sortTableKey, userId);

            updateLotXIdDetail(parcel, userId);

            transactionManager.commit(txStatus);
        } catch (Exception e) {
            transactionManager.rollback(txStatus);
            throw new UserException(e.getMessage());
        }

    }


}
