package com.slic.travelapp;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.slic.travelapp.models.ApiRequest;

/* 2nd Additional Function : Weather nowcast + Things to Bring list
* 1. To allow user to fill up list of Things to Bring for the day.
* 2. Recommendation of things to bring based on Weather and Location, e.g. Shades, Umbrella
*
* Priority: Lowest
* Current Status of Implementation : Able to GET weather nowcast and change icon accordingly
* To do : Dynamic List view to add/remove items
* */

public class ItemsFragment extends Fragment implements ApiRequest.Communicator {

    private ImageView weatherView1;
    private ApiRequest apiRequest = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_items, container, false);

        apiRequest = ApiRequest.getInstance();
        apiRequest.getCityNowcast();
        apiRequest.setCommunication(this);

        weatherView1 = (ImageView) rootView.findViewById(R.id.image_weather1);

        return rootView;
    }

    // Called by apiRequest after it recives a response for a GET request
    // Updates icon to display
    @Override
    public void updateWeather(int i) {
        // 2xx Thunderstorm 3xx Drizzle 5xx Rain
        // 6xx Snow 7xx Haze 800 Clear 80x Clouds
        // 900-906(changed to 4xx) Extreme 951-956 Breezy

        if(i == 7) {
            weatherView1.setImageResource(R.mipmap.ic_weather_haze);
        } else if(i == 8) {
            weatherView1.setImageResource(R.mipmap.ic_weather_clear);
        } else {
            weatherView1.setImageResource(R.mipmap.ic_weather_rain);
        }

    }
}
