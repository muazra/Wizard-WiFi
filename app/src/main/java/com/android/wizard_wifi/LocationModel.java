package com.android.wizard_wifi;

/**
 * Class for location object.
 * @author Muaz Rahman
 */
public class LocationModel {

    private String mZipcode;
    private String mLatitude;
    private String mLongitude;
    private String mName;

    public String getZipcode() {
        return mZipcode;
    }

    public void setZipcode(String zipcode) {
        mZipcode = zipcode;
    }

    public String getLatitude() {
        return mLatitude;
    }

    public void setLatitude(String latitude) {
        mLatitude = latitude;
    }

    public String getLongitude() {
        return mLongitude;
    }

    public void setLongitude(String longitude) {
        mLongitude = longitude;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

}
