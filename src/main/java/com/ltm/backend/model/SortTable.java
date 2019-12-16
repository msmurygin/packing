package com.ltm.backend.model;

import java.io.Serializable;

public class SortTable implements Serializable {

    private String sortTableKey;
    private String defaulLabelPrinter;
    private String defaultReportPrinter;
    private String areaKey;
    private String putawayZone;
    private boolean locationHasBeenAssigned = false;

    public String getSortTableKey() {
        return sortTableKey;
    }

    public void setSortTableKey(String sortTableKey) {
        this.sortTableKey = sortTableKey;
    }

    public String getDefaulLabelPrinter() {
        return defaulLabelPrinter;
    }

    public void setDefaulLabelPrinter(String defaulLabelPrinter) {
        this.defaulLabelPrinter = defaulLabelPrinter;
    }

    public String getDefaultReportPrinter() {
        return defaultReportPrinter;
    }

    public String getPutawayZone() {
        return putawayZone;
    }

    public void setPutawayZone(String putawayZone) {
        this.putawayZone = putawayZone;
    }

    public String getAreaKey() {
        return areaKey;
    }

    public void setAreaKey(String areaKey) {
        this.areaKey = areaKey;
    }

    public void setDefaultReportPrinter(String defaultReportPrinter) {
        this.defaultReportPrinter = defaultReportPrinter;
    }

    public boolean isLocationHasBeenAssigned() {
        return locationHasBeenAssigned;
    }

    public void setLocationHasBeenAssigned(boolean locationHasBeenAssigned) {
        this.locationHasBeenAssigned = locationHasBeenAssigned;
    }

    public SortTable(String sortTableKey, String defaulLabelPrinter, String defaultReportPrinter, String areaKey, String putawayZone) {
        this.sortTableKey = sortTableKey;
        this.defaulLabelPrinter = defaulLabelPrinter;
        this.defaultReportPrinter = defaultReportPrinter;
        this.areaKey = areaKey;
        this.putawayZone = putawayZone;
    }
}
