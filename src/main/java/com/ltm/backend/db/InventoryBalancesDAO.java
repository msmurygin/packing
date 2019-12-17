package com.ltm.backend.db;

import com.ltm.MyUI;
import com.ltm.backend.exception.UserException;
import com.ltm.backend.model.Parcel;
import com.ltm.backend.model.SortTable;
import com.ltm.backend.model.UID;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ltm.backend.utils.SessionUtils;
import com.vaadin.ui.UI;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.support.JdbcDaoSupport;


public abstract class InventoryBalancesDAO extends JdbcDaoSupport implements InventoryBalancesManager {

    private static final Logger LOGGER = Logger.getLogger(InventoryBalancesDAO.class);
    private static final int UOM = 6;
    private static final int SEQ = 9999;

    public synchronized void updatePickdetail(Parcel currentParcel, String sortTableKey) throws UserException {
        LOGGER.debug("-==Updating PICKDETAIL table BEGIN==-");
        LOGGER.debug("      --- Current parcel == " + currentParcel.toString());

        String systemCartonType = currentParcel.getOrderDetail().getCartonType();
        String userCartonType = currentParcel.getOrderDetail().getSelectedCartonType();


        final  List<String> pickDetailList = new ArrayList<>();
        List<UID> cartonizedUID = currentParcel.getUidList();
        cartonizedUID.stream().forEach(item -> {
            pickDetailList.add(item.getPickDetailKey());
        });

        getJdbcTemplate().batchUpdate("UPDATE PICKDETAIL SET status = '6', id =  ? , dropid = ?  , TRACKINGID = ?, " +
                                           " LOC = ? , PDUDF1 = ?, SELECTEDCARTONTYPE = ? WHERE PICKDETAILKEY = ?",
            pickDetailList, pickDetailList.size(), (preparedStatement, pickdetailKey) -> {
                    LOGGER.debug("--- Updating pickdetail record with dropid = " + currentParcel.getDropId() +
                                 " and pickdetailKey =  " + pickdetailKey);
                    int index = 1;
                    preparedStatement.setString(index++, currentParcel.getDropId());
                    preparedStatement.setString(index++, currentParcel.getDropId());
                    preparedStatement.setString(index++, currentParcel.getDropId());
                    preparedStatement.setString(index++, sortTableKey);
                    preparedStatement.setString(index++, systemCartonType);
                    preparedStatement.setString(index++, userCartonType);
                    preparedStatement.setString(index++, pickdetailKey);
                });


        LOGGER.debug("-==Updating PICKDETAIL table END==-");
    }




    @Override
    public void updateLotxLocxId(Parcel currentParcel, String sortTableKey, String userId) throws UserException {

        LOGGER.debug("-==Updating LOTXLOCXID table BEGIN==-");
        LOGGER.debug("      --- Current parcel == " + currentParcel.toString());
        currentParcel.getUidList().stream().forEach(uid -> {

            LOGGER.debug(uid);
            LOGGER.debug("      |--- Checking existing destination record count. ");
            // Check existing  destination lli
            int recCount = getJdbcTemplate().queryForObject("select count(1) from lotxlocxid WHERE LOT = ? AND LOC = ? AND ID = ?   ",
                            new Object[]{uid.getLot(), sortTableKey, currentParcel.getDropId()}, Integer.class);    /// Целевой ID
            LOGGER.debug("      |___ recCount = " + recCount);
            if (recCount == 0) {

                // Insert into locxlocxid у новос строки

                getJdbcTemplate().update("INSERT INTO LOTXLOCXID( LOT, LOC, ID, STORERKEY, SKU," +
                                " QTY, QTYALLOCATED, QTYPICKED, QTYEXPECTED, QTYPICKINPROCESS, PENDINGMOVEIN" +
                                ", STATUS, TAREWGT, NETWGT, GROSSWGT, ADDDATE, ADDWHO, EDITDATE, EDITWHO)\n" +
                                "VALUES (?, ?, ?, ?, ?, 1 , 0, 1, 0, 0, 0, 'OK', 0, 0, 0,  getUtcDate(), ?, getUtcDate(), ?)",
                uid.getLot(), sortTableKey, currentParcel.getDropId(), uid.getStorerKey(), uid.getSku(), userId, userId);
                LOGGER.debug("      |--- Updating inplace ");

            } else {
                LOGGER.debug("      |--- Updating by increasingf qty in destination records and descrementing in source record");
                getJdbcTemplate().update("UPDATE lotxlocxid SET QTY = QTY + ?, QtyPicked = QtyPicked + ?,  editdate =  getUtcDate() , editwho = ?   WHERE lot = ? and loc = ? and id = ? ",
                        uid.getQty(), uid.getQty(),  userId,  uid.getLot(), sortTableKey, currentParcel.getDropId());
            }


            getJdbcTemplate().update("UPDATE lotxlocxid SET QTY = QTY - ?, QtyPicked = QtyPicked - ?,  editdate =  getUtcDate() , editwho = ?   WHERE lot = ? and loc = ? and id = ? ",
                    uid.getQty(),
                    uid.getQty(),
                    //currentDate,29444
                    userId,
                    uid.getLot(),
                    uid.getLoc(),
                    uid.getId());


         });

        LOGGER.debug("-==Updating LOTXLOCXID table END==-");


    }


