package com.ltm.backend.utils;


import java.io.Serializable;

public class ObjectWrapper <T> implements Serializable {

    public static final String SORTTABEL = "SORTTABEL";
    public static final String SCANED_ORDERS = "SCANED_ORDERS";
    public static final String PARCELS = "PARCELS";
    public static final String PARCELS_UI="PARCELS_UI";
    public static final String CARTONS="CARTONS";



    private T value;
    private String name;


    public ObjectWrapper (){

    }

    public ObjectWrapper (String name,T initialObject){
        this.name = name;
        this.value = initialObject;
    }

    public T getObject()  { return this.value; }


    public String getName(){
        return this.name;
    }


}
