package com.ltm.ui;

import com.vaadin.ui.Label;
import org.apache.log4j.Logger;

public class SessionErrorView extends com.vaadin.ui.HorizontalLayout {

    private static final Logger LOGGER = Logger.getLogger(SessionErrorView.class);
    public SessionErrorView(){
        Label label = new Label();
        label.setValue("Ошибка сессии...");
        addComponent(label);
    }
}
