package com.ltm.ui;

import com.ltm.MyUI;
import com.ltm.backend.model.LocToBroadCastWrapper;
import com.ltm.backend.model.LocsToBroadcast;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.util.ArrayList;
import java.util.List;

public class LocationView extends VerticalLayout {

    private List<LocsToBroadcast> locsToBroadcastList = null;
    private VerticalLayout contentLayout = new VerticalLayout();

    public LocationView(LocToBroadCastWrapper pLocToBroadCastWrapper){

        Button refreshButton = new Button();
        refreshButton.setIconAlternateText("Обновить");
        refreshButton.setStyleName(ValoTheme.BUTTON_BORDERLESS);
        refreshButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        refreshButton.addStyleName(ValoTheme.BUTTON_TINY);
        refreshButton.setIcon(VaadinIcons.REFRESH);
        refreshButton.addClickListener(clickEvent -> {
            //------------------------------------------------------------------------------
            // Remove broadcasted location from lock map
            ((MyUI)(MyUI.getCurrent())).refreshBroadcastedLocationButtonAction();
            //-----------------------------------------------------------------------------
        });

        HorizontalLayout navLayout = new HorizontalLayout();
        navLayout.addComponents(new Label("Назначенные ячейки"), refreshButton);
        addComponents(navLayout, contentLayout);
        contentLayout.setMargin(false);
        contentLayout.setSpacing(false);
        setUp(pLocToBroadCastWrapper);
    }

    public void setUp(LocToBroadCastWrapper locToBroadCastWrapper) {

        if (locsToBroadcastList == null) this.locsToBroadcastList = new ArrayList<>();

        contentLayout.removeAllComponents();
        locsToBroadcastList.clear();
        locsToBroadcastList.addAll(locToBroadCastWrapper.getSortLocationList());

        // show message
        if (locsToBroadcastList.size() > 1){
            InformWindow informWindow = new InformWindow("Внимание, отбор из двух ячеек!", "Предупреждение");
            UI.getCurrent().addWindow(informWindow);
        }


        locsToBroadcastList.stream()
                .forEach(sortLocItem ->{
                    HorizontalLayout vl = new HorizontalLayout();
                    Label sortLocation = new Label(sortLocItem.getSortLocation());
                    Label dropId = new Label(sortLocItem.getDropId());
                    vl.addComponents(sortLocation, dropId);
                    contentLayout.addComponent(vl);
        });

    }
}
