package com.ltm.ui;

import com.ltm.MyUI;
import com.ltm.backend.controller.PackService;
import com.ltm.backend.controller.PackServiceImpl;
import com.ltm.backend.controller.ParcelService;
import com.ltm.backend.controller.ParcelSharedService;
import com.ltm.backend.controller.UIDScanResult;
import com.ltm.backend.controller.cartonization.CartonRecommenderFactory;
import com.ltm.backend.controller.cartonization.CartonizationService;
import com.ltm.backend.controller.cartonization.CartonizationServiceImpl;
import com.ltm.backend.db.DBService;
import com.ltm.backend.exception.CartonTypeException;
import com.ltm.backend.exception.ScanUIDException;
import com.ltm.backend.exception.UserException;
import com.ltm.backend.model.OrderDetail;
import com.ltm.backend.model.UID;
import com.ltm.backend.utils.SerializeSessionUtils;
import com.ltm.backend.utils.SessionUtils;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.ShortcutAction;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Sizeable;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.List;


public class MainView extends HorizontalLayout {
    private static final Logger LOGGER = Logger.getLogger(MainView.class);

    private static final String UID_ID = "UID";
    private static final String PACKAGE_TYPE_ID = "PACKAGE_TYPE";

    private static final String MULTI_PACKAGING_SUPPORTED_MESSAGE = " (многопосылочный)";
    private static final String MULTI_PACKAGING_NOT_SUPPORTED_MESSAGE = " (НЕ многопосылочный)";

    private final PackService packService = PackServiceImpl.getInstance();
    private final ParcelService parcelService = ParcelService.getInstance();
    private final CartonizationService cartonizationService = new CartonizationServiceImpl(
        DBService.getInstance(),
        new CartonRecommenderFactory(DBService.getInstance()),
        () -> ((MyUI) UI.getCurrent()).getCurrentSessionUtils().getCartonizationMemory()
    );

    // 1 - УИТ инпут
    private Label uidLabel = new Label("УИТ");
    private TextField uidTextField = new TextField();

    // 2 - тип посылки
    private TextField packageTypeTextField = new TextField();
    private Label packageTypeLabel = new Label();

    // 3  - таблица
    private Label gridCaptionLabel = new Label();
    private Grid<OrderDetail> grid = new Grid<>();

    // 4 GridLayout
    private GridLayout parcelGridLayout;
    private String selectedComponentId;

    // Parcel type input view
    private VerticalLayout gridLayout;
    private HorizontalLayout changeTableLayout;

    private LocationView locationView;


