package com.ltm.backend.utils;


import com.ltm.backend.db.DBService;
import com.ltm.backend.model.OrderDetail;
import com.ltm.backend.model.Parcel;
import com.ltm.backend.model.SortTable;
import com.ltm.backend.model.UID;
import com.ltm.backend.utils.cookies.CookieUtil;
import com.ltm.ui.ParcelLayout;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import org.apache.log4j.Logger;
import java.util.*;

public class SessionUtils {


    public static final String COOKIE_NAME = "com.epiphany.SessionID";
    public static final String SORT_TABLE = "sort-table";
    public static final String USER_ID = "userid";
    public static final String ENCODING = "UTF-8";

    public static final String CARTONIZATION_MAP = "CARTONIZATION_MEMORY";
    public static final String SCANNED_ORD_MAP = "SCANNED_ORD_MEMORY";

    public static final String PARCEL_MAP = "PARCEL_MEMORY";
    public static final String PARCEL_UI_MAP = "PARCEL_UI_MEMORY";
    public static final String UID_TEXTFIELD_REF = "UID_TEXTFIELD_REF" ;
    public static final String GRID_REF = "GRID_REF" ;
    public static final String GRID_CAPTION_LABEL = "GRID_CAPTION_REF";
    public static final String PACKAGE_TYPE_LABEL_REF = "PACKAGE_TYPE_LABEL_REF";
    public static final String BROADCASTED_LOCATION ="BROADCASTED_LOCATION" ;

    private static final Logger logger = Logger.getLogger(SessionUtils.class);
    private final CookieUtil cookieUtil = new CookieUtil();
    private static SessionUtils instance;



    private  Map<String, List<OrderDetail>> cartonizationResultMap;
    private  Map<String, Boolean> scansByOrdersMap ;
    private  Map<String, Parcel> parcelMap ;
    private  Map<String, ParcelLayout> parcelUIMap ;
    private List<Boolean> closeMethodList ; // YM-179


    public SessionUtils(){
        cartonizationResultMap = new HashMap<>();
        scansByOrdersMap = new HashMap();
        parcelMap = new HashMap<>();
        parcelUIMap = new HashMap<>();
        closeMethodList = new  ArrayList<>(); // YM-179
    }

    private static SessionUtils getCurrent(){
        if (instance == null){
            instance = new SessionUtils();
        }

        return instance;
    }






    /**
     * Session validation
     * @return
     * @throws Exception
     */
    public boolean validateSession() throws Exception{
       return cookieUtil.validateSession();
    }



    /**
     *
     * @return
     */
    public boolean isUserSortTableSetup() {
        return getAttr(SORT_TABLE) !=  null;
    }







    /**
     * saving sortation station
     * @param table
     */
    public void setUserTable(SortTable table){
        setAttr(SORT_TABLE, table);
    }






    /**
     * User sort table
     * @return
     */
    public SortTable getUserTable(){
        Object o = getAttr(SORT_TABLE);
        if (o != null){
           return (SortTable) o;
        }

        return null;
    }
    public SortTable getUserTable(VaadinSession session){
        Object o = getAttr(SORT_TABLE, session);
        if (o != null){
            return (SortTable) o;
        }

        return null;
    }





    public String getUserId(){
        return  (String) getAttr(USER_ID) ;
    }

    public String getUserId(VaadinSession pSession){
        return  (String) getAttr(USER_ID, pSession) ;
    }



    /**
     * Getting session attribute
     * @param pName
     * @return
     */
    private  Object getAttr(String pName){
        return VaadinSession.getCurrent().getAttribute(pName);
    }



    private  Object getAttr(String pName, VaadinSession pSession){
        return pSession.getAttribute(pName);
    }






    /**
     * Setting session attribute
     * @param pName
     * @param pValue
     */
    public  void setAttr(String pName, Object pValue){
        VaadinSession.getCurrent().setAttribute(pName, pValue);
    }



    /**
     * Getter  for the cartonization memory mao
     * @return
     */
    public Map<String, List<OrderDetail>> getCartonizationMemory(){
        return this.cartonizationResultMap;
    }


