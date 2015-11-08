package com.slic.travelapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/* Travel App
* Things to Accomplish                      Status
* 1) Itineary Planner                       0%
* 2) Search autocorrect                     0%
* 3) Additional Function(Uber,Feedback)     0%
*
* Main Activity, simply as a holder for the various fragments and certain functions
* Callable Functions: Uber(Additional Function), Search Widget(Function 2)
*
* */

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        ActivityCompat.OnRequestPermissionsResultCallback{

    private static final int REQUEST_LOCATION = 1;
    private static final String FILE_PATH = "location_cache.dat";
    private static final String CLIENT_ID = "475054250915-pjo5gu01j8hjc2dnab0qg4prra4ocljc.apps.googleusercontent.com";
    public FloatingActionButton fab;
    private static LatLng srcLoc = null;
    private static LatLng destLoc = null;
    private static String destName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        checkSelfPermission();

        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.hide();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Shall we uber over?", Snackbar.LENGTH_LONG)
                        .setAction("Go!", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getTaxi();
                            }
                        }).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



        //launchFragment(R.id.nav_home); // Initialize screen to the choosen fragment
    }

    private void checkSelfPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
            Toast.makeText(this, "Need permission for maps to work properly", Toast.LENGTH_LONG).show();
            checkSelfPermission();
            shout("Denied in onRequest");
        } else {
            // permission has been granted, continue as usual
//            Location myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            shout("Granted in SelfCheck");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {
            if(grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // We can now safely use the API we requested access to
//                Location myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                shout("Granted in onRequest");
            } else {
                Toast.makeText(this, "Need permission for maps to work properly", Toast.LENGTH_LONG).show();
                shout("Denied in onRequest");
                // Permission was denied or request was cancelled
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.action_settings).setVisible(false); // Disable setting menu

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        launchFragment(item.getItemId());
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void launchFragment(int id) {
        Fragment fragment = null;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Bundle bundle = new Bundle();

        // Handle navigation view item clicks here.
        fab.hide();
        if (id == R.id.nav_home) {
            bundle.putStringArrayList("LOCLIST", retrieveLocation());
            fragment = new MapsFragment();
            fragment.setArguments(bundle);
        } else if (id == R.id.nav_plan) {
            fragment = new PlanFragment();
        } else if (id == R.id.nav_find) {
            fragment = new FindFragment();
        } else if (id == R.id.nav_item) {
            fragment = new ItemsFragment();
        } else if (id == R.id.nav_share) {
        } else if (id == R.id.nav_send) {
        }

        if (fragment != null) {
            fragmentTransaction.replace(R.id.container_body, fragment);
            fragmentTransaction.commit();
        }
    }

    public static ArrayList<String> retrieveLocation() {
        ArrayList<String> locationList = new ArrayList<String>();

        StringBuilder sb = new StringBuilder();
        try{
            File f = new File(FILE_PATH);
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            String line;
            while((line = reader.readLine()) != null) {
                //System.out.println(line);
                sb.append(line+"\n");
            }
            for(String loc: sb.toString().split("\n"))
                locationList.add(loc);
        } catch(Exception e) {
            e.printStackTrace();
            locationList.add("Sentosa");
            locationList.add("Woodlands Singapore");
            locationList.add("Orchard Singapore");
            locationList.add("Changi Village Singapore");
        }
        return locationList;
    }

    public void setSrc(LatLng loc){
        srcLoc = loc;
        shout("SRC: " + srcLoc.toString());
    }
    public void setDest(LatLng loc){
        destLoc = loc;
        shout("DEST: " + destLoc.toString());
    }
    public void setName(String s) {
        destName = s;
        shout("Name: " + destName.toString());
    }
    public void clearLoc(){
        srcLoc = new LatLng(0, 0);
        destLoc = new LatLng(0, 0);
        destName = "";
        shout("SRC: " + srcLoc.toString());
        shout("DEST: " + destLoc.toString());
        shout("Name: " + destName.toString());
    }

    public void getTaxi() {
        //uber://?client_id=YOUR_CLIENT_ID&action=setPickup&pickup[latitude]=37.775818&pickup[longitude]=-122.418028&pickup[nickname]=UberHQ&pickup[formatted_address]=1455%20Market%20St%2C%20San%20Francisco%2C%20CA%2094103&dropoff[latitude]=37.802374&dropoff[longitude]=-122.405818&dropoff[nickname]=Coit%20Tower&dropoff[formatted_address]=1%20Telegraph%20Hill%20Blvd%2C%20San%20Francisco%2C%20CA%2094133&product_id=a1111c8c-c720-46c3-8534-2fcdd730040d
        String uberUri = "uber://?client_id=" + CLIENT_ID + "&action=setPickup" +
                //"&pickup[latitude]=" + srcLoc.latitude +
                //"&pickup[longitude]=" + srcLoc.longitude +
                "&pickup=my_location" +
                "&dropoff[latitude]=" + destLoc.latitude +
                "&dropoff[longitude]=" + destLoc.longitude +
                "&dropoff[nickname]=" + destName;

        try {
            PackageManager pm = this.getPackageManager();
            pm.getPackageInfo("com.ubercab", PackageManager.GET_ACTIVITIES);
            //String uri = "uber://?action=setPickup&pickup=my_location&client_id=" + CLIENT_ID;

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(uberUri));
            startActivity(intent);
        } catch (PackageManager.NameNotFoundException e) {
            // No Uber app! Open mobile website.
            String url = "https://m.uber.com/sign-up?client_id=" + CLIENT_ID;
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }
    }

    private void shout(String s) {
        Log.d("SLIC", s);
    }


    public void doSearch(String query){
        shout("SEARCH: " + query);
    }
    public void getPlace(String query){
        shout("PLACE: " + query);
    }
    private void showLocations(Cursor c){
        shout("SHOW LOCATIONS!");
    }
}
