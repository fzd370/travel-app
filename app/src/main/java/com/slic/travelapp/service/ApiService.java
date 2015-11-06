package com.slic.travelapp.service;

import com.slic.travelapp.models.WeatherResponse;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * RetroFit Service to make Weather API requests
 */
public interface ApiService {
    public static final String APIkey = "appid=5b5db5ef2677402d4471e463baaab5ad";

    @GET("/data/2.5/forecast?" + APIkey)
    void getGeoForcast(@Query("lat") double lat, @Query("lon") double lon, Callback<List<String>> response);

    @GET("/data/2.5/forecast?" + APIkey)
    void getCityForecast(@Query("q") String cityname, Callback<List<String>> response);

    @GET("/data/2.5/weather?" + APIkey)
    void getCityNowcast(@Query("q") String cityname, Callback<WeatherResponse> response);
}
