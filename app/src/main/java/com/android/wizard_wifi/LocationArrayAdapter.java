package com.android.wizard_wifi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Muaz on 10/13/14.
 */
public class LocationArrayAdapter extends ArrayAdapter<LocationModel> {
    private final List<LocationModel> mLocationModels;
    private final Context mContext;

    public LocationArrayAdapter(Context context, List<LocationModel> models) {
        super(context, R.layout.location_row, models);
        mLocationModels = models;
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View locationRow = inflater.inflate(R.layout.location_row, parent, false);

        LocationModel locationModel = mLocationModels.get(position);
        TextView nameTextView = (TextView) locationRow.findViewById(R.id.locationName);
        nameTextView.setText(locationModel.getName());
        return locationRow;
    }
}
