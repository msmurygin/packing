package com.ltm.backend.db;

import com.ltm.MyUI;
import com.ltm.backend.exception.UserException;
import com.ltm.backend.model.CartonType;
import com.ltm.backend.model.LocToBroadCastWrapper;
import com.ltm.backend.model.LocsToBroadcast;
import com.ltm.backend.model.Parcel;
import com.ltm.backend.model.PickDetail;
import com.ltm.backend.model.SortTable;
import com.ltm.backend.model.UID;
import com.ltm.backend.utils.ObjectWrapperList;
import com.vaadin.ui.UI;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.jdbc.util.BasicFormatterImpl;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.jdbc.support.lob.DefaultLobHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


public class DBService {
    private static final Logger LOGGER = Logger.getLogger(DBService.class);
    private static DBService instance;

    private static final String CARRIER_NAME_COLUMN_LABEL = "CarrierName";
    private static final String SUSR1_COLUMN_LABEL = "susr1";

    private final JdbcTemplate jdbcTemplate;
    private final InventoryBalancesManager inventoryBalancesManager;
    private final KeyGenService keyGenService;

    private static final String RETRIEVE_CARTONS_QUERY =
            "SELECT CARTONIZATIONGROUP, CARTONTYPE, CARTONDESCRIPTION, CUBE, WIDTH, HEIGHT, LENGTH, USESEQUENCE " +
                    "FROM CARTONIZATION " +
                    "WHERE CARTONIZATIONGROUP = ?";

    private static final String RETRIEVE_CARTONS_PRESENTED_IN_WAREHOUSE_QUERY =
            "SELECT CARTONIZATIONGROUP, CARTONTYPE, CARTONDESCRIPTION, CUBE, WIDTH, HEIGHT, LENGTH, USESEQUENCE " +
            "FROM CARTONIZATION " +
            "WHERE CARTONIZATIONGROUP = ? AND DISPLAYRFPACK = '1'";

    private static final String RETRIEVE_NONPACK_CARTON_QUERY =
            "SELECT CARTONIZATIONGROUP, CARTONTYPE, CARTONDESCRIPTION, CUBE, WIDTH, HEIGHT, LENGTH, USESEQUENCE " +
                    "FROM CARTONIZATION " +
                    "WHERE CARTONTYPE = 'NONPACK'";

    private static final String RETRIEVE_PICK_DETAILS_QUERY =
            "select " +
            "pd.QTY, " +
            "sku.PUTAWAYCLASS, " +
            "0 as CUBICCAPACITY, " +
            "SKU.STDCUBE, " +
            "p.WIDTHUOM3 as WIDTH, " +
            "p.HEIGHTUOM3 as HEIGHT, " +
            "p.LENGTHUOM3 as LENGTH, " +
            "pd.PICKDETAILKEY, " +
            "pd.CASEID " +
            "from " +
            "PICKDETAIL pd inner join loc l on l.loc = pd.LOC " +
            "inner join SKU sku on sku.SKU = pd.SKU and sku.STORERKEY = pd.STORERKEY " +
            "inner join pack p on sku.packkey = p.packkey " +
            "where " +
            "ORDERKEY = ? " +
            "and l.LOCATIONTYPE = 'SORT'";

    private static final RowMapper<CartonType> CARTON_TYPE_MAPPER = (rs, rowNum) ->
        new CartonType(
            rs.getString("CARTONIZATIONGROUP"),
            rs.getString("CARTONTYPE"),
            rs.getString("CARTONDESCRIPTION"),
            rs.getDouble("CUBE"),
            rs.getDouble("WIDTH"),
            rs.getDouble("HEIGHT"),
            rs.getDouble("LENGTH"),
            rs.getInt("USESEQUENCE")
        );


    public static DBService getInstance(){
        if (instance == null) {
            instance = new DBService();
        }
        return instance;
    }

    private DBService(){
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("db/Spring-Module.xml");
        jdbcTemplate = context.getBean("jdbcTemplate", JdbcTemplate.class);
        inventoryBalancesManager = context.getBean("InventoryBalancesTranManager", InventoryBalancesManager.class);
        keyGenService = context.getBean("KeyGenSO", KeyGenService.class);
    }


