package com.ltm.backend.controller.cartonization;

import com.ltm.MyUI;
import com.ltm.backend.model.OrderDetail;
import com.ltm.backend.model.UID;
import com.vaadin.ui.UI;

import java.util.List;

public interface CartonizationService {
    List<OrderDetail> cartonize(UID pUid);

    static List<OrderDetail> getCartonizedOrderDetailsFromSession(String orderKey) {
        return ((MyUI) UI.getCurrent()).getCurrentSessionUtils().getCartonizationMemory().get(orderKey);
    }

    static void saveCartonizedOrderDetailsToSession(String orderKey, List<OrderDetail> details) {
        ((MyUI) UI.getCurrent()).getCurrentSessionUtils().getCartonizationMemory().put(orderKey, details);
    }
}
