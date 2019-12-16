package com.ltm.ui;

import com.ltm.backend.controller.PackServiceImpl;

import com.ltm.backend.controller.ParcelService;
import com.ltm.backend.exception.UserException;
import com.ltm.backend.model.OrderDetail;
import com.ltm.backend.model.Parcel;
import com.ltm.backend.model.UID;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Sizeable;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.SQLException;
import java.util.List;
import java.util.ListIterator;

public class ParcelLayout extends VerticalLayout {



    //Labels ------------------------------------------------------------------------------------
    Label placeLabel = null;
    Label packageIdLabel = null;
    Label packedQtyLabel = null;


    // ------------------------------------------------------------------------------------------
    VerticalLayout packageDetailUIDLayout = null;
    private Parcel parcel = null;
    private UID uid = null;



    public void addUID(Parcel parcel){
        this.removeAllComponents();
        paint(true);
    }


    public ParcelLayout(Parcel parcel, UID uid){
        this.parcel = parcel;
        this.uid = uid;

        paint(true);
    }


    public void paint(boolean selected){
        setMargin(false);
        setSpacing(false);
        //Labels ------------------------------------------------------------------------------------
        placeLabel = new Label("МЕСТО "+parcel.getPlace());



        packageIdLabel = new Label("ID "+ parcel.getDropId());
        packedQtyLabel = new Label("Упакованно(" + parcel.getPackedQty() + ")");
        // ------------------------------------------------------------------------------------------


        packageDetailUIDLayout = new VerticalLayout();
        packageDetailUIDLayout.setSpacing(false);
        packageDetailUIDLayout.setMargin(false);
        packageDetailUIDLayout.setWidth(100, Sizeable.Unit.PERCENTAGE);

        boolean isclosed = this.parcel.isClosed();


        String parcel_detail_style_selected = "parcel-details-selected";
        String parcel_detail_style_not_selected = "parcel-details";

        String parcel_detail_style_bg_selected ="parcel-detail-bg-selected";
        String parcel_detail_style_bg ="parcel-detail-bg";

        String parcel_image_background_selected ="parcel-image-background-selected";
        String parcel_image_background ="parcel-image-background";


        String labels_style = selected ?  "parcel-label-style-selected" : "parcel-label-style"  ;

        if (isclosed){
            parcel_detail_style_selected ="parcel-details-closed";
            parcel_detail_style_not_selected =parcel_detail_style_selected;

            parcel_detail_style_bg_selected = "parcel-detail-bg-closed";
            parcel_detail_style_bg = parcel_detail_style_bg_selected;

            parcel_image_background_selected = "parcel-image-background-closed";
            parcel_image_background = parcel_image_background_selected;

            labels_style = "parcel-label-style-closed";
        }

        placeLabel.addStyleName(labels_style);
        packageIdLabel.addStyleName(labels_style);
        packedQtyLabel.addStyleName(labels_style);


        packageDetailUIDLayout.setStyleName(selected ? parcel_detail_style_selected : parcel_detail_style_not_selected );


        ListIterator<UID> itr1 = parcel.getUidList().listIterator(parcel.getUidList().size());

        while (itr1.hasPrevious()) {
            //LOGGER.debug(packageDetail.getUid() + " added..");
            UID uid = itr1.previous();
            Label uidLabel = new Label("" + uid.getSerialNumber() + " ");
            uidLabel.addStyleName(labels_style);
            Button button = new Button();
            button.setStyleName(ValoTheme.BUTTON_BORDERLESS);
            button.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
            button.addStyleName("button-delete-uid");
            button.setIcon(VaadinIcons.ARROW_FORWARD);
            button.setData(uid);
            button.setEnabled(!isclosed);
            button.addClickListener(clickEvent -> {
                        if (clickEvent.getButton().getData()!=null){
                            UID uid1 = (UID)clickEvent.getButton().getData();
                            parcel.removeUID(uid1);
                            rePaint(selected);
                        }
                Notification.show("Eject UID button clicked!", Notification.Type.TRAY_NOTIFICATION);
            });


            HorizontalLayout layout = new HorizontalLayout();
            layout.setMargin(false);
            layout.setSpacing(false);
            layout.addComponents(uidLabel, button);
            layout.setExpandRatio(uidLabel,0.8F);
            layout.setExpandRatio(button,0.2F);
            packageDetailUIDLayout.addComponent(layout);
            //--------------------------------------------------------------------------------------------------
        }




        Panel packageDetailUIDPanel = new Panel();
        packageDetailUIDPanel.setHeight(100, Sizeable.Unit.PIXELS);
        packageDetailUIDPanel.setStyleName(selected ? parcel_detail_style_bg_selected : parcel_detail_style_bg);
        packageDetailUIDPanel.setContent(packageDetailUIDLayout);
        packageDetailUIDPanel.getContent().setSizeUndefined();



        // Beautiful rectangles on a screen ------------------------------------------------------------------
        addComponents(placeLabel, packageIdLabel, packedQtyLabel, packageDetailUIDPanel);
        if (!isclosed){
            Button button = new Button("Закрыть");
            button.setData(this.parcel);
            button.setStyleName(ValoTheme.BUTTON_BORDERLESS);
            button.addStyleName("button-close-uid");
            button.setIcon(VaadinIcons.CLOSE_CIRCLE);
            button.addClickListener(clickEvent -> {


                if (clickEvent.getButton().getData()!=null){

                    Parcel parcel = (Parcel)clickEvent.getButton().getData();


                    //888888888888888888888888888888888888888888888
                    // ********************************************
                    // 09/07/2019, Maxim Smurygin ROLL-BACK, START
                    // NEVER  SHOW CONF WINDOW

                    // YM-168, Maxim Smurygin, 25,02/2019, confirmation window block START
                    //if (VaadinSession.getCurrent().getAttribute(parcel.getDropId() + "_CONFIRMED") == null) {
                    //    UI.getCurrent().addWindow(new ConfirmCloseParcelWindow("Окно подтверждения", "Закрыть посылку?", null, null, parcel, this));
                    //    return;
                    //}
                    // YM-168, Maxim Smurygin, 25,02/2019,  confirmation window block START

                    // 09/07/2019, Maxim Smurygin ROLL-BACK, END
                    //*********************************************
                    //888888888888888888888888888888888888888888888




                    ParcelService.getInstance().close(parcel, this);

                }

            });
            addComponent(button);
        }
        setStyleName(selected ? parcel_image_background_selected : parcel_image_background);
        //----------------------------------------------------------------------------------------------------
    }


    public void makeUISelected(){
        removeStyleName("parcel-image-background-selected");
        addStyleName("parcel-image-background");
    }


    public void rePaint(Boolean selected){

        this.removeAllComponents();
        paint(selected);
    }

}
