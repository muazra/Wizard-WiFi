package com.android.wizard_wifi.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class findAddressUtil {

    public static List<Address> find(Location location, Context context){
        Geocoder geocode = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> address = geocode.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (address.size() > 0) {
                return address;
            }
        }catch(IOException e){}

        return null;
    }
}
