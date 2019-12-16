package com.ltm.backend.model;

import com.ltm.backend.db.DBService;

public class ConfigBean {


    private String serverName;
    private String resourceName;
    private String apiUser;
    private String apiPass;


    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }


    public ConfigBean(String serverName, String resourceName) {
        this.serverName = serverName;
        this.resourceName = resourceName;
    }

    public ConfigBean() {
    }

    public String getApiUser() {
        return apiUser;
    }

    public void setApiUser(String apiUser) {
        this.apiUser = apiUser;
    }

    public String getApiPass() {
        return apiPass;
    }

    public void setApiPass(String apiPass) {
        this.apiPass = apiPass;
    }


}
