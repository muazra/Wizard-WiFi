package com.android.wizard_wifi;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;

public class PollService extends IntentService{
    private static final String TAG = "PollService";

    public PollService(){
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent){
        if(!isLocationEnabled()) return;
        Log.i(TAG, "Received an intent " + intent);
    }

    private boolean isLocationEnabled(){
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

}
