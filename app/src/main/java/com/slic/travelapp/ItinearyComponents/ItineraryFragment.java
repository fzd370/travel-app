package com.slic.travelapp.ItinearyComponents;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.slic.travelapp.MainActivity;
import com.slic.travelapp.R;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class ItineraryFragment extends ListFragment {

    private OnFragmentInteractionListener mListener;

    private ArrayList<ItineraryItem> itinerary;
    private ItineraryAdapter itineraryAdapter;

    private int budget;
    private String hotel;
    private ArrayList<String> destinations;
    private boolean exhaustiveMode;
    private ArrayList<ItineraryItem> route;


    public static ItineraryFragment newInstance() {
        ItineraryFragment fragment = new ItineraryFragment();
//        Bundle args = new Bundle();
//        args.putInt("budget", budget);
//        args.putString("hotel", hotel);
//        args.putBoolean("exhaustiveMode", exhaustiveMode);
//        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItineraryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

//        if (getArguments() != null) {
//            budget = getArguments().getInt("budget");
//            hotel = getArguments().getString("hotel");
//            exhaustiveMode = getArguments().getBoolean("exhaustiveMode");
//        }

        super.onCreate(savedInstanceState);
        destinations = new ArrayList<>();
        itinerary = new ArrayList<>();
        itineraryAdapter = new ItineraryAdapter(getActivity(), itinerary);
        setListAdapter(itineraryAdapter);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View listLayout = inflater.inflate(R.layout.list_content, container, false);
        ListView listView = (ListView) listLayout.findViewById(android.R.id.list);
//        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
//                FrameLayout.LayoutParams.WRAP_CONTENT,
//                FrameLayout.LayoutParams.WRAP_CONTENT
//        );
//        params.setMargins(0, 0, 0, 1);
//        listView.setLayoutParams(params);
//        listView.requestLayout();
        listView.setSelector(android.R.color.transparent);
        listView.setDivider(null);
        listView.setDividerHeight(0);

        return listLayout;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

//        if (null != mListener) {
//            // Notify the active callbacks interface (the activity, if the
//            // fragment is attached to one) that an item has been selected.
//            mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
//        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void getItineraryDestinations(ArrayList<String> itineraryDestinations);
    }

    public void updateItinerary(int budget) {
        Log.i("ItineraryFragment", "Budget updated");
        this.budget = budget;
        updateItinerary();
    }

    public void updateItinerary(String hotel) {
        Log.i("Itinerary Fragment", "Hotel updated");
        this.hotel = hotel;
        this.destinations.remove(hotel);
        updateItinerary();
    }

    public void updateItinerary(boolean exhaustiveMode) {
        Log.i("ItineraryFragment", "Search mode updated");
        this.exhaustiveMode = exhaustiveMode;
        updateItinerary();
    }

    public void updateItinerary(ArrayList<String> destinations) {
        Log.i("ItineraryFragment", "Destinations updated: " + destinations.toString());
        this.destinations = destinations;
        updateItinerary();
    }

    public void updateItinerary() {
        if (hotel == null) {
            return;
        }

        if (destinations.isEmpty()) {
            itineraryAdapter.clear();
            itineraryAdapter.notifyDataSetChanged();
            return;
        }

        Log.i("ItineraryFragment", "Refreshing itinerary");
        OptimalRouteFinder optimalRouteFinder = new OptimalRouteFinder(budget, hotel, destinations, exhaustiveMode);
        route = optimalRouteFinder.findRoute();
        ArrayList<String> itineraryStops = new ArrayList<>();
        for (ItineraryItem item : route) {
            itineraryStops.add(item.destination);
        }
        mListener.getItineraryDestinations(itineraryStops);
        itineraryAdapter.clear();
        route.add(0, new ItineraryItem("Start from " + hotel,
                "Total cost: " + optimalRouteFinder.candidateSolution.getTotalCost() + ", total time: " + optimalRouteFinder.candidateSolution.total_time + " minutes"));
        itineraryAdapter.addAll(route);
        itineraryAdapter.notifyDataSetChanged();
    }

    class OptimalRouteFinder {
        class CandidateSolution {
            ArrayList<ItineraryItem> pathTaken = new ArrayList<>();
            int total_time = Integer.MAX_VALUE;
            int total_cost = Integer.MAX_VALUE;

            String getTotalCost() {
                return "$" + total_cost / 100 + "." + total_cost % 100;
            }
        }

        boolean exhaustiveMode;
        int budget;
        String hotel;
        ArrayList<String> destinations;
        CandidateSolution candidateSolution;

        public OptimalRouteFinder(int budget, String hotel, ArrayList<String> destinations, boolean exhaustiveMode) {
            this.budget = budget;
            this.hotel = hotel;
            this.destinations = destinations;
            this.exhaustiveMode = exhaustiveMode;
            candidateSolution = new CandidateSolution();
        }

        ArrayList<ItineraryItem> findRoute() {
            Log.i("findRoute", "Starting from " + hotel + ", exhaustiveMode: " + String.valueOf(exhaustiveMode));
            Log.i("findRoute", "Destinations: " + destinations.toString());
            try {
                tracePath(hotel, 0, 0, new ArrayList<>(destinations), new ArrayList<ItineraryItem>());
            } catch (InterruptedException e) {
                Log.i("findRoute", "First optimal solution found.");
                return candidateSolution.pathTaken;
            }
            return candidateSolution.pathTaken;
        }

        void tracePath(String currentLocation, int currentTiming, int currentCost,
                       ArrayList<String> remainingDestinations,
                       ArrayList<ItineraryItem> pathTaken) throws InterruptedException {

            Log.i("Current timing and cost", String.format("%d %d", currentTiming, currentCost));

            int recursionDepth = pathTaken.size();

            if (currentCost > budget || currentTiming > this.candidateSolution.total_time) {
                return;
            }

//            Log.i("remainingDestinations", String.valueOf(remainingDestinations.isEmpty()));
            if (remainingDestinations.isEmpty()) {
                for (TransportMode transportMode : TransportMode.values()) {
                    ArrayList<ItineraryItem> finalPathTaken = new ArrayList<>(pathTaken);
                    Log.i("tracePath", String.format("%sFinished visiting %s, now returning to hotel by %s", new String(new char[recursionDepth * 4]).replace("\0", " "), pathTaken.toString(), transportMode.toString()));
                    int cost = Routes.getCost(transportMode, currentLocation, hotel);
                    int finalCost = currentCost + cost;
                    if (finalCost <= budget) {
                        int time = Routes.getTiming(transportMode, currentLocation, hotel);
                        int finalTiming = currentTiming + time;
                        if (finalTiming < candidateSolution.total_time) {
                            candidateSolution.total_cost = finalCost;
                            candidateSolution.total_time = finalTiming;
                            finalPathTaken.add(new ItineraryItem(transportMode, hotel, cost, time, true));
                            candidateSolution.pathTaken = finalPathTaken;
                            if (!exhaustiveMode) {
                                throw new InterruptedException();
                            }
                            Log.i("FinalPathTaken", candidateSolution.pathTaken.toString());
                        }
                    }
                }
            } else {
                for (TransportMode transportMode : TransportMode.values()) {
                    for (String destination : remainingDestinations) {
                        Log.i("tracePath", String.format("%sVisited %s, now visiting %s by %s", new String(new char[recursionDepth * 4]).replace("\0", " "), pathTaken.toString(), destination, transportMode.toString()));
                        int cost = Routes.getCost(transportMode, currentLocation, destination);
                        int timing = Routes.getTiming(transportMode, currentLocation, destination);
                        ArrayList<String> r = new ArrayList<>(remainingDestinations);
                        r.remove(destination);
                        ArrayList<ItineraryItem> p = new ArrayList<>(pathTaken);
                        p.add(new ItineraryItem(transportMode, destination, cost, timing));
                        tracePath(destination,
                                currentTiming + timing,
                                currentCost + cost,
                                r,
                                p);
                    }
                }
            }
        }
    }
}