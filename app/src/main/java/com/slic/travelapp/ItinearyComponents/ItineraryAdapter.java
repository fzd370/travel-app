package com.slic.travelapp.ItinearyComponents;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.slic.travelapp.R;

import java.util.ArrayList;

public class ItineraryAdapter extends ArrayAdapter<ItineraryItem> {
    public ItineraryAdapter(Context context, ArrayList<ItineraryItem> destinations) {
        super(context, 0, destinations);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.itinerary_item_card, parent, false);
        }

        ItineraryItem item = getItem(position);
        RelativeLayout itemLayout = (RelativeLayout) convertView.findViewById(R.id.itinerary_item);

        TextView directions = (TextView) itemLayout.findViewById(R.id.textView2);
        TextView details = (TextView) itemLayout.findViewById(R.id.textView3);

        directions.setText(item.directions);
        details.setText(item.details);

        return convertView;
    }
}
