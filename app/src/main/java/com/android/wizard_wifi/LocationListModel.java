package com.android.wizard_wifi;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton class for list of locationModels.
 * @author Muaz Rahman
 */
public class LocationListModel {

    public List<LocationModel> locationList;
    private LocationListModel() {
        locationList = new ArrayList<LocationModel>();
    }

    static LocationListModel obj = null;
    public static synchronized LocationListModel instance() {
        if (obj == null) obj = new LocationListModel();
        return obj;
    }
}
