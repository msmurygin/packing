package com.ltm.backend.db;

import com.ltm.backend.exception.UserException;

public interface KeyGenService {

    int getNextKey(String pKeyName) throws UserException;
}
