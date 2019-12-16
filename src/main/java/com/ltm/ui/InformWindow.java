package com.ltm.ui;

public class InformWindow extends ConfirmWindow {

    public InformWindow(String msg, String title) {
        super(title, msg, null, null, false);
    }

    @Override
    public void confirmEvent(){
        close();
    }

    public void action(boolean close) {
        if (close) {
            close();
        } else {
           confirmEvent();
        }
    }
}
