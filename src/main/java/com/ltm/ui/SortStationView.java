package com.ltm.ui;

import com.ltm.MyUI;
import com.ltm.backend.db.DBService;
import com.ltm.backend.model.SortTable;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;

public class SortStationView extends Window  {

    private ComboBox<SortTable> sortTableComboBox = null;




    public SortStationView(){


        super("Выбор стола сортировки");

        center();
        setClosable(false);
        setModal(true);
        setWidth(350, UNITS_PIXELS);

        sortTableComboBox = new ComboBox<>();
        sortTableComboBox.setItemCaptionGenerator(SortTable::getSortTableKey);
        sortTableComboBox.setItems(DBService.getInstance().getSortTableItems());



        sortTableComboBox.addValueChangeListener(event -> {

            SortTable table = sortTableComboBox.getValue();
            if (table != null) {


                // Saving user sort table
                ((MyUI)UI.getCurrent()).getCurrentSessionUtils().setUserTable(table);

                //------------------------------------------------------------------------------
                // Remove broadcasted location from lock map
                //((MyUI)(MyUI.getCurrent())).refreshBroadcastedLocationButtonAction();
                //-----------------------------------------------------------------------------


                // Refresh the UI layout
                ((MyUI)(MyUI.getCurrent())).initLayouts(true);

                // Closing modal Window
                close();
            }


        });



        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(new MarginInfo(true,true,true,true));
        layout.addComponents(sortTableComboBox/*, buttonLayout*/);
        //layout.setExpandRatio(sortTableComboBox, 0.7F);
        //layout.setExpandRatio(buttonLayout, 0.3F);
        layout.setSizeFull();


        Panel root = new Panel();
        root.setWidth(100, Unit.PERCENTAGE);
        root.setContent(layout);
        setContent(root);


    }
}
