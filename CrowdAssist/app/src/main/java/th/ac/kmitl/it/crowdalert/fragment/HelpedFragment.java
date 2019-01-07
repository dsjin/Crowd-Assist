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
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import org.joda.time.LocalDate;
import org.joda.time.Years;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import th.ac.kmitl.it.crowdalert.ConfirmActivity;
import th.ac.kmitl.it.crowdalert.MainActivity;
import th.ac.kmitl.it.crowdalert.ManageRequestingActivity;
import th.ac.kmitl.it.crowdalert.R;
import th.ac.kmitl.it.crowdalert.component.CustomBottomSheetBehavior;
import th.ac.kmitl.it.crowdalert.model.AssistantModel;
import th.ac.kmitl.it.crowdalert.model.Request;
import th.ac.kmitl.it.crowdalert.model.UserModel;
import th.ac.kmitl.it.crowdalert.util.DatabaseHelper;
import th.ac.kmitl.it.crowdalert.util.LocationHelper;
import th.ac.kmitl.it.crowdalert.util.StatusViewModel;

public class HelpedFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private final String SP_REQUEST = "request_information";
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private FloatingActionButton confirm;
    private String requestUid;
    private FirebaseDatabase mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private Request data;
    private HashMap<String, Marker> markers;
    private CustomBottomSheetBehavior behavior;
    private SharedPreferences sp;
    private Marker requesterMarker;
    private String type;
    private TextView name;
    private TextView age;
    private TextView gender;
    private TextView roleTextView;
    private TextView timeEstimated;
    private LinearLayout progress;
    private LinearLayout userLayout;
    private ImageView userImage;
    private FirebaseStorage mStorage;
    private ArrayList<AssistantModel> officerList;
    private ArrayList<AssistantModel> volunteerList;
    private LiveData<DataSnapshot> liveData;
    private ImageButton resend;
    private DatabaseHelper helper;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location currentLocation;
    private Marker requesterPin;

    public HelpedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mUser = mAuth.getCurrentUser();
        markers = new HashMap<>();
        sp = getActivity().getSharedPreferences(SP_REQUEST, Context.MODE_PRIVATE);
        requestUid = sp.getString("request_uid", null);
        type = sp.getString("type", null);
        officerList = new ArrayList<>();
        volunteerList = new ArrayList<>();
        helper = new DatabaseHelper(getActivity());
        if (requestUid == null){
            // TODO: 20/12/2560 Handle Shared Preference : emergencyUid is null.
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_helped, container, false);
        View includeLayout = view.findViewById(R.id.bottom_layout);
        View bottomSheet = includeLayout.findViewById(R.id.design_bottom_sheet);
        behavior = (CustomBottomSheetBehavior) BottomSheetBehavior.from(bottomSheet);
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        resend = view.findViewById(R.id.resend);
        resend.setOnClickListener(this);
        confirm = view.findViewById(R.id.confirmButton);
        name = view.findViewById(R.id.name);
        age = view.findViewById(R.id.age);
        gender = view.findViewById(R.id.gender);
        roleTextView = view.findViewById(R.id.role);
        timeEstimated = view.findViewById(R.id.timeEstimated);
        progress = view.findViewById(R.id.progress_layout);
        userLayout = view.findViewById(R.id.userLayout);
        userImage = view.findViewById(R.id.userImageView);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ConfirmActivity.class);
                getActivity().startActivity(intent);
            }
        });
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
        mDatabase.getReference().child(type+"_assistance").child(requestUid).child("user").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                AssistantModel data = dataSnapshot.getValue(AssistantModel.class);
                if (data != null){
                    data.setAssistantUid(dataSnapshot.getKey());
                    MarkerOptions options = new MarkerOptions()
                            .position(new LatLng(data.getLat(), data.getLng()))
                            .title(data.getAssistantUid());
                    // TODO: 20/12/2560 Change Pin
                    switch (data.getRole()){
                        case "officer":
                            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.officer_pin));
                            officerList.add(data);
                            break;
                        case "user":
                            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.volunteer_pin));
                            volunteerList.add(data);
                            break;
                    }
                    Marker marker = mMap.addMarker(options);
                    marker.setTitle("คลิกเพื่อดูรายละเอียด");
                    marker.setTag(data.getAssistantUid());
                    markers.put(data.getAssistantUid(), marker);
                    Integer count = sp.getInt("assistance", 0);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putInt("assistance", count+1);
                    editor.apply();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                AssistantModel data = dataSnapshot.getValue(AssistantModel.class);
                if (data != null){
                    data.setAssistantUid(dataSnapshot.getKey());
                    Marker marker = markers.get(data.getAssistantUid());
                    marker.setPosition(new LatLng(data.getLat(), data.getLng()));
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        confirm.show();
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        mDatabase.getReference().child(type).child(requestUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    data = dataSnapshot.getValue(Request.class);
                    if (data.getRequesterUid() != mUser.getUid()){
                        // TODO: 20/12/2560 Handle emergencyUid != userUid.
                    }
                    if (dataSnapshot.child("timestamp").getValue(Long.class) != null){
                        data.setTimestamp(dataSnapshot.child("timestamp").getValue(Long.class));
                    }
                    MarkerOptions options = new MarkerOptions()
                            .position(new LatLng(data.getLat(), data.getLng()))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.requester_pin));
                    if (mMap != null && requesterMarker==null){
                        requesterMarker = mMap.addMarker(options);
                        requesterMarker.setTitle("คลิกเพื่อดูรายละเอียด");
                        requesterMarker.setTag(data.getRequesterUid());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(data.getLat(), data.getLng()), 15));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        StatusViewModel viewModel = ViewModelProviders.of(this).get(StatusViewModel.class);
        liveData = viewModel.getDataSnapshotLiveData();
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        confirm.show();
        //mMap.setMyLocationEnabled(true);
        /*
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(13.731111, 100.781158))
                .title("Your Location"));*/
        // TODO: 20/12/2560 Change Info Window of Marker
        if (data!=null && requesterMarker==null){
            MarkerOptions options = new MarkerOptions()
                    .position(new LatLng(data.getLat(), data.getLng()))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.me_pin));
            requesterMarker = mMap.addMarker(options);
            requesterMarker.setTitle("คลิกเพื่อดูรายละเอียด");
            requesterMarker.setTag(data.getRequesterUid());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(data.getLat(), data.getLng()), 15));
        }
        mMap.setOnInfoWindowClickListener(this);
        if (currentLocation != null){
            double currentLatitude = currentLocation.getLatitude();
            double currentLongitude = currentLocation.getLongitude();
            LatLng latLng = new LatLng(currentLatitude, currentLongitude);
            if (requesterPin == null){
                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.me_pin));
                requesterPin = mMap.addMarker(options);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
            }else{
                requesterPin.setPosition(latLng);
            }
        }
    }
    @Override
    public void onInfoWindowClick(Marker marker) {
        if (marker.getTag().equals(requesterMarker.getTag())){
            Intent intent = new Intent(getActivity(), ManageRequestingActivity.class);
            intent.putExtra("uid", requestUid);
            intent.putExtra("type", type);
            intent.putExtra("officer", officerList);
            intent.putExtra("volunteer", volunteerList);
            intent.putExtra("data", data);
            intent.putExtra("request_id", requestUid);
            getActivity().startActivityForResult(intent, 5000);
        }else{
            confirm.hide();
            String uid = (String) marker.getTag();
            setUser(uid, marker);
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    private void setUser(final String uid, final Marker marker){
        progress.setVisibility(View.VISIBLE);
        userLayout.setVisibility(View.GONE);
        mDatabase.getReference().child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    UserModel userInfo = dataSnapshot.getValue(UserModel.class);
                    LocalDate birthOfDate = new LocalDate(userInfo.getDateOfBirth());
                    LocalDate now = new LocalDate(Calendar.getInstance().getTime().getTime());
                    name.setText(String.format("%s %s", userInfo.getFirstName(), userInfo.getLastName()));
                    gender.setText(String.format("%s, ", "male".equals(userInfo.getGender())? "ชาย" : "หญิง"));
                    age.setText(String.format(Locale.forLanguageTag("th-TH"), "%d", Years.yearsBetween(birthOfDate, now).getYears()));
                    roleTextView.setText(String.format("%s", "user".equals(dataSnapshot.child("role").getValue(String.class))? "อาสาสมัคร":"เจ้าหน้าที่"));
                    com.google.maps.model.LatLng userPosition = new com.google.maps.model.LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
                    com.google.maps.model.LatLng requesterPosition = new com.google.maps.model.LatLng(requesterMarker.getPosition().latitude, requesterMarker.getPosition().longitude);
                    timeEstimated.setText(String.format("คาดว่าจะมาถึงในอีก %s", LocationHelper.getEstimateTravelTime(userPosition, requesterPosition)));
                    mStorage.getReference().child("profile").child(uid+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.with(getActivity())
                                    .load(uri)
                                    .resize(500, 500)
                                    .into(userImage);
                        }
                    });
                    progress.setVisibility(View.GONE);
                    userLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onStart() {
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION))
            return;
        mGoogleApiClient.connect();
        if ("Helped".equals(sp.getString("mode", ""))){
            liveData.observe(this, new Observer<DataSnapshot>() {
                @Override
                public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        if ("close".equals(dataSnapshot.getValue())){
                            showMessageOK("คำร้องได้สิ้นสุดลงแล้ว", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    SharedPreferences.Editor editor = sp.edit();
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

    private void showMessageOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .create()
                .show();
    }

    @Override
    public void onStop() {
        super.onStop();
        liveData.removeObservers(this);
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.resend:
                int time = sp.getInt("time", 0);
                String emergencyUid = sp.getString("request_uid", null);
                if (time != 0){
                    helper.updateEmergencyTime(emergencyUid, time+1);
                }
                Toast.makeText(getActivity(), "ส่งคำร้องใหม่แล้ว", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(getActivity(), permission) == PackageManager.PERMISSION_GRANTED;
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
    private void handleNewLocation(Location location) {
        currentLocation = location;
        helper.updateRequesterLocation(requestUid, location, type);

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        if (mMap != null){
            if (requesterPin == null){
                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.me_pin));
                requesterPin = mMap.addMarker(options);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
            }else{
                requesterPin.setPosition(latLng);
            }
        }
    }
}
