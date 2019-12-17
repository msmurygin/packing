package com.ltm.backend.controller;

import com.ltm.MyUI;
import com.ltm.backend.db.DBService;
import com.ltm.backend.exception.UserException;
import com.ltm.backend.model.OrderDetail;
import com.ltm.backend.model.Parcel;
import com.ltm.backend.model.UID;
import com.ltm.backend.utils.SessionUtils;
import com.ltm.ui.ParcelLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * This class draw parcels grid
 */
public class ParcelService  implements Serializable {
    private static final Logger log = Logger.getLogger(ParcelService.class);
    private int placeNumber = 0;
    private static  ParcelService instance;

    private ParcelService() { }

    public static ParcelService getInstance(){
        if (instance == null){
            instance = new ParcelService();
        }
        return instance;
    }


    /**
     * This method only add to memory, controlling places and generating dropid's
     */
    public void addParcel(UIDScanResult scanResult) {
        checkPlacePos();

        UID uid = scanResult.getUid();
        SessionUtils sessionUtils = ((MyUI) UI.getCurrent()).getCurrentSessionUtils();
        Parcel parcel = sessionUtils.getParcelFromSession(uid);

        if (parcel == null || scanResult.isNeedToGenerateNewDropId()) {                                                      // do not have parcel yet
            placeNumber++;
            String dropid = generateNewParcelNumber();
            String orderKey = uid.getOrderKey();
            List<OrderDetail> orderDetailList = sessionUtils.getCartonizationMemory().get(orderKey);
            parcel = new Parcel(placeNumber, dropid, orderKey, uid.getPutawayClass(), orderDetailList);
            parcel.addUid(uid);
            parcel.start();
            sessionUtils.setParcelToSession(uid, parcel);
        } else {
            parcel.inc();
            parcel.addUid(uid);
        }
    }

    /**
     * If session rset plac number must be reset
     */
    private void checkPlacePos() {
        if (this.getParcelMap().size() == 0 ) this.placeNumber = 0;
    }


    public void removeParcel(UIDScanResult scanResult){
        ((MyUI)UI.getCurrent()).getCurrentSessionUtils().removeParcelFromSession(scanResult.getUid());
    }


    public void removeParcel(String parcelId){
        ((MyUI)UI.getCurrent()).getCurrentSessionUtils().removeParcelFromSession(parcelId);
    }


    public void removeParcelLayout(String parcelId){
        ((MyUI)UI.getCurrent()).getCurrentSessionUtils().removeParcelLayoutFromSession(parcelId);
    }



    public Parcel getParcelByUID(UID uid){
        return ((MyUI)UI.getCurrent()).getCurrentSessionUtils().getParcelFromSession(uid);
    }

    public Map<String, Parcel> getParcelMap() {
        return ((MyUI)UI.getCurrent()).getCurrentSessionUtils().getParcelMemory();
    }
    public Map<String, ParcelLayout> getParcelUILayout() {
        return ((MyUI)UI.getCurrent()).getCurrentSessionUtils().getParcelUIMemory();
    }



    public void drawParcel2 (GridLayout gridLayout, final UID uid) {



        gridLayout.removeAllComponents();



        Map<String, Parcel> unsortedParcelMemory =((MyUI) UI.getCurrent()).getCurrentSessionUtils().getParcelMemory();



        LinkedHashMap<String, Parcel> sortedParcelMap = unsortedParcelMemory.entrySet().stream().sorted((p1, p2) -> String.valueOf(p1.getValue().getPlace()).compareTo(String.valueOf(p2.getValue().getPlace())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));


        Iterator<Map.Entry<String, Parcel>> it = sortedParcelMap.entrySet().iterator();
        while (it.hasNext()){

            Parcel currenParcel = it.next().getValue();
            ParcelLayout parcelLayout = new ParcelLayout(currenParcel, uid);
            gridLayout.addComponent(parcelLayout);

            if (!currenParcel.getUidList().contains(uid)){
                parcelLayout.makeUISelected();
            }

        }
    }

    /**
     * Система автоматически генерирует номер посылки, к которому будет привязывать упаковываемые к нему УИТы
     */

    public String generateNewParcelNumber() {
        return DBService.getInstance().getNextKey("RPT_YMCRT02", "%09d","P"  );
    }




    public  void close(Parcel parcel , ParcelLayout parcelLayout){


        parcel.getOrderDetail().setClosed(true);
        parcel.end();
        try {
            PackServiceImpl.getInstance().closeParcel(parcel);
            parcelLayout.rePaint(true);

        } catch (SQLException | UserException e) {
            log.error("Could not close parcel", e);
            parcel.getOrderDetail().setClosed(false);
            parcelLayout.rePaint(true);
            // TODO PARCEL CLOSE ROLL BACK NEEDED
            Notification.show(e.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }



}