    @Override
    public void updateSkuxLoc(Parcel currentParcel, String sortTableKey, String userId) {
        LOGGER.debug("-==Updating SKUXLOC table BEGIN==-");
        LOGGER.debug("      --- Current parcel == " + currentParcel.toString());
        currentParcel.getUidList().stream().forEach(uid -> {
                LOGGER.debug("      |--- For current UID = " + uid);
                // Source llit
                // Check existing  destination lli
                int recCount = getJdbcTemplate().queryForObject("select count(1) from SKUXLOC WHERE LOC = ? AND SKU = ? AND STORERKEY = ? ",
                        new Object[]{
                                sortTableKey,  // целевая ячейка
                                uid.getSku(),
                                uid.getStorerKey()},
                        Integer.class
                );

                if (recCount == 0) {
                    getJdbcTemplate().update("INSERT INTO SKUXLOC (STORERKEY, SKU, LOC,  QTY, QTYPICKED, LOCATIONTYPE, ADDDATE, ADDWHO, " +
                            "EDITDATE, EDITWHO) VALUES (?, ?, ?, 1, 1,'PACK', getUtcDate(), ?, getUtcDate(), ?)",
                        uid.getStorerKey(),
                        uid.getSku(),
                        sortTableKey,
                        userId,
                        userId);

                } else {
                    getJdbcTemplate().update("UPDATE SKUXLOC SET QTY = QTY + ?, QtyPicked = QtyPicked + ?,  editdate =  getUtcDate() , editwho = ?   WHERE  loc = ? and sku = ?  AND STORERKEY = ?",
                        uid.getQty(),
                        uid.getQty(),
                        //currentDate,29444
                        userId,
                        sortTableKey,
                        uid.getSku(),
                        uid.getStorerKey()
                    );

                }
                getJdbcTemplate().update("UPDATE SKUXLOC SET QTY = QTY - ?, QtyPicked = QtyPicked - ?,  editdate =  getUtcDate() , editwho = ?   WHERE loc = ? and sku = ?  AND STORERKEY = ?",
                        uid.getQty(),
                        uid.getQty(),
                        //currentDate,29444
                        userId,
                        uid.getLoc(),
                        uid.getSku(),
                        uid.getStorerKey()
                );
            });
        LOGGER.debug("-==Updating SKUXLOC table END==-");
    }

    @Override
    public void updatePickDetailCaseId(Parcel currentParcel, String userId) {
        LOGGER.debug("-==Updating PICKDETAIL CASEID table BEGIN==-");
        LOGGER.debug("      --- Current parcel " + currentParcel.toString());

        String prevDropId = "";
        String newCaseId = "";
        Iterator<UID> it  = currentParcel.getUidList().iterator();
        while (it.hasNext()) {
            UID uid = it.next();

            String pdk =  uid.getPickDetailKey();
            String currDropid = getJdbcTemplate()
                .queryForObject("select DROPID from PICKDETAIL where PICKDETAILKEY = ?",
                    new Object[]{pdk},
                    String.class
            );

            if (!currDropid.equalsIgnoreCase(prevDropId)) {
                newCaseId =  DBService.getInstance().getNextKey("CARTONID", "%010d", null);
                currentParcel.getOrderDetail().setCaseId(newCaseId);
                insertIntoDropidDetail(currentParcel, userId);
            }
            prevDropId = currDropid;
            getJdbcTemplate().update("UPDATE PICKDETAIL SET CASEID = ? where PICKDETAILKEY = ? ", newCaseId,  uid.getPickDetailKey());

        }
    }



