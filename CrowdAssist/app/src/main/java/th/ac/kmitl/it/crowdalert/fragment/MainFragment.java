package th.ac.kmitl.it.crowdalert.fragment;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import th.ac.kmitl.it.crowdalert.MainActivity;
import th.ac.kmitl.it.crowdalert.R;
import th.ac.kmitl.it.crowdalert.component.SendingDialog;
import th.ac.kmitl.it.crowdalert.model.Request;
import th.ac.kmitl.it.crowdalert.util.GoToCallback;
import th.ac.kmitl.it.crowdalert.util.LocationHelper;
import th.ac.kmitl.it.crowdalert.util.LogoutCallback;

public class MainFragment extends Fragment implements OnMapReadyCallback , View.OnClickListener, View.OnLongClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private View mLayoutBottomSheet;
    private Location location;
    private LocationRequest mLocationRequest;
    private final String PROFILE_SP = "profile";
    private SharedPreferences sp;
    private GoToCallback callback;
    private FrameLayout rootLayout;

    public MainFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainActivity)getActivity()).setListener(this);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        sp = getActivity().getSharedPreferences(PROFILE_SP, Context.MODE_PRIVATE);
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)
                .setFastestInterval(1 * 1000);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        rootLayout = view.findViewById(R.id.root_view);
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        if (location != null){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()), 15));
        }
        //mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.requester_pin)).position(new LatLng(13.731111, 100.781158)));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            if (context instanceof Activity){
                callback = (GoToCallback) context;
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement Callback");
        }
    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }


    @Override
    public void onClick(View view) {
        //Toast.makeText(getActivity(), "Clicked!", Toast.LENGTH_LONG).show();
        callback.goTo("Setting");
    }

    @Override
    public boolean onLongClick(View view) {
        if (sp.getBoolean("verify", false)) {
            Request data = getData();
            Location location = new Location("");
            location.setLatitude(data.getLat());
            location.setLongitude(data.getLng());
            Observable.fromCallable(resolveCallable(location)).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .take(1)
                    .subscribe(action1(data));
        }else{
            Snackbar.make(rootLayout, "กรุณายืนยันตัวตนก่อนการใช้งาน", Snackbar.LENGTH_SHORT).setAction("ไปยังหน้ายืนยัน",this).show();
        }
        return true;
    }

    private Request getData(){
        if (mGoogleApiClient.isConnected()){
            Request data = new Request();
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            data.setRequesterUid(mUser.getUid());
            data.setLat(location.getLatitude());
            data.setLng(location.getLongitude());
            data.setStatus("wait");
            data.setTime(1);
            return data;
        }
        return null;
    }
    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(getActivity(), permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION))
            return;
        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        } else {
            this.location = currentLocation;
            if(mMap != null){
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()), 15));
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if(mMap != null && this.location == null){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()), 15));
            this.location = location;
        }else{
            this.location = location;
        }
    }

    private Consumer<String> action1(final Request data){
        return new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                data.setArea(s);
                SendingDialog dialog = new SendingDialog(getActivity(), data);
                dialog.show(10000);
            }
        };
    }

    private Callable<String> resolveCallable(final Location location){
        return new Callable<String>() {
            @Override
            public String call() throws Exception {
                return LocationHelper.resolveArea(location);
            }
        };
    }
}
