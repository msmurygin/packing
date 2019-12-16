package com.ltm.backend.db;

import com.ltm.backend.exception.UserException;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

public class KeyGenDAO extends JdbcDaoSupport implements KeyGenService{



    @Override
    public int getNextKey(String keyName) throws UserException {

        getJdbcTemplate()
                .update("UPDATE NCOUNTER SET KEYCOUNT =  KEYCOUNT + 1   WHERE KEYNAME = ?"
                        , keyName);

        return  getJdbcTemplate()
                    .queryForObject("SELECT KEYCOUNT FROM NCOUNTER WHERE KEYNAME = ? ",
                            new Object[]{
                                    keyName
                            }
                            , Integer.class);





    }

}
