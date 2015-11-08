package com.slic.travelapp.ItinearyComponents;
/**
 * Created by Jiayu on 06/11/2015.
 */
public class RouteInfoKey {
    TransportMode transportMode;
    String origin;
    String destination;

    public RouteInfoKey(TransportMode transportMode, String origin, String destination) {
        this.transportMode = transportMode;
        this.origin = origin;
        this.destination = destination;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RouteInfoKey that = (RouteInfoKey) o;

        if (transportMode != that.transportMode) return false;
        if (!origin.equals(that.origin)) return false;
        return destination.equals(that.destination);

    }

    @Override
    public int hashCode() {
        int result = transportMode.hashCode();
        result = 31 * result + origin.hashCode();
        result = 31 * result + destination.hashCode();
        return result;
    }
}
