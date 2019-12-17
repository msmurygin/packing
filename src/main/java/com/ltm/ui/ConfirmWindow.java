package com.ltm.ui;

import com.ltm.MyUI;
import com.ltm.backend.controller.UIDScanResult;
import com.ltm.backend.exception.UserException;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.SQLException;

import static com.vaadin.event.ShortcutAction.KeyCode.ENTER;
import static com.vaadin.event.ShortcutAction.KeyCode.ESCAPE;


public class ConfirmWindow extends Window {
    private final String userValue;
    private UIDScanResult scannedUID;

    public ConfirmWindow(String title, String msg,
                         UIDScanResult scannedUID,
                         String userValue,
                         boolean showNoButton) {
        super(title);
        this.scannedUID = scannedUID;
        this.userValue = userValue;

        // Win settings
        center();
        setClosable(false);
        setModal(true);
        setWidth(350, Unit.PIXELS);
        setHeight(220, Unit.PIXELS);

        Panel root = new Panel();

        TextArea message = new TextArea();
        message.setRows(4);
        message.setWordWrap(true);
        message.setValue(msg);
        message.setReadOnly(true);
        message.setWidth(100, Unit.PERCENTAGE);

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setMargin(new MarginInfo(false,false,true,false));

        Button ok = new Button("OK" , clickEvent -> action(false));
        ok.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        root.addShortcutListener(new KeyboardActionHandler("Enter", ENTER, ok::click));
        buttonLayout.addComponent(ok);

        if (showNoButton) {
            ok.setCaption("Подтвердить");
            Button no = new Button("Отказаться", clickEvent -> action(true));
            no.addStyleName(ValoTheme.BUTTON_DANGER);
            buttonLayout.addComponent(no);
            root.addShortcutListener(new KeyboardActionHandler("Esc", ESCAPE, no::click));
        }

        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(new MarginInfo(true,true,true,true));
        layout.addComponents(message, buttonLayout);
        layout.setExpandRatio(message, 0.7F);
        layout.setExpandRatio(buttonLayout, 0.3F);
        layout.setSizeFull();

        root.setSizeFull();
        root.setContent(layout);
        setContent(root);

        root.focus();
    }

    /**
     * Main packing
     */
    public void confirmEvent() throws UserException , SQLException {
        System.out.println("Confirm event");

        // Yes please confirm! flag is off
        scannedUID.confirmCartonTypeInput();

        ((MyUI)UI.getCurrent()).doPack(scannedUID, userValue);

        close();
    }



    public void action(boolean close) {
        if (close) {
            close();
        } else {
            try {
                confirmEvent();
            } catch (UserException | SQLException e) {
               Notification.show("Ошибка", e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
            }
        }
    }
}