    public MainView() {

        final MyUI myUI = (MyUI) UI.getCurrent();

        Button changeTable = new Button();
        changeTable.setIconAlternateText("Сменить рабочий стол");
        changeTable.setStyleName(ValoTheme.BUTTON_BORDERLESS);
        changeTable.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        changeTable.addStyleName(ValoTheme.BUTTON_TINY);
        changeTable.setIcon(VaadinIcons.ADD_DOCK);
        changeTable.addClickListener(clickEvent -> UI.getCurrent().addWindow(new SortStationView()));

        Label currentSortTable = new Label("Стол упаковки: " + getSessionUtils().getUserTable().getSortTableKey());

        changeTableLayout = new HorizontalLayout();
        changeTableLayout.setWidth(100, Unit.PERCENTAGE);
        changeTableLayout.setMargin(false);
        changeTableLayout.setSpacing(true);

        HorizontalLayout changeTableGroup = new HorizontalLayout();
        changeTableGroup.addComponents(changeTable, currentSortTable);
        changeTableGroup.setMargin(false);
        changeTableGroup.setSpacing(false);
        changeTableLayout.addComponents(changeTableGroup);
        changeTableLayout.setExpandRatio(changeTableGroup, 0.7F);

        // Unique product identifier input view
        VerticalLayout uidInpuLayout = new VerticalLayout();
        uidTextField.setPlaceholder("Сосканируйте УИТ");
        uidTextField.setId(UID_ID);
        uidTextField.addFocusListener(this::selectionEvent);
        uidTextField.selectAll();
        uidTextField.focus();
        myUI.setUidTextField(uidTextField);
        uidInpuLayout.addComponents(uidLabel, uidTextField);

        // Parcel type input input view
        packageTypeTextField.setPlaceholder("Тип Посылки");
        packageTypeTextField.setId(PACKAGE_TYPE_ID);
        packageTypeTextField.addFocusListener(this::selectionEvent);
        uidInpuLayout.addComponents(packageTypeLabel, packageTypeTextField);

        // Grid
        gridLayout = new VerticalLayout();
        grid.setVisible(false);
        grid.setWidth(100, Unit.PERCENTAGE);
        grid.setHeight(180, Unit.PIXELS);
        grid.addColumn(OrderDetail::getPutawayClass).setCaption("Класс SKU");
        grid.addColumn(OrderDetail::getSumOpenQty).setCaption("Всего штук");
        grid.addColumn(OrderDetail::getCartonType).setCaption("Тип посылки");
        grid.addColumn(OrderDetail::getEstimatedParcelsQty).setCaption("Всего посылок");
        grid.addColumn(od -> (int) od.getPackedQty() + "/" + (int) od.getSumOpenQty()).setCaption("Упаковка");

        myUI.setGridRef(this.grid);
        myUI.setGridCaptionLabelRef(gridCaptionLabel);
        myUI.setPackageTypeTextFieldRef(this.packageTypeTextField);
        gridLayout.addComponents(gridCaptionLabel, grid);

        // Top panel
        HorizontalLayout topPanel = new HorizontalLayout();
        topPanel.setWidth(100, Unit.PERCENTAGE);
        topPanel.addComponents(uidInpuLayout, gridLayout);
        topPanel.setExpandRatio(uidInpuLayout, 0.1F);
        topPanel.setExpandRatio(uidInpuLayout, 0.4F);
        topPanel.setExpandRatio(gridLayout, 0.5F);

        VerticalLayout topPanelWithButtonsNavigationLayout = new VerticalLayout();
        topPanelWithButtonsNavigationLayout.setMargin(false);
        topPanelWithButtonsNavigationLayout.setSpacing(false);
        topPanelWithButtonsNavigationLayout.addComponents(changeTableLayout, topPanel);
        topPanelWithButtonsNavigationLayout.setWidth(100, Unit.PERCENTAGE);
        topPanelWithButtonsNavigationLayout.setExpandRatio(changeTableLayout, 0.01F);
        topPanelWithButtonsNavigationLayout.setExpandRatio(topPanel, 0.99F);

        // Parcel GridLayout system
        parcelGridLayout = new GridLayout();
        parcelGridLayout.removeAllComponents();
        parcelGridLayout.setColumns(5); //TODO: Fix the grid size
        parcelGridLayout.setRows(1);
        parcelGridLayout.setSpacing(true);
        parcelGridLayout.setMargin(new MarginInfo(true, false, false, false));
        parcelGridLayout.setWidth(100, Sizeable.Unit.PERCENTAGE);
        myUI.setParcelGridLayoutRef(parcelGridLayout);

        // Splitter itself
        VerticalSplitPanel rootSplitPanel = new VerticalSplitPanel();
        rootSplitPanel.setSizeFull();
        rootSplitPanel.setSplitPosition(260, Unit.PIXELS);
        rootSplitPanel.setMinSplitPosition(260, Unit.PIXELS);
        rootSplitPanel.setFirstComponent(topPanelWithButtonsNavigationLayout);
        rootSplitPanel.setSecondComponent(parcelGridLayout);

        setSizeFull();
        setMargin(new MarginInfo(false, true));
        addComponent(rootSplitPanel);
        setExpandRatio(rootSplitPanel, 0.85F);

        // adding listeners and exceptions handling
        addInputListeners(topPanel);

        uidTextField.setEnabled(false);
        packageTypeTextField.setEnabled(false);
    }

    private void addInputListeners(HorizontalLayout topPanel) {

        // Unique Product Identifier TextField Action
        topPanel.addShortcutListener(
            new KeyboardActionHandler("Enter", ShortcutAction.KeyCode.ENTER, this::executeAction));

        // Do the same for tabs
        topPanel.addShortcutListener(
            new KeyboardActionHandler("TAB", ShortcutAction.KeyCode.TAB, this::executeAction));
    }

    /**
     * Setting current selected component ref
     */
    private void selectionEvent(FieldEvents.FocusEvent selectionEvent) {
        this.selectedComponentId = ((TextField) selectionEvent.getSource()).getId();

        // if we click on first component, need to clear
        if (this.selectedComponentId.equalsIgnoreCase(UID_ID)) {
            clearAllInputFields();
        }
    }

