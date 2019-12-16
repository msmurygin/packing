package com.ltm.backend.model;

import com.ltm.ui.ParcelLayout;
import com.vaadin.ui.GridLayout;

public interface ParcelAction {

    ParcelLayout createNewDraw(GridLayout parcelGridLayout, Parcel parcel);

    void addToExistingDraw(ParcelLayout parcelGridLayout, Parcel parcel);

}
