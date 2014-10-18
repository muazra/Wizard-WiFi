package com.android.wizard_wifi;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.wizard_wifi.util.findAddressUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ListActivity {
    private static final String TAG = "MainActivity";
    private Context mContext = this;

    private SharedPreferences mService;
    private SharedPreferences mLocations;
    private SharedPreferences.Editor mEditor;
    private Boolean mServiceStatus;

    private Button mSaveButton;
    private Button mClearButton;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar_main);
        mSaveButton = (Button) findViewById(R.id.save_current_location_button);
        mClearButton = (Button) findViewById(R.id.clear_all_locations_button);

        setupCache();
        setupButtons();

        LocationArrayAdapter adapter =
                new LocationArrayAdapter(this, LocationListModel.instance().locationList);
        setListAdapter(adapter);

        Intent i = new Intent(mContext, PollService.class);
        mContext.startService(i);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        mService = getSharedPreferences("SERVICE", 0);
        mServiceStatus = mService.getBoolean("service", false);

        MenuItem serviceItem = menu.findItem(R.id.action_service);
        if(!mServiceStatus)
            serviceItem.setIcon(R.drawable.ic_action_service_off);
        else
            serviceItem.setIcon(R.drawable.ic_action_service_on);
        serviceItem.setVisible(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_service) {
            mServiceStatus = mService.getBoolean("service", false);
            mEditor = mService.edit();

            if(!mServiceStatus) {
                item.setIcon(R.drawable.ic_action_service_on);
                mEditor.putBoolean("service", true);
                Toast.makeText(this, "Service turned ON", Toast.LENGTH_SHORT).show();
            }
            else{
                item.setIcon(R.drawable.ic_action_service_off);
                mEditor.putBoolean("service", false);
                Toast.makeText(this, "Service turned OFF", Toast.LENGTH_SHORT).show();
            }
            mEditor.apply();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupCache(){
        if(LocationListModel.instance().locationList.size() > 0)
            return;

        mLocations = getSharedPreferences("LOCATIONS", 0);
        if(mLocations.getInt("num_locations", 0) == 0) return;

        List<LocationModel> locationListTemp = new ArrayList<LocationModel>();
        String jsonString = mLocations.getString("locations", null);

        try{
            JSONObject respJson = new JSONObject(jsonString);
            JSONArray jsonArray = respJson.getJSONArray("locations");
            JSONObject jsonObject;

            for(int i = 0; i < jsonArray.length(); i++){
                LocationModel location = new LocationModel();
                jsonObject = jsonArray.getJSONObject(i);

                location.setName(jsonObject.getString("name"));
                location.setZipcode(jsonObject.getString("zipcode"));
                location.setLatitude(jsonObject.getString("latitude"));
                location.setLongitude(jsonObject.getString("longitude"));

                locationListTemp.add(location);
            }
        }catch(JSONException e){
            e.printStackTrace();
        }

        LocationListModel.instance().locationList = locationListTemp;
    }

    private void setupButtons(){
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBar.setVisibility(View.VISIBLE);
                saveCurrentLocation();
            }
        });

        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBar.setVisibility(View.VISIBLE);

                LocationListModel.instance().locationList.clear();
                mLocations = getSharedPreferences("LOCATIONS", 0);
                mEditor = mLocations.edit();
                mEditor.putInt("num_locations", 0);
                mEditor.apply();

                mProgressBar.setVisibility(View.INVISIBLE);

                Intent intent = new Intent(mContext, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });
    }

    private void saveCurrentLocation(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LocationModel locationObject = new LocationModel();

                locationObject.setLatitude(String.valueOf(location.getLatitude()));
                locationObject.setLongitude(String.valueOf(location.getLongitude()));
                locationObject.setZipcode(findAddressUtil.find(location, mContext).get(0).getPostalCode());

                buildSaveLocationDialog(locationObject);
            }
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {}
            @Override
            public void onProviderEnabled(String s) {}
            @Override
            public void onProviderDisabled(String s) {}
        };
        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
    }

    private void buildSaveLocationDialog(final LocationModel locationObject){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_DARK);
        builder.setTitle("Enter location name");
        final EditText name = new EditText(this);
        name.setTextColor(getResources().getColor(android.R.color.white));
        builder.setView(name);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                locationObject.setName(name.getText().toString().toUpperCase());
                LocationListModel.instance().locationList.add(locationObject);
                mProgressBar.setVisibility(View.INVISIBLE);

                Intent intent = new Intent(mContext, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        builder.show();
    }

    @Override
    public void onStop(){
        super.onStop();

        mLocations = getSharedPreferences("LOCATIONS", 0);
        mEditor = mLocations.edit();

        List<LocationModel> tempLocationList = LocationListModel.instance().locationList;
        int num_locations = tempLocationList.size();
        mEditor.putInt("num_locations", num_locations);

        JSONArray jArray = new JSONArray();
        for(int i = 0; i < num_locations; i++){
            JSONObject jObject = new JSONObject();
            try {
                jObject.put("name", tempLocationList.get(i).getName());
                jObject.put("zipcode", tempLocationList.get(i).getZipcode());
                jObject.put("latitude", tempLocationList.get(i).getLatitude());
                jObject.put("longitude", tempLocationList.get(i).getLongitude());
                jArray.put(jObject);
            }catch(JSONException e){
                e.printStackTrace();
            }
        }

        JSONObject mainJObject = new JSONObject();
        try{
            mainJObject.put("locations", jArray);
        }catch(JSONException e){
            e.printStackTrace();
        }

        Log.d(TAG, "mainObject.toString() = " + mainJObject.toString());

        mEditor.putString("locations", mainJObject.toString());
        mEditor.apply();

    }
}
