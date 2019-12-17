package com.ltm.backend.controller.cartonization;

import com.ltm.backend.model.Carton;

import java.util.Optional;

public interface CartonRecommender {
    Optional<Carton> recommendCarton(DimensionsData dimensionsData, String cartonGroup);

    default Carton recommendCartonOrDefault(DimensionsData dimensionsData, String cartonGroup) {
        return recommendCarton(dimensionsData, cartonGroup)
            .orElse(new Carton(cartonGroup, "не найдено", "не найдено", 0, 0, 0, 0, 0));
    }
}