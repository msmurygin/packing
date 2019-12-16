package com.ltm.backend.controller;

import com.ltm.MyUI;
import com.ltm.backend.db.DBService;
import com.ltm.backend.model.ConfigBean;
import com.ltm.backend.model.LocsToBroadcast;
import com.vaadin.server.VaadinServletService;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocationBroadcasterService {


    private static final Logger LOGGER = Logger.getLogger(LocationBroadcasterService.class.getName());
    private static LocationBroadcasterService instance = null;
    private static VaadinServletService service = null;

    private static Thread executorServiceThread;

    public static LocationBroadcasterService init(VaadinServletService pService){
        if (instance == null){
            service = pService;
            instance = new LocationBroadcasterService();
        }
        return instance;
    }

    private LocationBroadcasterService(){

        LOGGER.debug("Service inti() ");
        LOGGER.debug("Getting location tob broadcast from db");

       long sleep = Long.parseLong(DBService.getInstance().getNsqlConfigValue("LT_REFRESHORDERFORPACK"));
        executorServiceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Map<String, List<LocsToBroadcast>> locsToBroadcastMap = DBService.getInstance().getLocsToBroadcast();

                        LocationBroadcaster.broadcast(locsToBroadcastMap);

                        Thread.currentThread().sleep(sleep);

                    }catch ( Exception e){
                        LOGGER.error(e);
                    }

                }
            }
        });
        executorServiceThread.start();
    }

    private static void refreshForcibly(){
        try {
            Map<String, List<LocsToBroadcast>> locsToBroadcastMap = DBService.getInstance().getLocsToBroadcast();
            LocationBroadcaster.broadcastForUI(((MyUI)(MyUI.getCurrent())), locsToBroadcastMap);
        }catch (Exception e){
            LOGGER.error(e);
        }
    }

}