    public UID getUIDBySN(final String theScannedUID){
        LocToBroadCastWrapper locToBroadCastWrapper = (LocToBroadCastWrapper) ((MyUI) UI.getCurrent()).getData();
        List<String> locListString = locToBroadCastWrapper
                .getSortLocationList().stream()
                .map(item -> item.getSortLocation())
                .collect(Collectors.toList());
        //--------------------------------------------------------------------------------------------------------------
       String sql = " select\n" +
               "        o.ORDERKEY,\n" +
               "        ISNULL(s.CARTONGROUP,'STD') as CARTONGROUP,\n" +
               "        sku.PUTAWAYCLASS, \n" +
               "        lld.OOTHER1, llh.LOT, l.loc, llh.ID, sku.SKU, sku.STORERKEY, lld.OQTY, pd.PICKDETAILKEY, sd.SORTLOCATION, o.CarrierName, s.susr1   \n" +
               "    from                                                                         \n" +
               "        LOTXIDDETAIL lld                                                         \n" +
               "    INNER JOIN                                                                   \n" +
               "         LOTXIDHEADER llh on llh.LOTXIDKEY = lld.LOTXIDKEY                       \n" +
               "    inner join SKU sku on sku.SKU = llh.SKU and sku.STORERKEY = llh.STORERKEY    \n" +
               "    inner join PICKDETAIL pd on pd.PICKDETAILKEY = llh.PICKDETAILKEY             \n" +
               "    inner join loc l on l.loc = pd.LOC                                           \n" +
               "    inner join ORDERS o on o.ORDERKEY = pd.ORDERKEY                              \n" +
               "    left outer join  STORER s on s.STORERKEY  = o.CarrierCode and s.[TYPE] = '3' \n" +
               "    inner join SORTATIONSTATIONDETAIL sd on pd.id = sd.DROPID                    \n" +
               "    where                                                                        \n" +
               "        lld.OOTHER1 = ?                                                          \n" +
               "        and lld.IOFLAG = 'O'                                                     \n" +
               "        and o.[TYPE] = '0'                                                       \n" +
               "        and pd.STATUS< '9'                                                       \n" +
               "        and l.LOCATIONTYPE = 'SORT'                                              \n" +
               "        AND l.PutawayZone IN ( SELECT distinct PutawayZone FROM  wmwhse1.AreaDetail WHERE AreaKey = ? ) ";
        //--------------------------------------------------------------------------------------------------------------

        String areaKey = ((MyUI) UI.getCurrent()).getCurrentSessionUtils().getUserTable().getAreaKey();
        UID result = null;
        try {
            result =  this.jdbcTemplate.queryForObject(
                    sql,
                    new Object[]{theScannedUID, areaKey},
                    (rs, rowNum) -> {
                        UID uid = new UID();
                        uid.setOrderKey(rs.getString("ORDERKEY"));
                        uid.setPutawayClass(rs.getString("PUTAWAYCLASS"));
                        uid.setSerialNumber(rs.getString("OOTHER1"));
                        uid.setCartonGroup(rs.getString("CARTONGROUP"));
                        uid.setLot(rs.getString("LOT"));
                        uid.setLoc(rs.getString("LOC"));
                        uid.setId(rs.getString("ID"));
                        uid.setSku(rs.getString("SKU"));
                        uid.setStorerKey(rs.getString("STORERKEY"));
                        uid.setQty(rs.getDouble("OQTY"));
                        uid.setPickDetailKey(rs.getString("PICKDETAILKEY"));
                        uid.setSortLocation(rs.getString("SORTLOCATION"));
                        uid.setCarrierName(rs.getString(CARRIER_NAME_COLUMN_LABEL));
                        uid.setSusr1(rs.getString(SUSR1_COLUMN_LABEL));
                        return uid;
                    });
        }catch (EmptyResultDataAccessException e){
            LOGGER.error("Following query execution failed:");
            LOGGER.error(new BasicFormatterImpl().format(sql));
            LOGGER.error("");
            LOGGER.error("faild for ScannedUID "+theScannedUID+", error "+e.getMessage());
        }
        return result;
    }



