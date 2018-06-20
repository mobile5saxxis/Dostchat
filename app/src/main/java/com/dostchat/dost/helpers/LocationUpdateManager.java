package com.dostchat.dost.helpers;

import android.Manifest;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.dostchat.dost.app.DostChatApp;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.dostchat.dost.R;

import java.util.List;
import java.util.Locale;

/**
 * Created by manoj on 27/01/18.
 */

public class LocationUpdateManager implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    Context appContext;
    protected GoogleApiClient mGoogleApiClient;
    Location currentLocation = null;
    LocationRequest mLocationRequest;
    Geocoder geocoder;
    int REQUEST_CHECK_SETTINGS_GPS = 1;
    AppCompatActivity activity;
    boolean isFirst = true;

    public LocationUpdateManager(Context appContext) {
        this.appContext = appContext;
        geocoder = new Geocoder(appContext, Locale.getDefault());
        mLocationRequest = new LocationRequest();
        mLocationRequest.setFastestInterval(6000);
        mLocationRequest.setInterval(6000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }


        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext);
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location == null) {
                        ennableLocation();
                        return;
                    }
                    currentLocation = location;
//                    if (activity.getSupportActionBar() != null)
//                        activity.getSupportActionBar().setTitle("At: " + getLocationArea());
                    setLocationToNav();

                    Log.i("CurrentLocation", "" + currentLocation.getLatitude() + "," + currentLocation.getLongitude());

//                    if (activity.getSupportActionBar() != null)
//                        activity.getSupportActionBar().setTitle("At: " + getLocationArea());
                    setLocationToNav();

                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            super.onLocationResult(locationResult);
                            Log.i("locationResult", "" + locationResult.getLocations().size());
                            for (Location location1 : locationResult.getLocations()) {
                                currentLocation.setLatitude(location1.getLatitude());
                                currentLocation.setLatitude(location1.getLatitude());
                                Log.i("Captured", currentLocation.getLatitude() + "," + currentLocation.getLongitude());

//                                if (activity.getSupportActionBar() != null)
//                                    activity.getSupportActionBar().setTitle("At: " + getLocationArea());
                                setLocationToNav();

                                if (isFirst) {
                                    //DostChatApp.getInstance().locationExchange(currentLocation);
                                } else {

                                }
                                isFirst = false;
                            }
                        }

                        @Override
                        public void onLocationAvailability(LocationAvailability locationAvailability) {
                            super.onLocationAvailability(locationAvailability);
                            if (!locationAvailability.isLocationAvailable()) {
                                ennableLocation();
                            }
                        }
                    }, null);
                })
                .addOnFailureListener(e -> e.printStackTrace());
    }

    private void setLocationToNav() {
        NavigationView navigationView = (NavigationView) activity.findViewById(R.id.nav_view);

        if (navigationView != null) {
            View headerView = navigationView.getHeaderView(0);

            if (headerView != null) {
                TextView wish = (TextView) headerView.findViewById(R.id.wish);

                if (wish != null) {
                    getLocationArea(wish);
                }
            }
        }
    }

    public void locationExchange(int senderId, int receipentId, TextView updateView) {
        if (currentLocation != null)
            DostChatApp.getInstance().locationExchange(currentLocation, senderId, receipentId, updateView);
    }

    public void locationExchange(TextView textView) {
        if (currentLocation != null)
            DostChatApp.locationUpdateManager.getLocationArea(textView, currentLocation.getLatitude(), currentLocation.getLongitude());
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // ennableLocation();
        Log.i("onConnectionFailed", "yes");
    }

    public void ennableLocation() {
        boolean gps_enabled = false;
        boolean network_enabled = false;
        gps_enabled = DostChatApp.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        network_enabled = DostChatApp.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!gps_enabled && !network_enabled) {
            displayLocationSettingsRequest();
        } else {
            buildGoogleApiClient();
            if (mGoogleApiClient != null) {
                mGoogleApiClient.connect();
            }
        }
    }

    private void displayLocationSettingsRequest() {


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                String TAG = "LocationDialog";
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS_GPS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(appContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }


    public void startLocation(AppCompatActivity activity) {
        this.activity = activity;
        buildGoogleApiClient();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();


            Log.i("LocationUpdateManager", "Location connected");
        }
    }

    public void getLocationArea(TextView textView, double latitude, double longitude) {
        if (currentLocation != null)

            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses != null) {
                    Address returnedAddress = addresses.get(0);
                    String area = returnedAddress.getSubLocality() + "," + returnedAddress.getLocality() + "," + returnedAddress.getCountryName();
                    textView.setText(area);
                } else {
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public String getLocationArea(TextView textView) {
        if (currentLocation == null)
            return "";
        String area = "";

        try {
            List<Address> addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                area = returnedAddress.getSubLocality() + "," + returnedAddress.getLocality() + "," + returnedAddress.getCountryName();
                textView.setText(area);
            } else {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("in area", area);
        return area;
    }

    public String getLocationArea() {
        if (currentLocation == null)
            return "";
        String area = "";

        try {
            List<Address> addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                area = returnedAddress.getSubLocality() + "," + returnedAddress.getLocality() + "," + returnedAddress.getCountryName();
            } else {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("in area", area);
        return area;
    }

    public String getRecepentLocationArea(String gpsString) {
        if (gpsString.trim().equals("") || gpsString.trim().equals(","))
            return "Friend's location not found";
        String latLang[] = gpsString.trim().split(",");
        if (currentLocation == null)
            return "";
        String area = "";

        try {
            List<Address> addresses = geocoder.getFromLocation(Double.parseDouble(latLang[0]), Double.parseDouble(latLang[1]), 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                area = returnedAddress.getSubLocality() + "," + returnedAddress.getLocality() + "," + returnedAddress.getCountryName();
            } else {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("in area", area);
        return area;
    }

    private String getCompleteAddress() {
        if (currentLocation == null)
            return "";
        String strAdd = "";
        try {
            List<Address> addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append(", ");
                }
                strAdd = strReturnedAddress.toString();
            } else {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strAdd;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }
}
