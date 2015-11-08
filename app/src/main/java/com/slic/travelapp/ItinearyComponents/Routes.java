package com.slic.travelapp.ItinearyComponents;
import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class Routes {
    static HashMap<RouteInfoKey, RouteInfo> routes = new HashMap<>();

    static int getCost(TransportMode transportMode, String origin, String destination) {
//        Log.i("getCost", transportMode + origin + destination);
        return Routes.routes.get(new RouteInfoKey(transportMode, origin, destination)).cost;
    }

    static int getTiming(TransportMode transportMode, String origin, String destination) {
//        Log.i("getTiming", transportMode + origin + destination);
        return Routes.routes.get(new RouteInfoKey(transportMode, origin, destination)).time;
    }

    static void generateRoutes(InputStream jsonInputStream) throws IOException {
        JsonReader jsonReader = new JsonReader(new InputStreamReader(jsonInputStream, "UTF-8"));
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String tm = jsonReader.nextName();
            TransportMode transportMode = TransportMode.getTransportMode(tm);
            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                String origin = jsonReader.nextName();
                jsonReader.beginObject();
                while (jsonReader.hasNext()) {
                    String destination = jsonReader.nextName();
                    jsonReader.beginObject();
                    while (jsonReader.hasNext()) {
                        jsonReader.skipValue();
                        int cost = jsonReader.nextInt();
                        jsonReader.skipValue();
                        int time = jsonReader.nextInt();
                        Routes.routes.put(new RouteInfoKey(transportMode, origin, destination), new RouteInfo(time, cost));
                    }
                    jsonReader.endObject();
                }
                jsonReader.endObject();
            }
            jsonReader.endObject();
        }
        jsonReader.endObject();
    }
}