    public int getUnSortedPickdetailCount(String orderKey){
        int rowCount  = this.jdbcTemplate.queryForObject("  select isnull(sum(cnt),0) from (\n" +
                "\t\t\t\t\t\t\tselect   count(1) as cnt\n" +
                "        \t\t\t\t\tfrom   ORDERDETAIL\n" +
                "\t\t\t\t\t        where   ORDERKEY = ? \n" +
                "\t\t\t\t\t        group by  ORDERKEY\n" +
                "        \t\t\t\t\thaving   sum(OPENQTY) > sum (QTYPICKED)      \n" +
                "        ) a ", Integer.class, orderKey);

        return rowCount;
    }




    public List<PickDetail> getPickDetails(String pOrderKey){
        List<PickDetail> theResult = null;

        try {
            theResult = this.jdbcTemplate.query(RETRIEVE_PICK_DETAILS_QUERY, new Object[]{pOrderKey}, rs -> {
                        List<PickDetail> theResultSetList = new ArrayList<>();
                        while (rs.next()){
                            PickDetail thePick = new PickDetail(
                                    rs.getDouble("QTY") ,
                                    rs.getString("PUTAWAYCLASS"),
                                    rs.getDouble("CUBICCAPACITY") ,
                                    rs.getDouble("STDCUBE"),
                                    rs.getDouble("WIDTH"),
                                    rs.getDouble("HEIGHT"),
                                    rs.getDouble("LENGTH"),
                                    rs.getString("PICKDETAILKEY"),
                                    rs.getString("CASEID"));

                            theResultSetList.add(thePick);
                        }

                        return theResultSetList;
                    });
        }catch (EmptyResultDataAccessException e){
            LOGGER.error("Following query execution failed:");
            LOGGER.error(new BasicFormatterImpl().format(RETRIEVE_PICK_DETAILS_QUERY));
            LOGGER.error("");
            LOGGER.error("faild for OrderKey  "+pOrderKey+", error "+e.getMessage());
        }


        return theResult;
    }

    public List<CartonType> getCartonTypeList(String cartonGroup) {
        try {
            return jdbcTemplate.query(RETRIEVE_CARTONS_QUERY, new Object[] {cartonGroup}, CARTON_TYPE_MAPPER);
        } catch (DataAccessException e) {
            LOGGER.error("Following query execution failed:");
            LOGGER.error(new BasicFormatterImpl().format(RETRIEVE_CARTONS_QUERY));
            LOGGER.error("");
            LOGGER.error("failed for cartonGroup  "+cartonGroup+", error "+e.getMessage());
            throw e;
        }
    }

    public List<CartonType> getCartonTypesPresentedInWarehouse(String cartonGroup) {
        try {
            return jdbcTemplate.query(RETRIEVE_CARTONS_PRESENTED_IN_WAREHOUSE_QUERY,
                new Object[] {cartonGroup}, CARTON_TYPE_MAPPER);
        } catch (DataAccessException e) {
            LOGGER.error("Following query execution failed:");
            LOGGER.error(new BasicFormatterImpl().format(RETRIEVE_CARTONS_PRESENTED_IN_WAREHOUSE_QUERY));
            LOGGER.error("");
            LOGGER.error("faild for cartonGroup  " + cartonGroup + ", error " + e.getMessage());
            throw e;
        }
    }

    public Optional<CartonType> getNonPackCartonType() {
        try {
            List<CartonType> cartonTypes = jdbcTemplate.query(RETRIEVE_NONPACK_CARTON_QUERY, CARTON_TYPE_MAPPER);
            return cartonTypes.stream().findAny();
        } catch (DataAccessException e){
            LOGGER.error("Following query execution failed:");
            LOGGER.error(new BasicFormatterImpl().format(RETRIEVE_NONPACK_CARTON_QUERY));
            throw e;
        }
    }

    public void updateInventory(Parcel parcel) throws UserException {
        inventoryBalancesManager.updateInventory(parcel);
    }


    public static void main(String[] args){
        //.getInstance().updateInventory();
    }




