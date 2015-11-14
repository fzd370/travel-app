package com.slic.travelapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**  Map Fragment
 *  Upon launching, it should be fed the relavent information for ploting of the markers.
 *  Markers plotted will be ordered with the name of the place - e.g "1. Orchard" to signify the first place to go
 *  Click of a marker will make appear the Uber Floating Button from Main Activity
 */
public class MapsFragment extends Fragment implements
        OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        View.OnClickListener
{
    private static boolean DEBUG_MODE = false;

    private GoogleMap mMap;
    private Geocoder myGcdr;

    private static Marker lastMarker = null;
    private static ArrayList<LatLng> geoList;
    private static ArrayList<String> locationList;
    private static SpellChecker spellChecker;
    private static String locationReplace;
    private EditText inputSearch;
    private Button buttonSearch;
    private LinearLayout barSearch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_maps, container, false);
        Bundle bundle = this.getArguments();
        try{
            if(bundle != null) {
                locationList = bundle.getStringArrayList("LOCLIST"); // Receives Location List that is read from file while in the Main Activity
                if(locationList == null) {
                    locationList = new ArrayList<String>();
                } else{
                    locationList.remove(locationList.size()-1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            shout("Get from bundle Fail");
        }

        spellChecker = new SpellChecker();

        inputSearch = (EditText) rootView.findViewById(R.id.input_search);
        buttonSearch = (Button) rootView.findViewById(R.id.button_search);
        buttonSearch.setOnClickListener(this);
        barSearch = (LinearLayout) rootView.findViewById(R.id.bar_search);

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
            Toast.makeText(getContext(), "NEED PERMISSION FOR MAPS TO FUNCTION CORRECTLY", Toast.LENGTH_LONG).show();
        }

        mMap.setOnMarkerClickListener(this);              // Used to show Taxi Button
        mMap.setOnMapClickListener(this);                 // Used to hide Taxi Button
        mMap.getUiSettings().setMapToolbarEnabled(false); // Disables (button-right)button that is used to open Google Maps
        onMyLocationButtonClick();                        // Defaults current view to user's location

        // POTENTIAL BUG - SHOULD INITTASK BE MOVED INTO onMapReady() ??1
        new InitTask().execute(locationList, null, null);    // Parse information into Initializer(String -> GeoCode -> Place markers)
    }

    // Called by Init
    // Translated String to Geocode(Input : List of Location names)
    public ArrayList<LatLng> getLoc(ArrayList<String> locationList) {
        myGcdr = new Geocoder(getContext());
        ArrayList<List<Address>> matchedList = new ArrayList<List<Address>>();
        ArrayList<LatLng> geoList = new ArrayList<LatLng>();
        shout("LOCLIST : " + locationList.toString());
        try {
            for(int i = 0; i < locationList.size(); i++) {
                List<Address> matched = myGcdr.getFromLocationName("Singapore " + locationList.get(i), 1);
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

    public LatLng getLatLng(String locationName) {
        LatLng loc = null;
        try {
            List<Address> matches = myGcdr.getFromLocationName("Singapore " +locationName, 1);
            double lat = matches.get(0).getLatitude();
            double lon = matches.get(0).getLongitude();
            loc = new LatLng(lat, lon);
        } catch (IOException e) {
            e.printStackTrace();
            shout("Problem in getLngLat");
        }
        return loc;
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

        if(geoList.size() <= 0 || locationList.size() <= 0){
            shout("Geo Size or Location size <= Zero");
            return;
        }

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

        lastMarker = mMap.addMarker(markerOptions);
        shout("Draw @ " + point.toString());
    }

    public static void toggleMarkers(){
        if(lastMarker!=null){
            if(lastMarker.isInfoWindowShown())
                lastMarker.hideInfoWindow();
            else
                lastMarker.showInfoWindow();
        }
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
            shout("Unable to get Current Location. Defaulting to SUTD");
            return new LatLng(1.340103, 103.962955);
        }
    }

    // Removes Indexing from title to feed into Uber API, as a name for the Drop-Off Point
    private String cleanTitle(String title) {
        if(title.length() <= 0)
            return "Destination";
        if(title.charAt(0) < '0' || title.charAt(0) > '9')
            return title;
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

        barSearch.setVisibility(View.GONE);
        hideKeyboard();
        return false;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        ((MainActivity)getActivity()).fab.hide();
        barSearch.setVisibility(View.VISIBLE);
        hideKeyboard();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.button_search) {
            hideKeyboard();
            ((MainActivity)getActivity()).showMarkerMenuItem();
            ArrayList<String> inputList = new ArrayList<String>();
            String inputText = inputSearch.getText().toString();
            if(inputText.length()>0){
                inputList.add(inputText.toString());
                new CheckTask().execute(inputList, null, null);
            }
        }
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

    private class CheckTask extends AsyncTask<ArrayList<String>, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            getActivity().findViewById(R.id.grey_screen).setVisibility(View.VISIBLE);
            getActivity().findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        }

        @Override
        protected Integer doInBackground(ArrayList<String>... params) {
            // Enable loading bar

            String locationInput = params[0].get(0);
            try{
                locationReplace = spellChecker.spellcorrector(locationInput);
            }catch (Exception e){
                e.printStackTrace();
                shout("Spellcheck Corrector failed");
            }

            if(locationReplace == null) {
                shout("No match found!");
                locationReplace = locationInput;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Integer integer) {
            try {
                mMap.clear();
                shout(locationReplace);
                LatLng loc = getLatLng(locationReplace);
                drawMarker(loc, locationReplace);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 11));
                inputSearch.setText(locationReplace);
                shout("CheckTask Sucess");
            } catch (Exception e){
                e.printStackTrace();
                shout("CheckTask Fail");
            } finally {
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE); // Disable user's touch
                getActivity().findViewById(R.id.grey_screen).setVisibility(View.GONE);               // Set grey background
                getActivity().findViewById(R.id.progressBar).setVisibility(View.GONE);               // Bring up progress bar
            }
        }
    }

    private static void shout(String s) {
        if(DEBUG_MODE) Log.d("SLIC", s);
    }

    protected void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static class SpellChecker {

        private static final boolean ALLOW_TIMEOUT = false;
        private static final int MS_TIMEOUT = 5000;
        private long started, time;
        public List<String> locations = null;
        public boolean isDone = false;

        public SpellChecker() {
            this.locations = locationGenerator();
        }

        public static List<String> locationGenerator(){
            List<String> itemList = new ArrayList<String>();
            itemList.add("Marina Bay Sands");
            itemList.add("Singapore Flyer");
            itemList.add("Vivo City");
            itemList.add("Resorts World Sentosa");
            itemList.add("Buddha Tooth Relic Temple");
            itemList.add("Zoo");
            return itemList;
        }
        //Takes in a word
        //returns list of possible words with edit distance 1
        public List<String> typos1(String s){

            List<String> typos1 = new ArrayList<String>();

            //missing letter typo eg: Sentosa -> sntosa
            for (int i = 0; i < s.length(); i++){
                typos1.add(s.substring(0,i) + s.substring(i+1));
            }

            //misplaced letter typo eg: Sentosa -> snetosa
            for (int i = 0; i < s.length()-1; i++){
                typos1.add(s.substring(0,i) + s.substring(i+1,i+2) + s.substring(i, i+1) + s.substring(i+2));
            }

            //extra letter typo eg: Sentosa -> sentossa
            for (int i=0; i < s.length()+1; i++){
                for (char c = 'a'; c <= 'z'; c++){
                    typos1.add(s.substring(0,i) + String.valueOf(c) + s.substring(i));
                }
            }

            //wrong letter typo eg Sentosa -> swntosa
            for (int i=0; i < s.length(); i++){
                for (char c = 'a'; c <= 'z'; c++){
                    typos1.add(s.substring(0,i) + String.valueOf(c) + s.substring(i+1));
                }
            }

            return typos1;
        }

        //takes in list of words and returns list of words with typos of edit distance 2
        public List<String> typos2(List<String> list){
            List<String> typos2 = new ArrayList<String>();
            for (String s : list){
                typos2.addAll(typos1(s));
            }
            return typos2;
        }

        public boolean checkTimeout(){
            if(ALLOW_TIMEOUT){
                time = System.currentTimeMillis();
                long timeTaken= time - started;
                if(time - started >= MS_TIMEOUT) {
                    Log.d("SLIC", "Timeout : " + timeTaken);
                    isDone = true;
                    return true;
                }
            }
            return false;
        }

        public String spellcorrector(String word){
            isDone = false;
            started = System.currentTimeMillis();
            word = word.replaceAll(" ", "");
            word = word.toLowerCase();
            //locationGenerator();
            List<String> locations1 = new ArrayList<String>();
            for (int i = 0; i < locations.size(); i++){
                String loc = locations.get(i).replaceAll(" ","");
                locations1.add(loc.toLowerCase());
                if(checkTimeout()) {
                    Log.d("SLIC", "Check Time 1");
                    return null;
                }
            }
            if (locations1.contains(word)){return locations.get(locations1.indexOf(word));}
            List<String> possiblewords1 = typos1(word);
            for (String s : possiblewords1){
                Pattern p = Pattern.compile(s);
                for (String location: locations1){
                    Matcher m = p.matcher(location);
                    if (m.find()){
                        return locations.get(locations1.indexOf(location));
                    }
                }
                if(checkTimeout()) {
                    Log.d("SLIC", "Check Time 2");
                    return null;
                }
            }
            List<String> possiblewords2 = typos2(possiblewords1);
            for (String s : possiblewords2){
                Pattern p = Pattern.compile(s);
                for (String location: locations1){
                    Matcher m = p.matcher(location);
                    if (m.find()){
                        return locations.get(locations1.indexOf(location));
                    }
                }
                if(checkTimeout()) {
                    Log.d("SLIC", "Check Time 3");
                    return null;
                }
            }
            isDone = true;
            return null;
        }
    }

}
