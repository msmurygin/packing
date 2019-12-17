package com.ltm.ui;

import com.ltm.MyUI;
import com.ltm.backend.controller.ParcelService;
import com.ltm.backend.controller.UIDScanResult;
import com.ltm.backend.exception.UserException;
import com.ltm.backend.model.Parcel;
import com.ltm.backend.utils.SerializeSessionUtils;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;

import java.sql.SQLException;

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
