package com.ltm.backend.db;

import com.ltm.MyUI;
import com.ltm.backend.exception.UserException;
import com.ltm.backend.model.*;
import com.ltm.backend.utils.SessionUtils;

import com.vaadin.ui.UI;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.util.Assert;


import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * Modification history
 * 20/06/2019 - 29444 , Maxim Smurygin, changed currentDate to getUTCDate()
 */
public abstract class InventoryBalancesDAO extends JdbcDaoSupport implements InventoryBalancesManager {

    private SortTable sortTable ;
    private static final Logger LOGGER = Logger.getLogger(InventoryBalancesDAO.class);
    private String userid =  null;
    private Date currentDate = new Date(System.currentTimeMillis());


    /**
     *  Init method
     *  vars check
     * @throws UserException
     */
    public void init() throws UserException{

        LOGGER.debug("Initialized...");
        try {
            // init
            this.userid     = ((MyUI) UI.getCurrent()).getCurrentSessionUtils().getUserId();
            this.sortTable  = ((MyUI)UI.getCurrent()).getCurrentSessionUtils().getUserTable();
            // init this in method

            //---------------------------------------------------------------------------
            Assert.notNull(this.sortTable, "Не выбран стол сортировки.");
            Assert.notNull(this.userid, "Ошибка сессии, urserid не найден.");
            Assert.notNull(this.currentDate, "Runtime исключение, currentDate");
            //---------------------------------------------------------------------------
        }catch (IllegalArgumentException e){
            LOGGER.error(e.getMessage());
            throw  new UserException(e.getMessage());
        }
    }




    public void updatePickdetail(Parcel currentParcel) throws UserException {
        LOGGER.debug("-==Updating PICKDETAIL table BEGIN==-");
        LOGGER.debug("      --- Current parcel == "+currentParcel.toString());

        String systemCartonType = currentParcel.getOrderDetail().getCartonType();
        String userCartonType = currentParcel.getOrderDetail().getSelectedCartonType();

        final  List<String> pickDetailList = new ArrayList<>();
        List<UID> cartonizedUID = currentParcel.getUidList();
        cartonizedUID.stream().forEach(item ->{
            pickDetailList.add(item.getPickDetailKey());
        });

        //final  List<String> pickDetailList = currentParcel.getOrderDetail().getPickDetailList();

        getJdbcTemplate().batchUpdate("UPDATE PICKDETAIL SET status = '6', id =  ? , dropid = ?  , TRACKINGID = ? , LOC = ? , PDUDF1 = ?, SELECTEDCARTONTYPE = ? WHERE PICKDETAILKEY = ?",  pickDetailList, pickDetailList.size(), (preparedStatement, pickdetailKey) -> {

                    LOGGER.debug("      --- Updating pickdetail record with dropid = " + currentParcel.getDropId() +" and pickdetailKey =  "+ pickdetailKey);
                    preparedStatement.setString(1, currentParcel.getDropId());
                    preparedStatement.setString(2, currentParcel.getDropId());
                    preparedStatement.setString(3, currentParcel.getDropId());
                    preparedStatement.setString(4, sortTable.getSortTableKey());
                    preparedStatement.setString(5, systemCartonType);
                    preparedStatement.setString(6, userCartonType);
                    preparedStatement.setString(7, pickdetailKey);
                });


        LOGGER.debug("-==Updating PICKDETAIL table END==-");
    }




