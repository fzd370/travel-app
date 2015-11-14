package com.slic.travelapp.ItinearyComponents;

import android.os.Parcel;
import android.os.Parcelable;

public class ItineraryItem implements Parcelable {
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

    protected ItineraryItem(Parcel in) {
        directions = in.readString();
        details = in.readString();
        destination = in.readString();
    }

    public static final Creator<ItineraryItem> CREATOR = new Creator<ItineraryItem>() {
        @Override
        public ItineraryItem createFromParcel(Parcel in) {
            return new ItineraryItem(in);
        }

        @Override
        public ItineraryItem[] newArray(int size) {
            return new ItineraryItem[size];
        }
    };

    @Override
    public String toString() {
        return this.directions + " " + this.details;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(directions);
        dest.writeString(details);
        dest.writeString(destination);
    }
}