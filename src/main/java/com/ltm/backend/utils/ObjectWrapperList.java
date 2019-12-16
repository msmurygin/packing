package com.ltm.backend.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ObjectWrapperList<T> implements Serializable {


    ArrayList<T> listValue ;

    public ObjectWrapperList(){
    }


    public ObjectWrapperList(ArrayList<T> initialValue){
        listValue = initialValue;
    }


    public void addToList(T value){
        if (this.listValue == null) this.listValue = new ArrayList<>();
        this.listValue.add(value);
    }


    public ArrayList<T> getList(){
        return listValue;
    }
}
