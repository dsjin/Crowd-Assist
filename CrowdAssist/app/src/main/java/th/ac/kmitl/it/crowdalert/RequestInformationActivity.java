package th.ac.kmitl.it.crowdalert;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import org.joda.time.LocalDate;
import org.joda.time.Years;

import java.util.Calendar;
import java.util.Locale;

import th.ac.kmitl.it.crowdalert.model.Request;
import th.ac.kmitl.it.crowdalert.util.ConvertHelper;

public class RequestInformationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseDatabase mDatabase;
    private FirebaseStorage mStorage;
    private GoogleMap mMap;
    private DatabaseReference requestInformation;
    private TextView date;
    private TextView status;
    private TextView officerAccepted;
    private TextView volunteerAccepted;
    private ImageView imageView;
    private Request information;
    private String number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_information);
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

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();

        bindView();
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
        information = (Request)getIntent().getSerializableExtra("data");
        if ("emergency".equals(getIntent().getStringExtra("type"))){
            emergencyTitle.setVisibility(View.VISIBLE);
            informationDetail.setVisibility(View.GONE);
            setUser(information.getRequesterUid());
            date.setText(String.format("เวลาส่งคำร้องขอ : %s", ConvertHelper.ConvertTimestampToDate(information.getTimestamp())));
            status.setText(String.format("สถานะคำร้องขอ : %s", ConvertHelper.ConvertStatusToThai(information.getStatus())));
        }else{
            informationDetail.setVisibility(View.VISIBLE);
            emergencyTitle.setVisibility(View.GONE);
            TextView typeTitle = findViewById(R.id.type_non_emergency);
            setUser(information.getRequesterUid());
            typeTitle.setText(String.format("หมวดหมู่ : %s", ConvertHelper.ConvertTypeToThai(information.getType())));
            TextView statusOfRequester = findViewById(R.id.statusOfRequester);
            statusOfRequester.setText(String.format("สถานะผู้ร้องขอ : %s", information.getRequesterType().equals("victim")? "ผู้ประสบเหตุ":"ผู้เห็นเหตุการณ์"));
            TextView description = findViewById(R.id.description);
            description.setText(information.getDescription());
            final ImageView cover = findViewById(R.id.coverImage);
            date.setText(String.format("เวลาส่งคำร้องขอ : %s", ConvertHelper.ConvertTimestampToDate(information.getTimestamp())));
            status.setText(String.format("สถานะคำร้องขอ : %s", ConvertHelper.ConvertStatusToThai(information.getStatus())));
            mStorage.getReference().child("non_emergency").child(getIntent().getStringExtra("request_id")+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.with(RequestInformationActivity.this)
                            .load(uri)
                            .into(cover);
                }
            });
        }
        officerAccepted = findViewById(R.id.officer_accept);
        volunteerAccepted = findViewById(R.id.user_accept);
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
                    volunteerAccepted.setText(String.format("%d",dataSnapshot.getValue(Integer.class)));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setAllGesturesEnabled(false);
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(information.getLat(), information.getLng())));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(information.getLat(), information.getLng()), 15));
    }
    private void setUser(final String uid) {
        mDatabase.getReference().child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                TextView name = findViewById(R.id.name);
                TextView gender = findViewById(R.id.gender);
                TextView age = findViewById(R.id.age);
                RatingBar ratingBar = findViewById(R.id.ratingBar);
                TextView rating = findViewById(R.id.rating);
                LinearLayout tel = findViewById(R.id.tel);
                LocalDate birthOfDate = new LocalDate(dataSnapshot.child("dateOfBirth").getValue(Long.class));
                LocalDate now = new LocalDate(Calendar.getInstance().getTime().getTime());
                name.setText(String.format("%s %s", dataSnapshot.child("firstName").getValue(String.class), dataSnapshot.child("lastName").getValue(String.class)));
                gender.setText(String.format("%s, ", "male".equals(dataSnapshot.child("gender").getValue(String.class)) ? "ชาย" : "หญิง"));
                age.setText(String.format(Locale.forLanguageTag("th-TH"), "%d", Years.yearsBetween(birthOfDate, now).getYears()));
                mStorage.getReference().child("profile").child(uid + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.with(RequestInformationActivity.this)
                                .load(uri)
                                .resize(500, 500)
                                .into(imageView);
                    }
                });
                Double rate;
                if (dataSnapshot.child("rating").child("scores").getValue(Double.class) != null && dataSnapshot.child("rating").child("counts").getValue(Double.class) != null) {
                    if (dataSnapshot.child("rating").child("counts").getValue(Double.class) == 0.0) {
                        rate = 0.0;
                    } else {
                        rate = dataSnapshot.child("rating").child("scores").getValue(Double.class) / dataSnapshot.child("rating").child("counts").getValue(Double.class);
                    }
                } else {
                    rate = 0.0;
                }
                rating.setText(String.format("( %.1f )", rate));
                ratingBar.setRating(rate.floatValue());
                number = dataSnapshot.child("tel").getValue(String.class);
                tel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (number != null){
                            Intent makeCall = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
                            try {
                                startActivity(makeCall);
                            }catch(SecurityException e){
                                Log.e("Permission Error:", e.getMessage());
                            }
                        }else{
                            Toast.makeText(RequestInformationActivity.this, "ผู้ร้องขอไม่ได้กำหนดเบอร์โทรศัพท์", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
