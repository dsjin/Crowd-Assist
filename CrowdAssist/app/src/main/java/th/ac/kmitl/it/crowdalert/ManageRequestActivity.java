package th.ac.kmitl.it.crowdalert;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import th.ac.kmitl.it.crowdalert.component.RecyclerGridViewAdapter;
import th.ac.kmitl.it.crowdalert.model.AssistantModel;
import th.ac.kmitl.it.crowdalert.model.Request;
import th.ac.kmitl.it.crowdalert.util.ConvertHelper;

public class ManageRequestActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener, OnMapReadyCallback {
    private Toolbar toolbar;
    private TextView title;
    private FirebaseDatabase mDatabase;
    private DatabaseReference data;
    private DatabaseReference assistants;
    private GoogleMap mMap;
    private Request information;
    private ArrayList<AssistantModel> assistantsList;
    private RecyclerGridViewAdapter adapter;
    private LinearLayout progreesBar;
    private CoordinatorLayout mainLayout;
    private TextView date;
    private Boolean finish1 = false;
    private Boolean finish2 = false;
    private TextView type;
    private HashMap<String, String> mapType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_request);
        toolbar = findViewById(R.id.toolbar_top);
        toolbar.setTitle("");
        title = toolbar.findViewById(R.id.toolbar_title);
        title.setVisibility(View.INVISIBLE);
        progreesBar = findViewById(R.id.progress_layout);
        mainLayout = findViewById(R.id.mainLayout);
        type = findViewById(R.id.type);
        date = findViewById(R.id.date);
        mapType = new HashMap<>();
        setupMapType();
        setSupportActionBar(toolbar);
        AppBarLayout appBarLayout = findViewById(R.id.appbar);
        appBarLayout.addOnOffsetChangedListener(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.inflateMenu(R.menu.manage_general_menu);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        assistantsList = new ArrayList<>();
        //adapter = new RecyclerGridViewAdapter(this, assistantsList);

        Intent intent = getIntent();
        String uid = intent.getStringExtra("uid");
        ValueEventListener dataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                information = dataSnapshot.getValue(Request.class);
                type.setText(String.format("Type : %s", mapType.get(information.getType())));
                date.setText(String.format("Date : %s", ConvertHelper.ConvertTimestampToDate(information.getTimestamp())));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(information.getLat(), information.getLng()), 15));
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(information.getLat(), information.getLng())));
                finish1 = true;
                if (finish1 && finish2){
                    progreesBar.setVisibility(View.GONE);
                    mainLayout.setVisibility(View.VISIBLE);
                    mainLayout.setAnimation(AnimationUtils.loadAnimation(ManageRequestActivity.this, R.anim.alpha_time1000));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ChildEventListener assistantsListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (!finish2){
                    finish1 = false;
                }
                assistantsList.add(dataSnapshot.getValue(AssistantModel.class));
                adapter.notifyItemInserted(assistantsList.size());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                for(AssistantModel model : assistantsList){
                    if (model.getAssistantUid().equals(dataSnapshot.getKey())){
                        Integer remove = assistantsList.indexOf(model);
                        assistantsList.remove(model);
                        adapter.notifyItemRemoved(remove);
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ValueEventListener assistantCompleteListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                finish2 = true;
                if (finish1 && finish2){
                    progreesBar.setVisibility(View.GONE);
                    mainLayout.setVisibility(View.VISIBLE);
                    mainLayout.setAnimation(AnimationUtils.loadAnimation(ManageRequestActivity.this, R.anim.alpha_time1000));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabase = FirebaseDatabase.getInstance();
        if (uid != null){
            data = mDatabase.getReference("non_emergency").child(uid);
            assistants = mDatabase.getReference("non_emergency_assistant").child(uid);
            data.addListenerForSingleValueEvent(dataListener);
            assistants.addChildEventListener(assistantsListener);
            assistants.addListenerForSingleValueEvent(assistantCompleteListener);
        }

        //Mock Data
        assistantsList.add(new AssistantModel());
        assistantsList.add(new AssistantModel());
        assistantsList.add(new AssistantModel());
        assistantsList.add(new AssistantModel());
        assistantsList.add(new AssistantModel());
        assistantsList.add(new AssistantModel());
        assistantsList.add(new AssistantModel());
        assistantsList.add(new AssistantModel());
        assistantsList.add(new AssistantModel());
        assistantsList.add(new AssistantModel());
        assistantsList.add(new AssistantModel());


        RecyclerView userView = findViewById(R.id.userItem);
        userView.setLayoutManager(new GridLayoutManager(this, 3));
        userView.setAdapter(adapter);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange() ){
            title.setVisibility(View.VISIBLE);
            title.setAnimation(AnimationUtils.loadAnimation(this, R.anim.alpha));
        }else{
            title.setVisibility(View.INVISIBLE);
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //TODO SET Camera
        mMap.getUiSettings().setAllGesturesEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupMapType(){
        mapType.put("type1", "อัคคีภัย");
        mapType.put("type2", "อุบัติเหตุทางถนน");
        mapType.put("type3", "อุบัติเหตุทางน้ำ");
        mapType.put("type4", "อื่นๆ");
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.manage_general_menu, menu);
        return true;
    }
}