    public Map<String, List<LocsToBroadcast>> getLocsToBroadcast(){

        String sql = "select distinct ad.AREAKEY  from SORTATIONSTATION s \n" +
                "inner join LOC l on l.loc = s.SORTATIONSTATIONKEY\n" +
                "inner join AREADETAIL ad on ad.PUTAWAYZONE =  l.PUTAWAYZONE\n";

        Map<String, List<LocsToBroadcast>> theResult = null;

        try {
            theResult =  this.jdbcTemplate.query(sql,
                    rs -> {
                        Map<String, List<LocsToBroadcast>> theResultMap = new HashMap<>();

                        while (rs.next()){
                            String key = rs.getString("AREAKEY");
                            List<LocsToBroadcast> broadcastsList = getLocsToBroadcastByAreaKey(key);
                            theResultMap.put(key, broadcastsList);
                        }

                        return theResultMap;
                    });
        }catch (EmptyResultDataAccessException e){
            LOGGER.error("Following query execution failed:");
            LOGGER.error(new BasicFormatterImpl().format(sql));
            LOGGER.error("");
            LOGGER.error("faild for pAreaKey Location to broadcast, error "+e.getMessage());
        }


        return theResult;

    }

    public List<LocsToBroadcast> getLocsToBroadcastByAreaKey(String pAreaKey){



        String sql = "select a.SORTLOCATION, a.id, a.orderkey, a.areakey from (  \n" +
                "\tselect DISTINCT sd.SORTLOCATION, pd.id, pd.orderkey, ad.AREAKEY , isnull(b.cnt1,0) as UnSortedPickdetailCount\n" +
                "\t\tfrom wmwhse1.pickdetail pd \n" +
                "\t\t\tinner join wmwhse1.SORTATIONSTATIONDETAIL sd on pd.id = sd.DROPID\n" +
                "\t\t\tinner join wmwhse1.orderdetail od on pd.orderkey = od.orderkey and pd.orderlinenumber = od.orderlinenumber\n" +
                "\t\t\tleft outer join (\n" +
                "\t\t\t\t\tselect   count(1) as cnt1, ORDERKEY\n" +
                "\t\t\t\t\tfrom   wmwhse1.ORDERDETAIL\n" +
                "\t\t\t\t\tgroup by  ORDERKEY\n" +
                "\t\t\t\t\thaving   sum(OPENQTY) > sum (QTYPICKED)      \n" +
                "\t\t\t)b on b.orderkey = od.orderkey\n" +
                "\t\t\tinner join wmwhse1.LOC l on pd.loc = l.loc\n" +
                "\t\t\tinner join wmwhse1.areadetail ad on l.putawayzone = ad.putawayzone\n" +
                "\t\twhere  od.openqty = od.qtypicked  and pd.STATUS = '5' and isnull( pd.DROPID ,'') = '' \n" +
                "\t\tand sd.LOCATIONSTATUS = '1' and ad.areakey =  ?\n" +
                ")a \n" +
                "where a.UnSortedPickdetailCount = 0\n" +
                "order by a.orderkey  ";


        List<LocsToBroadcast> theResult = null;

        try {
            theResult =  this.jdbcTemplate.query(sql,
                    new Object[]{pAreaKey},
                    rs -> {
                        List<LocsToBroadcast> theResultSetList = new ArrayList<>();
                        while (rs.next()){
                            LocsToBroadcast broadcastedLocs = new LocsToBroadcast(
                                    rs.getString("SORTLOCATION") ,
                                    rs.getString("ID"),
                                    rs.getString("AREAKEY"),
                                    rs.getString("ORDERKEY")
                                    );

                            theResultSetList.add(broadcastedLocs);
                        }

                        return theResultSetList;
                    });
        }catch (EmptyResultDataAccessException e){
            LOGGER.error("Following query execution failed:");
            LOGGER.error(new BasicFormatterImpl().format(sql));
            LOGGER.error("");
            LOGGER.error("faild for pAreaKey Location to broadcast, error "+e.getMessage());
        }


        return theResult;
    }