    public void  setToCartonizationMemory(Object pObject){
        this.cartonizationResultMap = ( Map<String, List<OrderDetail>>) pObject;
    }

    public void  setParcelMemory(Object pObject){
        this.parcelMap = ( Map<String, Parcel>) pObject;
    }


    public void  setParcelUIMemory(Object pObject){
        this.parcelUIMap = ( Map<String, ParcelLayout>) pObject;
    }





    public Map<String, Boolean>  getScannedByOrdersMemory(){
        return this.scansByOrdersMap;
    }


    public Map<String, Parcel> getParcelMemory(){
        return parcelMap;
    }



    public   Map<String, ParcelLayout> getParcelUIMemory(){
        return parcelUIMap;
    }


    // Этот метот очищает память всех компонентов
    public void invalidateSession() {
        // Clean all scanned enties
        getScannedByOrdersMemory().clear();
        getCartonizationMemory().clear();
        getParcelMemory().clear();
        getParcelUIMemory().clear();
        getCloseMethodList().clear();


    }


    // Этот метот фиксирует отсканированные заказы на отгрузку для последующей проверки

    public void addToScanned(UID uid){
        getScannedByOrdersMemory().put(uniqueKey(uid), true);
    }
    // Этот метот проверяет, что заказ сканируется впервые
    public boolean isThisIsTheFirstScanValidation(UID uid){
        String key = uniqueKey(uid);
        return  ((getScannedByOrdersMemory() == null ) || (getScannedByOrdersMemory().get(key) == null));
    }


    public int getCartonizedOrdersSize(){
        if (getCartonizationMemory() == null)
            return  0;
        else
            return getCartonizationMemory().entrySet().size();
    }


    public String uniqueKey(UID uid){
        String orderKey = uid.getOrderKey();
        String putawayClass = uid.getPutawayClass();
        String key = orderKey+"-"+putawayClass;
        return key;
    }





    public Parcel getParcelFromSession(UID uid) {
        return getParcelMemory().get( uniqueKey(uid));
    }




    public ParcelLayout getParcelUIFromSession(UID uid){
        return getParcelUIMemory().get(uniqueKey(uid));
    }



    public Parcel getParcelFromSessionByPrefix(UID uid) {
        Parcel theResutl = null;
        String key = uniqueKey(uid);

        Map<String, Parcel> parcelMap = getParcelMemory();
        Iterator<Map.Entry<String, Parcel>> it = parcelMap.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<String, Parcel> cur = it.next();
            if (cur.getKey().startsWith(key)) theResutl = cur.getValue();
        }
        return theResutl;
    }



    public void setParcelToSession(UID uid, Parcel pParcel) {
        String pId = uniqueKey(uid);
        pParcel.setParcelId(pId);
        getParcelMemory().put(pId, pParcel);
    }

    public void removeParcelFromSession(UID uid) {
        if (getParcelFromSession(uid) != null)  getParcelMemory().remove(uniqueKey(uid));
    }

    public void removeParcelFromSession(String pParcelId) {
        if (pParcelId!= null)  getParcelMemory().remove(pParcelId);
    }



    public void addParcelUIToSession(ParcelLayout layout, UID uid){
        getParcelUIMemory().put(uniqueKey(uid), layout);

    }

    public void removeParcelLayoutFromSession(String parcelId) {
        if (parcelId!= null)  getParcelUIMemory().remove(parcelId);
    }

    public ObjectWrapperList<?> checkSessionData() {
       return DBService.getInstance().getSessionData(getUserId());
    }



    public void setScannedOrdersMemory(Object object) {
        this.scansByOrdersMap = (Map<String, Boolean>)object;
    }




    public List<Boolean> getCloseMethodList(){
        return this.closeMethodList;
    }

    public boolean needReprint() {

        int i = 0;
        for (Boolean b : this.closeMethodList){
            if (i> 0 && b){
                return true;
            }
            i++;
        }

        return false;
    }

    public void setCloseMethodList(Object object) {
        this.closeMethodList = (List<Boolean>) object;
    }
}
