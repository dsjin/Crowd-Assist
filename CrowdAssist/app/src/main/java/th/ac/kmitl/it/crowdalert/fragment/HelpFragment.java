package th.ac.kmitl.it.crowdalert.fragment;


import android.Manifest;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.RequestResult;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import th.ac.kmitl.it.crowdalert.ConfirmActivity;
import th.ac.kmitl.it.crowdalert.MainActivity;
import th.ac.kmitl.it.crowdalert.ManageRequestingActivity;
import th.ac.kmitl.it.crowdalert.R;
import th.ac.kmitl.it.crowdalert.RequestInformationActivity;
import th.ac.kmitl.it.crowdalert.model.Request;
import th.ac.kmitl.it.crowdalert.util.DatabaseHelper;
import th.ac.kmitl.it.crowdalert.util.StatusViewModel;

public class HelpFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener, GoogleMap.OnInfoWindowClickListener, DirectionCallback , GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private final String SP_REQUEST = "request_information";
    private final String SP_PROFILE = "profile";
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private FloatingActionButton direction;
    private FloatingActionButton confirm;
    private FirebaseDatabase mDatabase;
    private String requestUid;
    private Request data;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private DatabaseHelper helper;
    private String type;
    private Location currentLocation;
    private LiveData<DataSnapshot> liveData;
    private SharedPreferences sp;
    private Marker requesterMarker;
    private Marker requestMarker;
    private Marker userMarker;
    private LatLng requesterLocation;

    public HelpFragment() {
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance();
        sp = getActivity().getSharedPreferences(SP_REQUEST, Context.MODE_PRIVATE);
        requestUid = sp.getString("request_uid", null);
        type = sp.getString("type", null);

        if (requestUid == null){
            // TODO: 20/12/2560 Handle Shared Preference : emergencyUid is null.
        }
        mDatabase.getReference().child(type).child(requestUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    data = dataSnapshot.getValue(Request.class);
                    if (mMap != null){
                        MarkerOptions options = new MarkerOptions()
                                .position(new LatLng(data.getLat(), data.getLng()))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.requester_pin));
                        requestMarker = mMap.addMarker(options);
                        requestMarker.setTitle("คลิกเพื่อดูรายละเอียด");
                        requestMarker.setTag(data.getRequesterUid());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabase.getReference().child(type+"_requester_point").child(requestUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Double lat = dataSnapshot.child("lat").getValue(Double.class);
                    Double lng = dataSnapshot.child("lng").getValue(Double.class);
                    if (mMap != null){
                        if (requesterMarker == null){
                            MarkerOptions options = new MarkerOptions()
                                    .position(new LatLng(lat, lng))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.runner_pin));
                            requesterMarker = mMap.addMarker(options);
                        }else{
                            requesterMarker.setPosition(new LatLng(lat, lng));
                        }
                    }else{
                        requesterLocation = new LatLng(lat, lng);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        /*
        mDatabase.getReference().child(type).child(requestUid).child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if ("close".equals(dataSnapshot.getValue())){
                        showMessageOK("คำร้องได้สิ้นสุดลงแล้ว", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ((MainActivity)activity).setToHome();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)
                .setFastestInterval(1 * 1000);
        helper = new DatabaseHelper(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help, container, false);
        SharedPreferences profile = getActivity().getSharedPreferences(SP_PROFILE, Context.MODE_PRIVATE);
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        direction = view.findViewById(R.id.fab_direction);
        direction.setOnClickListener(this);
        confirm = view.findViewById(R.id.confirmButton);
        if (profile.getString("role", "null").equals("officer")){
            confirm.setVisibility(View.VISIBLE);
            confirm.setOnClickListener(this);
        }
        StatusViewModel viewModel = ViewModelProviders.of(this).get(StatusViewModel.class);

        liveData = viewModel.getDataSnapshotLiveData();

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //mMap.setMyLocationEnabled(true);
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(13.731111, 100.781158), 15));
        //mMap.setOnInfoWindowClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
        if (currentLocation != null){
            double currentLatitude = currentLocation.getLatitude();
            double currentLongitude = currentLocation.getLongitude();

            LatLng latLng = new LatLng(currentLatitude, currentLongitude);
            if (userMarker == null){
                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.me_pin));
                //userMarker = mMap.addMarker(options);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
            }else{
                userMarker.setPosition(latLng);
            }
        }
        if (requesterLocation != null){
            if (requesterMarker == null){
                MarkerOptions options = new MarkerOptions()
                        .position(requesterLocation)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.runner_pin));
                requesterMarker = mMap.addMarker(options);
            }else{
                requesterMarker.setPosition(requesterLocation);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fab_direction:
                CharSequence details[] = new CharSequence[] {"นำทางไปยังจุดแจ้งเหตุ", "นำทางไปยังผู้ร้องขอ"};
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("เลือกการนำทาง");
                builder.setItems(details, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0){
                                    getDirection("request_point");
                                }else{
                                    getDirection("requester_point");
                                }
                            }
                        });
                builder.show();
                break;
            case R.id.confirmButton:
                Intent intent = new Intent(getActivity(), ConfirmActivity.class);
                getActivity().startActivity(intent);
        }
    }

    private void getDirection(String type){
        // TODO: 24/12/2560 Add Google Direction API
        switch (type){
            case "request_point":
                GoogleDirection.withServerKey(getString(R.string.google_direction_api))
                        .from(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                        .to(new LatLng(data.getLat(), data.getLng()))
                        .transportMode(TransportMode.WALKING)
                        .execute(this);
                break;
            case "requester_point":
                GoogleDirection.withServerKey(getString(R.string.google_direction_api))
                        .from(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                        .to(new LatLng(requesterMarker.getPosition().latitude, requesterMarker.getPosition().longitude))
                        .transportMode(TransportMode.WALKING)
                        .execute(this);
        }
    }

    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {
        String status = direction.getStatus();
        if(status.equals(RequestResult.OK)) {
            Route route = direction.getRouteList().get(0);
            Leg leg = route.getLegList().get(0);
            ArrayList<LatLng> directionPositionList = leg.getDirectionPoint();
            PolylineOptions polylineOptions = DirectionConverter.createPolyline(getActivity(), directionPositionList, 5, Color.RED);
            mMap.addPolyline(polylineOptions);
        }
    }

    @Override
    public void onStart() {
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION))
            return;
        mGoogleApiClient.connect();
        if ("Help".equals(sp.getString("mode", ""))){
            liveData.observe(this, new Observer<DataSnapshot>() {
                @Override
                public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        if ("close".equals(dataSnapshot.getValue())){
                            showMessageOK("คำร้องได้สิ้นสุดลงแล้ว", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {SharedPreferences.Editor editor = sp.edit();
                                    editor.putString("mode","Rate");
                                    editor.apply();
                                    ((MainActivity)getActivity()).setToRate();
                                }
                            });
                        }
                    }
                }
            });
        }
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        liveData.removeObservers(this);
    }
    @Override
    public void onDirectionFailure(Throwable t) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        /*
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else {
            handleNewLocation(location);
        }*/
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(getActivity(), permission) == PackageManager.PERMISSION_GRANTED;
    }
    private void handleNewLocation(Location location) {
        currentLocation = location;
        helper.updateHelperLocation(requestUid, location, type);

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        if (mMap != null){
            if (userMarker == null){
                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.me_pin));
                userMarker = mMap.addMarker(options);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
            }else{
                userMarker.setPosition(latLng);
            }
        }
    }
    private void showMessageOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .create()
                .show();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if (marker.getTag().equals(requestMarker.getTag())){
            Intent intent = new Intent(getActivity(), RequestInformationActivity.class);
            intent.putExtra("uid", requestUid);
            intent.putExtra("type", type);
            intent.putExtra("data", data);
            intent.putExtra("request_id", requestUid);
            getActivity().startActivity(intent);
        }
    }
}
