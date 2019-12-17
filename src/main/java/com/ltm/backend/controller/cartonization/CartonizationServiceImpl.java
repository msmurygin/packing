package com.ltm.backend.controller.cartonization;

import com.ltm.backend.db.DBService;
import com.ltm.backend.model.Carton;
import com.ltm.backend.model.OrderDetail;
import com.ltm.backend.model.PickDetail;
import com.ltm.backend.model.UID;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CartonizationServiceImpl implements CartonizationService {

    private final DBService dbService;
    private final CartonRecommenderFactory cartonRecommenderFactory;
    private final Supplier<Map<String, List<OrderDetail>>> cartonizationMemorySupplier;

    public CartonizationServiceImpl(DBService dbService,
                                    CartonRecommenderFactory cartonRecommenderFactory,
                                    Supplier<Map<String, List<OrderDetail>>> cartonizationMemorySupplier) {
        this.dbService = dbService;
        this.cartonRecommenderFactory = cartonRecommenderFactory;
        this.cartonizationMemorySupplier = cartonizationMemorySupplier;
    }

    @Override
    public List<OrderDetail> cartonize(UID uid) {
        List<OrderDetail> savedOrderDetails = cartonizationMemorySupplier.get()
            .computeIfAbsent(uid.getOrderKey(), orderKey -> cartonize(orderKey, uid.getCartonGroup()));

        for (OrderDetail od : savedOrderDetails) {
            if (od.getPutawayClass().equalsIgnoreCase(uid.getPutawayClass())) {
                uid.setCartonType(od.getCartonType());
                uid.setCartonDescription(od.getCartonDescription());
            }
        }

        return savedOrderDetails;
    }

    private List<OrderDetail> cartonize(String orderKey, String cartonGroup) {
        Map<String, List<PickDetail>> pickDetailsByPutAwayClass =
            dbService.getPickDetails(orderKey).stream()
                .collect(Collectors.groupingBy(PickDetail::getPutawayClass));

        return pickDetailsByPutAwayClass.entrySet().stream()
            .map(entry -> createOrderDetail(orderKey, cartonGroup, entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }

    private OrderDetail createOrderDetail(String orderKey,
                                          String cartonGroup,
                                          String putAwayClass,
                                          List<PickDetail> pickDetails) {
        DimensionsData dim = new DimensionsData();
        List<String> pickDetailKeys = new ArrayList<>();
        double itemsCount = 0;

        for (PickDetail pd : pickDetails) {
            dim.merge(pd.getLength(), pd.getWidth(), pd.getHeight(), pd.getCubeStd(), pd.getQty());
            pickDetailKeys.add(pd.getPickDetailKey());
            itemsCount += pd.getQty();
        }

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderKey(orderKey);
        orderDetail.setPutawayClass(putAwayClass);
        orderDetail.setPickDetailList(pickDetailKeys);
        orderDetail.setSumOpenQty(itemsCount);

        Carton carton = cartonRecommenderFactory.createRecommender()
            .recommendCartonOrDefault(dim, cartonGroup);
        orderDetail.setCartonType(carton.getCartonType());
        orderDetail.setCartonDescription(carton.getCartonDescription());

        int parcelsQuantity = calculateParcelsQuantity(carton.getCube(), dim.getTotalVolume());
        orderDetail.setEstimatedParcelsQty(parcelsQuantity);

        return orderDetail;
    }

    static int calculateParcelsQuantity(double cartonVolume, double itemsVolume) {
        if (itemsVolume > cartonVolume) {
            if (Double.compare(cartonVolume, 0.0) == 0) {
                cartonVolume = 1;
            }
            return (int) Math.ceil(itemsVolume / cartonVolume);
        } else {
            return 1;
        }
    }
}
