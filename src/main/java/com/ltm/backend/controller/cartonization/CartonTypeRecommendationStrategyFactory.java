package com.ltm.backend.controller.cartonization;

import com.ltm.backend.db.DBService;

public class CartonTypeRecommendationStrategyFactory {
    private static final String YM_STRATEGY_FOR_CARTONIZATION_CONFIG_KEY = "YM_STRATEGY_FOR_CARTONIZATION";
    private static final String TRUE = "1";


    public static CartonTypeRecommendationStrategy createStrategy(DBService dbService) {
        String usageYMStrategyForCartonRecommendation = dbService.getNsqlConfigValue(YM_STRATEGY_FOR_CARTONIZATION_CONFIG_KEY);

        if (TRUE.equals(usageYMStrategyForCartonRecommendation)) {
            return new CartonTypeRecommendationByDimensionsAndVolumeStrategy(dbService);
        } else {
            return new CartonTypeRecommendationByVolumeOnlyStrategy(dbService);
        }
    }
}
