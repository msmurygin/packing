package com.ltm.backend.model;

import java.io.Serializable;

public class DropIdCaseId   implements Serializable {
    private String dropId;
    private String caseId;

    public String getDropId() {
        return dropId;
    }

    public void setDropId(String dropId) {
        this.dropId = dropId;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }
}