    public List<SortTable> getSortTableItems() {
        String sql = "select distinct l.LOC , pz.DEFAULTLABELPRINTER, pz.DEFAULTREPORTPRINTER , ad.AREAKEY, l.PUTAWAYZONE \n" +
                "from  LOC l\n" +
                "\tinner join PUTAWAYZONE pz on pz.PUTAWAYZONE = l.PUTAWAYZONE\n" +
                "\tinner join AREADETAIL ad on ad.PUTAWAYZONE =  l.PUTAWAYZONE\n" +
                "WHERE l.LOCATIONTYPE = 'PACK'";

        List<SortTable> theResult = null;

        try {
            theResult =  this.jdbcTemplate.query(sql,

                    rs -> {
                        List<SortTable> theResultSetList = new ArrayList<>();
                        while (rs.next()){
                            SortTable sortTable = new SortTable(
                                    rs.getString("LOC") ,
                                    rs.getString("DEFAULTLABELPRINTER"),
                                    rs.getString("DEFAULTREPORTPRINTER"),
                                    rs.getString("AREAKEY"),
                                    rs.getString("PUTAWAYZONE"));

                            theResultSetList.add(sortTable);
                        }

                        return theResultSetList;
                    });
        }catch (EmptyResultDataAccessException e){
            LOGGER.error("Following query execution failed:");
            LOGGER.error(new BasicFormatterImpl().format(sql));
            LOGGER.error("");
            LOGGER.error("faild for SORTATIONSTATION, error "+e.getMessage());
        }


        return theResult;
    }



    public int getNextKey(String pCounterName) throws UserException{
        return keyGenService.getNextKey(pCounterName);
    }


    public String getNextKey(String pCounterName, String format, String prefix){
        if (format == null) {
            return "";
        }

        int key = 0;
        try {
            key = getNextKey(pCounterName);
        } catch (UserException e) {
            e.printStackTrace();
        }
        String formattedKey = String.format(format , key);
        return StringUtils.isNotBlank(prefix) ? prefix + formattedKey : formattedKey;
    }

    public boolean getTaskCountByLoc(String loc) {

        String sql = "  select count(1)  " +
                "from wmwhse1.pickdetail pd " +
                "inner join SORTATIONSTATIONDETAIL sd on pd.id = sd.DROPID\n" +
                "inner join orderdetail od on pd.orderkey = od.orderkey and pd.orderlinenumber = od.orderlinenumber\n" +
                "inner join LOC l on pd.loc = l.loc\n" +
                "inner join areadetail ad on l.putawayzone = ad.putawayzone\n" +
                "where  od.openqty = od.qtypicked\n" +
                "and   sd.SORTLOCATION = ? ";


        int rowCount  = this.jdbcTemplate.queryForObject(sql, Integer.class, loc);

        return rowCount > 0 ;
    }

    public static String  makeWhereINClause(Iterable<? extends CharSequence>  inString){
        String inClause = "('"+ String.join("','", inString) + "')";
        return inClause;
    }


    public boolean getTaskCountByLocList(List<String> locsList) {


        String sql = "  select count(1)  " +
                "from wmwhse1.pickdetail pd " +
                "inner join SORTATIONSTATIONDETAIL sd on pd.id = sd.DROPID\n" +
                "inner join orderdetail od on pd.orderkey = od.orderkey and pd.orderlinenumber = od.orderlinenumber\n" +
                "inner join LOC l on pd.loc = l.loc\n" +
                "inner join areadetail ad on l.putawayzone = ad.putawayzone\n" +
                "where  od.openqty = od.qtypicked\n" +
                "and   sd.SORTLOCATION in " +makeWhereINClause(locsList) +" and pd.STATUS = '5' and isnull( pd.DROPID ,'') = '' ";



        int rowCount  = this.jdbcTemplate.queryForObject(sql, Integer.class);

        return rowCount > 0 ;
    }




    public String getNsqlConfigValue(String pConfigKey) {
        String sql = "select nsqlvalue from NSQLCONFIG WHERE CONFIGKEY = ? ";

        String theResult = null;

        try {
            theResult =  this.jdbcTemplate.query(sql,
                    new Object[]{pConfigKey},
                    rs -> {
                        String s = null;
                        if (rs.next()){
                           s =  rs.getString("nsqlvalue");
                        }

                        return s;
                    });
        }catch (EmptyResultDataAccessException e){
            LOGGER.error("Following query execution failed:");
            LOGGER.error(new BasicFormatterImpl().format(sql));
            LOGGER.error("");
            LOGGER.error("faild for NSQLCONFIG, error "+e.getMessage());
        }


        return theResult== null ? "5000" : theResult;
    }

