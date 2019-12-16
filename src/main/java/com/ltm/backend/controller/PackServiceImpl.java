package com.ltm.backend.controller;

import com.ltm.MyUI;
import com.ltm.backend.db.DBService;
import com.ltm.backend.exception.CartonTypeException;
import com.ltm.backend.exception.ScanUIDException;
import com.ltm.backend.exception.UserException;
import com.ltm.backend.model.LocToBroadCastWrapper;
import com.ltm.backend.model.LocsToBroadcast;
import com.ltm.backend.model.OrderDetail;
import com.ltm.backend.model.Parcel;
import com.ltm.backend.model.UID;
import com.ltm.backend.utils.PrintThread;
import com.ltm.backend.utils.SessionUtils;
import com.ltm.ui.ParcelLayout;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

import static com.ltm.backend.controller.cartonization.CartonizationService.getCartonizedOrderDetailsFromSession;
import static org.apache.commons.lang3.StringUtils.isNotBlank;


public class PackServiceImpl implements PackService {

    private static final Logger LOGGER = Logger.getLogger(PackServiceImpl.class);
    private static PackService instance = null;

    public static PackService getInstance() {
        if (instance == null) {
            instance = new PackServiceImpl();
        }
        return instance;
    }

    public PackServiceImpl() {

    }


    /**
     * Сотрудник сканирует в поле C6 номер УИТа из ячейки сортировки.
     */
    @Override
    public UID uiIdScan(String theScannedUID) {
        return DBService.getInstance().getUIDBySN(theScannedUID);
    }

    @Override
    public boolean validateUID(UID uid) {
        // Если просканированный УИТ принадлежит заказу, который ещё не полностью отсортирован, выдаётся сообщение
        return DBService.getInstance().getUnSortedPickdetailCount(uid.getOrderKey()) == 0;
    }

    @Override
    public boolean isThisIsTheFirstScanValidation(UID uid) {
        return getSessionUtils().isThisIsTheFirstScanValidation(uid);
    }

    @Override
    public boolean validateScannedCaseType(String pCase1, String pCase2) {
        return pCase1.equalsIgnoreCase(pCase2);
    }

    @Override
    public boolean isOpenParcelExist(UIDScanResult theScanResult) {
        return getCartonizedOrderDetailsFromSession(theScanResult.getUid().getOrderKey()) == null
            && getOrderDetailSize() > 0;
    }

    /**
     * Closing parcel
     */
    @Override
    public void closeParcel(Parcel parcel) throws UserException {

        parcel.checkCanBeClosed();

        updateDB(parcel);

        // St closed just for ui draw parcel purpose
        parcel.setClosed();

        // Если послыка закрыта, ее необходимо переименовать, для того, чтобы можно было открыть новую посылку
        // c таким же именем, добавим постфикс с кодом dropid
        makeParcelMapKeyClosed(parcel);

        // TODO: Need to send number of place and total estimated parcel count
        // number parcel  parcel.getPlace()

        // Unlock Order
        ParcelSharedService.getInstance().removeOrderOnClose(parcel.getOrderKey());

        String nsqlconfig = DBService.getInstance().getNsqlConfigValue("LT_printlistofitems");
        PrintThread thread = new PrintThread(parcel.getDropId(), 1, 1, (MyUI) UI.getCurrent(), VaadinSession.getCurrent(), nsqlconfig);

        Thread printThread = new Thread(thread);
        printThread.setName("[Thread-" + parcel.getDropId() + "]");
        printThread.start();

        // removing close flag from session
        VaadinSession.getCurrent().setAttribute(parcel.getDropId() + "_CONFIRMED", null);
    }

    public void makeParcelMapKeyClosed(Parcel currentParcel) {
        String currentParcelKey = currentParcel.getParcelId();
        String newParcelKey = currentParcelKey + "_" + currentParcel.getDropId();
        ParcelService parcelService = ParcelService.getInstance();

        currentParcel.setParcelId(newParcelKey);
        parcelService.removeParcel(currentParcelKey);
        parcelService.getParcelMap().put(newParcelKey, currentParcel);

        ParcelLayout tempParcelLayout = parcelService.getParcelUILayout().get(currentParcelKey);
        if (tempParcelLayout != null) {
            parcelService.removeParcelLayout(currentParcelKey);
            parcelService.getParcelUILayout().put(newParcelKey, tempParcelLayout);
        }
    }

