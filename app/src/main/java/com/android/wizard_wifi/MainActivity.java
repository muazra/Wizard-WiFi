package com.android.wizard_wifi;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class MainActivity extends Activity {

    private SharedPreferences mService;
    private Boolean mServiceStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mService = getSharedPreferences("SERVICE", 0);
        mServiceStatus = mService.getBoolean("service", false);

        MenuItem serviceItem = menu.findItem(R.id.action_service);
        if(!mServiceStatus)
            serviceItem.setIcon(R.drawable.ic_action_service_off);
        else
            serviceItem.setIcon(R.drawable.ic_action_service_on);

        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_service) {

            SharedPreferences.Editor editor;
            editor = mService.edit();

            if(!mServiceStatus) {
                item.setIcon(R.drawable.ic_action_service_on);
                editor.putBoolean("service", true);
                Toast.makeText(this, "Service turned ON", Toast.LENGTH_LONG).show();
            }
            else{
                item.setIcon(R.drawable.ic_action_service_off);
                editor.putBoolean("service", false);
                Toast.makeText(this, "Service turned OFF", Toast.LENGTH_LONG).show();
            }

            editor.apply();
        }
        return super.onOptionsItemSelected(item);
    }
}