    public void updateSortationDetail(String pDropId) {
        this.jdbcTemplate.update("update SORTATIONSTATIONDETAIL set DROPID = '' where DROPID = ? ", pDropId);
    }


    /**
     * YM-178
     * Данный метод сохраняет объекты в базу данных
     *
     *
     * CREATE TABLE wmwhse1.PACKING_SESSIONDATA
     * (
     * 	ID int IDENTITY(1,1) NOT NULL,
     * 	USERID NVARCHAR(50) NOT NULL,
     * 	SESSIONDATA varbinary(MAX),
     * 	ADDDATE datetime DEFAULT getUTCDATE()
     * )
     * CREATE UNIQUE INDEX PACKING_SESSIONDATA_USERID_IDX ON SCPRD.wmwhse1.PACKING_SESSIONDATA (USERID)
     * @param bytes
     */
    public void saveSessionObject(String pUser, byte[] bytes) {


        NamedParameterJdbcTemplate jdbcTemplate1 = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
        MapSqlParameterSource parameters = new MapSqlParameterSource();

        parameters.addValue("USERID", pUser);
        parameters.addValue("SESSIONDATA",
                new SqlLobValue(
                        new ByteArrayInputStream(bytes),
                        bytes.length, new DefaultLobHandler()
                ), Types.BLOB);

        jdbcTemplate1.update("INSERT INTO wmwhse1.PACKING_SESSIONDATA (USERID, SESSIONDATA) VALUES(:USERID, :SESSIONDATA)", parameters);

    }

    public void updateSessionObject(String pUser, byte[] objectAsBytes) {
        NamedParameterJdbcTemplate jdbcTemplate1 = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
        MapSqlParameterSource parameters = new MapSqlParameterSource();

        parameters.addValue("USERID", pUser);
        parameters.addValue("SESSIONDATA",
                new SqlLobValue(
                        new ByteArrayInputStream(objectAsBytes),
                        objectAsBytes.length, new DefaultLobHandler()
                ), Types.BLOB);

        jdbcTemplate1.update("UPDATE wmwhse1.PACKING_SESSIONDATA SET SESSIONDATA =:SESSIONDATA, ADDDATE = getUTCDate() WHERE USERID = :USERID ", parameters);
    }


    public void deleteSessionObject(String pUserId) {
    }



    public ObjectWrapperList<?> getSessionData(String pUser){
        return  null;
    }

    public static Object deserialize(byte[] data) {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = null;
        Object theResult = null;
        try {
            is = new ObjectInputStream(in);
            theResult =  is.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
      return  theResult;

    }

    public boolean isSortLocationActive(List<String> locListString) {
        String sql = " select LOCATIONSTATUS from SORTATIONSTATIONDETAIL where SORTLOCATION IN "+makeWhereINClause(locListString);

        List<String> theResult = null;

        try {
            theResult =  this.jdbcTemplate.query(sql,

                    rs -> {
                        List<String> theResultSetList = new ArrayList<>();
                        while (rs.next()){
                            theResultSetList.add(rs.getString("LOCATIONSTATUS"));
                        }

                        return theResultSetList;
                    });
        }catch (EmptyResultDataAccessException e){
            LOGGER.error("Following query execution failed:");
            LOGGER.error(new BasicFormatterImpl().format(sql));
            LOGGER.error("");
            LOGGER.error("faild for SORTATIONSTATION, error "+e.getMessage());
        }



        return !(theResult != null && theResult.stream().filter( e -> e.equalsIgnoreCase("0")).collect(Collectors.toList()).size() > 0);


    }
    public boolean isSortLocationActive(String loc) {
        String sql = " select count(1) from SORTATIONSTATIONDETAIL where SORTLOCATION = ? and LOCATIONSTATUS='1'";
        int rowCount  = this.jdbcTemplate.queryForObject(sql, Integer.class, loc);
        return rowCount > 0 ;
    }



    public boolean isCartonTypeExists(String cartonType) {
        String sql = "select count(1) from  CARTONIZATION where CARTONTYPE = ?";
        int rowCount  = jdbcTemplate.queryForObject(sql, Integer.class, cartonType);
        return rowCount > 0 ;
    }
}
