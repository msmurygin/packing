package com.ltm.backend.model;

import com.ltm.backend.exception.UserException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ParcelTest {
    @Test
    public void shouldThrowExceptionIfParcelDoesNotContainUid() {
        OrderDetail orderDetail = new OrderDetail("", "", 2, "", "", 1, "", null);
        Parcel parcel = new Parcel(1, "", "", "", Arrays.asList(orderDetail));

        assertThrows(UserException.class, () -> parcel.checkCanBeClosed());
    }

    @Test
    public void shouldThrowExceptionIfMultiPackagingIsNotSupportedAndParcelDoesNotContainAllUidsOfOrder() {
        OrderDetail orderDetail = new OrderDetail("", "", 2, "", "", 1, "", null);
        Parcel parcel = new Parcel(1, "", "", "", Arrays.asList(orderDetail));

        UID uid = Mockito.mock(UID.class);
        Mockito.when(uid.isMultiPackagingBanned()).thenReturn(true);

        parcel.addUid(uid);

        assertThrows(UserException.class, () -> parcel.checkCanBeClosed());
    }
}