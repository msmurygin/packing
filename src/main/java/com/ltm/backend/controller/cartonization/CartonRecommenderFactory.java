package com.ltm.backend.controller.cartonization;

import com.ltm.backend.db.DBService;

public class CartonRecommenderFactory {
    private static final String YM_STRATEGY_FOR_CARTONIZATION_CONFIG_KEY = "YM_STRATEGY_FOR_CARTONIZATION";
    private static final String TRUE = "1";

    private final DBService db;

    public CartonRecommenderFactory(DBService db) {
        this.db = db;
    }

    public CartonRecommender createRecommender() {
        String usageYMStrategyForCartonRecommendation =
            db.getNsqlConfigValue(YM_STRATEGY_FOR_CARTONIZATION_CONFIG_KEY);

        if (TRUE.equals(usageYMStrategyForCartonRecommendation)) {
            return new CartonRecommenderByDimensionsAndVolume(db);
        } else {
            return new CartonRecommenderByVolume(db);
        }
    }
}
