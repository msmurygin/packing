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

public class CartonizationUsingLinearDimensionsAndVolumeTest extends AbstractCartonizationTest {

    @BeforeEach
    void init() {
        initDb();
        cartonRecommenderFactory = mock(CartonRecommenderFactory.class);
        when(cartonRecommenderFactory.createRecommender()).thenReturn(new CartonRecommenderByDimensionsAndVolume(db));
    }

    /**
     * Для маленького товара должна подойти самая маленькая коробка
     */
    @Test
    void recommendSmallestCarton() {
        List<PickDetail> pickDetails = Arrays.asList(newPickDetail(10, 10, 10, 1));

        CARTONS.forEach(c -> {
            assertTrue(cartonFitsByLinearDimensions(c, pickDetails), c::getCartonType);
            assertTrue(cartonFitsByVolume(c, pickDetails), c::getCartonType);
        });

        testSinglePutAwayClassOrder(uid, pickDetails, CARTON_A);
    }

    /**
     * Здесь весь заказ по объему влезет в любую коробку, проверяем соответствие линейных измерений
     */
    @Test
    void recommendCartonByLinearDimension() {
        List<PickDetail> pickDetails = Arrays.asList(
            newPickDetail(95, 45, 1, 1),
            newPickDetail(90, 55, 1, 1),
            newPickDetail(80, 59, 1, 1)
        );

        assertFalse(cartonFitsByLinearDimensions(CARTON_A, pickDetails));
        assertTrue(cartonFitsByLinearDimensions(CARTON_B, pickDetails));
        assertTrue(cartonFitsByLinearDimensions(CARTON_C, pickDetails));

        CARTONS.forEach(c -> assertTrue(cartonFitsByVolume(c, pickDetails), c::getCartonType));

        testSinglePutAwayClassOrder(uid, pickDetails, CARTON_B);
    }

    /**
     * Здесь все товары подходят по линейным измерениям, проверяем суммарный объем заказа
     */
    @Test
    void recommendCartonByVolume() {
        List<PickDetail> pickDetails = Arrays.asList(
            newPickDetail(10, 10, 10, 20),
            newPickDetail(15, 15, 15, 20),
            newPickDetail(20, 20, 20, 20)
        );

        CARTONS.forEach(c -> assertTrue(cartonFitsByLinearDimensions(c, pickDetails), c::getCartonType));

        assertFalse(cartonFitsByVolume(CARTON_A, pickDetails));
        assertFalse(cartonFitsByVolume(CARTON_B, pickDetails));
        assertTrue(cartonFitsByVolume(CARTON_C, pickDetails));

        testSinglePutAwayClassOrder(uid, pickDetails, CARTON_C);
    }

    /**
     * Товар не подходит по линейным измерениям, нужно рекомендовать NON_PACK
     */
    @Test
    void recommendNonPackBecauseOfLinearDimensions() {
        List<PickDetail> pickDetails = Arrays.asList(
            newPickDetail(10, 10, 10, 1),
            newPickDetail(20, 20, 20, 1),
            newPickDetail(46, 46, 46, 1)
        );

        CARTONS.forEach(c -> assertFalse(cartonFitsByLinearDimensions(c, pickDetails)));

        testSinglePutAwayClassOrder(uid, pickDetails, NON_PACK);
    }

    /**
     * Все товары маленькие, но по объему заказ не входит ни в одну коробку,
     * нужно рекомендовать NON_PACK.
     * Автоматического разбиения заказа на несколько коробок нет.
     */
    @Test
    void recommendNonPackBecauseOfVolume() {
        List<PickDetail> pickDetails = Arrays.asList(
            newPickDetail(10, 10, 10, 50),
            newPickDetail(15, 15, 15, 50),
            newPickDetail(20, 20, 20, 50)
        );

        CARTONS.forEach(c -> {
            assertTrue(cartonFitsByLinearDimensions(c, pickDetails));
            assertFalse(cartonFitsByVolume(c, pickDetails));
        });

        testSinglePutAwayClassOrder(uid, pickDetails, NON_PACK);
    }
}
