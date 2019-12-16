package com.ltm.backend.exception;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class UserException extends Exception {

    public UserException(String message){
        super(message);
    }
    private boolean needToRollBackParcel = false;



    public UserException(){
        super();
    }


    public UserException (String message, boolean rollback){
        super(message);
        this.needToRollBackParcel = rollback;
    }

    @Override
    public String getMessage(){

        AtomicReference<String> msg = new AtomicReference<>("");

        if (super.getMessage() == null) {
            Arrays.asList(getStackTrace()).forEach(item -> {
                msg.set(msg + item.toString() + "\n");
            });
        }else{
            msg.set(super.getMessage());
        }


        return msg.get();
    }

    public boolean needToRollBackParcel() {
        return needToRollBackParcel;
    }

    protected void setRollback(boolean rollback) {
        this.needToRollBackParcel = rollback;
    }
}
