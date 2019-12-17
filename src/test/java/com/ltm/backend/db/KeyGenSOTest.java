package com.ltm.backend.db;

import com.ltm.backend.exception.UserException;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import static com.ltm.backend.db.KeyGenSO.DEFAULT_CACHE_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KeyGenSOTest {

    @Test
    void getNextKey() throws UserException {
        String keyName = "testKey";
        int oldKey = 100;

        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq(keyName)))
            .thenReturn(oldKey + DEFAULT_CACHE_SIZE)
            .thenReturn(oldKey + DEFAULT_CACHE_SIZE * 2);

        KeyGenDAO dao = new KeyGenDAO();
        dao.setJdbcTemplate(jdbc);
        PlatformTransactionManager txManager = mock(PlatformTransactionManager.class);

        KeyGenSO keyGenService = new KeyGenSO();
        keyGenService.setKeyGenDao(dao);
        keyGenService.setTransactionManager(txManager);

        int expectedNextKey = oldKey + 1;

        for (int i = 0; i < DEFAULT_CACHE_SIZE; i++) {
            int nextKey = keyGenService.getNextKey(keyName);
            assertEquals(expectedNextKey++, nextKey);
        }
        verify(jdbc, times(1)).queryForObject(anyString(), eq(Integer.class), eq(keyName));
        verify(txManager, times(1)).getTransaction(any());
        verify(txManager, times(1)).commit(any());

        // потратили кеш, новые значения должны подтянуться из базы
        int nextKey = keyGenService.getNextKey(keyName);
        assertEquals(expectedNextKey, nextKey);
        verify(jdbc, times(2)).queryForObject(anyString(), eq(Integer.class), eq(keyName));
        verify(txManager, times(2)).getTransaction(any());
        verify(txManager, times(2)).commit(any());
    }
}