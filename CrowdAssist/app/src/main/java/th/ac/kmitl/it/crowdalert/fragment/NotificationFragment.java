package th.ac.kmitl.it.crowdalert.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import th.ac.kmitl.it.crowdalert.R;
import th.ac.kmitl.it.crowdalert.component.RecyclerNotificationAdapter;
import th.ac.kmitl.it.crowdalert.model.NotificationModel;
import th.ac.kmitl.it.crowdalert.model.UserModel;
import th.ac.kmitl.it.crowdalert.util.LocationHelper;

public class NotificationFragment extends Fragment implements ChildEventListener{
    private RecyclerView recyclerView;
    private RecyclerNotificationAdapter adapter;
    private ArrayList<NotificationModel> data;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseDatabase mDatabase;
    private LinearLayout progreesBar;
    private LinearLayout textLayout;
    private final String SP_LOCATION = "location_information";
    SharedPreferences sp;

    public NotificationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        data = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        sp = getActivity().getSharedPreferences(SP_LOCATION, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        data = new ArrayList<>();
        recyclerView = view.findViewById(R.id.notification_recycler_view);
        progreesBar = view.findViewById(R.id.progress_layout);
        textLayout = view.findViewById(R.id.text_layout);
        adapter = new RecyclerNotificationAdapter(data, getActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        progreesBar.setVisibility(View.VISIBLE);
        DatabaseReference ref = mDatabase.getReference("notification").child(mUser.getUid());
        //ref.orderByChild("timestamp_sort").addChildEventListener(this);
        ref.orderByChild("timestamp_sort").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot ds) {
                if (progreesBar.getVisibility() == View.GONE){
                    recyclerView.setVisibility(View.GONE);
                    progreesBar.setVisibility(View.VISIBLE);
                }
                for(DataSnapshot dataSnapshot : ds.getChildren()){
                    String uid = dataSnapshot.getKey();
                    Long timestamp = dataSnapshot.child("timestamp").getValue(Long.class);

                    Double lat = dataSnapshot.child("lat").getValue(Double.class);
                    Double lng = dataSnapshot.child("lng").getValue(Double.class);
                    UserModel userModel = dataSnapshot.child("user").getValue(UserModel.class);
                    String requesterUid = dataSnapshot.child("requesterUid").getValue(String.class);
                    String type = dataSnapshot.child("type").getValue(String.class);
                    Location location = new Location("");
                    location.setLatitude(lat);
                    location.setLongitude(lng);
                    //Double distance = dataSnapshot.child("distance").getValue(Double.class);
                    Location storeLocation = new Location("");
                    String currentlat = sp.getString("location_lat", null);
                    String currentLon = sp.getString("location_lon", null);
                    if (currentlat != null && currentLon != null){
                        storeLocation.setLatitude(Double.parseDouble(currentlat));
                        storeLocation.setLongitude(Double.parseDouble(currentLon));
                        data.add(new NotificationModel(uid, userModel.getUserId(), timestamp, LocationHelper.distance(storeLocation, location), userModel, type, requesterUid));
                    }else{
                        data.add(new NotificationModel(uid, userModel.getUserId(), timestamp, 0.0, userModel, type, requesterUid));
                    }
                    adapter.notifyItemInserted(data.size());
                }
                if (data.isEmpty()){
                    textLayout.setVisibility(View.VISIBLE);
                }
                progreesBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return view;
    }

    private ArrayList<NotificationModel> mockData(){
        /*
        ArrayList<NotificationModel> mockData = new ArrayList<>();
        mockData.add(new NotificationModel("uid","noizs", Long.parseLong("1516262750490"), 0.5));
        mockData.add(new NotificationModel("uid","dsjinj", Long.parseLong("1516262750490"), 1.0));
        mockData.add(new NotificationModel("uid","mama", Long.parseLong("1516262750490"), 1.2));*
        return mockData;*/
        return null;
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        if (progreesBar.getVisibility() == View.GONE){
            recyclerView.setVisibility(View.GONE);
            progreesBar.setVisibility(View.VISIBLE);
        }

        String uid = dataSnapshot.getKey();
        Long timestamp = dataSnapshot.child("timestamp").getValue(Long.class);

        Double lat = dataSnapshot.child("lat").getValue(Double.class);
        Double lng = dataSnapshot.child("lng").getValue(Double.class);
        UserModel userModel = dataSnapshot.child("user").getValue(UserModel.class);
        String requesterUid = dataSnapshot.child("requesterUid").getValue(String.class);
        String type = dataSnapshot.child("type").getValue(String.class);
        Location location = new Location("");
        location.setLatitude(lat);
        location.setLongitude(lng);
        //Double distance = dataSnapshot.child("distance").getValue(Double.class);
        Location storeLocation = new Location("");
        String currentlat = sp.getString("location_lat", null);
        String currentLon = sp.getString("location_lon", null);
        if (currentlat != null && currentLon != null){
            storeLocation.setLatitude(Double.parseDouble(currentlat));
            storeLocation.setLongitude(Double.parseDouble(currentLon));
            data.add(new NotificationModel(uid, userModel.getUserId(), timestamp, LocationHelper.distance(storeLocation, location), userModel, type, requesterUid));
        }else{
            data.add(new NotificationModel(uid, userModel.getUserId(), timestamp, 0.0, userModel, type, requesterUid));
        }
        adapter.notifyItemInserted(data.size());
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

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
}
