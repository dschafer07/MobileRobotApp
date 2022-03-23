package com.example.robovision;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;


public class gps extends AppCompatActivity implements LocationListener {
    private double latitude, longitude, altitude,accuracy,speed;
    TextView txtLatitude, txtLongitude, txtaltitude, txtaccuracy,txtspeed;

    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gps);

        txtLatitude = (TextView) findViewById(R.id.txtLat);
        txtLongitude = (TextView) findViewById(R.id.txtLong);
        txtaltitude = (TextView) findViewById(R.id.txtalt);
        txtaccuracy = (TextView) findViewById(R.id.txtacc);
        txtspeed = (TextView) findViewById(R.id.txtsp);

        locationManager = (LocationManager)  getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(gps.this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(gps.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0, this);


    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        altitude = location.getAltitude();
        accuracy = location.getAccuracy();
        speed = location.getSpeed();


        txtLatitude.setText(String.valueOf(latitude));
        txtLongitude.setText(String.valueOf(longitude));
        txtaltitude.setText(String.valueOf(altitude));
        txtaccuracy.setText(String.valueOf(accuracy));
        txtspeed.setText(String.valueOf(speed));

        //save battery
        locationManager.removeUpdates(this);

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }
}