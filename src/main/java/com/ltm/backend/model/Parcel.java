package com.ltm.backend.model;

import com.ltm.backend.exception.UserException;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.*;

public class Parcel  implements Serializable {

    private String parcelId;
    private int place;
    private String dropId;
    private int packedQty  = 1;
    private final List<UID> uidList = new ArrayList<>();
    private long startTime;
    private long endTime;
    private String orderKey;
    private OrderDetail orderDetail;
    private String putawayclass;
    private boolean isClosed = false;



    public Parcel(int place, String dropId, String orderKey, String putawayclass, List<OrderDetail> orderDetailList) {

        Assert.notNull(place, "Place must be specified in Parcel constructor");
        Assert.notNull(dropId, "Place must be specified in Parcel constructor");
        Assert.notNull(orderKey, "OrderKey must be specified in Parcel constructor");
        Assert.notNull(putawayclass, "Putawayclass must be specified in Parcel constructor");



        this.place = place;
        this.dropId = dropId;
        this.orderKey = orderKey;
        this.putawayclass = putawayclass;

        this.orderDetail = orderDetailList.stream()
                        .filter(odItem -> odItem.getPutawayClass().equalsIgnoreCase(this.putawayclass))
                        .findFirst()
                        .get();


    }


    public void inc(){
        packedQty++;
    }
    public void dec(){
        packedQty--;
    }

    public void addUid(UID uid) {
        this.uidList.add(uid);
    }

    public List<UID> getUidList() {
        return uidList;
    }

    public int getPlace() {
        return place;
    }

    public void setPlace(int place) {
        this.place = place;
    }

    public String getDropId() {
        return dropId;
    }

    public void setDropId(String dropId) {
        this.dropId = dropId;
    }

    public int getPackedQty() {
        return packedQty;
    }

    public void setPackedQty(int packedQty) {
        this.packedQty = packedQty;
    }

    @Override
    public String toString() {
        String detail = "" ;

       for (UID item : uidList ){
           detail =  detail + item.toString() ;
       }

        return ":::{ Место: "+place+", DropId: "+dropId+ " Упаковано: "+ packedQty+"} -  ("+detail+")";
    }


    public Date getStartTime() {
        //return startTime;
        Date date = new Date(startTime);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.HOUR, - 3);
        Date d = cal.getTime();
        return d;

    }

    public void start() {

        this.startTime = System.currentTimeMillis();


    }

    public static void main (String[] args){


        Date date = new Date(System.currentTimeMillis());


        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.HOUR, - 3);
        Date d = cal.getTime();
        //DateFormat gmt = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
        //gmt.setTimeZone(TimeZone.getTimeZone("GMT"));






    }



    public Date getEndTime() {

        Date date = new Date(endTime);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.HOUR, -3);
        Date d = cal.getTime();
        return d;
    }

    public void end() {
        this.endTime =  System.currentTimeMillis();
    }


    public String getOrderKey() {
        return orderKey;
    }

    public void setOrderKey(String orderKey) {
        this.orderKey = orderKey;
    }

    public OrderDetail getOrderDetail() {
        return orderDetail;
    }

    public void removeUID(UID uid) {

        if (getUidList().contains(uid)){
            getUidList().remove(uid);
        }


        Iterator<CartonizedUID> it = getOrderDetail().getCartonizedUIDS().iterator();


        while (it.hasNext()) {
            CartonizedUID cartonizedUID = it.next();

            if (cartonizedUID.getUid().equals(uid)){
                it.remove();
                break;
            }


        }






        if (packedQty == 0){
            place--;
        }


        end();

        dec();

        getOrderDetail().setPackedQty(this.packedQty);
        getOrderDetail().removePickDetailKey(uid);
    }


    public String getParcelId() {
        return parcelId;
    }

    public void setParcelId(String parcelId) {
        this.parcelId = parcelId;
    }

    public void setClosed() {
        this.isClosed = true;
    }

    public boolean isClosed (){
        return this.isClosed;
    }

    public void checkCanBeClosed() throws UserException {
        if (uidList.isEmpty()) {
            throw new UserException("Нельзя закрыть пустую коробку");
        }

        if (isMultiPackagingBanned() && containsNotAllUidsOfOrder()) {
            throw new UserException("Перевозчик не поддерживает многопосылочность. Заказ упакован не до конца. В эту коробку необходимо добавить все оставшиеся товары.");
        }
    }


    private boolean isMultiPackagingBanned() {
        UID packedUid = uidList.stream()
                .findAny()
                .get();
        return packedUid.isMultiPackagingBanned();
    }

    private boolean containsNotAllUidsOfOrder() {
        return packedQty != orderDetail.getSumOpenQty();
    }
}
