package com.ltm.backend.utils;

/**
 * Created by maxim on 11.08.17.
 */
public class Proc {

    private String ptcid;
    private String userid;
    private String taskid;
    private String compid;
    private String appflag;
    private String rectype;
    private String server;
    private String miscmsg;
    private String rectotal;

    public String getPtcid() {
        return ptcid;
    }

    public void setPtcid(String ptcid) {
        this.ptcid = ptcid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getTaskid() {
        return taskid;
    }

    public void setTaskid(String taskid) {
        this.taskid = taskid;
    }

    public String getCompid() {
        return compid;
    }

    public void setCompid(String compid) {
        this.compid = compid;
    }

    public String getAppflag() {
        return appflag;
    }

    public void setAppflag(String appflag) {
        this.appflag = appflag;
    }

    public String getRectype() {
        return rectype;
    }

    public void setRectype(String rectype) {
        this.rectype = rectype;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getMiscmsg() {
        return miscmsg;
    }

    public void setMiscmsg(String miscmsg) {
        this.miscmsg = miscmsg;
    }

    public String getRectotal() {
        return rectotal;
    }

    public void setRectotal(String rectotal) {
        this.rectotal = rectotal;
    }
}
