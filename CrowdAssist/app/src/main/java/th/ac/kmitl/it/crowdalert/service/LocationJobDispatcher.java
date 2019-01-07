package th.ac.kmitl.it.crowdalert.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import th.ac.kmitl.it.crowdalert.util.LocationHelper;

public class LocationJobDispatcher extends JobService implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    private FirebaseDatabase mDatabase;
    private FirebaseAuth mAuth;
    private final String SP_LOCATION = "location_information";
    private final String SP_PROFILE = "profile";
    SharedPreferences sp;
    SharedPreferences spProfile;
    Location storeLocation;

    @Override
    public boolean onStartJob(JobParameters job) {
        setup();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        Log.d("onStopJob: ", "Stop");
        return false;
    }

    private void setup(){
        if (mGoogleApiClient == null){
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();
        mDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        sp = getSharedPreferences(SP_LOCATION, Context.MODE_PRIVATE);
        spProfile = getSharedPreferences(SP_PROFILE, Context.MODE_PRIVATE);
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (!hasPermission(android.Manifest.permission.ACCESS_FINE_LOCATION))
            jobFinished(null, false);
        if (mAuth.getCurrentUser() != null){
            String currentlat = sp.getString("location_lat", null);
            String currentLon = sp.getString("location_lon", null);
            if (currentlat != null && currentLon != null){
                storeLocation = new Location("");
                storeLocation.setLatitude(Double.parseDouble(currentlat));
                storeLocation.setLongitude(Double.parseDouble(currentLon));
            }
            if (storeLocation == null){
                Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                updateDatabase(currentLocation);
            }else{
                Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (LocationHelper.distance(storeLocation, currentLocation) >= 1.0){
                    updateDatabase(currentLocation);
                }
            }
            jobFinished(null, true);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void updateDatabase(Location location){
        DatabaseReference ref = mDatabase.getReference("location");
        GeoFire geoFire = new GeoFire(ref);
        if (spProfile.getString("device", null) != null){
            geoFire.setLocation(mAuth.getCurrentUser().getUid()+"@"+spProfile.getString("device",""), new GeoLocation(location.getLatitude(), location.getLongitude()));
        }else{
            geoFire.setLocation(mAuth.getCurrentUser().getUid(), new GeoLocation(location.getLatitude(), location.getLongitude()));
        }
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("location_lat", String.valueOf(location.getLatitude()));
        editor.putString("location_lon", String.valueOf(location.getLongitude()));
        editor.apply();
    }
}
