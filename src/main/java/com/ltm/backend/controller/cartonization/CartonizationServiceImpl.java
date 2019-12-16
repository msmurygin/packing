package com.ltm.backend.controller.cartonization;

import com.ltm.backend.db.DBService;
import com.ltm.backend.model.CartonType;
import com.ltm.backend.model.OrderDetail;
import com.ltm.backend.model.PickDetail;
import com.ltm.backend.model.UID;
import com.vaadin.server.VaadinSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartonizationServiceImpl implements CartonizationService {
    public static final String PARCELS_COUNT_ATTRIBUTE_NAME = "PARCELS_COUNT";

    private final DBService dbService;
    private final CartonTypeRecommendationStrategy cartonTypeRecommendationStrategy;

    public CartonizationServiceImpl(DBService dbService, CartonTypeRecommendationStrategy cartonTypeRecommendationStrategy) {
        this.dbService = dbService;
        this.cartonTypeRecommendationStrategy = cartonTypeRecommendationStrategy;
    }

    @Override
    public List<OrderDetail> cartonize(UID uid) {
        String orderKey = uid.getOrderKey();
        String cartonGroup = uid.getCartonGroup();

        List<OrderDetail> cartonizedOrderDetailCache = CartonizationService.getCartonizedOrderDetailsFromSession(orderKey);

        if (cartonizedOrderDetailCache == null) {
            cartonizedOrderDetailCache = cartonize(orderKey, cartonGroup);
            VaadinSession.getCurrent().setAttribute(PARCELS_COUNT_ATTRIBUTE_NAME, cartonizedOrderDetailCache.size());
        }

        for (OrderDetail od : cartonizedOrderDetailCache){
            if (od.getPutawayClass().equalsIgnoreCase(uid.getPutawayClass())){
                uid.setCartonType(od.getCartonType());
                uid.setCartonDescription(od.getCartonDescription());
            }
        }

        return cartonizedOrderDetailCache;
    }

    private List<OrderDetail> cartonize(String orderKey, String cartonGroup) {
        List<PickDetail> pickDetails =  dbService.getPickDetails(orderKey);
        Map<String, PickDetail> groupedByPutawayClassMap = new HashMap<>();

        pickDetails.forEach( currentPickDetail -> {
            String putawayClass = currentPickDetail.getPutawayClass();

            PickDetail groupedByPutawayClassObject = groupedByPutawayClassMap.get(putawayClass);
            if (groupedByPutawayClassObject == null){
                // creating new

                double qty  = currentPickDetail.getQty();
                double cubestd= currentPickDetail.getCubeStd();
                double capacity = qty * cubestd;

                currentPickDetail.setCubicCapacity(capacity);
                // Adding pickdetaiKeyTemp to List
                currentPickDetail.addPickDetailKeyToList(currentPickDetail.getPickDetailKeyTemp());
                currentPickDetail.addCaseIdToList(currentPickDetail.getCaseIdTemp());
                currentPickDetail.setPickDetailKeyTemp("");
                currentPickDetail.setCaseIdTemp("");
                groupedByPutawayClassMap.put(putawayClass, currentPickDetail);
            }else{

                double qty  = groupedByPutawayClassObject.getQty();
                double cube = groupedByPutawayClassObject.getCubicCapacity();
                groupedByPutawayClassObject.getPickDetailKeyList().add(currentPickDetail.getPickDetailKeyTemp());


                if ( !groupedByPutawayClassObject.getCaseIdList().contains(currentPickDetail.getCaseIdTemp())) {
                    groupedByPutawayClassObject.getCaseIdList().add(currentPickDetail.getCaseIdTemp());
                }

                currentPickDetail.setPickDetailKeyTemp("");
                currentPickDetail.setCaseIdTemp("");
                groupedByPutawayClassObject.setQty(qty + currentPickDetail.getQty());
                groupedByPutawayClassObject.setCubicCapacity(cube + (currentPickDetail.getQty() * currentPickDetail.getCubeStd()) );
            }
        });

        // Iterating all grouped by putaway class picks and transform it into
        // OrderDetail with quantity increase
        List<OrderDetail> cartonizedPicksResult = new ArrayList<>();

        groupedByPutawayClassMap.forEach((key,pickDetail) -> {
            OrderDetail odResutl = joinPicks(pickDetail, orderKey, cartonGroup);
            cartonizedPicksResult.add(odResutl);
        });

        CartonizationService.saveCartonizedOrderDetailsToSession(orderKey, cartonizedPicksResult);

        return cartonizedPicksResult;
    }

    private OrderDetail joinPicks(PickDetail pickDetail, String orderKey, String cartonGroup) {

        OrderDetail result = new OrderDetail();

        result.setPutawayClass(pickDetail.getPutawayClass());
        result.setOrderKey(orderKey);
        result.setPacked("0/"+ (int) pickDetail.getQty());
        result.setPickDetailList(pickDetail.getPickDetailKeyList());
        result.setSumOpenQty(pickDetail.getQty());

        CartonType cartonTypeToPackIn = cartonTypeRecommendationStrategy.getRecommendedCartonType(pickDetail, cartonGroup);

        result.setCartonType(cartonTypeToPackIn.getCartonType());
        result.setCartonDescription(cartonTypeToPackIn.getCartonDescription());

        int parcelsQuantity = calculateParcelsQuantity(cartonTypeToPackIn, pickDetail);
        result.setEstimatedParcelsQty(parcelsQuantity);

        return result;
    }

    static int calculateParcelsQuantity(CartonType cartonTypeToPackIn, PickDetail pickDetail) {
        if (pickDetail.getCubicCapacity() > cartonTypeToPackIn.getCube()){
            double totalBoxCube = cartonTypeToPackIn.getCube() == 0 ? 1 :  cartonTypeToPackIn.getCube();
            double totalPickCube = pickDetail.getCubicCapacity();
            return (int) Math.ceil(totalPickCube / totalBoxCube);
        } else {
            return 1;
        }
    }
}
