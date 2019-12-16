package com.ltm.backend.exception;



public class CartonTypeException extends UserException {

    private String userValueProvided;
    private String systemValue;

    public CartonTypeException(String message){
        super(message);
    }

    public CartonTypeException(String message, String userValueProvided, String systemValue) {
        super(message);
        this.userValueProvided = userValueProvided;
        this.systemValue = systemValue;
    }


    public CartonTypeException(String message, String userValueProvided, String systemValue, boolean rollback) {
        this(message, userValueProvided, systemValue);
        this.setRollback(rollback);
    }

    public String getUserValueProvided() {
        return userValueProvided;
    }

    public void setUserValueProvided(String userValueProvided) {
        this.userValueProvided = userValueProvided;
    }

    public String getSystemValue() {
        return systemValue;
    }

    public void setSystemValue(String systemValue) {
        this.systemValue = systemValue;
    }
}

