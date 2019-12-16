package com.ltm.backend.controller;

import com.ltm.MyUI;
import com.vaadin.ui.UI;

import java.util.*;

public class ParcelSharedService {

    private static  ParcelSharedService instance;
    private static Map<String, String> sharedOrders;



    private ParcelSharedService(){
        this.sharedOrders = new HashMap<>();
    }


    public static ParcelSharedService getInstance(){
         if (instance == null)
             instance = new ParcelSharedService();

         return instance;
    }


    public void addOrderLock(String pOrder){
       String userId =  ((MyUI) UI.getCurrent()).getCurrentSessionUtils().getUserId();
        if (this.sharedOrders.get(pOrder) == null) this.sharedOrders.put(pOrder, userId);
    }

    public void removeOrderOnClose (String pOrder){

        String savedUserId = this.sharedOrders.get(pOrder);

        if (savedUserId!= null){

            String userId =  ((MyUI) UI.getCurrent()).getCurrentSessionUtils().getUserId();

            if (savedUserId.equalsIgnoreCase(userId)) this.sharedOrders.remove(pOrder);
        }
    }


    public void removeOrderOnCloseByUser (){
        String userId =  ((MyUI) UI.getCurrent()).getCurrentSessionUtils().getUserId();

        Iterator<Map.Entry<String, String>> it = this.sharedOrders.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<String, String> next = it.next();
            if (next.getValue().equalsIgnoreCase(userId)){
                it.remove();
            }
        }
    }



    public boolean isOrderExistForEnotherUser(String pOrder){

        String savedUserId = this.sharedOrders.get(pOrder);


        if (savedUserId!= null){

            String userId =  ((MyUI) UI.getCurrent()).getCurrentSessionUtils().getUserId();

            if (!savedUserId.equalsIgnoreCase(userId)) return true;
        }


        return false;
    }

}
