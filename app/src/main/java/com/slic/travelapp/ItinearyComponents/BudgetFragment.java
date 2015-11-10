package com.slic.travelapp.ItinearyComponents;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.slic.travelapp.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BudgetFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BudgetFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BudgetFragment extends Fragment {
    private static final String[] HOTELS = {
            "Marina Bay Sands", "Resorts World Sentosa", "Singapore Flyer", "Vivo City", "Buddha Tooth Relic Temple", "Zoo"
    };

    private OnFragmentInteractionListener mListener;
    private EditText editText;
    private EditText editText2;
    private RadioGroup radioGroup;

    public static BudgetFragment newInstance() {
        BudgetFragment fragment = new BudgetFragment();
        return fragment;
    }

    public BudgetFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_budget, container, false);

        editText = (EditText) view.findViewById(R.id.editText);
        Spinner spinner = (Spinner) view.findViewById(R.id.spinner);

        radioGroup = (RadioGroup) view.findViewById(R.id.radioGroup);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, Data.attractions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mListener.onHotelUpdated((String) parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 3) {
                    // do nothing
                } else {
                    String str = s.toString();
                    Log.i("TextWatcher", "Before replace: " + str);
                    str = str.replace(".", "");
                    Log.i("TextWatcher", "Before adding dot: " + str);
                    str = str.substring(0, str.length() - 2) + "." + str.substring(str.length()-2, str.length());
                    Log.i("TextWatcher", "Final string: " + str);
                    editText.removeTextChangedListener(this);
                    editText.setText(str);
                    editText.setSelection(str.length());
                    editText.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Log.i("BudgetFragment", "Budget updated");
                    String string = editText.getText().toString();
                    int budget;
                    if (string.isEmpty()) {
                        budget = 0;
                    } else {
                        string = string.replace(".", "");
                        budget = Integer.parseInt(string);
                    }
                    mListener.onBudgetUpdated(budget);
//                    handled = true;
                }
                return handled;
            }
        });



/*        editText2.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Log.i("BudgetFragment", "Handling hotel input");
                    mListener.onHotelUpdated(editText2.getText().toString());
//                    handled = true;
                }
                return handled;
            }
        });*/

        ((RadioButton) radioGroup.findViewById(R.id.radioButton)).setChecked(true);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                mListener.onSearchModeUpdated(checkedId == R.id.radioButton2);
            }
        });

        return view;

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
    public void onDestroyView() {
        super.onDestroyView();
        Log.i("BudgetFragment", "View destroyed");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
        public void onBudgetUpdated(int budget);

        public void onHotelUpdated(String hotel);

        public void onSearchModeUpdated(boolean exhaustiveMode);
    }

}