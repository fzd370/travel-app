package com.slic.travelapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.slic.travelapp.models.ApiRequest;
import com.slic.travelapp.models.Weather;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/* 2nd Additional Function : Weather nowcast + Things to Bring list
* 1. To allow user to fill up list of Things to Bring for the day.
* 2. Recommendation of things to bring based on Weather and Location, e.g. Shades, Umbrella
*
* Priority: Lowest
* Current Status of Implementation : Able to GET weather nowcast and change icon accordingly
* To do : Dynamic List view to add/remove items
* */

public class ItemsFragment extends Fragment implements
        ApiRequest.Communicator,
        View.OnClickListener{

    private ApiRequest apiRequest = null;

    private ImageView weatherView;
    private TextView weatherText;

    private ListView listView;

    private ArrayList<String> listValues = ((MainActivity) getActivity()).itemList;
    private static ArrayAdapter<String> listAdapter;
    private EditText inputItem;
    private Button inputButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_items, container, false);

        apiRequest = ApiRequest.getInstance();
        apiRequest.getCityNowcast();
        apiRequest.setCommunication(this);

        weatherView = (ImageView) rootView.findViewById(R.id.image_weather);
        weatherText = (TextView) rootView.findViewById(R.id.text_weather);

        inputButton = (Button) rootView.findViewById(R.id.button_item_add);
        inputButton.setOnClickListener(this);
        inputItem = (EditText) rootView.findViewById(R.id.input_item);

        listAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_list_item_1, android.R.id.text1, listValues);
//                R.layout.bring_item, R.id.item_text, listValues);
        listView = (ListView) rootView.findViewById(R.id.item_list);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String itemValue = (String) listView.getItemAtPosition(position);
                shout("Position: " + position + "\nItem: " + itemValue);
                hideKeyboard();
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String itemValue = (String) listView.getItemAtPosition(position);
                shout("Position: " + position + "\nItem: " + itemValue);
                listValues.remove(position);
                listAdapter.notifyDataSetChanged();
                hideKeyboard();
                return false;
            }
        });


        Toast.makeText(getContext(),"Press and hold on item to remove", Toast.LENGTH_LONG).show();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    // Called by apiRequest after it recives a response for a GET request
    // Updates icon to display
    @Override
    public void updateWeather(int i) {
        // 2xx Thunderstorm 3xx Drizzle 5xx Rain
        // 6xx Snow 7xx Haze 800 Clear 80x Clouds
        // 900-906(changed to 4xx) Extreme 951-956 Breezy

        shout("WEATHER: " + String.valueOf(i));

        if(i == 7) {
            weatherView.setImageResource(R.mipmap.ic_weather_haze);
            weatherText.setText("Hazy Day");
            if(listValues.isEmpty()) listValues.add("N95 Mask");
        } else if(i == 8) {
            weatherView.setImageResource(R.mipmap.ic_weather_clear);
            weatherText.setText("Sunny Day");
            if(listValues.isEmpty()){
                listValues.add("Shades");
                listValues.add("Sunblock");
            }

        } else {
            weatherView.setImageResource(R.mipmap.ic_weather_rain);
            weatherText.setText("Rainy Day");
            if(listValues.isEmpty()) listValues.add("Umbrella");
        }
        listAdapter.notifyDataSetChanged();
    }

    public static void updateSet(){
        listAdapter.notifyDataSetChanged();
    }

    public void shout(String s) {
        Log.d("SLIC", s);
    }
    protected void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.button_item_add) {
            String userinput = inputItem.getText().toString();
            if(!userinput.isEmpty()){
                listValues.add(inputItem.getText().toString());
                listAdapter.notifyDataSetChanged();
                inputItem.setText("");
            }
            listView.setSelection(listAdapter.getCount()-1);
        }
    }
}
