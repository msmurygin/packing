package com.ltm.ui;

import com.vaadin.event.ShortcutListener;

public class KeyboardActionHandler extends ShortcutListener {
    private Runnable runnable = null;

    public KeyboardActionHandler(Runnable runnable, String caption, int keyCode, int... modifierKeys) {
        super(caption, keyCode, modifierKeys);
        this.runnable = runnable;
    }

    @Override
    public void handleAction(Object sender, Object target) {
        runnable.run();
    }
}