    @Override
    public void updateLotxLocxId(Parcel currentParcel) throws UserException {

        LOGGER.debug("-==Updating LOTXLOCXID table BEGIN==-");
        LOGGER.debug("      --- Current parcel == "+currentParcel.toString());
        // BUG : https://support.lt-m.ru/attachments/download/21324/screenshot_1_1554725056.png
        //List<LotxLocxId> units = getLotxLocxId(currentParcel);
      //  units.stream().forEach(uid -> {
        currentParcel.getUidList().stream().forEach(uid -> {

            LOGGER.debug(uid);
            LOGGER.debug("      |--- Checking existing destination record count. ");
            // Check existing  destination lli
            int recCount = getJdbcTemplate().queryForObject("select count(1) from lotxlocxid WHERE LOT = ? AND LOC = ? AND ID = ?   ",
                            new Object[]{uid.getLot(), sortTable.getSortTableKey(), currentParcel.getDropId()}, Integer.class);    /// Целевой ID
            LOGGER.debug("      |___ recCount = " + recCount);


            if (recCount == 0 ) {

                // Insert into locxlocxid у новос строки

                getJdbcTemplate().update("INSERT INTO LOTXLOCXID( LOT, LOC, ID, STORERKEY, SKU," +
                                " QTY, QTYALLOCATED, QTYPICKED, QTYEXPECTED, QTYPICKINPROCESS, PENDINGMOVEIN" +
                                ", STATUS, TAREWGT, NETWGT, GROSSWGT, ADDDATE, ADDWHO, EDITDATE, EDITWHO)\n" +
                                "VALUES (?, ?, ?, ?, ?, 1 , 0, 1, 0, 0, 0, 'OK', 0, 0, 0,  getUtcDate(), ?, getUtcDate(), ?)",
                uid.getLot(), sortTable.getSortTableKey(), currentParcel.getDropId(), uid.getStorerKey(), uid.getSku()/*,currentDate - 29444*/,  userid/*, currentDate - 29444*/, userid);


                LOGGER.debug("      |--- Updating inplace ");
                //getJdbcTemplate().update("UPDATE lotxlocxid SET id =  ? , loc = ?, editdate =  ?, editwho = ?   WHERE lot = ? and loc = ? and id = ? ",
                //        currentParcel.getDropId(), sortTable.getSortTableKey(), currentDate, userid,  uid.getLot(), uid.getLoc(), uid.getId() /* исходный ID */ );

            }else{
                LOGGER.debug("      |--- Updating by increasingf qty in destination records and descrementing in source record");
                getJdbcTemplate().update("UPDATE lotxlocxid SET QTY = QTY + ?, QtyPicked = QtyPicked + ?,  editdate =  getUtcDate() , editwho = ?   WHERE lot = ? and loc = ? and id = ? ",
                        uid.getQty(), uid.getQty(), /*currentDate, 29444 */ userid,  uid.getLot(), sortTable.getSortTableKey(), currentParcel.getDropId());



                // Delete zero qty records !))
                //getJdbcTemplate().update("DELETE FROM lotxlocxid where QTY = 0 AND lot = ? and loc = ? and id = ? ",  uid.getLot(), uid.getLoc(), uid.getId() );
            }


            getJdbcTemplate().update("UPDATE lotxlocxid SET QTY = QTY - ?, QtyPicked = QtyPicked - ?,  editdate =  getUtcDate() , editwho = ?   WHERE lot = ? and loc = ? and id = ? ",
                    uid.getQty(),
                    uid.getQty(),
                    //currentDate,29444
                    userid,
                    uid.getLot(),
                    uid.getLoc(),
                    uid.getId());


         });

        LOGGER.debug("-==Updating LOTXLOCXID table END==-");


    }


