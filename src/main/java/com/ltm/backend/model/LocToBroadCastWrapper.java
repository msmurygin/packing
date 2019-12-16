package com.ltm.backend.model;
import java.util.ArrayList;
import java.util.List;

public class LocToBroadCastWrapper {
    private List<LocsToBroadcast> locsToBroadcastList = null;
    private String orderKey;
    private SortTable sortTable;




    public LocToBroadCastWrapper(){
        locsToBroadcastList  = new ArrayList<>();
    }



    public LocToBroadCastWrapper(String pOrderKey , List<LocsToBroadcast> listToAdd){
        locsToBroadcastList  = new ArrayList<>();
        locsToBroadcastList.addAll(listToAdd);
        this.orderKey = pOrderKey;
    }



    public void add(LocsToBroadcast locsToBroadcast){
        this.locsToBroadcastList.add(locsToBroadcast);
    }


    public List<LocsToBroadcast> getSortLocationList(){
        return this.locsToBroadcastList;
    }


    public String getOrderKey() {
        return orderKey;
    }

    public void setOrderKey(String orderKey) {
        this.orderKey = orderKey;
    }

    public SortTable getSortTable() {
        return sortTable;
    }

    public void setSortTable(SortTable sortTable) {
        this.sortTable = sortTable;
    }
}
