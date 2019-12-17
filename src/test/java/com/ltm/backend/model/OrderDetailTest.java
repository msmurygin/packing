package com.ltm.backend.model;

import com.ltm.backend.exception.UserException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class OrderDetailTest {

    private static final String TEST_VALUE_1 ="TEST-VAL_1";
    private static final String TEST_VALUE_2 ="TEST-VAL_2";

    @Test
    public void shouldChangeSelectedCartonTypeOnlyOnce() {
        OrderDetail orderDetail = new OrderDetail("", "", 2, "", "", 1, null);
        orderDetail.setSelectedCartonTypeIfAbsent(TEST_VALUE_1);
        assertNotNull(orderDetail.getSelectedCartonType());
        orderDetail.setSelectedCartonTypeIfAbsent(TEST_VALUE_2);
        assertEquals(TEST_VALUE_1, orderDetail.getSelectedCartonType() );

    }

}
