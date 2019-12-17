package com.ltm.backend.controller.cartonization;

import com.ltm.backend.db.DBService;
import com.ltm.backend.model.Carton;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.ToIntFunction;

import static com.ltm.backend.controller.cartonization.CartonizationServiceImpl.calculateParcelsQuantity;

public class CartonRecommenderByVolume implements CartonRecommender {

    private final DBService dbService;

    CartonRecommenderByVolume(DBService dbService) {
        this.dbService = dbService;
    }

    @Override
    public Optional<Carton> recommendCarton(DimensionsData dim, String cartonGroup) {
        ToIntFunction<Carton> parcelQuantityCalculator = c ->
            calculateParcelsQuantity(c.getCube(), dim.getTotalVolume());

        return dbService.getCartons(cartonGroup).stream()
            .min(Comparator.comparingInt(parcelQuantityCalculator).thenComparing(Comparator.naturalOrder()));
    }

}