    @Override
    public void updateSkuxLoc(Parcel currentParcel)throws UserException{

        LOGGER.debug("-==Updating SKUXLOC table BEGIN==-");
        LOGGER.debug("      --- Current parcel == "+currentParcel.toString());

        //List<LotxLocxId> units = getSkuXLoc(currentParcel);
        //units.stream().forEach(uid -> {
        currentParcel.getUidList().stream().forEach(uid -> {
                LOGGER.debug("      |--- For current UID = " + uid);
                // Source llit

                // Check existing  destination lli
                int recCount = getJdbcTemplate().queryForObject("select count(1) from SKUXLOC WHERE LOC = ? AND SKU = ? AND STORERKEY = ?    ",
                        new Object[]{
                                sortTable.getSortTableKey(),  // целевая ячейка
                                uid.getSku(),
                                uid.getStorerKey()},
                        Integer.class
                );

                if (recCount == 0 ) {
                    getJdbcTemplate().update("INSERT INTO SKUXLOC (STORERKEY, SKU, LOC,  QTY, QTYPICKED, LOCATIONTYPE, ADDDATE, ADDWHO, EDITDATE, EDITWHO)"+
                    " VALUES (?, ?, ?, 1, 1,'PACK', getUtcDate(), ?, getUtcDate(), ?)",
                            uid.getStorerKey(),
                            uid.getSku(),
                            sortTable.getSortTableKey(),
                            //currentDate,29444
                            userid,
                           // currentDate,29444
                            userid);

//                    getJdbcTemplate().update("UPDATE SKUXLOC SET loc = ?, editdate =  ?, editwho = ?   WHERE loc = ? and sku = ?  AND STORERKEY = ? ",
//                            sortTable.getSortTableKey(),
//                            currentDate, userid,
//                            uid.getLoc(),
//                            uid.getSku(),
//                            uid.getStorerKey()
//                    );

                }else{
                    getJdbcTemplate().update("UPDATE SKUXLOC SET QTY = QTY + ?, QtyPicked = QtyPicked + ?,  editdate =  getUtcDate() , editwho = ?   WHERE  loc = ? and sku = ?  AND STORERKEY = ?",

                            uid.getQty(),
                            uid.getQty(),
                            //currentDate,29444
                            userid,
                            sortTable.getSortTableKey(),
                            uid.getSku(),
                            uid.getStorerKey()
                    );



                    // Delete zero qty records !))
//                    getJdbcTemplate().update("DELETE FROM SKUXLOC where QTY = 0 AND loc = ? and sku = ?  AND STORERKEY = ?",
//                            uid.getLoc(),
//                            uid.getSku(),
//                            uid.getStorerKey()
//                    );
                }
                    getJdbcTemplate().update("UPDATE SKUXLOC SET QTY = QTY - ?, QtyPicked = QtyPicked - ?,  editdate =  getUtcDate() , editwho = ?   WHERE loc = ? and sku = ?  AND STORERKEY = ?",
                            uid.getQty(),
                            uid.getQty(),
                            //currentDate,29444
                            userid,
                            uid.getLoc(),
                            uid.getSku(),
                            uid.getStorerKey()
                    );
            });
        LOGGER.debug("-==Updating SKUXLOC table END==-");
    }

    @Override
    public void updatePickDetailCaseId(Parcel currentParcel){
        LOGGER.debug("-==Updating PICKDETAIL CASEID table BEGIN==-");
        LOGGER.debug("      --- Current parcel "+currentParcel.toString());

        String prevDropId = "";
        String newCaseId = "";
        Iterator<UID> it  =currentParcel.getUidList().iterator();
        while (it.hasNext()){
            UID uid = it.next();

            String pdk =  uid.getPickDetailKey();
            String currDropid = getJdbcTemplate().queryForObject("select DROPID from PICKDETAIL where PICKDETAILKEY = ?    ",
                    new Object[]{ pdk},
                    String.class
            );

            if (!currDropid.equalsIgnoreCase(prevDropId)){
                newCaseId =  DBService.getInstance().getNextKey("CARTONID", "%010d", null  );
                currentParcel.getOrderDetail().setCaseId(newCaseId);
                insertIntoDropidDetail(currentParcel);
            }
            prevDropId = currDropid;



            getJdbcTemplate().update("UPDATE PICKDETAIL SET CASEID = ? where PICKDETAILKEY = ? ", newCaseId,  uid.getPickDetailKey());

        }




    }
    //@Override
    public void updatePickDetailCaseId1(Parcel currentParcel) {

        LOGGER.debug("-==Updating PICKDETAIL CASEID table BEGIN==-");
        LOGGER.debug("      --- Current parcel "+currentParcel.toString());



        String prevDropId = "";
        String newCaseId = "";

        Iterator<CartonizedUID> it  = currentParcel.getOrderDetail().getCartonizedUIDS().iterator();
        while (it.hasNext()){
            CartonizedUID uid = it.next();

            String currDropid = getJdbcTemplate().queryForObject("select DROPID from PICKDETAIL where PICKDETAILKEY =?    ",
                    new Object[]{ uid.getUid().getPickDetailKey()},
                    String.class
            );

            if (!currDropid.equalsIgnoreCase(prevDropId)){
                newCaseId =  DBService.getInstance().getNextKey("CARTONID", "%010d", null  );
                // saving caseid
            }

            currentParcel.getOrderDetail().setCaseId(newCaseId);
            insertIntoDropidDetail(currentParcel);

            LOGGER.debug("::::Updating PICKDETAIL SET CASEID = "+ newCaseId+" WHERE PICKDETAILKEY = "+ uid.getUid().getPickDetailKey()+"::::::::");
            getJdbcTemplate().update("UPDATE PICKDETAIL SET CASEID = ? where PICKDETAILKEY = ? ", newCaseId,  uid.getUid().getPickDetailKey());
            prevDropId = currDropid;



        }
        LOGGER.debug("-==Updating PICKDETAIL table END==-");
    }


