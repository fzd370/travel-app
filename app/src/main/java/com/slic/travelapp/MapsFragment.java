package com.slic.travelapp;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**  Map Fragment
 *  Upon launching, it should be fed the relavent information for ploting of the markers.
 *  Markers plotted will be ordered with the name of the place - e.g "1. Orchard" to signify the first place to go
 *  Click of a marker will make appear the Uber Floating Button from Main Activity
 */
public class MapsFragment extends Fragment implements
        OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener{
    private GoogleMap mMap;
    private static ArrayList<LatLng> geoList;
    private static ArrayList<String> locationList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_maps, container, false);

        Bundle bundle = this.getArguments();
        try{
            if(bundle != null) {
                locationList = bundle.getStringArrayList("LOCLIST"); // Receives Location List that is read from file while in the Main Activity

                // POTENTIAL BUG - SHOULD INITTASK BE MOVED INTO onMapReady() ??
                new InitTask().execute(locationList, null, null);    // Parse information into Initializer(String -> GeoCode -> Place markers)
                //geoList = getLoc(locationList);
            }
        } catch (Exception e) {
            e.printStackTrace();
            shout("Init failed");
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        return rootView;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Reqeusts permission - DOES THIS EVEN WORK?
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            Toast.makeText(getContext(), "NEED PERMISSION", Toast.LENGTH_LONG).show();
        }

        mMap.setOnMarkerClickListener(this);              // Used to show Taxi Button
        mMap.setOnMapClickListener(this);                 // Used to hide Taxi Button
        mMap.getUiSettings().setMapToolbarEnabled(false); // Disables (button-right)button that is used to open Google Maps

    }

    // Called by Init
    // Translated String to Geocode(Input : List of Location names)
    public ArrayList<LatLng> getLoc(ArrayList<String> locationList) {
        Geocoder myGcdr = new Geocoder(getContext());
        ArrayList<List<Address>> matchedList = new ArrayList<List<Address>>();
        ArrayList<LatLng> geoList = new ArrayList<LatLng>();
        shout("LOCLIST : " + locationList.toString());
        try {
            for(int i = 0; i < locationList.size(); i++) {
                List<Address> matched = myGcdr.getFromLocationName(locationList.get(i), 1);
                matchedList.add(matched);
            }
            shout("MATCHLIST : " + matchedList.toString());
        } catch (IOException e) {
            Toast.makeText(getContext(), "Not able to find location", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Geocoder Exception", Toast.LENGTH_SHORT).show();
        }

        try {
            for(int i = 0; i < matchedList.size(); i++) {
                double lat = matchedList.get(i).get(0).getLatitude();
                double lon = matchedList.get(i).get(0).getLongitude();
                LatLng loc = new LatLng(lat, lon);
                geoList.add(loc);
            }
            shout("GEOLIST : " + geoList.toString());
        } catch (Exception e) {
            Toast.makeText(getContext(), "A problem occured in retriving location", Toast.LENGTH_SHORT).show();
        }
        return geoList;
    }

    // Called by Init
    // Call to automatically place the markers, to call
    private void placeMarkers() {
        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        //Start building the bounds
        LatLngBounds.Builder bound = LatLngBounds.builder();

        // Draw markers and add points into bound builder
        try {
            shout("GEOLIST2 : " + geoList.toString());
            for(int i = 0; i<geoList.size(); i++) {
                //Draw a marker
                drawMarker(geoList.get(i), String.valueOf(i+1) + ". " + locationList.get(i)); // #Order of locations are included
                //Add a point to the bound
                bound.include(geoList.get(i));
            }
            //Build the bound and zoom to it
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bound.build().getCenter(), 11));
        } catch (IllegalStateException e) {
            Toast.makeText(getContext(), "Unable to build bound", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Fail to place marker", Toast.LENGTH_SHORT).show();
        }
    }

    // Called by placeMarkers
    // Sets the neccesarry params for individual marker
    private void drawMarker(LatLng point, String name) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point).title(name);
        mMap.addMarker(markerOptions);
        shout("Draw @ " + point.toString());
    }

    @Override
    public boolean onMyLocationButtonClick() {
        LatLng loc = getCurrentLocation();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 11));
        return false;
    }

    public LatLng getCurrentLocation(){
        try {
            Location location = mMap.getMyLocation();
            return new LatLng(location.getLatitude(), location.getLongitude());
        } catch (Exception e) {
            e.printStackTrace();
            shout("Unable to get Current Location. Defaulting to SoMaPaH");
            return new LatLng(1.340103, 103.962955);
        }
    }

    // Removes Indexing from title to feed into Uber API, as a name for the Drop-Off Point
    private String cleanTitle(String title) {
        int space = title.indexOf(" ");
        if(space+1 < title.length())
            return title.substring(space+1);
        else {
            shout("WHAT's WRONG WITH THE TITLE?");
            return "Destination " + title.substring(0, space);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        ((MainActivity)getActivity()).clearLoc();
        ((MainActivity)getActivity()).setSrc(getCurrentLocation()); // Not in effect, Uber API has it's own get currentLocation
        ((MainActivity)getActivity()).setDest(marker.getPosition());
        ((MainActivity)getActivity()).setName(cleanTitle(marker.getTitle()));
        ((MainActivity)getActivity()).fab.show(); //getTaxi(getSrcLoc(), destLoc);
        return false;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        ((MainActivity)getActivity()).fab.hide();
    }

    private class InitTask extends AsyncTask<ArrayList<String>, Integer, Integer> {
        @Override
        protected Integer doInBackground(ArrayList<String>... params) {
            geoList = getLoc(params[0]);
            return null;
        }
        @Override
        protected void onPostExecute(Integer integer) {
            try {
                placeMarkers();
            } catch (Exception e){
                e.printStackTrace();
                shout("Unable to place markers on post execute");
            }
        }
    }

    private void shout(String s) {
        Log.d("SLIC", s);
    }
}
