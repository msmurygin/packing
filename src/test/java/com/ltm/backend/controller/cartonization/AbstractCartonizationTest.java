package com.ltm.backend.controller.cartonization;

import com.ltm.backend.db.DBService;
import com.ltm.backend.model.Carton;
import com.ltm.backend.model.OrderDetail;
import com.ltm.backend.model.PickDetail;
import com.ltm.backend.model.UID;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AbstractCartonizationTest {
    static final String CARTON_GROUP = "CG1";
    static final String PUTAWAY_CLASS = "chemistry";

    static final AtomicInteger PICK_DETAIL_KEYGEN = new AtomicInteger();

    static final Carton NON_PACK = new Carton(CARTON_GROUP, "nonpack", "", 999999999, 0, 0, 0, 0);
    static final Carton CARTON_A = newCarton("cartonA", 80, 50, 35);
    static final Carton CARTON_B = newCarton("cartonB", 100, 60, 40);
    static final Carton CARTON_C = newCarton("cartonC", 120, 70, 45);
    static final List<Carton> CARTONS = Arrays.asList(CARTON_A, CARTON_B, CARTON_C);

    static final UID uid = new UID();
    DBService db;
    CartonRecommenderFactory cartonRecommenderFactory;

    static {
        uid.setOrderKey("order-123");
        uid.setCartonGroup(CARTON_GROUP);
        uid.setPutawayClass(PUTAWAY_CLASS);
    }

    void initDb() {
        db = mock(DBService.class);
        when(db.getCartonsPresentedInWarehouse(eq(CARTON_GROUP))).thenReturn(CARTONS);
        when(db.getCartons(eq(CARTON_GROUP))).thenReturn(CARTONS);
        when(db.getNonPackCarton()).thenReturn(Optional.of(NON_PACK));
    }

    void testSinglePutAwayClassOrder(UID uid, List<PickDetail> pickDetails, Carton expectedCarton) {
        testSinglePutAwayClassOrder(uid, pickDetails, expectedCarton, 1);
    }

    void testSinglePutAwayClassOrder(UID uid,
                                     List<PickDetail> pickDetails,
                                     Carton expectedCarton,
                                     int expectedCartonCount) {
        when(db.getPickDetails(uid.getOrderKey())).thenReturn(pickDetails);

        CartonizationService service = new CartonizationServiceImpl(db, cartonRecommenderFactory, HashMap::new);

        List<OrderDetail> orderDetails = service.cartonize(uid);
        assertEquals(1, orderDetails.size());

        OrderDetail orderDetail = orderDetails.get(0);
        assertEquals(expectedCarton.getCartonType(), orderDetail.getCartonType());
        assertEquals(expectedCartonCount, orderDetail.getEstimatedParcelsQty());
    }

    static boolean cartonFitsByLinearDimensions(Carton carton, List<PickDetail> pickDetails) {
        Box cartonBox = Box.of(carton);
        return pickDetails.stream().allMatch(pd -> {
            Box box = Box.of(pd.getLength(), pd.getWidth(), pd.getHeight(), pd.getCubeStd());
            return cartonBox.getLongestDim() > box.getLongestDim()
                && cartonBox.getMiddleDim() > box.getMiddleDim()
                && cartonBox.getShortestDim() > box.getShortestDim();
        });
    }

    static double calcOrderVolume(List<PickDetail> pickDetails) {
        return pickDetails.stream().mapToDouble(pd -> pd.getCubeStd() * pd.getQty()).sum();
    }

    static boolean cartonFitsByVolume(Carton carton, List<PickDetail> pickDetails) {
        return carton.getCube() > calcOrderVolume(pickDetails);
    }

    static Carton newCarton(String cartonType, double x, double y, double z) {
        return new Carton(CARTON_GROUP, cartonType, "", x * y * z, x, y, z, 0);
    }

    static PickDetail newPickDetail(double x, double y, double z, int quantity) {
        return new PickDetail(quantity, PUTAWAY_CLASS, x * y * z, x, y, z,
            String.valueOf(PICK_DETAIL_KEYGEN.incrementAndGet()), "case123");
    }
}
