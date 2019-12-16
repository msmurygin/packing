package com.ltm.backend.controller.cartonization;

import com.ltm.backend.db.DBService;
import com.ltm.backend.model.CartonType;
import com.ltm.backend.model.PickDetail;

import java.util.List;
import java.util.Optional;

public class CartonTypeRecommendationByDimensionsAndVolumeStrategy implements CartonTypeRecommendationStrategy {

    static final CartonType FAKE_CARTON_TYPE = new CartonType(
            null,
            "fakeCartonType",
            "fakeCartonType",
            Double.MAX_VALUE,
            0,
            0,
            0,
            0
    );
    private static final double PICK_VOLUME_MULTIPLIER = 1.05;

    private final DBService dbService;

    CartonTypeRecommendationByDimensionsAndVolumeStrategy(DBService dbService) {
        this.dbService = dbService;
    }

    @Override
    public CartonType getRecommendedCartonType(PickDetail pickDetail, String cartonGroup) {
        List<CartonType> cartonTypesPresentedInWarehouse = dbService.getCartonTypesPresentedInWarehouse(cartonGroup);

        Optional<CartonType> optimalCartonTypeToPackIn = cartonTypesPresentedInWarehouse.stream()
                .filter(cartonType -> cartonType.getWidth() > pickDetail.getWidth() && cartonType.getHeight() > pickDetail.getHeight() && cartonType.getLength() > pickDetail.getLength())
                .filter(cartonType -> cartonType.getBoxVolume() > PICK_VOLUME_MULTIPLIER * pickDetail.getCubicCapacity())
                .min(CartonType::compareTo);

        Optional<CartonType> nonPackCartonType = dbService.getNonPackCartonType();

        return optimalCartonTypeToPackIn.orElse(nonPackCartonType.orElse(FAKE_CARTON_TYPE));
    }
}
