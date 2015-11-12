package com.slic.travelapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.slic.travelapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

public class FeedbackActivity extends AppCompatActivity {

    private static final String API_STRING = "https://maker.ifttt.com/trigger/feedback/with/key/dfN43kYwXTYfBWctdXhh3j";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
    }

    public void HappyFunction(View v) {
        //Sending POST...
        String http = API_STRING;
        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL(http);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Host", "maker.ifttt.com");
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            urlConnection.connect();

            //JSONObject
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("value1", "happy");
            DataOutputStream out = new DataOutputStream(urlConnection.getOutputStream());
            String str = jsonParam.toString();
            Log.d("MEL", "Request: " + str);
            out.writeBytes(str);
            out.flush();
            out.close();

            //get Response otherwise data cannot send up

            Logger.getLogger("log").info("URL: " + url + ", response: " + urlConnection.getResponseCode());


        } catch (MalformedURLException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        } catch (JSONException e) {

            e.printStackTrace();
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
        Toast.makeText(getApplicationContext(), "Thank You for the Feedback!!!", Toast.LENGTH_SHORT).show();
    }

    public void NeutralFunction(View v) {
        //Sending POST...
        String http = "https://maker.ifttt.com/trigger/feedback/with/key/dfN43kYwXTYfBWctdXhh3j";
        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL(http);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Host", "maker.ifttt.com");
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            urlConnection.connect();

            //JSONObject
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("value1", "neutral");
            DataOutputStream out = new DataOutputStream(urlConnection.getOutputStream());
            String str = jsonParam.toString();
            Log.d("MEL", "Request: " + str);
            out.writeBytes(str);
            out.flush();
            out.close();

            //get Response otherwise data cannot send up

            Logger.getLogger("log").info("URL: " + url + ", response: " + urlConnection.getResponseCode());


        } catch (MalformedURLException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        } catch (JSONException e) {

            e.printStackTrace();
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
        //Toast.makeText(getApplicationContext(), "Thank You for the Feedback!!!", Toast.LENGTH_SHORT).show();
        showAppreciationAlert();
    }

    public void NthappyFunction(View v) {
        //Sending POST...
        String http = "https://maker.ifttt.com/trigger/feedback/with/key/dfN43kYwXTYfBWctdXhh3j";
        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL(http);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Host", "maker.ifttt.com");
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            urlConnection.connect();

            //JSONObject
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("value1", "sad");
            DataOutputStream out = new DataOutputStream(urlConnection.getOutputStream());
            String str = jsonParam.toString();
            Log.d("MEL", "Request: " + str);
            out.writeBytes(str);
            out.flush();
            out.close();

            //get Response otherwise data cannot send up

            Logger.getLogger("log").info("URL: " + url + ", response: " + urlConnection.getResponseCode());


        } catch (MalformedURLException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        } catch (JSONException e) {

            e.printStackTrace();
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
        showAppreciationAlert();
//        Toast.makeText(getApplicationContext(), "Thank You for the Feedback!!!", Toast.LENGTH_SHORT).show();
    }

    public void showAppreciationAlert() {
        AlertDialog.Builder alertDialogueBuilder = new AlertDialog.Builder(this);
        alertDialogueBuilder.setTitle("Alert")
                .setMessage("Thank You for the Feedback!")
                .setPositiveButton("Your welcome.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FeedbackActivity.this.finish();
                    }
                });
        alertDialogueBuilder.create().show();
    }

}
