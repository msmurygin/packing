package com.ltm.backend.controller.cartonization;

import com.ltm.backend.db.DBService;
import com.ltm.backend.model.CartonType;
import com.ltm.backend.model.PickDetail;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

class CartonTypeRecommendationByDimensionsAndVolumeStrategyTest {
    @Test
    public void shouldRecommendCartonOnlyIfItHasAppropriateDimensions() {
        CartonType appropriateCartonType = new CartonType("cartonGroup", "", "", 200, 3.0, 3.0, 3.0, 1);
        CartonType notAppropriateByDimensionsCartonType = new CartonType("cartonGroup", "", "", 200, 1.0, 1.0, 1.0, 1);

        List<CartonType> cartonTypesFromDb = Arrays.asList(
                notAppropriateByDimensionsCartonType,
                appropriateCartonType
        );

        PickDetail pickDetailToPack = new PickDetail(1, "",100, 100, 2, 2, 2, "", "");

        DBService dbService = Mockito.mock(DBService.class);
        Mockito.when(dbService.getCartonTypesPresentedInWarehouse("cartonGroup")).thenReturn(cartonTypesFromDb);
        Mockito.when(dbService.getNonPackCartonType()).thenReturn(Optional.empty());

        CartonTypeRecommendationByDimensionsAndVolumeStrategy strategy = new CartonTypeRecommendationByDimensionsAndVolumeStrategy(dbService);

        CartonType result = strategy.getRecommendedCartonType(pickDetailToPack, "cartonGroup");

        Assertions.assertEquals(appropriateCartonType, result);
    }

    @Test
    public void shouldRecommendCartonOnlyIfItHasAppropriateVolume() {
        CartonType appropriateCartonType = new CartonType("cartonGroup", "", "", 106, 3.0, 3.0, 3.0, 1);
        CartonType notAppropriateByVolumeCartonType = new CartonType("cartonGroup", "", "", 104, 3.0, 3.0, 3.0, 1);

        List<CartonType> cartonTypesFromDb = Arrays.asList(
                notAppropriateByVolumeCartonType,
                appropriateCartonType
        );

        PickDetail pickDetailToPack = new PickDetail(1, "",100, 100, 2, 2, 2, "", "");

        DBService dbService = Mockito.mock(DBService.class);
        Mockito.when(dbService.getCartonTypesPresentedInWarehouse("cartonGroup")).thenReturn(cartonTypesFromDb);
        Mockito.when(dbService.getNonPackCartonType()).thenReturn(Optional.empty());

        CartonTypeRecommendationByDimensionsAndVolumeStrategy strategy = new CartonTypeRecommendationByDimensionsAndVolumeStrategy(dbService);

        CartonType result = strategy.getRecommendedCartonType(pickDetailToPack, "cartonGroup");

        Assertions.assertEquals(appropriateCartonType, result);
    }

    @Test
    public void shouldRecommendCartonWithMinimalVolumeBetweenAppropriate() {
        CartonType appropriateCartonTypeWithMinimalVolume = new CartonType("cartonGroup", "", "", 106, 3.0, 3.0, 3.0, 1);
        CartonType appropriateCartonType1 = new CartonType("cartonGroup", "", "", 204, 3.0, 3.0, 3.0, 1);
        CartonType appropriateCartonType2 = new CartonType("cartonGroup", "", "", 304, 3.0, 3.0, 3.0, 1);
        CartonType appropriateCartonType3 = new CartonType("cartonGroup", "", "", 404, 3.0, 3.0, 3.0, 1);
        CartonType appropriateCartonType4 = new CartonType("cartonGroup", "", "", 504, 3.0, 3.0, 3.0, 1);
        CartonType appropriateCartonType5 = new CartonType("cartonGroup", "", "", 604, 3.0, 3.0, 3.0, 1);
        CartonType appropriateCartonType6 = new CartonType("cartonGroup", "", "", 704, 3.0, 3.0, 3.0, 1);

        List<CartonType> cartonTypesFromDb = Arrays.asList(
                appropriateCartonType1,
                appropriateCartonType2,
                appropriateCartonTypeWithMinimalVolume,
                appropriateCartonType3,
                appropriateCartonType4,
                appropriateCartonType5,
                appropriateCartonType6
        );

        PickDetail pickDetailToPack = new PickDetail(1, "",100, 100, 2, 2, 2, "", "");

        DBService dbService = Mockito.mock(DBService.class);
        Mockito.when(dbService.getCartonTypesPresentedInWarehouse("cartonGroup")).thenReturn(cartonTypesFromDb);
        Mockito.when(dbService.getNonPackCartonType()).thenReturn(Optional.empty());

        CartonTypeRecommendationByDimensionsAndVolumeStrategy strategy = new CartonTypeRecommendationByDimensionsAndVolumeStrategy(dbService);

        CartonType result = strategy.getRecommendedCartonType(pickDetailToPack, "cartonGroup");

        Assertions.assertEquals(appropriateCartonTypeWithMinimalVolume, result);
    }

    @Test
    public void shouldRecommendNonPackCartonIfThereAreNotAppropriateCartonTypes() {
        CartonType nonPackCartonType = new CartonType("", "", "NONPACK", 204, 3.0, 3.0, 3.0, 1);

        List<CartonType> cartonTypesFromDb = new ArrayList<>();

        PickDetail pickDetailToPack = new PickDetail(1, "",100, 100, 2, 2, 2, "", "");

        DBService dbService = Mockito.mock(DBService.class);
        Mockito.when(dbService.getCartonTypesPresentedInWarehouse("cartonGroup")).thenReturn(cartonTypesFromDb);
        Mockito.when(dbService.getNonPackCartonType()).thenReturn(Optional.of(nonPackCartonType));

        CartonTypeRecommendationByDimensionsAndVolumeStrategy strategy = new CartonTypeRecommendationByDimensionsAndVolumeStrategy(dbService);

        CartonType result = strategy.getRecommendedCartonType(pickDetailToPack, "cartonGroup");

        Assertions.assertEquals(nonPackCartonType, result);
    }

    @Test
    public void shouldReturnFakeRecommendationIfThereAreNotAppropriateCartonTypesAndThereIsNotNonPack() {
        List<CartonType> cartonTypesFromDb = new ArrayList<>();

        PickDetail pickDetailToPack = new PickDetail(1, "",100, 100, 2, 2, 2, "", "");

        DBService dbService = Mockito.mock(DBService.class);
        Mockito.when(dbService.getCartonTypesPresentedInWarehouse("cartonGroup")).thenReturn(cartonTypesFromDb);
        Mockito.when(dbService.getNonPackCartonType()).thenReturn(Optional.empty());

        CartonTypeRecommendationByDimensionsAndVolumeStrategy strategy = new CartonTypeRecommendationByDimensionsAndVolumeStrategy(dbService);

        CartonType result = strategy.getRecommendedCartonType(pickDetailToPack, "cartonGroup");

        Assertions.assertEquals(CartonTypeRecommendationByDimensionsAndVolumeStrategy.FAKE_CARTON_TYPE, result);
    }
}