    @Override
    public void updateDB(Parcel parcel) throws UserException {
        DBService.getInstance().updateInventory(parcel);
    }

    @Override
    public int getOrderDetailSize() {
        return getSessionUtils().getCartonizedOrdersSize();
    }

    /**
     * Main logic goes here
     */
    public UIDScanResult doUIDScan(String scanned) throws ScanUIDException {

        if (StringUtils.isBlank(scanned)) {
            LOGGER.error("Ошибка сканирования uid, scanned val = " + scanned);
            throw new ScanUIDException("Ошибка сканирования УИТ");
        }

        // 1 - SCAN
        final UID uid = uiIdScan(scanned);

        if (uid == null) {
            LOGGER.error("Ошибка сканирования uid, uid не найден в БД ");
            throw new ScanUIDException("УИТ не найден. Отнесите УИТ старшему смены.");
        }

        // 2 - VALIDATE
        if (!this.validateUID(uid)) {
            LOGGER.error("Ошибка сканирования uid, заказ по данному УИТ еще не полностью осортирован " + uid);
            throw new ScanUIDException("УИТ " + uid.getSerialNumber() + " принадлежит заказу " + uid.getOrderKey()
                + ", по которому ещё не все строки отсортированы. Верните товар в ячейку сортировки");
        }

        // 3 - Validate busy order
        if (ParcelSharedService.getInstance().isOrderExistForEnotherUser(uid.getOrderKey())) {
            LOGGER.error("Ошибка сканирования uid, заказ по данному УИТ  закреплен за другим пользователем");
            throw new ScanUIDException("Ошибка сканирования uid, заказ по данному УИТ  закреплен за другим пользователем");
        }

        /*
        Если данный УИТ принадлежит заказу, по которому ещё не было операций упаковки,
        система делает активным блок интерфейса G2-J13 и L2-R13
        Если это не первое сканирование, то производим дальнейшею операцию из этого куса кода
        */
        boolean isFirstScan = this.isThisIsTheFirstScanValidation(uid);

        //  Пользователь сканирует УИТ, система проверяет находится этот УИТ в предлагаемой ячейке. Если УИТ числится по системе в другой ячейке, то выводится сообщение - "Вы сканируете УИТ не из ячейки %" Положите УИТ в ячейку %. Пользователь физически перекладывает УИТ в указанную ячейку. По системе никаких перемещений не выполняется.
        Object uiData = UI.getCurrent().getData();
        if (uiData instanceof LocToBroadCastWrapper) {
            List<String> sortLocations = ((LocToBroadCastWrapper) uiData).getSortLocationList().stream()
                .map(LocsToBroadcast::getSortLocation)
                .collect(Collectors.toList());
            if (!sortLocations.contains(uid.getSortLocation())) {
                LOGGER.error("Ошибка сканирования uid, UID is not belongs to selected locations!");
                String locsLabel = sortLocations.size() > 1 ? " ячеек " : " ячейки ";
                String locs = String.join(", ", sortLocations);
                throw new ScanUIDException("Вы сканируете УИТ не из" + locsLabel + locs +
                    ", положите УИТ в ячейку " + uid.getSortLocation());
            }
        }

        // Check if it was packed yet
        List<OrderDetail> orderDetails = getCartonizedOrderDetailsFromSession(uid.getOrderKey());
        if (orderDetails != null) {

            // Get not closed order detail with same putawayclass and orderkey
            OrderDetail notClosedOrderDetail = orderDetails.stream()
                .filter(item -> item.getPutawayClass().equalsIgnoreCase(uid.getPutawayClass())
                    && item.getOrderKey().equalsIgnoreCase(uid.getOrderKey()))
                .findAny()
                .orElse(null);

            if (notClosedOrderDetail != null && notClosedOrderDetail.getCartonizedUIDS() != null) {
                if (notClosedOrderDetail.isClosed()
                    && notClosedOrderDetail.getSumOpenQty() != notClosedOrderDetail.getPackedQty()) {
                    // Mark OrderLine Still opened
                    notClosedOrderDetail.setClosed(false);
                    return new UIDScanResult(uid, true, true);
                }

                boolean hasPackedUid = notClosedOrderDetail.getCartonizedUIDS().stream()
                    .anyMatch(uidItem -> uidItem.getUid().getSerialNumber().equalsIgnoreCase(uid.getSerialNumber()));
                if (hasPackedUid) {
                    throw new ScanUIDException("УИТ " + uid.getSerialNumber() + "\n уже упакован в ящик:  "
                        + ParcelService.getInstance().getParcelByUID(uid).getDropId());
                }
            }
        }

        return new UIDScanResult(uid, isFirstScan);
    }

