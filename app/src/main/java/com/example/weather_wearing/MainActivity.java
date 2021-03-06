package com.example.weather_wearing;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener {

    private final int REQUEST_PERMISSION_FOR_ACCESS_FINE_LOCATION = 100;

    double mylat;
    double mylon;

    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationManager mLocationMgr;
    private LocationCallback mLocationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null)
                    return;
                Location location = locationResult.getLastLocation();
                updateMapLocation(location);
            }
        };
        mLocationMgr = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
        enableLocation(false);
    }
    @Override
    public void onStop() {
        super.onStop();
        //Toast.makeText(MapsActivity.this, "????????? Google API", Toast.LENGTH_LONG).show();
        mGoogleApiClient.disconnect();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_FOR_ACCESS_FINE_LOCATION) {
            if (grantResults.length != 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableLocation(true);
                return;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        enableLocation(true);
    }
    @Override
    public void onConnectionSuspended(int i) {
        switch (i) {
            case CAUSE_NETWORK_LOST:
                break;
            case CAUSE_SERVICE_DISCONNECTED:
                break;
        }
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //Toast.makeText(MapsActivity.this, "Google API ????????????",Toast.LENGTH_LONG).show();
    }

    public void enableLocation(boolean on) {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            // ??????????????????????????????????????????
            // ????????????????????????????????????
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder altDlgBuilder =
                        new AlertDialog.Builder(this);
                altDlgBuilder.setTitle("??????");
                altDlgBuilder.setMessage("??????????????????");
                altDlgBuilder.setCancelable(false);
                altDlgBuilder.setPositiveButton("??????",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface,
                                                int i) {
                                // ?????????????????????????????????????????????????????????
                                // ???????????????????????????onRequestPermissionsResult()
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{
                                                android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQUEST_PERMISSION_FOR_ACCESS_FINE_LOCATION);
                            }
                        });
                altDlgBuilder.show();
                return;
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                android.Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_PERMISSION_FOR_ACCESS_FINE_LOCATION);
                return;
            }
        }
        // ???????????????
        if (on) {
            // ???????????????????????????
            mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) { //????????????????????????
                        updateMapLocation(location);
                    } else { //????????????????????????
                        AlertDialog.Builder altDlgBuilder = new AlertDialog.Builder(MainActivity.this);
                        altDlgBuilder.setTitle("??????");
                        altDlgBuilder.setMessage("???????????????????????????????????????");
                        altDlgBuilder.setCancelable(false);
                        altDlgBuilder.setPositiveButton("??????",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        finish();
                                    }
                                });
                        altDlgBuilder.show();
                    }
                }
            });
            LocationRequest locationRequest = LocationRequest.create();
            // ????????????(??????)
            //locationRequest.setInterval(5000);
            locationRequest.setSmallestDisplacement(5);
            if (mLocationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                //Toast.makeText(MapsActivity.this, "??????GPS??????",Toast.LENGTH_LONG).show();
            } else if (mLocationMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
                //Toast.makeText(MapsActivity.this, "??????????????????",Toast.LENGTH_LONG).show();
            }
            mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());
        } else {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            //Toast.makeText(MapsActivity.this, "????????????", Toast.LENGTH_LONG).show();
        }
    }

    private void updateMapLocation(Location location) {
        mylat = location.getLatitude();
        mylon = location.getLongitude();
        Intent intent = new Intent(MainActivity.this,DataActivity.class);
        intent.putExtra("mylat",location.getLatitude());
        intent.putExtra("mylon",location.getLongitude());
        startActivity(intent);
    }
}




