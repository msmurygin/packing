package com.ltm.backend.utils;


import com.ltm.MyUI;
import com.vaadin.server.VaadinSession;
import org.apache.log4j.Logger;



public class PrintThread implements Runnable{


    private static final Logger LOGGER = Logger.getLogger(PrintThread.class);
    private String dropId = null ;
    private int num;
    private int total ;
    private String printListitems; // Maxim Smurygin, 12/08/2019, 30544, YM-232,

    private MyUI currentUI;
    private VaadinSession vaadinSession;

    // Maxim Smurygin, 12/08/2019, 30544, YM-232, START
    public PrintThread(String pDropId, int pNum, int pTotal , MyUI currentUI , VaadinSession vaadinSession, String printListItems){
        this(pDropId, pNum, pTotal, currentUI, vaadinSession);
        this.printListitems   = printListItems;
    }
    // Maxim Smurygin, 12/08/2019, 30544, YM-232, END

    public PrintThread(String pDropId, int pNum, int pTotal , MyUI currentUI , VaadinSession vaadinSession){
        this.dropId = pDropId;
        this.currentUI = currentUI;
        this.vaadinSession = vaadinSession;
    }

    @Override
    public void run() {
        LOGGER.debug("Started...");
        try {
            Thread.sleep(2000);
            new PrintUtils(this.dropId, num , total, currentUI,  this.vaadinSession,
                    printListitems!= null && printListitems.equalsIgnoreCase("1")? "Y" : "N" /* 30544, YM-232 added system settings flag */  );
        } catch (Exception e) {
            LOGGER.error(e);
        }
        LOGGER.debug("Printing done");
    }



}