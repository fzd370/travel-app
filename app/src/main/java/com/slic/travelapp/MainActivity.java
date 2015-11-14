package com.slic.travelapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.design.internal.NavigationMenuPresenter;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.slic.travelapp.ItinearyComponents.AttractionsFragment;
import com.slic.travelapp.ItinearyComponents.BudgetFragment;
import com.slic.travelapp.ItinearyComponents.ItineraryFragment;
import com.slic.travelapp.ItinearyComponents.ItineraryItem;
import com.slic.travelapp.ItinearyComponents.Routes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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
        ActivityCompat.OnRequestPermissionsResultCallback,
        BudgetFragment.OnFragmentInteractionListener,
        AttractionsFragment.OnFragmentInteractionListener,
        ItineraryFragment.OnFragmentInteractionListener {

    private static final int REQUEST_LOCATION = 1;
    private static final String FILE_PATH = "location_cache.dat";
    private static final String CLIENT_ID = "475054250915-pjo5gu01j8hjc2dnab0qg4prra4ocljc.apps.googleusercontent.com";
    private static boolean shownItemAlert = false;
    private static boolean shownMapAlert = false;
    private static boolean notificationTriggered = false;
    private static boolean notificationShow = false;
    public FloatingActionButton fab;
    private static LatLng srcLoc = null;
    private static LatLng destLoc = null;
    private static String destName = null;

    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private BudgetFragment budgetFragment;
    private AttractionsFragment attractionsFragment;
    private ItineraryFragment itineraryFragment;

    // state variables
    private int budget;
    private String hotel;
    private boolean itineraryExhaustiveEnumeration;
    private ArrayList<String> checkedAttractions;
    private ArrayList<String> itineraryDestinations;
    private ArrayList<ItineraryItem> savedItinerary;
    public static ArrayList<String> itemList = new ArrayList<String>();

    private static NavigationView navigationView = null;
    private static Menu menu = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            savedItinerary = savedInstanceState.getParcelableArrayList("ITINERARY");
            notificationShow = savedInstanceState.getBoolean("NOTIFICATION");
            shout("SAVEDItinerary: " + savedItinerary.toString());
            shout("SAVEDNotification: " + String.valueOf(notificationShow));
        } else {
            shout("No saved state");
        }
        notificationTriggered = false;

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        checkSelfPermission();

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
        findViewById(R.id.progressBar).setVisibility(View.GONE);    // Disable Progress bar

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        drawer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
//                findViewById(R.id.drawer_layout).requestFocus();
                return false;
            }
        });
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.view_container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        try (InputStream fileInputStream = getResources().openRawResource(R.raw.attractions)) {
            Routes.generateRoutes(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        launchFragment(R.id.nav_home); // Initialize screen to the choosen fragment
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); // Prevents keyboard froum auto launching
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
            if (grantResults.length == 1
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
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("ITINERARY", savedItinerary);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        savePreference();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPreference();
    }

    public void savePreference() {
        SharedPreferences.Editor editor =PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putBoolean("NOTIFICATION", notificationShow);
        editor.commit();
        shout("Notification: " + notificationShow);
    }
    public void loadPreference(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs != null) {
            notificationShow = prefs.getBoolean("NOTIFICATION", false);
            shout("Notification: " + notificationShow);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        this.menu = menu;
        hideAllMenuItem();
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
        } else if (id == R.id.action_delete) {
            itemList.clear();
            ItemsFragment.updateSet();
        } else if (id == R.id.action_marker) {
            MapsFragment.toggleMarkers();
        }

        return super.onOptionsItemSelected(item);
    }

    protected void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        launchFragment(item.getItemId());
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        hideKeyboard();
        return true;
    }

    public void launchFragment(int id) {
        Fragment fragment = null;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Bundle bundle = new Bundle();

        // Handle navigation view item clicks here.
        fab.hide();
        if (menu != null) {
            hideAllMenuItem();
            //shout("Menu updated");
        }

        if (id == R.id.nav_home) {
//            bundle.putStringArrayList("LOCLIST", retrieveLocation());
            hideMapNotification();
            bundle.putStringArrayList("LOCLIST", itineraryDestinations);
            fragment = new MapsFragment();
            fragment.setArguments(bundle);
        } else if (id == R.id.nav_plan) {
            // Hides fragment layers, Show Tab and Pager
            findViewById(R.id.tabs).setVisibility(View.VISIBLE);
            findViewById(R.id.view_container).setVisibility(View.VISIBLE);
            findViewById(R.id.fragment_container).setVisibility(View.GONE);

        }
        else if (id == R.id.nav_item) {
            fragment = new ItemsFragment();
            if (menu != null) {
                showDeleteMenuItem();
                //shout("Menu updated");
            }
        } else if (id == R.id.nav_contacts) {
            startContacts();
        } else if (id == R.id.nav_feedback) {
            startFeedback();
        }

        if (fragment != null) {
            // Hides the Tab and viewPager, Show fragment layer
            findViewById(R.id.tabs).setVisibility(View.GONE);
            findViewById(R.id.appbar).setVisibility(View.VISIBLE);
            findViewById(R.id.view_container).setVisibility(View.GONE);
            findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
            fragmentTransaction.replace(R.id.container_body, fragment);
            fragmentTransaction.commit();
        }
    }

    public void setSrc(LatLng loc) {
        srcLoc = loc;
        shout("SRC: " + srcLoc.toString());
    }

    public void setDest(LatLng loc) {
        destLoc = loc;
        shout("DEST: " + destLoc.toString());
    }

    public void setName(String s) {
        destName = s;
        shout("Name: " + destName.toString());
    }

    public void clearLoc() {
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

    public void startFeedback() {
        Intent intent = new Intent(this, FeedbackActivity.class);
        startActivity(intent);
    }

    public void startContacts() {
       // Toast.makeText(this, "Contacts Activity Started", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, ContactsActivity.class);
        startActivity(intent);
    }

    public void showItemAlert() {
        if(!shownItemAlert){
            AlertDialog.Builder alertDialogueBuilder = new AlertDialog.Builder(this);
            alertDialogueBuilder//.setTitle("Alert")
                    .setMessage("Press and hold on an item to remove it.")
                    .setPositiveButton("Got it!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            shownItemAlert = true;
            alertDialogueBuilder.create().show();
        }
    }

    public void showMapAlert() {
        if(!shownMapAlert) {
            AlertDialog.Builder alertDialogueBuilder = new AlertDialog.Builder(this);
            alertDialogueBuilder.setTitle("Alert")
                    .setMessage("Location markers placed into map")
                    .setNegativeButton("Got it!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            alertDialogueBuilder.create().show();
            shownMapAlert = true;
        }
    }
    public void showMapNotification(){
        navigationView.getMenu().getItem(0).setActionView(R.layout.item_menu_home);
        notificationShow = true;
    }
    public void hideMapNotification(){
        navigationView.getMenu().getItem(0).setActionView(R.layout.item_menu_blank);
        notificationShow = false;
    }

    public void hideAllMenuItem() {
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_delete).setVisible(false);
        menu.findItem(R.id.action_marker).setVisible(false);
    }

    public void showMarkerMenuItem() {
        menu.findItem(R.id.action_marker).setVisible(true);
    }

    public void showDeleteMenuItem() {
        menu.findItem(R.id.action_delete).setVisible(true);
    }

    private void shout(String s) {
        Log.d("SLIC", s);
    }

    @Override
    public void onAttractionsSelected(ArrayList<String> selectedAttractions) {
        Log.i("MainActivity", "Attraction selected");
        if (itineraryFragment != null) {
            itineraryFragment.updateItinerary(selectedAttractions);
            if(notificationTriggered || notificationShow) showMapNotification();
            notificationTriggered = true;
        }
    }

    @Override
    public void onBudgetUpdated(int budget) {
        Log.i("MainActivity", "Budget updated: " + String.valueOf(budget));
        this.budget = budget;
        if (itineraryFragment != null) {
            itineraryFragment.updateItinerary(budget);
        }
    }

    @Override
    public void onHotelUpdated(String hotel) {
        Log.i("MainActivity", "Hotel updated to " + hotel);
        this.hotel = hotel;
        if (attractionsFragment != null) {
            attractionsFragment.updateHotel(hotel);
        }
        if (itineraryFragment != null) {
            itineraryFragment.updateItinerary(hotel);
        }
    }

    @Override
    public void onSearchModeUpdated(boolean exhaustiveMode) {
        Log.i("MainActivity", "Exhaustive mode set to " + String.valueOf(exhaustiveMode));
        this.itineraryExhaustiveEnumeration = exhaustiveMode;
        if (itineraryFragment != null) {
            itineraryFragment.updateItinerary(exhaustiveMode);
        }
    }

    @Override
    public void getItineraryDestinations(ArrayList<String> itineraryDestinations) {
        this.itineraryDestinations = itineraryDestinations;
    }

    @Override
    public void updateSavedItinerary(ArrayList<ItineraryItem> route) {
        this.savedItinerary = route;
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final FragmentManager mFragmentManager;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragmentManager = fm;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            switch (position) {
                case 0:
                    return BudgetFragment.newInstance();
                case 1:
                    return AttractionsFragment.newInstance();
                case 2:
                    return ItineraryFragment.newInstance();
                default:
                    // Return a PlaceholderFragment (defined as a static inner class below).
                    return PlaceholderFragment.newInstance(position + 1);
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
            // save the appropriate reference depending on position
            switch (position) {
                case 0:
                    budgetFragment = (BudgetFragment) createdFragment;
                    Log.i("instantiateItem", "Created BudgetFragment");
                    break;
                case 1:
                    attractionsFragment = (AttractionsFragment) createdFragment;
                    Log.i("instantiateItem", "Created AttractionsFragment");
                    break;
                case 2:
                    itineraryFragment = (ItineraryFragment) createdFragment;
                    Log.i("instantiateItem", "Created ItineraryFragment");
            }
            return createdFragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "BUDGET";
                case 1:
                    return "ATTRACTIONS";
                case 2:
                    return "ITINERARY";
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }
}
