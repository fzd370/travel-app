package com.slic.travelapp.ItinearyComponents;

public class ItineraryItem {
    String directions;
    String details;
    String destination;

    ItineraryItem(String heading, String subheading) {
        directions = heading;
        details = subheading;
    }

    ItineraryItem(TransportMode transportMode, String destination, int cost, int time) {
        this.destination = destination;
        details = "$" + cost / 100 + "." + cost % 100 + ", " + time + " minutes";
        switch (transportMode) {
            case TAXI:
                directions = "Take a taxi to ";
                break;
            case PUBLIC_TRANSPORT:
                directions = "Take public transport to ";
                break;
            case FOOT:
                directions = "Walk to ";
                details = time + " minutes";
                break;
        }
        directions += destination;
    }

    ItineraryItem(TransportMode transportMode, String destination, int cost, int time, boolean lastStop) {
        if (lastStop) {
            this.destination = destination;
            details = "$" + cost / 100 + "." + cost % 100 + ", " + time + " minutes";
            switch (transportMode) {
                case TAXI:
                    directions = "Take a taxi back to ";
                    break;
                case PUBLIC_TRANSPORT:
                    directions = "Take public transport back to ";
                    break;
                case FOOT:
                    directions = "Walk back to ";
                    details = time + " minutes";
                    break;
            }
            directions += destination;
        }
    }

    @Override
    public String toString() {
        return this.directions + " " + this.details;
    }
}