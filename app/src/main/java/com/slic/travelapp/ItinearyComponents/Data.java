package com.slic.travelapp.ItinearyComponents;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Jiayu on 05/11/2015.
 */
public class Data {
    static ArrayList<String> attractions = new ArrayList<>();

    static {
        Collections.addAll(attractions, "Marina Bay Sands", "Singapore Flyer", "Vivo City", "Buddha Tooth Relic Temple", "Zoo");
    }
}
