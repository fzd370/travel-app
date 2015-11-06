package com.slic.travelapp;

import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import com.google.android.gms.maps.model.LatLng;
import com.slic.travelapp.service.PlaceProvider;

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
        NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

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

        initSearchView();
        handleIntent(getIntent());

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

    public void initSearchView() {
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) findViewById(R.id.searchView);
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true); // Do not iconify the widget; expand it by default
    }

    public void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doSearch(query);
        }else if(intent.getAction().equals(Intent.ACTION_VIEW)){
            String query = intent.getStringExtra(SearchManager.EXTRA_DATA_KEY);
            getPlace(query);
        }
    }
    public void doSearch(String query){
        shout("SEARCH: " + query);
        Bundle data = new Bundle();
        data.putString("query", query);
        getLoaderManager().restartLoader(0, data, this);
    }
    public void getPlace(String query){
        shout("PLACE: " + query);
        Bundle data = new Bundle();
        data.putString("query", query);
        getLoaderManager().restartLoader(1, data, this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
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

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle query) {
        CursorLoader cLoader = null;
        if(arg0==0)
            cLoader = new CursorLoader(getBaseContext(), PlaceProvider.SEARCH_URI, null, null, new String[]{ query.getString("query") }, null);
        else if(arg0==1)
            cLoader = new CursorLoader(getBaseContext(), PlaceProvider.DETAILS_URI, null, null, new String[]{ query.getString("query") }, null);
        return cLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        showLocations(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void showLocations(Cursor c){
        shout("SHOW LOCATIONS!");
    }
}
