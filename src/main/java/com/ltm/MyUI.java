package com.ltm;

import com.ltm.backend.controller.LocationBroadcastListener;
import com.ltm.backend.controller.LocationBroadcaster;
import com.ltm.backend.controller.PackServiceImpl;
import com.ltm.backend.controller.UIDScanResult;
import com.ltm.backend.exception.UserException;
import com.ltm.backend.model.LocToBroadCastWrapper;
import com.ltm.backend.model.OrderDetail;
import com.ltm.backend.utils.ObjectWrapperList;
import com.ltm.backend.utils.SessionUtils;
import com.ltm.ui.ConfirmRestoreSessionWindow;
import com.ltm.ui.LocationView;
import com.ltm.ui.MainView;
import com.ltm.ui.SessionErrorView;
import com.ltm.ui.SortStationView;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Grid;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.JavaScriptFunction;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import elemental.json.JsonArray;
import org.apache.log4j.Logger;

import java.sql.SQLException;


@Push
@Theme("mytheme")
public class MyUI extends UI implements LocationBroadcastListener {
    private static final Logger LOGGER = Logger.getLogger(MyUI.class);

    // Main root layout
    private HorizontalLayout layout;
    private Grid<OrderDetail> gridRef;
    private Label gridCaptionLabelRef;
    private TextField packageTypeTextFieldRef;
    private TextField uidTextField;
    private GridLayout parcelGridLayoutRef;

    // Location broadcaster, global UI loc assginer
    private LocationView broadcastedLocsView;

    private SessionUtils sessionUtils;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        LOGGER.info("Current session "+ VaadinSession.getCurrent().getSession().getId());

        initSessionUtil();

        // Main UI initialization
        initLayouts(false);

        // Register this current session to broadcast the location assigner
        LocationBroadcaster.register(this);

        // Destroy window implementation
        this.addDetachListener(new DetachListener() {
            public void detach(DetachEvent event) {
                LOGGER.info("######### Detached ##########");
            }
        });

        JavaScript.getCurrent().addFunction("aboutToClose", new JavaScriptFunction() {
            @Override
            public void call(JsonArray element)   {
                LOGGER.info("Window/Tab is Closed.");
                VaadinService.getCurrentRequest().getWrappedSession().invalidate();
                //TODO Call Method to Clean the Resource before window/Tab Close.
                // TODO Need to add confirmation popup window
                PackServiceImpl.getInstance().removeAll();
            }
        });

        JavaScript js = Page.getCurrent().getJavaScript();
        js.execute("window.onbeforeunload = function (e) { var e = e || window.event; aboutToClose(); return; };");
        js.execute("window.onunload = function (e) {var e = e || window.event; aboutToClose(); return; };");
        js.execute("window.closePackModule = function (e) { aboutToClose(); return; };");
    }

    // Must also unregister when the UI expires
    @Override
    public void detach() {
        LocationBroadcaster.unregister(this);
        super.detach();
    }

    @Override
    public void receiveBroadcast(final LocToBroadCastWrapper locToBroadCastWrapper) {
        // Must lock the session to execute logic safely
        access(() -> {
            MainView mainView = (MainView) layout;
            try {
                if (broadcastedLocsView == null) {
                    broadcastedLocsView = new LocationView(locToBroadCastWrapper);
                } else {
                    broadcastedLocsView.setUp(locToBroadCastWrapper);
                }
                mainView.addBroadcastedView(broadcastedLocsView);
                mainView.enableInput();
            } catch (Exception e) {
                LOGGER.error(e);
            }
        });
    }

    @Override
    public void removeLocationFromUI() {
        access(() -> {
            try {
                UI.getCurrent().setData(null);
                ((MainView) layout).removeBroadcastedView();
            } catch (Exception e) {
                LOGGER.error(e);
            }
        });
    }

    /**
     * Getting main UI or Sort table input layout
     */
    private HorizontalLayout getLayout(boolean restoreSessionDataSkipped) {
        try {
            if (getCurrentSessionUtils().validateSession()) {

                // YM-179, 04/07/2019, Maxim Smurygin, Let's check saved session START
                ObjectWrapperList<?> userSessionData = getCurrentSessionUtils().checkSessionData();   ///// -==> 09/07/2019, Maxim Smurygin ROLL-BACK, THIS METHOD IS ALWAYS RETURN NULL  /////
                if (userSessionData != null && !restoreSessionDataSkipped) {
                    ConfirmRestoreSessionWindow restoreSessionWindow = new ConfirmRestoreSessionWindow("Сообщение системы", "Сессия разорвана и будет восстановлена.");
                    MyUI.getCurrent().addWindow(restoreSessionWindow);
                } else
                    // YM-179, 04/07/2019, Maxim Smurygin, Let's check saved session END
                // Cookies ok
                // let's check table number
                if (getCurrentSessionUtils().isUserSortTableSetup())
                    layout =  new MainView();
                else {
                    MyUI.getCurrent().addWindow(new SortStationView());
                }
            }
        } catch (Exception e) {
            LOGGER.error(e);
            layout = new SessionErrorView();
        }
        return layout;
    }

    /**
     * Initialising all UI components
     */
    public void initLayouts(boolean restoreSessionDataSkipped) {
        HorizontalLayout layout = getLayout(restoreSessionDataSkipped);
        setContent(layout);
    }

    /**
     * Packing logic
     */
    public void doPack(UIDScanResult userScannedUID, String userConfirmedPackType) throws UserException, SQLException {
        ((MainView)this.layout).doPack(userScannedUID, userConfirmedPackType);
    }

    public void showPopup(String caption, String description, Notification.Type type) {
        Notification.show(caption, description, type);
    }

    /**
     * Refresh button logic
     */
    public void refreshBroadcastedLocationButtonAction() {
        MyUI myUI = (MyUI) UI.getCurrent();
        Object uiData = myUI.getData();
        if (uiData instanceof LocToBroadCastWrapper) {
            LocToBroadCastWrapper locsWrapper = (LocToBroadCastWrapper) uiData;
            LocationBroadcaster.unregister(locsWrapper.getOrderKey());
            myUI.removeLocationFromUI();
        }
    }

    public  SessionUtils getCurrentSessionUtils() {
        return this.sessionUtils;
    }

    private void initSessionUtil() {
        sessionUtils = new SessionUtils();
    }

    public void setGridRef(Grid<OrderDetail> gridRef) {
        this.gridRef = gridRef;
    }

    public Grid<OrderDetail> getGridRef() {
        return this.gridRef;
    }

    public void setGridCaptionLabelRef(Label gridCaptionLabel) {
        this.gridCaptionLabelRef = gridCaptionLabel;
    }

    public Label getGridCaptionLabelRef() {
        return this.gridCaptionLabelRef;
    }

    public void setPackageTypeTextFieldRef(TextField packageTypeTextFieldRef) {
        this.packageTypeTextFieldRef = packageTypeTextFieldRef;
    }

    public  TextField getPackageTypeTextFieldRef() {
        return this.packageTypeTextFieldRef;
    }

    public  void setUidTextField(TextField uidTextField) {
        this.uidTextField = uidTextField;
    }

    public TextField getUidTextField() {
        return this.uidTextField;
    }

    public void setParcelGridLayoutRef(GridLayout parcelGridLayoutRef) {
        this.parcelGridLayoutRef = parcelGridLayoutRef;
    }

    public GridLayout getParcelGridLayoutRef() {
        return this.parcelGridLayoutRef;
    }
}
