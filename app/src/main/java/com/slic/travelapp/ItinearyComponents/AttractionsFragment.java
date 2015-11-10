package com.slic.travelapp.ItinearyComponents;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;

import com.slic.travelapp.R;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class AttractionsFragment extends ListFragment {

    private OnFragmentInteractionListener mListener;

    private String previousHotel;
    private ArrayList<String> attractions;
    private HashSet<String> checkedAttractions;
    private ArrayAdapter<String> attractionsAdapter;
    private ListView listView;

    public static AttractionsFragment newInstance() {
        AttractionsFragment fragment = new AttractionsFragment();
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AttractionsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View listLayout = inflater.inflate(android.R.layout.list_content, container, false);
        listView = (ListView) listLayout.findViewById(android.R.id.list);
//        listView.setPadding(0, 8, 0, 0);
//        ((FrameLayout) listView.getParent()).setPadding(0, 8, 0, 0);
        listView.setSelector(android.R.color.transparent);
//        listView.setDivider(null);
//        listView.setDividerHeight(0);
        return listLayout;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        attractions = new ArrayList<>(Data.attractions);
        checkedAttractions = new HashSet<>();

        attractionsAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.attraction, R.id.text1, attractions);
        setListAdapter(attractionsAdapter);

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
    public void onDestroyView() {
        super.onDestroyView();
        Log.i("AttractionsFragment", "View destroyed");
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Log.i("AttractionsFragment", "List item click");

        if (null != mListener) {
            String attraction = attractionsAdapter.getItem(position);
            CheckBox checkBox = (CheckBox) v.findViewById(R.id.checkBox);
            if (!checkBox.isChecked()) {
                checkedAttractions.add(attraction);
            } else {
                checkedAttractions.remove(attraction);
            }
            checkBox.toggle();

            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onAttractionsSelected(new ArrayList<>(checkedAttractions));
        }
    }

    public void updateHotel(String hotel) {
        Log.i("AttractionsFragment", "Updating hotel");
//        CheckBox checkbox = (CheckBox) listView.getChildAt(attractionsAdapter.getPosition(hotel)).findViewById(R.id.checkBox);
//        if (checkbox.isChecked()) {
//            checkbox.toggle();
//            checkedAttractions.remove(hotel);
//        }
//        if (previousHotel != null) {
//            attractionsAdapter.add(previousHotel);
//        }
//        previousHotel = hotel;
//        attractionsAdapter.remove(hotel);
//        attractionsAdapter.notifyDataSetChanged();

//        try {
//            CheckBox checkbox = ((CheckBox) listView.getChildAt(attractionsAdapter.getPosition(hotel)).findViewById(R.id.checkBox));
//            if (checkbox.isChecked()) {
//                checkbox.toggle();
//            }
//        } catch (Exception e) {
//            // doesn't matter
//        }

        checkedAttractions.clear();
        mListener.onAttractionsSelected(new ArrayList<>(checkedAttractions));
        attractions = new ArrayList<>(Data.attractions);
        attractions.remove(hotel);
        checkedAttractions.remove(hotel);
        attractionsAdapter.clear();
//        listView.post(new Runnable() {
//            @Override
//            public void run() {
//                attractionsAdapter.notifyDataSetChanged();
//            }
//        });
        listView.invalidateViews();
        attractionsAdapter.addAll(attractions);
        resetCheckboxes();
//        listView.post(new Runnable() {
//            @Override
//            public void run() {
//                attractionsAdapter.notifyDataSetChanged();
//            }
//        });

    }

    private void resetCheckboxes() {
        for (int i = 0; i <= listView.getLastVisiblePosition() - listView.getFirstVisiblePosition(); i++) {
            CheckBox checkBox = ((CheckBox) listView.getChildAt(i).findViewById(R.id.checkBox));
            if (checkBox.isChecked()) {
                checkBox.toggle();
            }
        }

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
        void onAttractionsSelected(ArrayList<String> selectedAttractions);
    }

}