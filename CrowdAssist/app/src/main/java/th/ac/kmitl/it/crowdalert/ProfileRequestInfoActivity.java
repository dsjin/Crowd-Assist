package th.ac.kmitl.it.crowdalert;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import org.joda.time.LocalDate;
import org.joda.time.Years;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import th.ac.kmitl.it.crowdalert.component.RecyclerRatingAndComment;
import th.ac.kmitl.it.crowdalert.model.EmergencyRequestModel;
import th.ac.kmitl.it.crowdalert.model.GeneralRequestModel;
import th.ac.kmitl.it.crowdalert.model.RatingAndCommentModel;
import th.ac.kmitl.it.crowdalert.model.UserModel;
import th.ac.kmitl.it.crowdalert.util.ConvertHelper;


public class ProfileRequestInfoActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseDatabase mDatabase;
    private FirebaseStorage mStorage;
    private RecyclerView recyclerView;
    private LinearLayout more;
    private RecyclerRatingAndComment adapter;
    private ArrayList<RatingAndCommentModel> list;
    private Intent passIntent;
    private GoogleMap mMap;
    private DatabaseReference requestInformation;
    private TextView date;
    private TextView status;
    private TextView officerAccepted;
    private TextView voluteerAccepted;
    private ImageView imageView;
    private LinearLayout textLayout;
    private UserModel user;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile_request_info);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        passIntent = getIntent();

        bindView();

        recyclerView = findViewById(R.id.rating_and_comment);
        list = new ArrayList<>();
        more = findViewById(R.id.moreButton);
        more.setOnClickListener(this);
        textLayout = findViewById(R.id.textLayout);
        adapter = new RecyclerRatingAndComment(list, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        db.collection("request").document(passIntent.getStringExtra("uid")).collection(mUser.getUid()).orderBy("timestamp", Query.Direction.DESCENDING).limit(5).get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()){
                            Log.d("Data", "Empty");
                        }
                        for (DocumentSnapshot document : task.getResult()) {
                            String uid = document.getId();
                            String comment = document.getString("comment");
                            Double rating = document.getDouble("rating");
                            String name = document.getString("name");
                            RatingAndCommentModel model = new RatingAndCommentModel();
                            model.setUid(uid);
                            model.setName(name);
                            model.setRating(rating);
                            model.setComment(comment);
                            list.add(model);
                            adapter.notifyDataSetChanged();
                            //Log.d("Data", document.getId() + " => " + document.getData());
                        }
                        if (list.isEmpty()){
                            textLayout.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.d("Hello", "Error getting documents: ", task.getException());
                    }
                }
            });
        //TODO BIND Requester DATABASE
    }

    private void bindView(){
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        TextView emergencyTitle = findViewById(R.id.emergency_title);
        LinearLayout informationDetail = findViewById(R.id.information_layout);
        mStorage = FirebaseStorage.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        requestInformation = mDatabase.getReference();
        date = findViewById(R.id.date);
        status = findViewById(R.id.status);
        imageView = findViewById(R.id.imageView);

        if ("emergency".equals(getIntent().getStringExtra("type"))){
            emergencyTitle.setVisibility(View.VISIBLE);
            informationDetail.setVisibility(View.GONE);
            requestInformation.child("emergency").child(getIntent().getStringExtra("uid")).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    EmergencyRequestModel information = dataSnapshot.getValue(EmergencyRequestModel.class);
                    setUser(information.getRequesterUid());
                    date.setText(String.format("เวลาส่งคำร้องขอ : %s", ConvertHelper.ConvertTimestampToDate(information.getTimestamp())));
                    status.setText(String.format("สถานะคำร้องขอ : %s", ConvertHelper.ConvertStatusToThai(information.getStatus())));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(information.getLat(), information.getLng()), 15));
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(information.getLat(), information.getLng())));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }else{
            informationDetail.setVisibility(View.VISIBLE);
            emergencyTitle.setVisibility(View.GONE);
            requestInformation.child("non_emergency").child(getIntent().getStringExtra("uid")).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    GeneralRequestModel information = dataSnapshot.getValue(GeneralRequestModel.class);
                    setUser(information.getRequesterUid());
                    TextView typeTitle = ProfileRequestInfoActivity.this.findViewById(R.id.type_non_emergency);
                    typeTitle.setText(String.format("หมวดหมู่ : %s", ConvertHelper.ConvertTypeToThai(information.getType())));
                    TextView description = ProfileRequestInfoActivity.this.findViewById(R.id.description);
                    description.setText(information.getDescription());
                    TextView statusOfRequester = findViewById(R.id.statusOfRequester);
                    statusOfRequester.setText(String.format("สถานะผู้ร้องขอ : %s", information.getRequesterType().equals("victim")? "ผู้ประสบเหตุ":"ผู้เห็นเหตุการณ์"));
                    date.setText(String.format("เวลาส่งคำร้องขอ : %s", ConvertHelper.ConvertTimestampToDate(information.getTimestamp())));
                    status.setText(String.format("สถานะคำร้องขอ : %s", ConvertHelper.ConvertStatusToThai(information.getStatus())));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(information.getLat(), information.getLng()), 15));
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(information.getLat(), information.getLng())));
                    final ImageView cover = findViewById(R.id.coverImage);
                    mStorage.getReference().child("non_emergency").child(getIntent().getStringExtra("uid")+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.with(ProfileRequestInfoActivity.this)
                                    .load(uri)
                                    .into(cover);
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        officerAccepted = findViewById(R.id.officer_accept);
        voluteerAccepted = findViewById(R.id.user_accept);
        mDatabase.getReference().child(getIntent().getStringExtra("type")+"_assistance").child(getIntent().getStringExtra("uid")).child("officerCount").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    officerAccepted.setText(String.format("%d",dataSnapshot.getValue(Integer.class)));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mDatabase.getReference().child(getIntent().getStringExtra("type")+"_assistance").child(getIntent().getStringExtra("uid")).child("userCount").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    voluteerAccepted.setText(String.format("%d",dataSnapshot.getValue(Integer.class)));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    
    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, AllCommentActivity.class);
        intent.putExtra("uid", passIntent.getStringExtra("uid"));
        startActivity(intent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setAllGesturesEnabled(false);
    }
    private void setUser(final String uid){
        mDatabase.getReference().child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                TextView name = findViewById(R.id.name);
                TextView gender = findViewById(R.id.gender);
                TextView age = findViewById(R.id.age);
                RatingBar ratingBar = findViewById(R.id.ratingBar);
                TextView rating = findViewById(R.id.rating);
                LocalDate birthOfDate = new LocalDate(dataSnapshot.child("dateOfBirth").getValue(Long.class));
                LocalDate now = new LocalDate(Calendar.getInstance().getTime().getTime());
                name.setText(String.format("%s %s", dataSnapshot.child("firstName").getValue(String.class), dataSnapshot.child("lastName").getValue(String.class)));
                gender.setText(String.format("%s, ", "male".equals(dataSnapshot.child("gender").getValue(String.class))? "ชาย" : "หญิง"));
                age.setText(String.format(Locale.forLanguageTag("th-TH"), "%d", Years.yearsBetween(birthOfDate, now).getYears()));
                mStorage.getReference().child("profile").child(uid+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.with(ProfileRequestInfoActivity.this)
                                .load(uri)
                                .resize(500, 500)
                                .into(imageView);
                    }
                });
                Double rate;
                if(dataSnapshot.child("rating").child("scores").getValue(Double.class) != null && dataSnapshot.child("rating").child("counts").getValue(Double.class) != null){
                    if (dataSnapshot.child("rating").child("counts").getValue(Double.class) == 0.0){
                        rate = 0.0;
                    }else{
                        rate = dataSnapshot.child("rating").child("scores").getValue(Double.class)/dataSnapshot.child("rating").child("counts").getValue(Double.class);
                    }
                }else{
                    rate = 0.0;
                }
                rating.setText(String.format("( %.1f )", rate));
                ratingBar.setRating(rate.floatValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
