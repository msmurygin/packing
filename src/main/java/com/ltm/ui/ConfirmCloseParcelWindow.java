package com.ltm.ui;

import com.ltm.MyUI;
import com.ltm.backend.controller.PackService;
import com.ltm.backend.controller.PackServiceImpl;
import com.ltm.backend.controller.ParcelService;
import com.ltm.backend.controller.ParcelSharedService;
import com.ltm.backend.controller.UIDScanResult;
import com.ltm.backend.controller.cartonization.CartonizationServiceImpl;
import com.ltm.backend.exception.UserException;
import com.ltm.backend.model.OrderDetail;
import com.ltm.backend.model.Parcel;

import com.ltm.backend.utils.PrintThread;
import com.ltm.backend.utils.SerializeSessionUtils;

import com.ltm.backend.utils.SessionUtils;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.*;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ConfirmCloseParcelWindow extends ConfirmWindow {
    private final Parcel currentParcel;
    private final ParcelLayout parcelLayout;

    public ConfirmCloseParcelWindow(String title, String msg, UIDScanResult scannedUID, String userValue, Parcel parcel, ParcelLayout parcelLayout) {
        super(title, msg, scannedUID, userValue, true);
        this.currentParcel = parcel;
        this.parcelLayout = parcelLayout;
    }


    @Override
    public void confirmEvent() throws UserException, SQLException {
        System.out.println("Confirm event");

        ((MyUI)UI.getCurrent()).getCurrentSessionUtils().getCloseMethodList().add(true);

        // YM-174 START
        // Увеличиваем, так как посылку закрыли вручную и сохраняем сессию
        Integer parcels_count_orig = ( Integer ) VaadinSession.getCurrent().getAttribute(CartonizationServiceImpl.PARCELS_COUNT_ATTRIBUTE_NAME);
        VaadinSession.getCurrent().setAttribute(CartonizationServiceImpl.PARCELS_COUNT_ATTRIBUTE_NAME, ++parcels_count_orig);
        // YM-174 END


        VaadinSession.getCurrent().setAttribute(this.currentParcel.getDropId()+"_CONFIRMED","TRUE");
        ParcelService.getInstance().close(currentParcel, parcelLayout);


        SerializeSessionUtils.saveSessionObjects();

        close();

    }

    public void action(boolean close) {
        if (close) {
            close();
        } else {
            try {
                confirmEvent();
            } catch (UserException | SQLException e) {
                Notification.show("Ошибка", e.getMessage(), Notification.Type.ERROR_MESSAGE);
            }
        }
    }
}
