package com.ltm;

import com.ltm.backend.controller.LocationBroadcasterService;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

@WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
@VaadinServletConfiguration(ui = MyUI.class, productionMode = true)
public class MyUIServlet extends VaadinServlet {

    public void init(ServletConfig servletConfig)throws ServletException {
        super.init(servletConfig);

        // Initialising broadcasting thread
        LocationBroadcasterService.init(getService());
    }
}