    public void doPackGeneral(Object theScannedUIDObj, String userInputCartonType) throws UserException {
        UIDScanResult scannedUID = (UIDScanResult) theScannedUIDObj;
        UID uid = scannedUID.getUid();
        String systemCartonType = uid.getCartonType();

        //Validation
        if (scannedUID.isFirstScan() && !scannedUID.isCartonConfirmed()) {
            if (!isCartonTypeExists(userInputCartonType)) {
                throw new UserException("Подтвержденный тип упаковки не существует.", true);
            }
            if (isNotBlank(systemCartonType) && isNotBlank(userInputCartonType)) {
                if (!validateScannedCaseType(systemCartonType, userInputCartonType)) {
                    throw new CartonTypeException("Подтвержденный тип упаковки не соответствует расчетному.",
                        userInputCartonType, systemCartonType, true);
                }
            } else {
                throw new UserException("Подтвержденный тип упаковки не соответствует расчетному.", true);
            }
        }

        // Adding uid object to OrderDetail Item
        for (OrderDetail item : getCartonizedOrderDetailsFromSession(uid.getOrderKey())) {
            if ((item.getCartonType().equalsIgnoreCase(uid.getCartonType())
                    && item.getPutawayClass().equalsIgnoreCase(uid.getPutawayClass()))
                || uid.isMultiPackagingBanned()) {

                item.setSelectedCartonType(userInputCartonType);
                validateAndPackUID(item, scannedUID);
            }
        }

        // Do this finaly {
        // After each scan we need to save orderkey + putawayclass to
        // prevent parcel type input more then one time
        getSessionUtils().addToScanned(uid);
    }

    /**
     * This method close parcel and print
     */
    private void validateAndPackUID(OrderDetail orderDetail, UIDScanResult scannedUID) throws UserException {
        UID uid = scannedUID.getUid();

        if (orderDetail.getPackedQty() == orderDetail.getSumOpenQty()) {
            throw new UserException("Данная посылка # " + ParcelService.getInstance().getParcelByUID(uid)
                + " уже упакована. ");
        }

        updateOrderDetailQuantity(orderDetail, uid);
        orderDetail.addUID(uid);

        // Here we need to  close parcel
        if (orderDetail.getPackedQty() == orderDetail.getSumOpenQty()) {

            // Setting end time for the process
            Parcel parcel = ParcelService.getInstance().getParcelByUID(uid);
            orderDetail.setClosed(true);
            parcel.end();

            getSessionUtils().getCloseMethodList().add(false);

            closeParcel(parcel);
        }
    }

    /**
     * Updating OrderDetail quantity
     */
    private void updateOrderDetailQuantity(OrderDetail odItem, UID uid) {
        int packedQty = (int) odItem.getPackedQty();
        String newValue = (++packedQty) + "/" + (int) odItem.getSumOpenQty();
        // increasing table quantity
        odItem.setPacked(newValue);
        //Increase packed qty
        odItem.setPackedQty(packedQty);
    }

    /***
     * ОЧищаем пямять
     */
    @Override
    public void removeAll() {
        MyUI myUI = (MyUI) UI.getCurrent();

        if (myUI != null) {
            try {
                myUI.getCurrentSessionUtils().invalidateSession();
            } catch (Exception e) {
                LOGGER.error(e);
            }
        }
    }

    private boolean isCartonTypeExists(String cartonType) {
        return DBService.getInstance().isCartonTypeExists(cartonType);
    }

    private static SessionUtils getSessionUtils() {
        return ((MyUI) UI.getCurrent()).getCurrentSessionUtils();
    }
}