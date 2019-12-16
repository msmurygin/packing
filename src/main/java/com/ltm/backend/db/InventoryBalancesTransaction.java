package com.ltm.backend.db;

import com.ltm.backend.exception.UserException;
import com.ltm.backend.model.Parcel;
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


        // Do initialisation -------------------------------------
        this.init();
        // -------------------------------------------------------


        TransactionDefinition txDef = new DefaultTransactionDefinition();
        TransactionStatus txStatus = transactionManager.getTransaction(txDef);
        try {
            //Updateing pickDetail
            updatePickdetail(parcel);


            updateLotxLocxId(parcel);


            updateSkuxLoc(parcel);



            // Inserting dropid first
            // dropiddetail inserting in update pickdetailCaseId method
            insertIntoDropId(parcel);


            updatePickDetailCaseId(parcel);


            //insertIntoDropId(parcel);


            //insertIntoDropidDetail(parcel);


            insertIntoTaskDetail(parcel);


            updateSerialInventory(parcel);


            insertITRN(parcel);


            insertItrnSerial(parcel);


            updateLotXIdDetail(parcel);

            transactionManager.commit(txStatus);
        } catch (Exception e) {
            transactionManager.rollback(txStatus);
            throw new UserException(e.getMessage());
        }

    }


}
