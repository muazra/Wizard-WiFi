package com.android.wizard_wifi;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;

import com.android.wizard_wifi.util.findAddressUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PollService extends IntentService{
    private static final String TAG = "PollService";
    private Context mContext = this;

    private static final int POLL_INTERVAL = 1000 * 15; // 15 seconds

    private static LocationManager mlocationManager;

    public PollService(){
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent){
        Log.i(TAG, "Received an intent " + intent);
        if(!isLocationEnabled()) return;

        SharedPreferences mLocations = getSharedPreferences("LOCATIONS", 0);
        if(mLocations.getInt("num_locations", 0) == 0) return;

        List<String> zipcodes = new ArrayList<String>();
        try {
            JSONArray jsonArray = new JSONObject(mLocations.getString("locations", null)).
                    getJSONArray("locations");
            for(int i = 0; i < jsonArray.length(); i++)
                zipcodes.add(jsonArray.getJSONObject(i).getString("zipcode"));
        }catch(JSONException e){
            e.printStackTrace();
        }

        locationandToggle(zipcodes);
    }

    private boolean isLocationEnabled(){
        mlocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return mlocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void locationandToggle(final List<String> zipcodes){
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                String zip = findAddressUtil.find(location, mContext).get(0).getPostalCode();

                WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
                if(zipcodes.contains(zip)){
                    //turn on Wi-Fi
                    if(!wifi.isWifiEnabled())
                        wifi.setWifiEnabled(true);
                }
                else{
                    //turn off Wi-Fi
                    if(!wifi.isWifiEnabled())
                        wifi.setWifiEnabled(false);
                }
            }
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {}
            @Override
            public void onProviderEnabled(String s) {}
            @Override
            public void onProviderDisabled(String s) {}
        };
        mlocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
    }

}
