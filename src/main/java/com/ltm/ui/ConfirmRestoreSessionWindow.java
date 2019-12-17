package com.ltm.ui;

import com.ltm.MyUI;
import com.ltm.backend.controller.LocationBroadcaster;
import com.ltm.backend.controller.PackService;
import com.ltm.backend.controller.ParcelService;
import com.ltm.backend.controller.ParcelSharedService;
import com.ltm.backend.controller.UIDScanResult;
import com.ltm.backend.db.DBService;
import com.ltm.backend.exception.UserException;
import com.ltm.backend.model.LocToBroadCastWrapper;
import com.ltm.backend.model.OrderDetail;
import com.ltm.backend.model.SortTable;
import com.ltm.backend.model.UID;
import com.ltm.backend.utils.ObjectWrapper;
import com.ltm.backend.utils.ObjectWrapperList;
import com.ltm.backend.utils.SessionUtils;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Notification;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

public class ConfirmRestoreSessionWindow  extends ConfirmWindow {

    public ConfirmRestoreSessionWindow(String title, String msg) {
        super(title, msg, null, null,false);
    }


    @Override
    public void confirmEvent() throws UserException, SQLException {
        System.out.println("Confirm event");

        MyUI myUi = (MyUI) MyUI.getCurrent();
        SessionUtils sessionUtils = getSessionUtils();
        VaadinSession vaadinSession = VaadinSession.getCurrent();
        UIDScanResult scannedUID = null;
        String packageString = null;
        String gridLabelString = null;
        LocToBroadCastWrapper locsToBroadcast = null;

        // HERE We full fill session variables
        String userId = sessionUtils.getUserId();
        ObjectWrapperList sessionData = DBService.getInstance().getSessionData(userId);

        if (sessionData != null) {
            Iterator<ObjectWrapper> it = sessionData.getList().iterator();
            while (it.hasNext()) {
                ObjectWrapper t = it.next();
                if (t.getName().equalsIgnoreCase(ObjectWrapper.CARTONS))
                    sessionUtils.setToCartonizationMemory(t.getObject());
                if (t.getName().equalsIgnoreCase(ObjectWrapper.PARCELS))
                    sessionUtils.setParcelMemory(t.getObject());
                if (t.getName().equalsIgnoreCase(ObjectWrapper.PARCELS_UI))
                    sessionUtils.setParcelUIMemory(t.getObject());
                if (t.getName().equalsIgnoreCase(ObjectWrapper.SORTTABEL))
                    sessionUtils.setUserTable((SortTable) t.getObject());
                if (t.getName().equalsIgnoreCase(ObjectWrapper.SCANED_ORDERS))
                    sessionUtils.setScannedOrdersMemory(t.getObject());
                if (t.getName().equalsIgnoreCase(SessionUtils.UID_TEXTFIELD_REF)) {
                    scannedUID = (UIDScanResult) t.getObject();
                    vaadinSession.setAttribute(SessionUtils.UID_TEXTFIELD_REF, scannedUID);
                }

                if (t.getName().equalsIgnoreCase(SessionUtils.PACKAGE_TYPE_LABEL_REF)) {
                    packageString = (String) t.getObject();
                }
                if (t.getName().equalsIgnoreCase(SessionUtils.GRID_CAPTION_LABEL)) {
                    gridLabelString = (String) t.getObject();
                }

                if (t.getName().equalsIgnoreCase(SessionUtils.BROADCASTED_LOCATION)) {
                    locsToBroadcast = (LocToBroadCastWrapper) t.getObject();
                }

                if (t.getName().equalsIgnoreCase(PackService.REPRINT_NEEDED)) {
                    sessionUtils.setCloseMethodList(t.getObject());
                }
            }

            // Refresh the UI layout
            myUi.initLayouts(true);

            // Draw layout
            UID uid = scannedUID.getUid();
            List<OrderDetail> cartonizedOrderDetailList = 
                getSessionUtils().getCartonizationMemory().get(uid.getOrderKey());
            myUi.getGridRef().setItems(cartonizedOrderDetailList);
            myUi.getPackageTypeTextFieldRef().setValue(packageString);
            myUi.getGridCaptionLabelRef().setValue(gridLabelString);
            myUi.getUidTextField().setData(scannedUID);
            myUi.getUidTextField().setValue(uid.getSerialNumber());

            myUi.getGridRef().setVisible(true);
            myUi.getPackageTypeTextFieldRef().setVisible(true);
            myUi.getGridCaptionLabelRef().setVisible(true);

            // Location to BroadCast
            LocationBroadcaster.saveBroadCatedLocation(locsToBroadcast);
            myUi.setData(locsToBroadcast);
            myUi.receiveBroadcast(locsToBroadcast);

            // Draw UI
            ParcelService.getInstance().drawParcel2(myUi.getParcelGridLayoutRef(), uid);

            close();
        }
    }

    public void action(boolean close) {
        if (close)  {
            ((MyUI) MyUI.getCurrent()).initLayouts(true);
            String userId = getSessionUtils().getUserId();
            DBService.getInstance().deleteSessionObject(userId);
            ParcelSharedService.getInstance().removeOrderOnCloseByUser();
            close();
        } else {
            try {
                confirmEvent();
            } catch (UserException | SQLException e) {
                Notification.show("Ошибка", e.getMessage(), Notification.Type.ERROR_MESSAGE);
            }
        }
    }
    
    private static SessionUtils getSessionUtils() {
        return ((MyUI) MyUI.getCurrent()).getCurrentSessionUtils();
    }

}