    private void executeAction() {
        if (this.selectedComponentId != null) {
            try {
                if (this.selectedComponentId.equalsIgnoreCase(UID_ID)) {
                    onUIDPressEnter();
                } else if (this.selectedComponentId.equalsIgnoreCase(PACKAGE_TYPE_ID)) {
                    onParcelTypePressEnter();
                }
            } catch (UserException | SQLException e) {

                e.printStackTrace();

                if (!(e instanceof CartonTypeException)) {
                    if (this.selectedComponentId.equalsIgnoreCase(UID_ID)) {
                        this.uidTextField.setValue("");
                        this.uidTextField.selectAll();
                        this.uidTextField.focus();
                    } else {
                        this.packageTypeTextField.setValue("");
                        this.packageTypeTextField.selectAll();
                        this.packageTypeTextField.focus();
                    }
                    Notification.show(e.getMessage(), Notification.Type.ERROR_MESSAGE);
                } else {
                    CartonTypeException ex = ((CartonTypeException) e);
                    String msg = ex.getMessage();
                    LOGGER.error(msg);
                    // TODO: Confirmation window need to show
                    final UIDScanResult scannedUID = (UIDScanResult) uidTextField.getData();
                    ConfirmWindow conf =
                        new ConfirmWindow("Подтвердите ввод", msg, scannedUID, ex.getUserValueProvided(), true);
                    UI.getCurrent().addWindow(conf);
                }

                // Remove scanned parcel from memory
                if (e instanceof UserException) {
                    if (((UserException) e).needToRollBackParcel()) {
                        final UIDScanResult scannedUID = (UIDScanResult) uidTextField.getData();
                        parcelService.removeParcel(scannedUID);
                    }
                }
            }
        }
    }

    private void onUIDPressEnter() throws UserException, SQLException {

        String enteredUid = uidTextField.getValue();

        LOGGER.debug("Unique Product Identifier TextField onEnter event: " + enteredUid);
        if (StringUtils.isBlank(enteredUid)) {
            // if value is null or empty, do nothing here
            // TODO: highlight with the colour!
            LOGGER.debug("UPI is empty!");
            throw new ScanUIDException("Необходимо просканировать УИТ");
        }

        UIDScanResult scannedUID = packService.doUIDScan(enteredUid);

        if (packService.isOpenParcelExist(scannedUID)) {
            throw new UserException("Просканирован УИТ из другого заказа. Верните товар в ячейку");
        }

        // TODO: Carton Group должен быть рассчетным - аналог картонизации
        // TODO: Необходимо подбирать CartonType вместо CartonGroup

        UID uid = scannedUID.getUid();
        List<OrderDetail> cartonizedOrderDetailList = cartonizationService.cartonize(uid);

        // THIS IS VERY IMPORTANT, SCANNED UID SAVED IN IN THE TEXT FIELD, WE WILL USE IT LATER ON
        uidTextField.setData(scannedUID);

        grid.setVisible(true);
        grid.setItems(cartonizedOrderDetailList);
        gridCaptionLabel.setValue(createCaptionMessage(uid));
        packageTypeLabel.setValue("Тип Посылки: " + uid.getCartonType());

        // Saving UID textfield reference
        VaadinSession vaadinSession = VaadinSession.getCurrent();
        vaadinSession.setAttribute(SessionUtils.UID_TEXTFIELD_REF, scannedUID);
        vaadinSession.setAttribute(SessionUtils.GRID_CAPTION_LABEL, this.gridCaptionLabel.getValue());
        vaadinSession.setAttribute(SessionUtils.PACKAGE_TYPE_LABEL_REF, this.packageTypeLabel.getValue());

        if (scannedUID.isFirstScan()) {
            this.packageTypeTextField.setValue("");
            this.packageTypeTextField.setEnabled(true);
            this.packageTypeTextField.selectAll();
            this.packageTypeTextField.focus();
        } else {
            this.packageTypeTextField.setValue(uid.getCartonDescription());
            this.packageTypeTextField.setEnabled(false);

            doPack(scannedUID, null);
        }
    }

    private static String createCaptionMessage(UID uid) {
        String multiPackagingSupportMessage = uid.isMultiPackagingBanned()
            ? MULTI_PACKAGING_NOT_SUPPORTED_MESSAGE
            : MULTI_PACKAGING_SUPPORTED_MESSAGE;

        return String.format("Заказ: %s Перевозчик: %s %s",
            uid.getOrderKey(),
            uid.getCarrierName(),
            multiPackagingSupportMessage);
    }

