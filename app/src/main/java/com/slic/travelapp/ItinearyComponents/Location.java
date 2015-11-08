package com.slic.travelapp.ItinearyComponents;
/**
 * Created by Jiayu on 05/11/2015.
 */
public class Location {
    static int count = 0;
    int id;
    String name;
    String formattedName;

    public Location(String formattedName) {
        this.id = count++;
        this.formattedName = formattedName;
        this.name = formattedName.trim().toLowerCase().replace(" ", "_");
    }

    @Override
    public String toString() {
        return formattedName;
    }
}