    @Override
    public void insertIntoDropId(Parcel currentParcel) throws UserException{
        LOGGER.debug("-==CREATING DROPID BEGIN==-");
        LOGGER.debug("      --- Parcel found = " + currentParcel.toString());

            getJdbcTemplate().update("INSERT INTO DROPID (WHSEID, DROPID, DROPLOC, DROPIDTYPE, ADDDATE, ADDWHO, EDITDATE, EDITWHO, STATUS)" +
                            "VALUES('wmwhse1', ?, ?, 1,  getUtcDate(), ?,  getUtcDate(), ?, '0' )",
                    currentParcel.getDropId(),
                    sortTable.getSortTableKey(),
                    /*currentDate, 29444*/
                    userid,
                    /*currentDate, 29444*/
                    userid
            );
        LOGGER.debug("-==CREATING DROPID END==-");
    }

    /**
     * This method is called from updatePickDetailCaseId
     * @param currentParcel
     */
    @Override
    public void insertIntoDropidDetail(Parcel currentParcel){

        LOGGER.debug("-==CREATING DROPIDDETAIL BEGIN==-");
        getJdbcTemplate().update(" INSERT INTO DROPIDDETAIL (WHSEID, DROPID, CHILDID, ADDDATE, ADDWHO, EDITDATE, EDITWHO, IDTYPE) VALUES('wmwhse1', ?, ?,  getUtcDate(), ?,  getUtcDate(), ?, 1)",
                currentParcel.getDropId(),
                currentParcel.getOrderDetail().getCaseId(),
               /* currentDate, 29444*/
                userid,
                /*currentDate, 29444*/
                userid
        );

        LOGGER.debug("-==CREATING DROPIDDETAIL END==-");
    }


    @Override
    public void insertIntoTaskDetail(Parcel currentParcel)  throws UserException{

        LOGGER.debug("-==CREATING TASKDETAIL BEGIN==-");


        String taskType = "PKG";
        double uom= 6.0;
        String statusTsk = "9";
        String userPosition = "1";
        int seqNo = 99999;
        String priority = "5";

        String storerKey = "";
        String sku = "";
        String lot = "";
        String loc = sortTable.getSortTableKey();
        String orderkey = currentParcel.getOrderKey();
        double qty = 0;
        String taskDetailKey = DBService.getInstance().getNextKey("TASKDETAILKEY", "%010d", null  );

        getJdbcTemplate().update( " INSERT INTO TASKDETAIL (TASKDETAILKEY,TASKTYPE,STORERKEY,SKU,LOT,UOM,UOMQTY,QTY,TOLOC,LOGICALTOLOC," +
                        " TOID,CASEID,STATUS,PRIORITY,USERPOSITION,USERKEY,STARTTIME,ENDTIME,SEQNO,ADDDATE,ADDWHO,EDITDATE,EDITWHO, ORDERKEY ) " +
                        " VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,  getUtcDate(), ?,  getUtcDate(), ? , ? )",
                taskDetailKey,
                taskType,
                storerKey,
                sku,
                lot,
                uom,
                qty,
                qty,
                loc,
                loc,
                currentParcel.getDropId(),
                "",
                statusTsk,
                priority,
                userPosition,
                userid,
                currentParcel.getStartTime(),
                currentParcel.getEndTime(),
                seqNo,
               /* currentDate, 29444*/
                userid,
               /* currentDate, 29444*/
                userid,
                orderkey
        );


    }



    @Override
    public void updateSerialInventory(Parcel currentParcel) throws UserException {


        LOGGER.debug("-==UPDATE SERIALINVENTORY BEGIN==-");
            // for all Uids in parcel
        currentParcel.getUidList().stream().forEach(UIDItem ->{
                 getJdbcTemplate().update("UPDATE SERIALINVENTORY SET ID = ? , LOC = ? ,EDITDATE =  getUtcDate() , EDITWHO = ? WHERE SERIALNUMBER = ?",
                         currentParcel.getDropId(),
                         sortTable.getSortTableKey(), /*currentDate,*/ userid,  UIDItem.getSerialNumber());

        });
        LOGGER.debug("-==UPDATE SERIALINVENTORY END==-");

    }

