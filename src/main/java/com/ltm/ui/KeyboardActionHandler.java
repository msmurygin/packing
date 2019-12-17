package com.ltm.ui;

import com.vaadin.event.ShortcutListener;

public class KeyboardActionHandler extends ShortcutListener {
    private final Runnable action;

    public KeyboardActionHandler(String caption, int keyCode, Runnable action) {
        super(caption, keyCode, new int[]{});
        this.action = action;
    }

    @Override
    public void handleAction(Object sender, Object target) {
        action.run();
    }
}
