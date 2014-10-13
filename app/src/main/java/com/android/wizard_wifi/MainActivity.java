package com.android.wizard_wifi;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;


public class MainActivity extends ListActivity {

    private static final String TAG = "MainActivity";

    private SharedPreferences mService;
    private SharedPreferences mLocations;
    private SharedPreferences.Editor mEditor;
    private Boolean mServiceStatus;

    private Button mSaveButton;
    private ProgressBar mProgressBar;

    private Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "OnCreate");

        setContentView(R.layout.activity_main);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar_main);
        mSaveButton = (Button) findViewById(R.id.save_current_location_button);

        setupCache();

        LocationsListAdapter adapter = new LocationsListAdapter(this, LocationListModel.instance().locationList);
        setListAdapter(adapter);

        setupButtons();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        Log.d(TAG, "OnCreateOptionsMenu");

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

        Log.d(TAG, "onOptionsItemSelected");

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

    private class LocationsListAdapter extends ArrayAdapter<LocationModel> {
        private final List<LocationModel> mLocationModels;
        private final Context mContext;

        public LocationsListAdapter(Context context, List<LocationModel> models) {
            super(context, R.layout.location_row, models);

            Log.d(TAG, "LocationsListAdapter Constructor");

            mLocationModels = models;
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            Log.d(TAG, "LocationListAdapter GET_VIEW");

            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View locationRow = inflater.inflate(R.layout.location_row, parent, false);

            LocationModel locationModel = mLocationModels.get(position);
            TextView nameTextView = (TextView) locationRow.findViewById(R.id.locationName);

            Log.d(TAG, "GET_VIEW -> location name = " + locationModel.getName());

            nameTextView.setText(locationModel.getName());

            return locationRow;
        }

    }

    private void setupCache(){
        Log.d(TAG, "setupCache");

        if(LocationListModel.instance().locationList.size() > 0)
            return;

        mLocations = getSharedPreferences("LOCATIONS", 0);
        int num_locations = mLocations.getInt("num_locations", 0);

        Log.d(TAG, "setupCache -> num_locations = " + num_locations);

        List<LocationModel> locationListTemp = new ArrayList<LocationModel>();
        LinkedHashSet<String> locationSet;

        for(int i = 0; i < num_locations; i++){
            String key = "location" + String.valueOf(i);
            Log.d(TAG, "setupCache -> key = " + key);
            locationSet = (LinkedHashSet<String>) mLocations.getStringSet(key, null);       //use JSONAray - then store as String //or check fav link
            Iterator<String> iterator = locationSet.iterator();
            LocationModel location = new LocationModel();

            Log.d(TAG, "setupCache -> locationSet = " +locationSet.toString());

            while(iterator.hasNext()){
                location.setName(iterator.next());
                location.setZipcode(iterator.next());
                location.setLatitude(iterator.next());
                location.setLongitude(iterator.next());
            }

            locationListTemp.add(location);
        }

        LocationListModel.instance().locationList = locationListTemp;

    }

    private void setupButtons(){
        Log.d(TAG, "setupButtons");

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBar.setVisibility(View.VISIBLE);
                saveCurrentLocation();
            }
        });
    }

    private void saveCurrentLocation(){
        Log.d(TAG, "saveCurrentLocation");

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LocationModel locationObject = new LocationModel();

                locationObject.setLatitude(String.valueOf(location.getLatitude()));
                locationObject.setLongitude(String.valueOf(location.getLongitude()));
                locationObject.setZipcode(find(location).get(0).getPostalCode());

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
        Log.d(TAG, "buildSaveLocationDialog");

        AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_DARK);
        builder.setTitle("Enter location name");
        final EditText name = new EditText(this);
        name.setTextColor(getResources().getColor(android.R.color.white));
        builder.setView(name);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.d(TAG, "buildSaveLocattionDialog -> in EditText = " + name.getText().toString());

                locationObject.setName(name.getText().toString());
                LocationListModel.instance().locationList.add(locationObject);


                mProgressBar.setVisibility(View.INVISIBLE);

                Intent intent = new Intent(mContext, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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

    public List<Address> find(Location location){
        Geocoder geocode = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> address = geocode.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (address.size() > 0) {
                return address;
            }
        }catch(IOException e){}

        return null;
    }

    @Override
    public void onStop(){
        super.onStop();

        Log.d(TAG, "onStop");

        mLocations = getSharedPreferences("LOCATIONS", 0);
        mEditor = mLocations.edit();

        List<LocationModel> tempLocationList = LocationListModel.instance().locationList;
        int num_locations = tempLocationList.size();

        Log.d(TAG, "onStop -> num_locations = " + num_locations);

        mEditor.putInt("num_locations", num_locations);

        Set<String> location = new LinkedHashSet<String>();
        String key;

        for(int i = 0; i < num_locations; i++){
            key = "location" + String.valueOf(i);

            Log.d(TAG, "onStop -> key = " + key);

            location.clear();

            location.add(tempLocationList.get(i).getName());
            location.add(tempLocationList.get(i).getZipcode());
            location.add(tempLocationList.get(i).getLatitude());
            location.add(tempLocationList.get(i).getLongitude());

            Log.d(TAG, "onStop - Location toString() = " + location.toString());

            mEditor.putStringSet(key, location);
        }

        mEditor.apply();

    }
}
