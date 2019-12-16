package com.ltm.backend.controller;

import com.ltm.MyUI;
import com.ltm.backend.db.DBService;
import com.ltm.backend.model.LocToBroadCastWrapper;
import com.ltm.backend.model.LocsToBroadcast;
import com.ltm.backend.model.SortTable;
import com.ltm.backend.utils.BaseConstants;
import com.ltm.backend.utils.SessionUtils;
import com.vaadin.data.provider.Sort;
import com.vaadin.server.VaadinServletService;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class LocationBroadcaster implements Serializable {
                     // Orderkey, list of Locations
    private static Map<String, LocToBroadCastWrapper> broadcastedLocations = new HashMap<>();
    private static final Logger LOGGER = Logger.getLogger(LocationBroadcaster.class.getName());
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static LinkedList<LocationBroadcastListener> listeners = new LinkedList<LocationBroadcastListener>();



    // YM-179, 05/07/2019, Maxim Smurygin, START
    /**
     * Saving broadcasted location from restored session
     * @param locsToBroadcast
     */
    public static void saveBroadCatedLocation (LocToBroadCastWrapper locsToBroadcast){
       broadcastedLocations.put(locsToBroadcast.getOrderKey(), locsToBroadcast);
    }

    public static synchronized void register(LocationBroadcastListener listener) {
        listeners.add(listener);
    }




    public static synchronized void unregister(LocationBroadcastListener listener) {
        try {
            listeners.remove(listener);
            LocToBroadCastWrapper locationToRemove = (LocToBroadCastWrapper) listener.getData();
            if (locationToRemove != null) {
                broadcastedLocations.remove(locationToRemove.getOrderKey());

                // YM-179, Maxim Smurygin , 05/7/2019, STARt
                // commented

                ///// -==> 09/07/2019, Maxim Smurygin ROLL-BACK, END  /////
                /// UNCOMMENTED FOR ROLL-BACK PURPOSE
                ParcelSharedService.getInstance().removeOrderOnCloseByUser();
                // YM-179, Maxim Smurygin , 05/7/2019, END

            }
        }catch (Exception e){
            LOGGER.error(e);
        }

    }




    public static synchronized void unregister(String pOrderKey) {
        broadcastedLocations.remove(pOrderKey);

    }

    public static synchronized void broadcast(final Map<String, List<LocsToBroadcast>> pLocsBrodcast) {

        LOGGER.debug("Broadcasting for all workstations");
        // For all workstations
        Iterator<LocationBroadcastListener>  it = listeners.iterator();

        while (it.hasNext()) {
        //for (final LocationBroadcastListener listener: listeners) {
            LocationBroadcastListener locationBroadcastListener = it.next();
            //=======================================================================//
            broadCastForCurrentUI(pLocsBrodcast, locationBroadcastListener);
            //=======================================================================//
        }

    }

    public static synchronized void broadCastForCurrentUI( final Map<String, List<LocsToBroadcast>> pLocsBrodcast, LocationBroadcastListener locationBroadcastListener){
        // Getting user sort table
        SessionUtils futureSessionUtil =  locationBroadcastListener.getCurrentSessionUtils();

        Object sortTableObject = futureSessionUtil.getUserTable(locationBroadcastListener.getSession());
        // DO this step only if user choose table and have the sort-table object in session
        if ( sortTableObject != null) {
            // if user UI has no location assigned yet
            if ( locationBroadcastListener.getData() == null){

                LOGGER.debug("User has no assigned location, itierating for "+((SortTable)sortTableObject).getSortTableKey()+ " User work in "+((SortTable)sortTableObject).getAreaKey() );
                String userAreaKey = ((SortTable)sortTableObject).getAreaKey();
                List<LocsToBroadcast> locList = pLocsBrodcast.get(userAreaKey);

                //=================================================================
                assignSortLocation(locList,  locationBroadcastListener , ((SortTable)sortTableObject));
                //=================================================================

            }else {
                if (locationBroadcastListener.getData() instanceof  LocToBroadCastWrapper){
                    LocToBroadCastWrapper locBroadCastWrapper = ((LocToBroadCastWrapper) locationBroadcastListener.getData());
                    List<LocsToBroadcast> sortLocationList = locBroadCastWrapper.getSortLocationList();

                    // Если назначенно больше одной ячейки
                    if (sortLocationList.size() > 1){
                        // Преобразуем в простой строковый список ячеек
                        List<String> locListString = sortLocationList.stream()
                                .map(item -> item.getSortLocation())
                                .collect(Collectors.toList());

                        if ( (!DBService.getInstance().getTaskCountByLocList(locListString)) ||
                                (!DBService.getInstance().isSortLocationActive(locListString))) {  // Если ячейка не активная удаляем из раздачи
                            //currentUI.setData(null); // moved to MyUI Class to removeLocationFromUI method
                            broadcastedLocations.remove(locBroadCastWrapper.getOrderKey());
                            executorService.execute(() -> locationBroadcastListener.removeLocationFromUI());
                        }

                    }else {
                        // Обычный вариант с одной записью (яч - заказ)
                        LocsToBroadcast locsToBroadcast = sortLocationList.get(0);
                        String loc = locsToBroadcast.getSortLocation();
                        String orderKey = locsToBroadcast.getOrderKey();
                        // Если нет задач, по ячейке, удалим из списка занятых ячеек, обнулим UI data и вызовим обновление UI
                        if ((!DBService.getInstance().getTaskCountByLoc(loc)) ||
                                (!DBService.getInstance().isSortLocationActive(loc))) { // Если ячейка не активная удаляем из раздачи
                            //currentUI.setData(null); // moved to MyUI Class to removeLocationFromUI method
                            broadcastedLocations.remove(orderKey);
                            executorService.execute(() -> locationBroadcastListener.removeLocationFromUI());
                        }
                    }

                    // Если ячейку диактивировали, а потом активировали, ее необходимо выдать на экран
                    String userAreaKey = ((SortTable)sortTableObject).getAreaKey();
                    List<LocsToBroadcast> locList = pLocsBrodcast.get(userAreaKey);
                    List<LocsToBroadcast> locListFiltered = locList.stream().filter(e -> e.getOrderKey().equalsIgnoreCase(locBroadCastWrapper.getOrderKey())).collect(Collectors.toList());
                    if (locListFiltered.size() > locBroadCastWrapper.getSortLocationList().size()){
                        broadcastedLocations.remove(locBroadCastWrapper.getOrderKey());
                        executorService.execute(() -> locationBroadcastListener.removeLocationFromUI());
                    }


                    if (!locBroadCastWrapper.getSortTable().getSortTableKey().equalsIgnoreCase( ((SortTable)sortTableObject).getSortTableKey())){
                        broadcastedLocations.remove(locBroadCastWrapper.getOrderKey());
                        executorService.execute(() -> locationBroadcastListener.removeLocationFromUI());
                    }

                }
            }
        }
    }


    private static  void assignSortLocation(List<LocsToBroadcast> locList , LocationBroadcastListener locationBroadcastListener, SortTable sortTable) {

        if (locList.size() == 0) {
            LOGGER.error("Nothing to assing for user " + locationBroadcastListener.getSession().getAttribute("userid") + ", the location list is empty");
            return;
        }

        for (LocsToBroadcast loc : locList) {
            String sortLocation = loc.getSortLocation();
            String orderKey  = loc.getOrderKey();
            LOGGER.debug("Проверка ячейки назначения " +sortLocation+" были ли она назначена кому-нибудь другому?");
            // Если ячейка не числится в списке назначенных ячеек и нашему экрану еще ничего ен назначалось
            if (broadcastedLocations.get(orderKey) == null && locationBroadcastListener.getData() == null ) {
                LOGGER.debug("Ячека " +sortLocation+" никому не назначена, нужно назначить ");

                // Ищем все ячейки по заказу - ЗО
                List<LocsToBroadcast> multipleSortLocationList = locList.stream()
                        .filter(sortLocationListItem -> orderKey.equalsIgnoreCase(sortLocationListItem.getOrderKey())).collect(Collectors.toList());

                LocToBroadCastWrapper locWrapper = new LocToBroadCastWrapper(orderKey, multipleSortLocationList);
                locWrapper.setSortTable(sortTable);
                locationBroadcastListener.setData(locWrapper); // закрепляем за UI - текущего пользователя
                broadcastedLocations.put(orderKey, locWrapper); // заносим в общий лист назначенных ячеек, передаем даные в UI

                if (locationBroadcastListener != null)
                    executorService.execute(() -> locationBroadcastListener.receiveBroadcast(locWrapper));
                else
                    executorService.execute(() -> locationBroadcastListener.receiveBroadcast(locWrapper));


                break;
            }
        }
    }

    public static  void broadcastForUI(final  MyUI currentUI , final Map<String, List<LocsToBroadcast>> pLocsBrodcast) {

        LOGGER.debug("Broadcasting for forcibly for current UI workstations");

        // Getting user sort table
        Object sortTableObject =  (currentUI).getCurrentSessionUtils().getUserTable(currentUI.getSession());
        // DO this step only if user choose table and have the sort-table object in session
        if ( sortTableObject != null) {
            // if user UI has no location assigned yet
            if ( currentUI.getData() == null){

                LOGGER.debug("User has no assigned location, itierating for "+((SortTable)sortTableObject).getSortTableKey()+ " User work in "+((SortTable)sortTableObject).getAreaKey() );
                String userAreaKey = ((SortTable)sortTableObject).getAreaKey();
                List<LocsToBroadcast> locList = pLocsBrodcast.get(userAreaKey);

                //=================================================================
                assignSortLocation(locList,  currentUI, ((SortTable)sortTableObject));
                //=================================================================

            }
        }

    }
}
