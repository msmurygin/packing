package com.ltm.backend.utils;

import com.ltm.MyUI;
import com.ltm.backend.controller.PackService;
import com.ltm.backend.controller.UIDScanResult;
import com.ltm.backend.controller.cartonization.CartonizationServiceImpl;
import com.ltm.backend.db.DBService;
import com.ltm.backend.model.OrderDetail;
import com.ltm.backend.model.Parcel;
import com.ltm.backend.model.SortTable;
import com.ltm.ui.ParcelLayout;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;
import org.apache.log4j.Logger;
import org.springframework.dao.DuplicateKeyException;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * YM-179, 04/07/2019, Maxim Smurygin
 */
public class SerializeSessionUtils {

    private static final Logger logger = Logger.getLogger(SerializeSessionUtils.class);



    public static void saveSessionObjects(){

        ObjectWrapperList objectWrapperList = new ObjectWrapperList();

        Map<String, List<OrderDetail>> cartonizationResultMap =  ((MyUI)UI.getCurrent()).getCurrentSessionUtils().getCartonizationMemory();
        Map<String, Boolean> scansByOrdersMap =  ((MyUI)UI.getCurrent()).getCurrentSessionUtils().getScannedByOrdersMemory();
        Map<String, Parcel> parcelMap = ((MyUI)UI.getCurrent()).getCurrentSessionUtils().getParcelMemory();
        Map<String, ParcelLayout> parcelUIMap =  ((MyUI)UI.getCurrent()).getCurrentSessionUtils().getParcelUIMemory();
        SortTable sortTable = ((MyUI)UI.getCurrent()).getCurrentSessionUtils().getUserTable();


        String packageTypeLabelValue =  (String) VaadinSession.getCurrent().getAttribute(SessionUtils.PACKAGE_TYPE_LABEL_REF);
        UIDScanResult scannedUID = (UIDScanResult) VaadinSession.getCurrent().getAttribute(SessionUtils.UID_TEXTFIELD_REF);
        String gridCaptionLabel =  (String) VaadinSession.getCurrent().getAttribute(SessionUtils.GRID_CAPTION_LABEL);

        objectWrapperList.addToList(new ObjectWrapper(ObjectWrapper.CARTONS,  cartonizationResultMap));
        objectWrapperList.addToList(new ObjectWrapper(ObjectWrapper.SCANED_ORDERS, scansByOrdersMap));
        objectWrapperList.addToList(new ObjectWrapper(ObjectWrapper.PARCELS, parcelMap));
        objectWrapperList.addToList(new ObjectWrapper(ObjectWrapper.PARCELS_UI, parcelUIMap));
        objectWrapperList.addToList(new ObjectWrapper(ObjectWrapper.SORTTABEL, sortTable));


        objectWrapperList.addToList(new ObjectWrapper(SessionUtils.PACKAGE_TYPE_LABEL_REF, packageTypeLabelValue));
        objectWrapperList.addToList(new ObjectWrapper(SessionUtils.UID_TEXTFIELD_REF, scannedUID));
        objectWrapperList.addToList(new ObjectWrapper(SessionUtils.GRID_CAPTION_LABEL , gridCaptionLabel));
        objectWrapperList.addToList(new ObjectWrapper(SessionUtils.BROADCASTED_LOCATION , ((MyUI)UI.getCurrent()).getData()));
        Object parcelCountObject = VaadinSession.getCurrent().getAttribute(CartonizationServiceImpl.PARCELS_COUNT_ATTRIBUTE_NAME);
        objectWrapperList.addToList(new ObjectWrapper(CartonizationServiceImpl.PARCELS_COUNT_ATTRIBUTE_NAME, parcelCountObject));
        objectWrapperList.addToList(new ObjectWrapper(PackService.REPRINT_NEEDED, ((MyUI)UI.getCurrent()).getCurrentSessionUtils().getCloseMethodList()));




        serializeObjectToDatabase( ((MyUI)UI.getCurrent()).getCurrentSessionUtils().getUserId(), objectWrapperList);


    }


    private  static void serializeObjectToDatabase(String pUser, Object pObject){

        // ********************************************
        // 09/07/2019, Maxim Smurygin ROLL-BACK, START
        // NEVER SAVE SESSION
        pObject = null;
        // 09/07/2019, Maxim Smurygin ROLL-BACK, END


        if (pObject == null) return;

        ByteArrayOutputStream baos =  null;
        ObjectOutputStream oos = null;
        byte[] objectAsBytes = null;
        try{
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(pObject);
            objectAsBytes = baos.toByteArray();

            DBService.getInstance().saveSessionObject(pUser, objectAsBytes);

        }catch (IOException | DuplicateKeyException e){
            logger.error(e.getMessage());

            if (e instanceof DuplicateKeyException){
                logger.debug("Record exist, let's update");
                DBService.getInstance().updateSessionObject(pUser, objectAsBytes);
            }
        }


    }



}


