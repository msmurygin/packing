package com.ltm.backend.db;

import com.ltm.backend.exception.UserException;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class KeyGenSO extends KeyGenDAO implements KeyGenService {
    private static final Logger LOGGER = Logger.getLogger(KeyGenSO.class);
    private PlatformTransactionManager transactionManager;
    public void setTransactionManager(PlatformTransactionManager txManager){
        this.transactionManager = txManager;
    }




    @Override
    public int getNextKey(String pKeyName) throws UserException {
        int value = 0;
        TransactionDefinition txDef = new DefaultTransactionDefinition();
        TransactionStatus txStatus = transactionManager.getTransaction(txDef);
        try {
            LOGGER.debug("--==Generating new value  for key: "+ pKeyName+" ==--");
            value = super.getNextKey(pKeyName);
            LOGGER.debug("Value "+value);
            LOGGER.debug("--==END of method? commiting ==--");
            transactionManager.commit(txStatus);
        } catch (Exception e) {
            transactionManager.rollback(txStatus);
            throw new UserException(e.getMessage());
        }

        return value;
    }


}
