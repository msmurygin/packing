package com.ltm.backend.controller.cartonization;

import com.ltm.backend.model.CartonType;
import com.ltm.backend.model.PickDetail;

public interface CartonTypeRecommendationStrategy {
    CartonType getRecommendedCartonType(PickDetail pickDetail, String cartonGroup);
}
