package com.ltm.backend.db;

import com.ltm.backend.exception.UserException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.HashMap;
import java.util.Map;
import java.util.PrimitiveIterator;
import java.util.stream.IntStream;

import static org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW;

public class KeyGenSO implements KeyGenService, DisposableBean {
    private static final Logger log = Logger.getLogger(KeyGenSO.class);
    static final int DEFAULT_CACHE_SIZE = 20;
    // Храним ключи для счетчиков
    private final Map<String, PrimitiveIterator.OfInt> cachedKeys = new HashMap<>();

    private PlatformTransactionManager transactionManager;
    private KeyGenDAO keyGenDao;

    public void setTransactionManager(PlatformTransactionManager txManager){
        this.transactionManager = txManager;
    }

    public void setKeyGenDao(KeyGenDAO keyGenDao) {
        this.keyGenDao = keyGenDao;
    }

    @Override
    public synchronized int getNextKey(String keyName) throws UserException {
        PrimitiveIterator.OfInt keys = cachedKeys.get(keyName);

        if (keys == null || !keys.hasNext()) {
            keys = getNextKeys(keyName).iterator();
            cachedKeys.put(keyName, keys);
        }
        return keys.nextInt();
    }

    private IntStream getNextKeys(String keyName) throws UserException {
        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition();
        txDef.setPropagationBehavior(PROPAGATION_REQUIRES_NEW);
        TransactionStatus txStatus = transactionManager.getTransaction(txDef);
        try {
            IntStream keys = keyGenDao.getNextKeys(keyName, DEFAULT_CACHE_SIZE);
            transactionManager.commit(txStatus);
            return keys;
        } catch (Exception e) {
            log.error("Failed to get next keys for " + keyName, e);
            transactionManager.rollback(txStatus);
            throw new UserException(e.getMessage());
        }
    }

    @Override
    public void destroy() throws Exception {
        System.out.println("Spring Container is destroy! Customer clean up");
        cachedKeys.clear();
    }
}
