package com.example.namiq.egisterpp.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.namiq.egisterpp.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MenuItem okMenu;
    private boolean chooseStatus;
    String coordinates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                chooseStatus = true;
                okMenu.setIcon(R.drawable.ok);
                LatLng coor = new LatLng(point.latitude, point.longitude);
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(coor));
                CameraUpdate center = CameraUpdateFactory.newLatLng(coor);
                mMap.moveCamera(center);
                coordinates = point.latitude + "," + point.longitude;
                Toast.makeText(MapActivity.this, point.longitude + " " + point.latitude, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        okMenu = menu.findItem(R.id.ok);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.ok) {
            if (chooseStatus) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("loc",coordinates);
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

}
