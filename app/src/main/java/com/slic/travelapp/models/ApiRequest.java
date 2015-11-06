package com.slic.travelapp.models;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.MapFragment;
import com.slic.travelapp.MainActivity;
import com.slic.travelapp.service.ApiService;
import com.squareup.okhttp.OkHttpClient;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

/**
 * Singleton to make Weather API Reuqest
 */
public class ApiRequest {
    private static ApiRequest theInstance = null;
    private ApiService api = null;
    private final String weatherAPI = "http://api.openweathermap.org";
    Communicator comm;

    public interface Communicator {
        void updateWeather(int i);
    }
    public void setCommunication(Communicator c){
        this.comm = c;
    }

    public static ApiRequest getInstance() {
        if (theInstance == null) {
            theInstance = new ApiRequest();
        }
        return theInstance;
    }

    private ApiRequest(){
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(weatherAPI)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setClient(new OkClient(new OkHttpClient()))
                .build();
        api = restAdapter.create(ApiService.class);
    }


    // 2xx Thunderstorm 3xx Drizzle 5xx Rain
    // 6xx Snow 7xx Haze 800 Clear 80x Clouds
    // 900-906(changed to 4xx) Extreme 951-956 Breezy
    public void getCityNowcast() {
        api.getCityNowcast("Singapore", new Callback<WeatherResponse>() {
            @Override
            public void success(WeatherResponse weatherResponse, Response response) {
                String id = weatherResponse.getWeather()[0].getId();
                shout(id);
                int code = Integer.valueOf(id.charAt(0));
                if(id.charAt(0) == '9' && id.charAt(1) == '0')
                    code = 4;

                shout("Code: " + String.valueOf(code));
                shout("Temp: " + weatherResponse.getMain().getTemp());

                comm.updateWeather(code);
            }

            @Override
            public void failure(RetrofitError error) {
                shout(error.getMessage());
                shout(error.getUrl());
            }
        });
    }

    private void shout(String s) {
        Log.d("SLIC", s);
    }
}
