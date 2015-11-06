package com.slic.travelapp.models;

/**
 * Created by seanlim on 6/11/2015.
 */
public class WeatherResponse {
    private Coord coord;
    private Main main;
    private Weather[] weather;

    public Weather[] getWeather() {
        return weather;
    }

    public Main getMain() {
        return main;
    }

    public Coord getCoord() {
        return coord;
    }

}