package com.ltm.backend.controller;

import com.ltm.backend.model.LocToBroadCastWrapper;
import com.ltm.backend.utils.SessionUtils;
import com.vaadin.server.VaadinSession;

public interface LocationBroadcastListener {
    void receiveBroadcast(LocToBroadCastWrapper message);
    void removeLocationFromUI();
    void setData(Object data);
    Object getData();
    SessionUtils getCurrentSessionUtils();
    VaadinSession getSession();
}
