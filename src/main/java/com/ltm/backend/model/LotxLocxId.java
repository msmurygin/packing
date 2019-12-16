package com.ltm.backend.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class LotxLocxId  implements Serializable {

    private String lot;

    private String loc;

    private String id;

    private String storerkey;

    private String sku;

    private BigDecimal qty;

    private BigDecimal qtyallocated;

    private BigDecimal qtypicked;

    private BigDecimal qtyexpected;

    private BigDecimal qtypickinprocess;

    private BigDecimal pendingmovein;

    private BigDecimal archiveQty;

    private String status;

    private Timestamp editdate;

    private String editwho;

    public String getLot() {
        return lot;
    }

    public void setLot(String lot) {
        this.lot = lot;
    }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStorerkey() {
        return storerkey;
    }

    public void setStorerkey(String storerkey) {
        this.storerkey = storerkey;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public BigDecimal getQty() {
        return qty;
    }

    public void setQty(BigDecimal qty) {
        this.qty = qty;
    }

    public BigDecimal getQtyallocated() {
        return qtyallocated;
    }

    public void setQtyallocated(BigDecimal qtyallocated) {
        this.qtyallocated = qtyallocated;
    }

    public BigDecimal getQtypicked() {
        return qtypicked;
    }

    public void setQtypicked(BigDecimal qtypicked) {
        this.qtypicked = qtypicked;
    }

    public BigDecimal getQtyexpected() {
        return qtyexpected;
    }

    public void setQtyexpected(BigDecimal qtyexpected) {
        this.qtyexpected = qtyexpected;
    }

    public BigDecimal getQtypickinprocess() {
        return qtypickinprocess;
    }

    public void setQtypickinprocess(BigDecimal qtypickinprocess) {
        this.qtypickinprocess = qtypickinprocess;
    }

    public BigDecimal getPendingmovein() {
        return pendingmovein;
    }

    public void setPendingmovein(BigDecimal pendingmovein) {
        this.pendingmovein = pendingmovein;
    }

    public BigDecimal getArchiveQty() {
        return archiveQty;
    }

    public void setArchiveQty(BigDecimal archiveQty) {
        this.archiveQty = archiveQty;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getEditdate() {
        return editdate;
    }

    public void setEditdate(Timestamp editdate) {
        this.editdate = editdate;
    }

    public String getEditwho() {
        return editwho;
    }

    public void setEditwho(String editwho) {
        this.editwho = editwho;
    }

    @Override
    public String toString() {
        return "LotxLocxId{" +
                "lot='" + lot + '\'' +
                ", loc='" + loc + '\'' +
                ", id='" + id + '\'' +
                ", storerkey='" + storerkey + '\'' +
                ", sku='" + sku + '\'' +
                ", qty=" + qty +
                ", qtypicked=" + qtypicked +
                '}';
    }
}
