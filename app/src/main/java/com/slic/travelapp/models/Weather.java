package com.slic.travelapp.models;

/**
 * Created by seanlim on 6/11/2015.
 */
public class Weather {
    private String id;
    private String icon;
    private String main;
    private String description;

    public String getId() {
        return id;
    }

    public String getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    public String getMain() {
        return main;
    }
    @Override
    public String toString()
    {
        return "ClassPojo [id = "+id+", icon = "+icon+", description = "+description+", main = "+main+"]";
    }

}