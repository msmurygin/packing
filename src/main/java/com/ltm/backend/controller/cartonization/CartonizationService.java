package com.ltm.backend.controller.cartonization;

import com.ltm.backend.model.OrderDetail;
import com.ltm.backend.model.UID;

import java.util.List;

public interface CartonizationService {
    List<OrderDetail> cartonize(UID pUid);
}
