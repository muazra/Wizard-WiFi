package com.android.wizard_wifi;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Looper;
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
    private static LocationManager mlocationManager;

    private static final int POLL_INTERVAL = 1000 * 20; // 15 seconds

    public PollService(){
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent){
        Log.i(TAG, "Received an intent " + intent);
        if(!isLocationEnabled()) return;

        SharedPreferences mLocations = getSharedPreferences("LOCATIONS", 0);
        if(mLocations.getInt("num_locations", 0) == 0) return;

        Log.i(TAG, "num_locations != 0");

        List<String> zipcodes = new ArrayList<String>();
        try {
            JSONArray jsonArray = new JSONObject(mLocations.getString("locations", null)).
                    getJSONArray("locations");
            for(int i = 0; i < jsonArray.length(); i++)
                zipcodes.add(jsonArray.getJSONObject(i).getString("zipcode"));
        }catch(JSONException e){
            e.printStackTrace();
        }

        Log.i(TAG, "zipcodes current length after JSON extraction = " + zipcodes.size());
        locationandToggle(zipcodes);
        Looper.loop();
    }

    private boolean isLocationEnabled(){
        mlocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return mlocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void locationandToggle(final List<String> zipcodes){
        Log.i(TAG, "in locationandToggle method");
        mlocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                String zip = findAddressUtil.find(location, mContext).get(0).getPostalCode();
                Log.i(TAG, "Current zipcode found in intent service = " + zip);
                WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
                if(zipcodes.contains(zip)){
                    //turn on Wi-Fi
                    if(!wifi.isWifiEnabled()) {
                        Log.i(TAG, "Wi-Fi currently disabled");
                        wifi.setWifiEnabled(true);
                        buildNotification(true);
                    }
                }
                else{
                    //turn off Wi-Fi
                    if(wifi.isWifiEnabled()) {
                        wifi.setWifiEnabled(false);
                        buildNotification(false);
                    }
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

    public static void setServiceAlarm(Context context, boolean isOn) {
        Intent i = new Intent(context, PollService.class);
        PendingIntent pi = PendingIntent.getService(
                context, 0, i, 0);

        AlarmManager alarmManager = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);

        if (isOn) {
            alarmManager.setRepeating(AlarmManager.RTC,
                    System.currentTimeMillis(), POLL_INTERVAL, pi);
        } else {
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }

    public static boolean isServiceAlarmOn(Context context) {
        Intent i = new Intent(context, PollService.class);
        PendingIntent pi = PendingIntent.getService(
                context, 0, i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    private void buildNotification(boolean isWifiOn){
        PendingIntent pi = PendingIntent
                .getActivity(mContext, 0, new Intent(mContext, MainActivity.class), 0);

        String notificationText = "Automatic Wi-Fi turn OFF";
        if(isWifiOn)
            notificationText = "Automatic Wi-Fi turn ON";

        Notification notification = new Notification.Builder(mContext)
                .setContentTitle("Wizard Wi-Fi")
                .setContentText(notificationText)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, notification);
    }

}