    @Override
    public void insertIntoDropId(Parcel currentParcel, String sortTableKey, String userId) {
        LOGGER.debug("-==CREATING DROPID BEGIN==-");
        LOGGER.debug("      --- Parcel found = " + currentParcel.toString());

            getJdbcTemplate().update("INSERT INTO DROPID (WHSEID, DROPID, DROPLOC, DROPIDTYPE, ADDDATE, ADDWHO, EDITDATE, EDITWHO, STATUS)" +
                            "VALUES('wmwhse1', ?, ?, 1,  getUtcDate(), ?,  getUtcDate(), ?, '0' )",
                    currentParcel.getDropId(),
                    sortTableKey,
                    userId,
                    userId
            );
        LOGGER.debug("-==CREATING DROPID END==-");
    }

    /**
     * This method is called from updatePickDetailCaseId
     * @param currentParcel
     */
    @Override
    public void insertIntoDropidDetail(Parcel currentParcel, String userId) {
        LOGGER.debug("-==CREATING DROPIDDETAIL BEGIN==-");
        getJdbcTemplate().update(" INSERT INTO DROPIDDETAIL (WHSEID, DROPID, CHILDID, ADDDATE, ADDWHO, EDITDATE, EDITWHO, IDTYPE) VALUES('wmwhse1', ?, ?,  getUtcDate(), ?,  getUtcDate(), ?, 1)",
                currentParcel.getDropId(),
                currentParcel.getOrderDetail().getCaseId(),
                userId,
                userId
        );
        LOGGER.debug("-==CREATING DROPIDDETAIL END==-");
    }


    @Override
    public void insertIntoTaskDetail(Parcel currentParcel, String sortTableKey, String userId) {
        LOGGER.debug("-==CREATING TASKDETAIL BEGIN==-");
        String taskType = "PKG";
        String statusTsk = "9";
        String userPosition = "1";
        String priority = "5";
        String storerKey = "";
        String sku = "";
        String lot = "";
        String loc = sortTableKey;
        String orderkey = currentParcel.getOrderKey();
        double qty = 0;
        String taskDetailKey = DBService.getInstance().getNextKey("TASKDETAILKEY", "%010d", null);
        getJdbcTemplate().update(" INSERT INTO TASKDETAIL (TASKDETAILKEY,TASKTYPE,STORERKEY,SKU,LOT,UOM,UOMQTY,QTY,TOLOC,LOGICALTOLOC, " +
                        " TOID,CASEID,STATUS,PRIORITY,USERPOSITION,USERKEY,STARTTIME,ENDTIME,SEQNO,ADDDATE,ADDWHO,EDITDATE,EDITWHO, ORDERKEY ) " +
                        " VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,  getUtcDate(), ?,  getUtcDate(), ? , ? )",
            taskDetailKey,
                taskType,
                storerKey,
                sku,
                lot,
                UOM,
                qty,
                qty,
                loc,
                loc,
                currentParcel.getDropId(),
                "",
                statusTsk,
                priority,
                userPosition,
                userId,
                currentParcel.getStartTime(),
                currentParcel.getEndTime(),
                SEQ,
                userId,
                userId,
                orderkey
        );


    }



    @Override
    public void updateSerialInventory(Parcel currentParcel, String sortTableKey, String userId) throws UserException {
        LOGGER.debug("-==UPDATE SERIALINVENTORY BEGIN==-");
        currentParcel.getUidList().stream().forEach(UIDItem -> {
            getJdbcTemplate().update("UPDATE SERIALINVENTORY SET ID = ? , LOC = ? ,EDITDATE =  getUtcDate() , " +
                    "EDITWHO = ? WHERE SERIALNUMBER = ?",
                currentParcel.getDropId(),
                sortTableKey,
                userId,
                UIDItem.getSerialNumber()
            );
        });
        LOGGER.debug("-==UPDATE SERIALINVENTORY END==-");

    }

