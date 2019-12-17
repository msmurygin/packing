package com.ltm.backend.controller.cartonization;

import com.ltm.backend.model.PickDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CartonizationUsingVolumeTest extends AbstractCartonizationTest {

    @BeforeEach
    void init() {
        initDb();
        cartonRecommenderFactory = mock(CartonRecommenderFactory.class);
        when(cartonRecommenderFactory.createRecommender()).thenReturn(new CartonRecommenderByVolume(db));
    }

    /**
     * Для маленького товара должна подойти самая маленькая коробка
     */
    @Test
    void recommendSmallestCarton() {
        List<PickDetail> pickDetails = Arrays.asList(newPickDetail(10, 10, 10, 1));

        CARTONS.forEach(c -> assertTrue(cartonFitsByVolume(c, pickDetails), c::getCartonType));

        testSinglePutAwayClassOrder(uid, pickDetails, CARTON_A);
    }

    /**
     * Заказ должен войти в одну коробку
     */
    @Test
    void recommendSingleCarton() {
        List<PickDetail> pickDetails = Arrays.asList(
            newPickDetail(10, 10, 10, 20),
            newPickDetail(15, 15, 15, 20),
            newPickDetail(20, 20, 20, 20)
        );

        assertFalse(cartonFitsByVolume(CARTON_A, pickDetails));
        assertFalse(cartonFitsByVolume(CARTON_B, pickDetails));
        assertTrue(cartonFitsByVolume(CARTON_C, pickDetails));

        testSinglePutAwayClassOrder(uid, pickDetails, CARTON_C);
    }

    /**
     * нужно 2 коробки
     */
    @Test
    void recommendTwoCartons() {
        List<PickDetail> pickDetails = Arrays.asList(
            newPickDetail(10, 10, 10, 35),
            newPickDetail(15, 15, 15, 35),
            newPickDetail(20, 20, 20, 35)
        );

        CARTONS.forEach(c -> assertFalse(cartonFitsByVolume(c, pickDetails)));

        testSinglePutAwayClassOrder(uid, pickDetails, CARTON_B, 2);
    }

    /**
     * нужно много коробок
     */
    @Test
    void recommendManyCartons() {
        List<PickDetail> pickDetails = Arrays.asList(
            newPickDetail(10, 10, 10, 350),
            newPickDetail(15, 15, 15, 350),
            newPickDetail(20, 20, 20, 350)
        );

        CARTONS.forEach(c -> assertFalse(cartonFitsByVolume(c, pickDetails)));

        testSinglePutAwayClassOrder(uid, pickDetails, CARTON_C, 12);
    }
}
