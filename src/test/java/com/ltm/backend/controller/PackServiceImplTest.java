package com.ltm.backend.controller;

import com.ltm.backend.exception.UserException;
import com.ltm.backend.model.OrderDetail;
import com.ltm.backend.model.Parcel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class PackServiceImplTest {
    @Test
    public void shouldThrowExceptionIfParcelDoesNotContainUIDs() {
        PackServiceImpl packService = new PackServiceImpl();
        OrderDetail orderDetail = new OrderDetail("", "", 1, "", "", 1, "", null);
        Parcel parcel = new Parcel(1, "", "", "", Arrays.asList(orderDetail));

        Assertions.assertThrows(UserException.class, () -> packService.closeParcel(parcel));
    }


}