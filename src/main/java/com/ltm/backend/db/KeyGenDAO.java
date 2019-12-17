package com.ltm.backend.db;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.util.stream.IntStream;

public class KeyGenDAO extends JdbcDaoSupport {
    private static final Logger log = Logger.getLogger(KeyGenDAO.class);

    /**
     * Следующие {@code size} значений счетчика для заданного {@code keyName}
     */
    public IntStream getNextKeys(String keyName, int size) {
        String updateCounterSql = "" +
            "UPDATE NCOUNTER SET KEYCOUNT = KEYCOUNT + ?, editdate = getUtcDate() " +
            "WHERE KEYNAME = ?";
        String selectCounterSql = "SELECT KEYCOUNT FROM NCOUNTER WHERE KEYNAME = ?";

        getJdbcTemplate().update(updateCounterSql, size, keyName);
        int currentValue = getJdbcTemplate().queryForObject(selectCounterSql, Integer.class, keyName);

        // старое значение не включается в результат
        int firstValue = currentValue - size + 1;
        log.debug("Generated inclusive range for key '" + keyName + "': "+ firstValue + ".." + currentValue);
        return IntStream.rangeClosed(firstValue, currentValue);
    }
}
