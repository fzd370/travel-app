package com.slic.travelapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.app.Activity;

import com.slic.travelapp.R;


public class ContactsActivity extends AppCompatActivity {

    private Button bt;
    private Button bt2;
    private Button bt3;
    private Button bt4;
    private Button bt5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        bt = (Button) findViewById(R.id.contact_button1);
        bt.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                callNumber(995);
            }
        }); // Ambulance
        bt2 = (Button) findViewById(R.id.contact_button2);
        bt2.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                callNumber(995);
            }
        }); // Fire
        bt3 = (Button) findViewById(R.id.contact_button3);
        bt3.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                callNumber(999);
            }
        }); // Police
        bt4 = (Button) findViewById(R.id.contact_button4);
        bt4.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                callNumber(65522222);
            }
        }); // Taxi
        bt5 = (Button) findViewById(R.id.contact_button5);
        bt5.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                callNumber(67773777);
            }
        }); // KFC
    }

    public void callNumber(int contact) {
        Intent phoneIntent = new Intent(Intent.ACTION_CALL);
        phoneIntent.setData(Uri.parse("tel:" + String.valueOf(contact)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions not granted", Toast.LENGTH_LONG).show();
                //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
        }
        startActivity(phoneIntent);
    }
}

