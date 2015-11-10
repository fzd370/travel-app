package com.slic.travelapp.ItinearyComponents;

/**
 * Created by Jiayu on 06/11/2015.
 */
public enum TransportMode {
    PUBLIC_TRANSPORT, TAXI, FOOT;

    static TransportMode getTransportMode(String transportMode) {
        switch (transportMode) {
            case "taxi":
                return TAXI;
            case "pt":
                return PUBLIC_TRANSPORT;
            case "foot":
                return FOOT;
            default:
                return TAXI;
        }
    }
}