    @Override
    public void insertITRN(Parcel currentParcel, String sortTableKey, String userId) {
        LOGGER.debug("-==INSERT ITRN BEGIN==-");
        currentParcel.getUidList().stream().forEach(UIDItem -> {
            String itrnkey  = null;
            itrnkey = DBService.getInstance().getNextKey("ITRNKEY", "%010d", null);
            UIDItem.setItrnKey(itrnkey); // Saving itrnKey for itrn serial Insert in separate method
            String storerKey = UIDItem.getStorerKey();
            String sku = UIDItem.getSku();
            String lot = UIDItem.getLot();
            String loc = UIDItem.getLoc();
            String fromId = UIDItem.getId();
            double qty = 1.0D;
            String toloc = sortTableKey;
            String toid = currentParcel.getDropId();
            String orderKey = currentParcel.getOrderKey();
            //Date currentDate = new Date(System.currentTimeMillis()); 29444
            getJdbcTemplate().update("INSERT INTO ITRN ( Itrnkey, TranType, StorerKey, Sku, Lot, FromLoc, FromID, " +
                "Qty, SourceType, EffectiveDate, AddDate, AddWho, EditDate, EditWho, toloc, toid,  sourcekey) VALUES " +
                "( ?, 'MV', ?, ?, ?,  ?, ?, ?, ?, getUtcDate(), getUtcDate(), ?, getUtcDate() ,?, ?, ?, ?)",
                itrnkey, storerKey, sku, lot, loc, fromId, qty, "PKG",  userId, userId, toloc, toid, orderKey);
        });

        LOGGER.debug("-==INSERT ITRN END==-");
    }


    @Override
    public void insertItrnSerial(Parcel currentParcel, String sortTableKey, String userId) {
        LOGGER.debug("-==INSERT ITRNSERIAL BEGIN==-");
        currentParcel.getUidList().stream().forEach(UIDItem -> {
            String itrnSerialkey = null;
            itrnSerialkey = DBService.getInstance().getNextKey("ITRNSERIALKEY", "%010d", null);
            String itrnkey  =  UIDItem.getItrnKey();
            String storerKey = UIDItem.getStorerKey();
            String sku = UIDItem.getSku();
            String lot = UIDItem.getLot();

            double qty = 1.0D;
            String toloc = sortTableKey;
            String toid = currentParcel.getDropId();
            String serialNumber = UIDItem.getSerialNumber();
            String data = "";
            long serialNumberLong = Long.parseLong(serialNumber);
            String serialNumberLongString = serialNumberLong + "";
            getJdbcTemplate().update("INSERT INTO ITRNSERIAL (ItrnSerialkey, ItrnKey, StorerKey, Sku, Lot, ID, Loc, " +
                "SerialNumber, Qty, Data2, Data3, Data4, Data5, SerialNumberLong, trantype, adddate, addwho, editwho) " +
                    "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,  getUtcDate(), ?, ?) ",
                itrnSerialkey, itrnkey, storerKey, sku, lot, toid, toloc, serialNumber, qty, data, data, data, data,
                serialNumberLong, "MV", userId, userId);
            });
        LOGGER.debug("-==INSERT ITRNSERIAL END==-");


    }


    @Override
    public void updateLotXIdDetail(Parcel currentParcel, String userId) {
        LOGGER.debug("-==UPDATE LOTXIDDETAIL BEGIN==-");
        getJdbcTemplate().batchUpdate("UPDATE LOTXIDDETAIL SET ID = ? ,EDITDATE =  getUtcDate() , EDITWHO = ? WHERE " +
            "OOTHER1 = ? AND SOURCEKEY = ?",
            currentParcel.getUidList(),
            currentParcel.getUidList().size(),
            (prepStmt, uid) -> {
                int index = 1;
                prepStmt.setString(index++, currentParcel.getDropId());
                prepStmt.setString(index++, userId);
                prepStmt.setString(index++, uid.getSerialNumber());
                prepStmt.setString(index++, currentParcel.getOrderKey());
        });

        LOGGER.debug("-==UPDATE LOTXIDDETAIL END==-");
    }




}