    @Override
    public void insertITRN(Parcel currentParcel) throws UserException{
        LOGGER.debug("-==INSERT ITRN BEGIN==-");

            // for all Uids in parcel
        currentParcel.getUidList().stream().forEach(UIDItem ->{

                String itrnkey  = null;
                itrnkey = DBService.getInstance().getNextKey("ITRNKEY", "%010d", null  );
                UIDItem.setItrnKey(itrnkey); // Saving itrnKey for itrn serial Insert in separate method


                String storerKey = UIDItem.getStorerKey();
                String sku = UIDItem.getSku();
                String lot = UIDItem.getLot();
                String loc = UIDItem.getLoc();
                String fromId = UIDItem.getId();
                double qty = 1.0D;
                String toloc = sortTable.getSortTableKey();
                String toid = currentParcel.getDropId();
                String orderKey = currentParcel.getOrderKey();
                //Date currentDate = new Date(System.currentTimeMillis()); 29444

                getJdbcTemplate().update("INSERT INTO ITRN ( Itrnkey, TranType, StorerKey, Sku, Lot, FromLoc, FromID,   Qty, SourceType, EffectiveDate, AddDate, AddWho, EditDate, EditWho, toloc, toid,  sourcekey) VALUES ( ?, 'MV', ?, ?, ?,  ?, ?, ?, ?, getUtcDate(), getUtcDate(), ?, getUtcDate() ,?, ?, ?, ?)", itrnkey, storerKey, sku, lot, loc, fromId, qty, "PKG",  userid, userid, toloc, toid, orderKey); // Removed currentdate, added getUtcDate() 29444

            });

        LOGGER.debug("-==INSERT ITRN END==-");
    }


    @Override
    public void insertItrnSerial(Parcel currentParcel) throws UserException{
        LOGGER.debug("-==INSERT ITRNSERIAL BEGIN==-");
        currentParcel.getUidList().stream().forEach(UIDItem ->{


                String ItrnSerialkey = null;
                ItrnSerialkey = DBService.getInstance().getNextKey("ITRNSERIALKEY", "%010d", null  );
                String itrnkey  =  UIDItem.getItrnKey();
                String storerKey = UIDItem.getStorerKey();
                String sku = UIDItem.getSku();
                String lot = UIDItem.getLot();

                double qty = 1.0D;
                String toloc = sortTable.getSortTableKey();
                String toid = currentParcel.getDropId();
                String serialNumber = UIDItem.getSerialNumber();
                String data = "";
                long SerialNumberLong = Long.parseLong(serialNumber);
                String serialNumberLongString =SerialNumberLong+"";

                //Date currentDate = new Date(System.currentTimeMillis()); 29444

                getJdbcTemplate().update("INSERT INTO ITRNSERIAL (ItrnSerialkey, ItrnKey, StorerKey, Sku, Lot, ID, Loc, SerialNumber, Qty, Data2, Data3, Data4, Data5, SerialNumberLong, trantype, adddate, addwho, editwho) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,  getUtcDate(), ?, ?) ", ItrnSerialkey, itrnkey, storerKey, sku, lot, toid, toloc, serialNumber, qty, data, data, data, data,  SerialNumberLong, "MV", /*currentDate, 29444 */userid, userid);

            });
        LOGGER.debug("-==INSERT ITRNSERIAL END==-");


    }


    @Override
    public void updateLotXIdDetail(Parcel currentParcel ){
        LOGGER.debug("-==UPDATE LOTXIDDETAIL BEGIN==-");



        getJdbcTemplate().batchUpdate("UPDATE LOTXIDDETAIL SET ID = ? ,EDITDATE =  getUtcDate() , EDITWHO = ? WHERE OOTHER1 = ? AND SOURCEKEY = ?",  currentParcel.getUidList(), currentParcel.getUidList().size(), (prepStmt, uid) -> {

            prepStmt.setString(1, currentParcel.getDropId());
            //prepStmt.setDate(  2, new java.sql.Date(currentDate.getTime()) ); 29444
            prepStmt.setString(2, userid);
            prepStmt.setString(3, uid.getSerialNumber());
            prepStmt.setString(4, currentParcel.getOrderKey());
        });


     //   getJdbcTemplate().update("insert into wmwhse1.TABLE_B values('1234', NULL)");


        LOGGER.debug("-==UPDATE LOTXIDDETAIL END==-");
    }



}
