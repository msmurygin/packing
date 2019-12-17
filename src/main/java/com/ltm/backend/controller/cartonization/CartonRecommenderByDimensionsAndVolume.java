package com.ltm.backend.controller.cartonization;

import com.ltm.backend.db.DBService;
import com.ltm.backend.model.Carton;

import java.util.Comparator;
import java.util.Optional;

public class CartonRecommenderByDimensionsAndVolume implements CartonRecommender {
    private static final double VOLUME_SAFETY_FACTOR = 1.1;

    private final DBService dbService;

    public CartonRecommenderByDimensionsAndVolume(DBService dbService) {
        this.dbService = dbService;
    }

    @Override
    public Optional<Carton> recommendCarton(DimensionsData dim, String cartonGroup) {
        Optional<Carton> carton = dbService.getCartonsPresentedInWarehouse(cartonGroup).stream()
            .filter(c -> isCartonBigEnough(c, dim))
            .min(Comparator.naturalOrder());
        return carton.isPresent() ? carton : dbService.getNonPackCarton();
    }

    private boolean isCartonBigEnough(Carton carton, DimensionsData dim) {
        Box cartonBox = Box.of(carton);
        return cartonBox.getLongestDim() > dim.getMaxLongestDim()
            && cartonBox.getMiddleDim() > dim.getMaxMiddleDim()
            && cartonBox.getShortestDim() > dim.getMaxShortestDim()
            && cartonBox.getVolume() > dim.getTotalVolume() * VOLUME_SAFETY_FACTOR;
    }
}