    /**
     * Call this method when Parcel Type input
     * this method should not throw the exception when
     * user scan the same parcel type in the current order and same parcel type
     */
    private void onParcelTypePressEnter() throws UserException, SQLException {

        String enteredPackageType = packageTypeTextField.getValue();
        LOGGER.debug("Value input " + enteredPackageType);

        final UIDScanResult scannedUID = (UIDScanResult) uidTextField.getData();

        if (StringUtils.isBlank(enteredPackageType)) {
            String enteredUid = uidTextField.getValue();
            if (enteredUid != null && enteredUid.isEmpty()) {
                packageTypeTextField.selectAll();
                packageTypeTextField.focus();
                return;
            } else {
                throw new UserException("Укажите тип посылки.");
            }
        }

        // Saving UID textfield reference
        VaadinSession vaadinSession = VaadinSession.getCurrent();
        vaadinSession.setAttribute(SessionUtils.UID_TEXTFIELD_REF, scannedUID);
        vaadinSession.setAttribute(SessionUtils.GRID_CAPTION_LABEL, this.gridCaptionLabel.getValue());
        vaadinSession.setAttribute(SessionUtils.PACKAGE_TYPE_LABEL_REF, this.packageTypeLabel.getValue());

        doPack(scannedUID, enteredPackageType);

        // Loc order
        ParcelSharedService.getInstance().addOrderLock(scannedUID.getUid().getOrderKey());
    }

    /**
     * Packing goes here
     */
    public void doPack(UIDScanResult userScannedUID, String userConfirmedPackType) throws UserException, SQLException {

        // Add to memory
        parcelService.addParcel(userScannedUID);

        // Do transaction
        packService.doPackGeneral(userScannedUID, userConfirmedPackType);

        //YM-179, Maxim Smurygin, 05/07/2019 Start
        SerializeSessionUtils.saveSessionObjects();
        //YM-179, Maxim Smurygin, 05/07/2019 END

        // Draw UI
        UID uid = userScannedUID.getUid();
        parcelService.drawParcel2(parcelGridLayout, uid);

        refreshUI();

        final List<OrderDetail> cartonizedOrderDetailFromMemory =
            getSessionUtils().getCartonizationMemory().get(uid.getOrderKey());

        if (cartonizedOrderDetailFromMemory.stream().allMatch(OrderDetail::isClosed)) {

            LOGGER.debug("Everything done for this orderkey = " + uid.getOrderKey());
            LOGGER.debug("Removing all from ui");
            packService.removeAll();

            String userId = getSessionUtils().getUserId();
            LOGGER.debug("Removing session data for user " + userId);

            DBService db = DBService.getInstance();
            db.deleteSessionObject(userId);  ///// -==> 09/07/2019, Maxim Smurygin ROLL-BACK, THIS METHOD IS EMPTY INSIDE - COMMENTED  /////
            // YM-179, 05/07/2019, Maxim Smurygin, END

            // Updating sortationDetail dropid to ''
            try {
                cartonizedOrderDetailFromMemory.forEach(orderDetail ->
                    orderDetail.getCartonizedUIDS().forEach(cartonizedUID ->
                        db.updateSortationDetail(cartonizedUID.getUid().getId())));
            } catch (Exception e) {
                e.printStackTrace();
            }

            grid.setVisible(false);
            gridCaptionLabel.setValue("");
            parcelGridLayout.removeAllComponents();
            packageTypeTextField.setEnabled(true);

            refreshUI();
        }
    }

    /**
     * Get Order details from service memory
     */
    private List<OrderDetail> getOrderDetailFromMemory() {
        // refresh UI grid
        UIDScanResult scanResult = (UIDScanResult) uidTextField.getData();
        return getSessionUtils().getCartonizationMemory().get(scanResult.getUid().getOrderKey());
    }


    /**
     * Updating ui table data
     */
    private void refreshUI() {

        // Refresh data
        this.uidTextField.setValue("");
        this.uidTextField.focus();
        this.uidTextField.selectAll();

        // refreshUI grid
        if (this.getOrderDetailFromMemory() != null) {
            grid.setItems(this.getOrderDetailFromMemory());
        }
    }

    private void clearAllInputFields() {
        packageTypeTextField.setValue("");
        packageTypeLabel.setValue("");
        gridCaptionLabel.setValue("");
        uidTextField.setValue("");
        uidTextField.selectAll();
        uidTextField.focus();
    }

    public void addBroadcastedView(LocationView broadcastedLocsView) {
        this.addComponent(broadcastedLocsView);
        locationView = broadcastedLocsView;
        this.setExpandRatio(broadcastedLocsView, 0.15F);
    }

    public void removeBroadcastedView() {
        if (locationView != null) {
            this.removeComponent(locationView);
        }
        locationView = null;
        disableInput();
    }

    private void disableInput() {
        uidTextField.setEnabled(false);
        packageTypeTextField.setEnabled(false);
    }

    public void enableInput() {
        uidTextField.setEnabled(true);
        packageTypeTextField.setEnabled(true);
        this.uidTextField.setValue("");
        this.uidTextField.focus();
        this.uidTextField.selectAll();
    }

    private static SessionUtils getSessionUtils() {
        return ((MyUI) UI.getCurrent()).getCurrentSessionUtils();
    }
}
