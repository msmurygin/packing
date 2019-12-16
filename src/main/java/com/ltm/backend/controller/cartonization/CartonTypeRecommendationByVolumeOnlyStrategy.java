package com.ltm.backend.controller.cartonization;

import com.ltm.backend.db.DBService;
import com.ltm.backend.model.CartonType;
import com.ltm.backend.model.PickDetail;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class CartonTypeRecommendationByVolumeOnlyStrategy implements CartonTypeRecommendationStrategy {

    private final DBService dbService;

    CartonTypeRecommendationByVolumeOnlyStrategy(DBService dbService) {
        this.dbService = dbService;
    }

    @Override
    public CartonType getRecommendedCartonType(PickDetail pickDetail, String cartonGroup) {
        List<CartonType> cartonTypeList = dbService.getCartonTypeList(cartonGroup);

        // Sort in revers order and iterate
        cartonTypeList.stream()
                .sorted(Comparator.reverseOrder())
                .forEach(currentCartonType -> {
                        int parcelsQuantity = CartonizationServiceImpl.calculateParcelsQuantity(currentCartonType, pickDetail);
                        currentCartonType.setParcelsQuantity(parcelsQuantity);
                    }
                );

        long fittedCartonsCount = cartonTypeList.stream().filter(item -> item.getParcelsQuantity() == 1).count();

        if (fittedCartonsCount == 1) {

            CartonType min = cartonTypeList.stream()
                    .filter(item-> item.getParcelsQuantity() == 1)
                    .findFirst().get();

            return min;
        } else if (fittedCartonsCount > 1) {
            Comparator <CartonType> comparator = Comparator.comparingDouble(CartonType::getCube);

            CartonType min = cartonTypeList.stream()
                    .filter(item-> item.getParcelsQuantity() == 1)
                    .min(comparator).get();

            return min;
        } else {
            Comparator <CartonType> comparator = Comparator.comparingInt(CartonType::getParcelsQuantity);

            CartonType min = cartonTypeList.stream()
                    .min(comparator).get();

            return min;
        }
    }
}
